package edu.is.jpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import edu.is.ResourceUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;

/**
 * Created by peter on 21/09/16.
 */
public class MultipartJpegLoaderTest {

    private MockWebServer server = new MockWebServer();

    private Context context;

    @Before
    public void startServer() throws IOException {
        context = InstrumentationRegistry.getContext();
        server.start();
    }

    @After
    public void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    public void loadInBackground() throws Exception {
        String boundary = "test-boundary";
        server.enqueue(createMultipartMixedReplaceResponse(boundary));
        MultipartJpegLoader loader = createLoader(server.url("/").toString());
        final AtomicInteger numberOfFrames = new AtomicInteger(0);
        loader.targetChanged(2, 2);
        loader.setCallbacks(new MultipartJpegLoader.Callbacks() {

            @Override
            public void jpegLoaded(Bitmap bitmap) {
                numberOfFrames.getAndIncrement();
            }

        });
        IOException res = loader.loadInBackground();
        if (res != null) {
            throw res;
        }
        Assert.assertThat("unexpected number of frames", numberOfFrames.get(), CoreMatchers.is(3));
    }

    @NonNull
    private MultipartJpegLoader createLoader(String url) {
        Bundle config = new Bundle();
        config.putString("url", url);
        return new MultipartJpegLoader(context, config);
    }

    private MockResponse createMultipartMixedReplaceResponse(String boundary) {
        Charset charset = Charset.forName("US-ASCII");
        Buffer buffer = new Buffer();
        try {
            BufferedOutputStream out = new BufferedOutputStream(buffer.outputStream());
            byte[] boundaryLine = ("\r\n--" + boundary).getBytes(charset);
            for (int i = 0; i < 3; ++i) {
                out.write(boundaryLine);
                out.write("\r\n".getBytes(charset));
                out.write(ResourceUtils.loadResource(edu.is.test.R.raw.sample, context).array());
            }
            out.write(boundaryLine);
            out.write("--".getBytes(charset));
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("failed to write mock output", e);
        }
        MockResponse response = new MockResponse()
                .setStatus("HTTP/1.1 200")
                .setHeader("Content-Type", "multipart/x-mixed-replace;boundary=" + boundary)
                .setBody(buffer);
        return response;
    }

}