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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCParams;

import java.util.ArrayList;


public class MainActivity extends Activity {
    protected Bitmap displaying;
    protected int displayHeight;
    protected int displayWidth;
    protected int displayID;
    protected Double resolutionFactor = 3.0;
    protected String serverURL = "http://192.168.43.30:43876";
    protected ImageView imView;
    protected ListView listView;
    private final String[] itemsList = new String[] {"MRI (low resolution)", "MRI (high resolution", "DTI", "Surface"};

    /**
     * Build the dimension string for the rpc caller
     *
     * e.g. DimensionBuilder(10,20) returns (String) "[10,20]"
     *
     * @param Width    (int)
     * @param Height   (int)
     * @return (String)
     */
    private String DimensionBuilder(int Width, int Height) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append((int) (Width/resolutionFactor));
        sb.append(",");
        sb.append((int) (Height/resolutionFactor));
        sb.append("]");
        String Dimension = sb.toString();
        return Dimension;
    }

    private class RequestConnectionVolumeRendering extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... Params) {
            JSONRPCClient client = JSONRPCClient.create(Params[0], JSONRPCParams.Versions.VERSION_2);
            client.setConnectionTimeout(3000);
            client.setSoTimeout(2000);
            try
            {
                String Dimension = DimensionBuilder(displayWidth, displayHeight);
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
   }

    private class RequestConnectionRotate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... Params) {
            JSONRPCClient client = JSONRPCClient.create(Params[0], JSONRPCParams.Versions.VERSION_2);
            client.setConnectionTimeout(3000);
            client.setSoTimeout(2000);
            try
            {
                String Dimension = DimensionBuilder(displayWidth, displayHeight);
                String results = (String) client.call("Visualize", "Rotation", "[5,5]", 1, 1, 1, Dimension, 1234);
                byte[] b = Base64.decode(results.toString(), 0);
                Bitmap bMap = BitmapFactory.decodeByteArray(b, 0, b.length);
                displaying = bMap; // because thread cannot interacts with UI
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


    /**
     * A set of onclick handler that calls the ViewPort activity with different items shown according
     * to the Bundle b "key" passed to the intent. A list of id corresponding to data type is made
     * below:
     *  1. MRI1 (lowres nii)
     *  2. MRI2 (highres nii)
     *  3. DTI (vtk)
     *  4. ...
     *
     */
    // TODO: Write out the connection manager
    public void onClickButtonMRI1(View view) {
        Bundle b = new Bundle();
        b.putInt("key", 1); // 1
        Intent intent = new Intent(this, ViewPort.class);
        intent.putExtras(b);
        startActivity(intent);
//        displayHeight = imView.getHeight();
//        displayWidth = imView.getWidth();
////        Display display = getWindowManager().getDefaultDisplay();
////        Point size = new Point();
////        display.getRealSize(size);
////        displayHeight = size.y;
////        displayWidth = size.x;
//
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            RequestConnectionVolumeRendering rc = new RequestConnectionVolumeRendering();
//            rc.execute(serverURL,"MNI152_T1_1mm.nii.gz");
//            while(displaying==imView.getDrawingCache()){}
//            imView.setImageBitmap(displaying);
//        }
//        else {
//            Log.d("DEBUG", "onClickButtonGo() called with: " + "Failed Network");
//            return;
//        }
    }

    public void onClickButtonMRI2(View view) {
        Bundle b = new Bundle();
        b.putInt("key", 2); // 1
        Intent intent = new Intent(this, ViewPort.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onClickButtonDTI(View view) {
        Bundle b = new Bundle();
        b.putInt("key", 3); // 1
        Intent intent = new Intent(this, ViewPort.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onClickButtonDICOM(View view) {
        Bundle b = new Bundle();
        b.putInt("key", 5); // 1
        Intent intent = new Intent(this, ViewPort.class);
        intent.putExtras(b);
        startActivity(intent);
    }



}

