package lwong.mat.test2;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.MemoryFile;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCParams;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity {
    protected Bitmap displaying;
    protected int displayHeight;
    protected int displayWidth;
    protected Bitmap displayingOld;

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
                sb.append(256);
                sb.append(",");
                sb.append(256);
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
                sb.append(400);
                sb.append(",");
                sb.append(400);
                sb.append("]");
                String Dimension = sb.toString();
                String results = (String) client.call("Visualize", "Rotation", "[5,5]", 1, 1, 1, Dimension, 1234);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

//    public void onClickButtonGo(View view) {
//        Intent intent = new Intent(this, ViewPort.class);
//        startActivities
//
//
//    }

    public void onClickButtonGo(View view) {
//        Intent intent = new Intent(this, ViewPort.class);
//        startActivity(intent);
        ImageView imView = (ImageView) findViewById(R.id.imageView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        displayHeight = size.y;
        displayWidth = size.x;

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            RequestConnectionVolumeRendering rc = new RequestConnectionVolumeRendering();
            rc.execute("http://137.189.141.230:43876","MNI152_T1_1mm.nii.gz");
        }
        while (displaying == null) {

        }
        displayingOld = displaying;
        imView.setImageBitmap(displaying);
    }

    public void onClickButtonDTITest(View vew) {
        ImageView imView = (ImageView) findViewById(R.id.imageView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        displayHeight = size.y;
        displayWidth = size.x;

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            RequestConnectionVolumeRendering rc = new RequestConnectionVolumeRendering();
            rc.execute("http://137.189.141.230:43876","tract.vtk");
        }
        while (displaying == null) {

        }
        displayingOld = displaying;
        imView.setImageBitmap(displaying);
    }

    public void onClickButtonRotate(View view) {
        ImageView imView = (ImageView) findViewById(R.id.imageView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        displayHeight = size.y;
        displayWidth = size.x;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            RequestConnectionRotate rc = new RequestConnectionRotate();
            rc.execute("http://137.189.141.230:43876", "[10,10]");
        }
        imView.setImageBitmap(displaying);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        // MotionEvent reports input details from the touch screen
//        // and other input controls. In this case, you are only
//        // interested in events where the touch position changed.
//
//        float x = e.getX();
//        float y = e.getY();
//
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_MOVE:
//
//                float dx = x - mPreviousX;
//                float dy = y - mPreviousY;
//
////                // reverse direction of rotation above the mid-line
////                if (y > getHeight() / 2) {
////                    dx = dx * -1 ;
////                }
////
////                // reverse direction of rotation to left of the mid-line
////                if (x < getWidth() / 2) {
////                    dy = dy * -1 ;
////                }
//
//
//        }
//
//        mPreviousX = x;
//        mPreviousY = y;
//        return true;
//    }


}

