package edu.is.jpeg;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * A native JPEG image decoder.
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

    /**
     * Decompresses the JPEG stored in {@code source} with a size of {@code length} into {@code target}.
     * @param source the encoded JPEG image
     * @param length the size of the JPEG image in bytes
     * @param target the decoded JPEG image
     */
    native public void decompress(ByteBuffer source, int length, Bitmap target);

}
