package edu.is;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.ByteBuffer;

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
        ByteBuffer source = ResourceUtils.loadResource(edu.is.test.R.raw.sample, InstrumentationRegistry.getContext());
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

    @Test
    public void decompressMediumSizedImage() throws IOException {
        Decompressor decompressor = new Decompressor();
        Bitmap target = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        ByteBuffer source = ResourceUtils.loadResource(edu.is.test.R.raw.sample600x400, InstrumentationRegistry.getContext());
        decompressor.decompress(source, source.limit(), target);
    }

    /**
     * Expected to fail since {@link Decompressor#decompress(ByteBuffer, int, Bitmap)} expects a direct buffer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void decompressExpectsDirectBuffer() throws IOException {
        Decompressor decompressor = new Decompressor();
        Bitmap target = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        ByteBuffer source = ByteBuffer.wrap(new byte[0]);
        decompressor.decompress(source, source.limit(), target);
    }

    /**
     * Expected to fail since {@link Decompressor#decompress(ByteBuffer, int, Bitmap)} expects a JPEG.
     */
    @Test(expected = Decompressor.Exception.class)
    public void decompressExpectsJpeg() throws IOException {
        Decompressor decompressor = new Decompressor();
        Bitmap target = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        ByteBuffer source = ByteBuffer.allocateDirect(2);
        source.put((byte) 0xff).put((byte) 0x00);
        decompressor.decompress(source, source.limit(), target);
    }

    private int[] asARGB(int pixel) {
        return new int[]{Color.alpha(pixel), Color.red(pixel), Color.green(pixel), Color.blue(pixel)};
    }

}