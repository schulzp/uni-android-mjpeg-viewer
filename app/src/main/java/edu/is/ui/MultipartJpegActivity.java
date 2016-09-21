package edu.is.ui;


import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

import edu.is.R;
import edu.is.jpeg.MultipartJpegLoader;

/**
 * A full screen activity backed by a surface view.
 */
public class MultipartJpegActivity extends Activity implements SurfaceHolder.Callback, MultipartJpegLoader.Callbacks {

    public static final String LOG_TAG = MultipartJpegActivity.class.getCanonicalName();
    public static final int LOADER_ID = 0;

    private SurfaceHolder mSurfaceHolder;

    private Paint mPaint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up a full-screen black window.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setBackgroundDrawableResource(android.R.color.black);

        setContentView(R.layout.activity_multipart_jpeg);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        Bundle loaderConfiguration = new Bundle();
        loaderConfiguration.putString(MultipartJpegLoader.BUNDLE_KEY_URL, "http://192.168.1.111:8080/video");
        initLoader(loaderConfiguration);
    }

    @Override
    public void surfaceChanged(SurfaceHolder sh, int f, int w, int h) {
        getLoader().targetChanged(w, h);
    }

    @Override
    public void surfaceCreated(SurfaceHolder sh) {
        getLoader().setCallbacks(this);
        getLoader().startLoading();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder sh) {
        getLoader().setCallbacks(null);
    }

    @Override
    public void jpegLoaded(Bitmap bitmap) {
        synchronized (mSurfaceHolder) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            try {
                canvas.drawBitmap(bitmap, 0, 0, mPaint);
            } finally {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void initLoader(final Bundle loaderConfiguration) {
        getLoaderManager().initLoader(LOADER_ID, loaderConfiguration, new LoaderManager.LoaderCallbacks<IOException>() {

            @Override
            public Loader<IOException> onCreateLoader(int i, Bundle bundle) {
                MultipartJpegLoader loader = new MultipartJpegLoader(getBaseContext(), loaderConfiguration);
                loader.setCallbacks(MultipartJpegActivity.this);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<IOException> loader, IOException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "Loader finished with exception", e);
                }
            }

            @Override
            public void onLoaderReset(Loader<IOException> loader) {
                Log.d(LOG_TAG, "Loader has been reset");
            }

        });
    }

    private MultipartJpegLoader getLoader() {
        Loader<?> loader = getLoaderManager().getLoader(LOADER_ID);
        if (!(loader instanceof MultipartJpegLoader)) {
            throw new IllegalStateException("Unexpected loader: Expected instance of " + MultipartJpegLoader.class + " but got " + loader);
        }
        return (MultipartJpegLoader) loader;
    }

}
