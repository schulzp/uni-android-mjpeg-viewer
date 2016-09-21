package edu.is.jpeg;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * Created by peter on 18/09/16.
 */
public class Decompressor {

    static {
        System.loadLibrary("decompressor");
    }

    public static class Exception extends RuntimeException {

        public Exception(String message) {
            super(message);
        }

    }

    native public void decompress(ByteBuffer source, int length, Bitmap target) throws Exception;

}
