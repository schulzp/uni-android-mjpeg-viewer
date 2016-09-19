package edu.is;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

/**
 * Created by peter on 18/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class DecompressorTest {

    @Test
    public void decompress() throws IOException {
        Decompressor decompressor = new Decompressor();
        Bitmap target = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        byte[] source = loadResource("sample.jpg");
        decompressor.decompress(source, source.length, target);

        int pixel = target.getPixel(0, 0);

        assertThat("unexpected color value 0,0", asARGB(pixel), CoreMatchers.is(asARGB(0xfffe0000)));

        pixel = target.getPixel(1, 0);
        assertThat("unexpected color value 1,0", asARGB(pixel), CoreMatchers.is(asARGB(0xff00ff00)));

        pixel = target.getPixel(0, 1);
        assertThat("unexpected color value 0,1", asARGB(pixel), CoreMatchers.is(asARGB(0xff0000fe)));

        pixel = target.getPixel(1, 1);
        assertThat("unexpected color value 1,1", asARGB(pixel), CoreMatchers.is(asARGB(0xff000000)));
    }

    private int[] asARGB(int pixel) {
        return new int[]{Color.alpha(pixel), Color.red(pixel), Color.green(pixel), Color.blue(pixel)};
    }

    private byte[] loadResource(String s) throws IOException {
        AssetFileDescriptor fd = InstrumentationRegistry.getContext().getResources().openRawResourceFd(edu.is.test.R.raw.sample);
        FileInputStream in = null;
        try {
            byte[] content = new byte[(int)fd.getLength()];
            FileInputStream inputStream = fd.createInputStream();
            inputStream.read(content);
            return content;
        } finally {
            if (in != null) {
                in.close();
            }
            fd.close();
        }
    }

}