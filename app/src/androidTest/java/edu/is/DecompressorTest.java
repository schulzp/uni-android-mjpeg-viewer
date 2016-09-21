package edu.is;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.is.jpeg.Decompressor;

import static org.junit.Assert.*;

/**
 * Tests for {@link Decompressor}.
 */
@RunWith(AndroidJUnit4.class)
public class DecompressorTest {

    @Test
    public void decompress() throws IOException {
        Decompressor decompressor = new Decompressor();
        Bitmap target = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        ByteBuffer source = loadResource(edu.is.test.R.raw.sample);
        decompressor.decompress(source, source.limit(), target);

        int pixel = target.getPixel(0, 0);

        assertThat("unexpected color value 0,0", asARGB(pixel), CoreMatchers.is(asARGB(0xfffe0000)));

        pixel = target.getPixel(1, 0);
        assertThat("unexpected color value 1,0", asARGB(pixel), CoreMatchers.is(asARGB(0xff00ff00)));

        pixel = target.getPixel(0, 1);
        assertThat("unexpected color value 0,1", asARGB(pixel), CoreMatchers.is(asARGB(0xff0000fe)));

        pixel = target.getPixel(1, 1);
        assertThat("unexpected color value 1,1", asARGB(pixel), CoreMatchers.is(asARGB(0xff000000)));
    }

    /**
     * This is expected to fail since {@link Decompressor#decompress(ByteBuffer, int, Bitmap)} expects a direct buffer.
     * @throws IOException
     */
    @Test(expected = Decompressor.Exception.class)
    public void decompressThrowsException() throws IOException {
        Decompressor decompressor = new Decompressor();
        Bitmap target = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        ByteBuffer source = ByteBuffer.wrap(new byte[0]);
        decompressor.decompress(source, source.limit(), target);
    }

    private int[] asARGB(int pixel) {
        return new int[]{Color.alpha(pixel), Color.red(pixel), Color.green(pixel), Color.blue(pixel)};
    }

    private ByteBuffer loadResource(int resource) throws IOException {
        AssetFileDescriptor fd = InstrumentationRegistry.getContext().getResources().openRawResourceFd(resource);
        FileInputStream in = null;
        try {
            ByteBuffer content = ByteBuffer.allocateDirect((int)fd.getLength());
            FileChannel channel = fd.createInputStream().getChannel();
            channel.read(content);
            return content;
        } finally {
            if (in != null) {
                in.close();
            }
            fd.close();
        }
    }

}