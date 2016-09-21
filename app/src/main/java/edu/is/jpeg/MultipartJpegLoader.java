package edu.is.jpeg;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peter on 21/09/16.
 */
public class MultipartJpegLoader extends AsyncTaskLoader<IOException> {

    private boolean mCanceled = false;

    public interface Callbacks {

        void jpegLoaded(Bitmap bitmap);

    }

    public static final String BUNDLE_KEY_URL = "url";

    public static final String BUNDLE_KEY_BUFFER_SIZE = "buffer.size";

    private static final Pattern MULTIPART_BOUNDARY_PATTERN = Pattern.compile("multipart/x-mixed-replace;\\s*boundary=([^ ;]+)");

    public static final String LOG_TAG = MultipartJpegLoader.class.getCanonicalName();

    private final Decompressor mDecompressor = new Decompressor();

    private final URL mUrl;

    private final Bundle mConfig;

    private Bitmap mBitmap;

    private WeakReference<Callbacks> mCallbacks;

    public MultipartJpegLoader(Context context, Bundle config) {
        super(context);
        mConfig = config;
        try {
            mUrl = URI.create(config.getString(BUNDLE_KEY_URL)).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("expected well-formed bundle entry 'url'", e);
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = new WeakReference<Callbacks>(callbacks);
    }

    @Override
    public boolean cancelLoad() {
        mCanceled = true;
        return super.cancelLoad();
    }

    @Override
    public IOException loadInBackground() {
        try {
            URLConnection urlConnection = mUrl.openConnection();
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();

            Matcher boundaryMatcher = MULTIPART_BOUNDARY_PATTERN.matcher(urlConnection.getHeaderField("Content-Type"));
            if (boundaryMatcher.matches()) {
                byte[] boundaryPattern = boundaryMatcher.group(1).getBytes(Charset.forName("UTF-8"));
                ByteBuffer buffer = ByteBuffer.allocateDirect(mConfig.getInt(BUNDLE_KEY_BUFFER_SIZE, 20000));
                MultipartJpegInputStreamReader reader = new MultipartJpegInputStreamReader(urlConnection.getInputStream(), boundaryPattern);

                while (!mCanceled && reader.read(buffer) > 0) {
                    try {
                        synchronized (mDecompressor) {
                            if (mBitmap != null) {
                                mDecompressor.decompress(buffer, buffer.position(), mBitmap);
                                jpegLoaded(mBitmap);
                            }
                        }
                    } catch (Decompressor.Exception e) {
                        Log.e(LOG_TAG, "Failed to decompress JPEG frame", e);
                        return new IOException("Failed to decompress JPEG frame", e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to load JPEG frame", e);
            return e;
        }
        return null;
    }

    public void targetChanged(int w, int h) {
        if (mBitmap == null || w != mBitmap.getWidth() || h != mBitmap.getHeight()) {
            synchronized (mDecompressor) {
                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
        }
    }

    private void jpegLoaded(Bitmap bitmap) {
        if (mCallbacks.get() != null) {
            mCallbacks.get().jpegLoaded(bitmap);
        }
    }

}

