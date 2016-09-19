package edu.is;

import android.graphics.Bitmap;

/**
 * Created by peter on 18/09/16.
 */
public class Decompressor {

    static {
        System.loadLibrary("decompressor");
    }

    native public void decompress(byte[] source, int length, Bitmap target);

}
