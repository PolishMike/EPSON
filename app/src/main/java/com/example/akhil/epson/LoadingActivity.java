package com.example.akhil.epson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoadingActivity extends AppCompatActivity {
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
    private final Handler mHideHandler = new Handler();
    private View mContentView;
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
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mControlsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Log.d("IP",sharedPreferences.getString(PREF_IP,"NULL"));
        Log.d("PORT",sharedPreferences.getString(PREF_PORT,"NULL"));

        ArrayList<String> parameterValue = new ArrayList<String>();
        parameterValue.add(0,"init");

        String ipAddress = sharedPreferences.getString(PREF_IP,"NULL");
        String portNumber = sharedPreferences.getString(PREF_PORT,"NULL");
        String requestType = "init";

        new HttpRequestAsyncTask(
                findViewById(R.id.content).getContext(), parameterValue, ipAddress, portNumber,
                requestType).execute();



    }


    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {
        private String requestReply,ipAddress, portNumber;
        private Context context;

        private AlertDialog alertDialog;
        private String requestType;
        private ArrayList<String>  parameterValue;


        public HttpRequestAsyncTask(Context context, ArrayList<String> parameterValue, String ipAddress,
                                    String portNumber, String requestType) {

            this.context = context;
            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.requestType = requestType;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP Address:")
                    .create();


        }

        @Override
        protected Void doInBackground(Void... params) {
            InputStream inputStream = null;

            /*alertDialog.setMessage("Data sent, waiting for reply from device...");
            alertDialog.setCancelable(false);
            if(!alertDialog.isShowing()) {
                alertDialog.show();
            }*/
            this.requestReply = sendRequest(parameterValue, ipAddress, context,
                    portNumber, inputStream, requestType);
            return null;
        }
        @Override
        protected void  onPostExecute(Void avoid) {

            Toast.makeText(this.context, this.requestReply, Toast.LENGTH_SHORT).show();

        }
    }

    public String sendRequest(ArrayList<String> parameterValue, String ipAddress, Context context,
                              String portNumber,InputStream inputStream,
                              String requestType) {


            /*
            The request will be of the form :
            http://192.168.X.X:80/?mode=xxxxx&unit=xx&code=xxxx
            */
        /*
        * Request Type defines the type of request That is send
        * Request Type Can Be Of The Following Forms :
          * init : Initial Message Which Checks The Connection
          * rc4key: Initialises The RSA KEY
          * normal: Normal Messages
        * */

        String serverResponse = "ERROR";

        String link = "";


        if(requestType.equals("init"))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0);
        else if(requestType.equals("rc4key"))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0)
                    +"&unit="+parameterValue.get(1);
        else
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0)
                    +"&unit="+parameterValue.get(1)+"&code="+parameterValue.get(2);

        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            inputStream = new BufferedInputStream(conn.getInputStream());
            serverResponse = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return serverResponse;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

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

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
