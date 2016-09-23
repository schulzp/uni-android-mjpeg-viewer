package edu.is.jpeg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Consumes an input stream assuming it a {@code multipart/x-mixed-replace} stream where each section is a JPEG image.
 */
public class MultipartJpegInputStreamReader {

    private static final byte[] buildPattern(byte[]...parts) {
        int size = 0;
        for (byte[] part : parts) {
            size += part.length;
        }
        ByteArrayOutputStream builder = new ByteArrayOutputStream(size);
        for (byte[] part : parts) {
            try {
                builder.write(part);
            } catch (IOException e) {
                throw new RuntimeException("Failed to build pattern", e);
            }
        }
        return builder.toByteArray();
    }

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final byte[] CRLF = "\r\n".getBytes(CHARSET);
    private static final byte[] JPEG_SOF = buildPattern(CRLF, new byte[] {(byte) 0xFF, (byte) 0xD8});
    private static final byte[] JPEG_EOF = buildPattern(new byte[] {(byte) 0xFF, (byte) 0xD9}, CRLF);

    private static final byte[] CONTENT_LENGHT_HEADER_START = "Content-Length: ".getBytes(CHARSET);
    private static final byte[] CONTENT_LENGHT_HEADER_END = CRLF;

    private final StreamSearcher searcher;
    private final InputStream inputStream;
    private final byte[] boundaryPattern;

    private byte[] nextPattern;

    public MultipartJpegInputStreamReader(InputStream inputStream, byte[] boundaryPattern) {
        this.boundaryPattern = buildPattern("--".getBytes(), boundaryPattern);
        this.searcher = new StreamSearcher(buildPattern(CRLF, this.boundaryPattern));
        this.nextPattern = boundaryPattern;
        this.inputStream = inputStream;
    }

    /**
     * Reads the next image into the {@code buffer}.
     * @param buffer the buffer for the image
     * @throws IOException if reading from the stream fails or writing to the buffer fails
     */
    public int read(ByteBuffer buffer) throws IOException {
        while (searcher.search(inputStream) > 0) {
            if (nextPattern == JPEG_SOF) {
                System.out.println("Hit start of image");
                buffer.clear();
                buffer.put(JPEG_SOF, CRLF.length, JPEG_SOF.length - CRLF.length);
                searcher.setBuffer(buffer);
                setNextPattern(JPEG_EOF);
            } else if (nextPattern == JPEG_EOF) {
                System.out.println("Hit end of image: " + buffer.position());
                setNextPattern(boundaryPattern);
                searcher.setBuffer(null);
                buffer.limit(buffer.position() - CRLF.length);
                return buffer.position();
            } else {
                System.out.println("Hit boundary");
                setNextPattern(JPEG_SOF);
                searcher.setBuffer(null);
            }
        }
        return -1;
    }
    
    protected void setNextPattern(byte[] nextPattern) {
        this.searcher.setPattern(nextPattern);
        this.nextPattern = nextPattern;
    }

}
