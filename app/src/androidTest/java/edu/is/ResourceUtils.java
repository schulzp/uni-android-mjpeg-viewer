package edu.is;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Resources related utilities.
 */
public class ResourceUtils {

    public static ByteBuffer loadResource(int resource, Context context) throws IOException {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(resource);
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
