package edu.is.jpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.util.TimingLogger;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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

    private static RuntimeException EXIT = new RuntimeException("exit");

    @Test
    public void loadInBackgroundFromRealSource() throws IOException {
        MultipartJpegLoader loader = createLoader("http://192.168.178.73:8080/video");
        final AtomicInteger numberOfFrames = new AtomicInteger(0);
        loader.targetChanged(640, 480);
        loader.setCallbacks(new MultipartJpegLoader.Callbacks() {

            @Override
            public void jpegLoaded(Bitmap bitmap) {
                if (numberOfFrames.getAndIncrement() >= 10) {
                    throw EXIT;
                }

            }

        });

        try {
            IOException res = loader.loadInBackground();
            if (res != null) {
                throw res;
            }
        } catch (Exception e) {
            if (e != EXIT) {
                throw e;
            }
        }
    }

    @Test
    public void loadInBackground() throws Exception {

        MockResponse response  = createMultipartMixedReplaceResponseFromRecord(edu.is.test.R.raw.response, "Ba4oTvQMY8ew04N8dcnM");
        server.enqueue(response);
        MultipartJpegLoader loader = createLoader(server.url("/").toString());
        final AtomicInteger numberOfFrames = new AtomicInteger(0);
        loader.targetChanged(200, 100);
        loader.setCallbacks(new MultipartJpegLoader.Callbacks() {

            @Override
            public void jpegLoaded(Bitmap bitmap) {

                numberOfFrames.getAndIncrement();

                File file = new File(Environment.getExternalStorageDirectory(), "frame-" + numberOfFrames.get() + ".jpg");
                System.out.println("Writing to " + file);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Failed to write bitmap to file " + file, e);
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to close file " + file, e);
                        }
                    }
                }

            }

        });

        IOException res = loader.loadInBackground();
        if (res != null) {
            throw res;
        }

        Assert.assertThat("unexpected number of frames", numberOfFrames.get(), CoreMatchers.is(37));
    }

    @NonNull
    private MultipartJpegLoader createLoader(String url) {
        Bundle config = new Bundle();
        config.putString("url", url);
        return new MultipartJpegLoader(context, config);
    }

    private MockResponse createMultipartMixedReplaceResponseFromImage(int resource, int repetitions, String boundary) {
        Charset charset = Charset.forName("US-ASCII");
        try {
            Buffer buffer = new Buffer();
            BufferedOutputStream out = new BufferedOutputStream(buffer.outputStream());
            byte[] boundaryLine = ("\r\n--" + boundary).getBytes(charset);
            for (int i = 0; i < repetitions; ++i) {
                out.write(boundaryLine);
                out.write("\r\n".getBytes(charset));
                out.write(ResourceUtils.loadResource(edu.is.test.R.raw.sample, context).array());
            }
            out.write(boundaryLine);
            out.write("--".getBytes(charset));
            out.flush();
            return createMultipartMixedReplaceResponse(boundary, buffer);
        } catch (IOException e) {
            throw new RuntimeException("failed to write mock output", e);
        }
    }

    private MockResponse createMultipartMixedReplaceResponseFromRecord(int resource, String boundary) {
        try {
            Buffer buffer = new Buffer();
            IOUtils.copy(ResourceUtils.openResource(edu.is.test.R.raw.response, context), buffer.outputStream());
            return createMultipartMixedReplaceResponse(boundary, buffer);
        } catch (IOException e) {
            throw new RuntimeException("failed to write recorded output", e);
        }
    }

    private MockResponse createMultipartMixedReplaceResponse(String boundary, Buffer buffer) {
        MockResponse response = new MockResponse()
                .setStatus("HTTP/1.1 200")
                .setHeader("Content-Type", "multipart/x-mixed-replace;boundary=" + boundary)
                .setBody(buffer);
        return response;
    }

}