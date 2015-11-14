package lwong.mat.test2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCParams;
import java.lang.Object.*;
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ViewPort extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;


    private GestureDetector gestureDetector;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    protected Bitmap displaying;
    protected Bitmap displayingOld;
    protected int displayHeight;
    protected int displayWidth;

    private class RequestConnectionRotate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... Params) {
            JSONRPCClient client = JSONRPCClient.create(Params[0], JSONRPCParams.Versions.VERSION_2);
            client.setConnectionTimeout(3000);
            client.setSoTimeout(2000);
            try
            {
                // todo: remove the clumsy string builder
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                sb.append(displayWidth/2);
                sb.append(",");
                sb.append(displayHeight/2);
                sb.append("]");
                String Dimension = sb.toString();
                String results = (String) client.call("Visualize", "Rotation", Params[1], 1, 1, 1, Dimension, 1234);
                byte[] b = Base64.decode(results.toString(), 0);
                Bitmap bMap = BitmapFactory.decodeByteArray(b, 0, b.length);
                displaying = bMap;
                return results;
            }
            catch (JSONRPCException e) {
                e.printStackTrace();

            }
            return null;
        }
    }
    private class RequestConnectionVolumeRendering extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... Params) {
            JSONRPCClient client = JSONRPCClient.create(Params[0], JSONRPCParams.Versions.VERSION_2);
            client.setConnectionTimeout(3000);
            client.setSoTimeout(2000);
            try
            {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                sb.append(displayWidth/2);
                sb.append(",");
                sb.append(displayHeight/2);
                sb.append("]");
                String Dimension = sb.toString();
                String results = (String) client.call("Visualize", "VolumeRendering", "None", Params[1], 1, 1, Dimension, 1234);
                byte[] b = Base64.decode(results.toString(), 0);
                Bitmap bMap = BitmapFactory.decodeByteArray(b, 0, b.length);
                displaying = bMap;
                return results;
            }
            catch (JSONRPCException e) {
                e.printStackTrace();

            }
            return null;
        }

        protected void onPostExecute() {

        }



    }

    private class SingleTapConfirm extends SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_port);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        // Display initial image
        ImageView imView = (ImageView) findViewById(R.id.imageView2);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        displayHeight = size.y;
        displayWidth = size.x;

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            RequestConnectionVolumeRendering rc = new RequestConnectionVolumeRendering();
            rc.execute("http://137.189.141.230:43876","pre_t2_brain_50p.nii");
        }
        while (displaying == null) {

        }
        displayingOld = displaying;
        imView.setImageBitmap(displaying);

        // gesture controls
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());


        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.imageView2);
//        mContentView.setOnDragListener(mRotationListener);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean t = gestureDetector.onTouchEvent(e);
                if (t) {
                    toggle();
                    return true;
                }
                else {
                    final int action = e.getAction();

                    float dx;
                    float dy;
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            mPreviousX = e.getX();
                            mPreviousY = e.getY();
                            break;

                        case MotionEvent.ACTION_UP:
                            dx = e.getX() - mPreviousX;
                            dy = e.getY() - mPreviousY;

                            if (dx*dx < 0.5 && dy*dx < 0.5) {
                                return false;
                            }
                            else {
                                ImageView imView = (ImageView) findViewById(R.id.imageView2);
                                Display display = getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getRealSize(size);
                                displayHeight = size.y;
                                displayWidth = size.x;

                                StringBuilder sb = new StringBuilder();
                                sb.append("[");
                                sb.append(-dx / 12.);
                                sb.append(",");
                                sb.append(dy / 12.);
                                sb.append("]");
                                String Rotate = sb.toString();

                                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                                if (networkInfo != null && networkInfo.isConnected()) {
                                    RequestConnectionRotate rc = new RequestConnectionRotate();
                                    rc.execute(Rotate);
                                }
                                while (displaying == displayingOld) {
                                    try {
                                        Thread.sleep(5);
                                    } catch (InterruptedException error) {
                                        error.printStackTrace();
                                        break;
                                    }
                                }
                                imView.setImageBitmap(displaying);
                                return true;
                            }
                    }

                }
                return false;
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            return true;
        }
    };

//    private final View.OnDragListener mRotationListener = new View.OnDragListener() {
//        float initialX;
//        float initialY;
//        float x;
//        float y;
//        boolean eventStarted = false;
//
//        @Override
//        public boolean onDrag(View v, DragEvent event) {
//            int action = event.getAction();
//            switch (action) {
//                case DragEvent.ACTION_DRAG_STARTED:
//                    initialX = event.getX();
//                    initialY = event.getY();
//                    break;
//                case DragEvent.ACTION_DRAG_LOCATION:
//                    x = event.getX();
//                    y = event.getY();
//                    break;
//                case DragEvent.ACTION_DRAG_ENDED:
//                    float dx = x - mPreviousX;
//                    float dy = y - mPreviousY;
//
//                    ImageView imView = (ImageView) findViewById(R.id.imageView2);
//                    Display display = getWindowManager().getDefaultDisplay();
//                    Point size = new Point();
//                    display.getRealSize(size);
//                    displayHeight = size.y;
//                    displayWidth = size.x;
//
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("[");
//                    sb.append(dx/3.);
//                    sb.append(",");
//                    sb.append(dy/3.);
//                    sb.append("]");
//                    String Dimension = sb.toString();
//
//                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//                    if (networkInfo != null && networkInfo.isConnected()) {
//                        RequestConnectionRotate rc = new RequestConnectionRotate();
//                        rc.execute(Dimension);
//                    }
//                    while (displaying == null) {
//
//                    }
//                    imView.setImageBitmap(displaying);
//                    break;
//                case DragEvent.ACTION_DRAG_ENTERED:
//                    break;
//                case DragEvent.ACTION_DRAG_EXITED:
//                    break;
//                case DragEvent.ACTION_DROP:
//                    break;
//            }
//            return true;
//        }
//    };
//
//
//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        // MotionEvent reports input details from the touch screen
//        // and other input controls. In this case, you are only
//        // interested in events where the touch position changed.
//
//        float dx;
//        float dy;
//
//        final int action = e.getAction();
//
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mPreviousX = e.getX();
//                mPreviousY = e.getY();
//                break;
//            case MotionEvent.ACTION_UP:
//                dx = e.getX() - mPreviousX;
//                dy = e.getY() - mPreviousY;
//
//                ImageView imView = (ImageView) findViewById(R.id.imageView2);
//                Display display = getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getRealSize(size);
//                displayHeight = size.y;
//                displayWidth = size.x;
//
//                StringBuilder sb = new StringBuilder();
//                sb.append("[");
//                sb.append(-dx/7.);
//                sb.append(",");
//                sb.append(dy/7.);
//                sb.append("]");
//                String Rotate = sb.toString();
//
//                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//                if (networkInfo != null && networkInfo.isConnected()) {
//                    RequestConnectionRotate rc = new RequestConnectionRotate();
//                    rc.execute(Rotate);
//                }
//                while (displaying == displayingOld) {
//                    try{
//                        Thread.sleep(5);
//                    }
//                    catch (InterruptedException error) {
//                        error.printStackTrace();
//                        break;
//                    }
//                }
//                imView.setImageBitmap(displaying);
//                break;
//        }
//
//        return true;
//    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
