package edu.is.jpeg;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.apache.commons.codec.binary.Hex;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.is.ResourceUtils;

import static org.junit.Assert.*;

/**
 * Created by peter on 22/09/16.
 */
public class MultipartJpegInputStreamReaderTest {

    private Context context;

    @Before
    public void before() {
        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void endian() {
        byte[] bytes = new byte[4];
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        Arrays.fill(bytes, 0, 3, (byte) 0);
        bytes[0] = 3;
        bytes[1] = -90;
        int i = buffer.getInt();
        buffer.rewind();

        Arrays.fill(bytes, 0, 3, (byte) 0);
        bytes[2] = 3;
        bytes[3] = -90;
        i = buffer.getInt();
        buffer.rewind();
    }

    @Test
    public void read() throws Exception {
        byte[] boundary = "test".getBytes();
        ByteBuffer source = ResourceUtils.loadResource(edu.is.test.R.raw.sample, context);
        InputStream inputStream = createMultipartInputStream(edu.is.test.R.raw.sample, 2, boundary);
        MultipartJpegInputStreamReader reader = new MultipartJpegInputStreamReader(inputStream, boundary);
        ByteBuffer target = ByteBuffer.allocate(10000);
        int frames = 0;
        try {
            while (reader.read(target) > 0) {
                assertThat("Unexpected buffer", target, ByteBufferMatchers.equals(source, false));
                frames++;
            }
        } catch (BufferOverflowException e) {
            target.rewind();
            throw new IOException("Failed to write frame " + frames + " into " + target + "\n" + toString(target, -1), e);
        }
        assertThat("Unexpected number of frames", frames, CoreMatchers.is(2));
    }

    private InputStream createMultipartInputStream(int resource, int repetitions, byte[] boundary) throws IOException {
        ByteBuffer resourceBuffer = ResourceUtils.loadResource(resource, context);

        final byte[] crlf = "\r\n".getBytes();
        final byte[] dashes = "--".getBytes();
        final byte[] contentTypeHeader = "Content-Type: image/jpeg".getBytes();
        final byte[] contentLengthHeader = ("Content-Length: " + resourceBuffer.limit()).getBytes();

        int boundaryLength = crlf.length
                + dashes.length
                + boundary.length;
        int partLength = boundaryLength
                + crlf.length
                + contentTypeHeader.length
                + crlf.length
                + contentLengthHeader.length
                + crlf.length
                + crlf.length
                + resourceBuffer.limit();

        int capacity = repetitions * partLength + boundaryLength + dashes.length;
        final ByteBuffer inputStreamBuffer = ByteBuffer.allocateDirect(capacity);

        for (int i = 0; i < repetitions; ++i) {
            resourceBuffer.rewind();
            inputStreamBuffer.put(crlf);
            inputStreamBuffer.put(dashes);
            inputStreamBuffer.put(boundary).put(crlf);
            inputStreamBuffer.put(contentTypeHeader).put(crlf);
            inputStreamBuffer.put(contentLengthHeader).put(crlf);
            inputStreamBuffer.put(crlf);
            inputStreamBuffer.put(resourceBuffer);
        }
        inputStreamBuffer.put(crlf);
        inputStreamBuffer.put(dashes);
        inputStreamBuffer.put(boundary);
        inputStreamBuffer.put(dashes);
        inputStreamBuffer.rewind();

        assertThat("Unexpected input buffer limit", inputStreamBuffer.limit(), CoreMatchers.is(capacity));

        return new ByteBufferInputStream(inputStreamBuffer);
    }

    public static class ByteBufferMatchers {

        public static Matcher<ByteBuffer> equals(final ByteBuffer expectedBuffer, final boolean restore) {
            return  new BaseMatcher<ByteBuffer>() {

                private ByteBuffer actualBuffer;

                @Override
                public boolean matches(Object item) {
                    actualBuffer = (ByteBuffer) item;
                    int actualBufferPosition = prepareByteBuffer(actualBuffer);
                    int expectedBufferPosition = prepareByteBuffer(expectedBuffer);
                    boolean equals = expectedBuffer.equals(actualBuffer);

                    if (restore) {
                        restoreByteBuffer(actualBuffer, actualBufferPosition);
                        restoreByteBuffer(expectedBuffer, expectedBufferPosition);
                    }

                    return equals;
                }

                @Override
                public void describeTo(Description description) {
                    description.appendValue(expectedBuffer)
                            .appendText("\n")
                            .appendText(MultipartJpegInputStreamReaderTest.toString(expectedBuffer, -1))
                            .appendText("\n")
                            .appendText(MultipartJpegInputStreamReaderTest.toString(actualBuffer, -1))
                    ;
                }

            };
        }

    }

    private static void restoreByteBuffer(ByteBuffer buffer, int position) {
        buffer.position(position);
        buffer.reset();
    }

    private static int prepareByteBuffer(ByteBuffer buffer) {
        int position = buffer.position();
        buffer.rewind();
        return position;
    }

    private static final String toString(ByteBuffer buffer, int limit) {
        limit = limit == -1 ? buffer.limit() : (limit + 4);
        StringBuilder builder = new StringBuilder(limit * 3 + 4);
        byte[] temp = new byte[1];
        builder.append("[ ");
        for (int i = 0; i < limit; ++i) {
            buffer.get(temp);
            builder.append(Hex.encodeHex(temp));
            builder.append(' ');
        }
        if (limit < buffer.limit()) {
            builder.append(" ...");
        }
        builder.append(" ]");
        return builder.toString();
    }

    private static class ByteBufferInputStream extends InputStream {

        private final ByteBuffer inputStreamBuffer;

        public ByteBufferInputStream(ByteBuffer inputStreamBuffer) {
            this.inputStreamBuffer = inputStreamBuffer;
        }

        @Override
        public int read() throws IOException {
            int result = -1;
            if (inputStreamBuffer.hasRemaining()) {
                result = 0xff & inputStreamBuffer.get();
            }
            return result;
        }

        @Override
        public synchronized void reset() throws IOException {
            inputStreamBuffer.rewind();
        }

    }
}