package edu.is;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Resources related utilities.
 */
public class ResourceUtils {

    public static ByteBuffer loadResource(int resource, Context context) throws IOException {
        InputStream source = openResource(resource, context);
        try {
            ByteArrayOutputStream cache = new ByteArrayOutputStream(10000);
            IOUtils.copy(source, cache);
            ByteBuffer buffer = ByteBuffer.allocateDirect(cache.size());
            buffer.put(cache.toByteArray());
            buffer.clear();
            return buffer;
        } finally {
            source.close();
        }
    }

    public static InputStream openResource(int resource, Context context) throws IOException {
        return context.getResources().openRawResource(resource);
    }

    public static FileInputStream openResourceFile(int resource, Context context) throws IOException {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(resource);
        return fd.createInputStream();
    }

}
