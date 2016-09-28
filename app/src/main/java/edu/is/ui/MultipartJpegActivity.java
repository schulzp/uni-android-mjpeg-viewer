package edu.is.ui;


import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    private Rect mTargetRect;
    private Rect mSourceRect;

    private long mLastImageMilliseconds = 0;
    private int mImagesSinceLastRefresh = 0;
    private double mFPS;

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

        try {
            Bundle loaderConfiguration = new Bundle();
            loaderConfiguration.putString(MultipartJpegLoader.BUNDLE_KEY_URL, determineUrl().toString());
            initLoader(loaderConfiguration);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize loader", e);
        }
    }

    private URL determineUrl() throws IOException {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String host = "";
        if (wifiInfo.getSSID().equals("Mikrowellenschleuder")) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            byte[] address = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dhcpInfo.ipAddress).array();
            address[3] = 0x01;
            host = InetAddress.getByAddress(address).getHostAddress();
            Log.i(LOG_TAG, "Connecting to gateway at " + host);
        }
        return new URL("http", host, 8080, "/video");
    }



    @Override
    public void surfaceChanged(SurfaceHolder sh, int f, int w, int h) {
        getLoader().targetChanged(320, 240);
        mSourceRect = new Rect(0, 0, 320, 240);
        mTargetRect = new Rect(
                (w - mSourceRect.width()) / 2,
                (h - mSourceRect.height()) / 2, w, h);

        mLastImageMilliseconds = System.currentTimeMillis();
    }

    @Override
    public void surfaceCreated(SurfaceHolder sh) {
        getLoader().setCallbacks(this);
        getLoader().forceLoad();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder sh) {
        getLoader().setCallbacks(null);
    }

    @Override
    public void jpegLoaded(Bitmap bitmap) {
        synchronized (mSurfaceHolder) {
            if (mImagesSinceLastRefresh++ >= 10) {
                mFPS = mImagesSinceLastRefresh * (1000d / (System.currentTimeMillis() - mLastImageMilliseconds));
                mImagesSinceLastRefresh = 0;
                mLastImageMilliseconds = System.currentTimeMillis();
            }
            Canvas canvas = mSurfaceHolder.lockCanvas();

            try {
                canvas.drawBitmap(bitmap, mSourceRect, mTargetRect, mPaint);
                //mPaint.setColor(Color.RED);
                //mPaint.setTextSize(20);
                //canvas.drawText("FPS: " + Math.floor(mFPS), 20, 50, mPaint);
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
