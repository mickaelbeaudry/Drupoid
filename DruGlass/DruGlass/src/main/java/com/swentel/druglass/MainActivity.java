package com.swentel.druglass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Main activity.
 */
public class MainActivity extends Activity {

    private static final String IMAGE_FILE_NAME = Environment.getExternalStorageDirectory().getPath() + "/ImageTest.jpg";
    private boolean picTaken = false;

    private TextToSpeech mSpeech;
    private GestureDetector mGestureDetector;

    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        tvResult = (TextView) findViewById(R.id.tap_instruction);
        tvResult.setVisibility(View.INVISIBLE);

        // Even though the text-to-speech engine is only used in response to a menu action, we
        // initialize it when the application starts so that we avoid delays that could occur
        // if we waited until it was needed to start it up.
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });

        // Create the gesture detector.
        mGestureDetector = createGestureDetector(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Here we launch our intent to take  the snapshot.
        // You must specify the file name that you wish the image to be saved as (imageFileName), in the extras for the intent,
        // along with the maximum amount of time to wish to wait to acquire the camera (maximumWaitTimeForCamera - time in
        // milliseconds, e.g. 2000 = 2 seconds). This is done because the first call to get the camera does not always
        // work (especially when the app is responding to a voice trigger) so repeated calls are made until the camera is
        // acquired or we give up.
        // You must also specify the width and height of the preview image to show, and also the width and height of the
        // image to be saved from the camera (Snapshot width and height). Valid values are as follows:
        //
        //Preview Sizes
        //width=1920	height=1080
        //width=1280	height=960
        //width=1280	height=720
        //width=1024	height=768
        //width=1024	height=576
        //width=960	height=720
        //width=800	height=480
        //width=768	height=576
        //width=720	height=576
        //width=720	height=480
        //width=640	height=480
        //width=640	height=368
        //width=640	height=360
        //width=512	height=384
        //width=512	height=288
        //width=416	height=304
        //width=416	height=240
        //width=352	height=288
        //width=320	height=240
        //width=320	height=192
        //width=256	height=144
        //width=240	height=160
        //width=224	height=160
        //width=176	height=144
        //width=960	height=1280
        //width=720	height=1280
        //width=768	height=1024
        //width=576	height=1024
        //width=720	height=960
        //width=480	height=800
        //width=576	height=768
        //width=576	height=720
        //width=480	height=720
        //width=480	height=640
        //width=368	height=640
        //width=384	height=512
        //width=288	height=512
        //width=304	height=416
        //width=240	height=416
        //width=288	height=352
        //width=240	height=320
        //width=192	height=320
        //width=144	height=256
        //width=160	height=240
        //width=160	height=224
        //width=144	height=176
        //
        //Snapshot Sizes
        //width=2592	height=1944
        //width=2560	height=1888
        //width=2528	height=1856
        //width=2592	height=1728
        //width=2592	height=1458
        //width=2560	height=1888
        //width=2400	height=1350
        //width=2304	height=1296
        //width=2240	height=1344
        //width=2160	height=1440
        //width=2112	height=1728
        //width=2112	height=1188
        //width=2048	height=1152
        //width=2048	height=1536
        //width=2016	height=1512
        //width=2016	height=1134
        //width=2000	height=1600
        //width=1920	height=1080
        //width=1600	height=1200
        //width=1600	height=900
        //width=1536	height=864
        //width=1408	height=792
        //width=1344	height=756
        //width=1296	height=972
        //width=1280	height=1024
        //width=1280	height=720
        //width=1152	height=864
        //width=1280	height=960
        //width=1024	height=768
        //width=1024	height=576
        //width=640	height=480
        //width=320	height=240

        if (!picTaken) {
            Intent intent = new Intent(this, GlassSnapshotActivity.class);
            intent.putExtra("imageFileName",IMAGE_FILE_NAME);
            intent.putExtra("previewWidth", 800);
            intent.putExtra("previewHeight", 480);
            intent.putExtra("snapshotWidth", 1280);
            intent.putExtra("snapshotHeight", 960);
            intent.putExtra("maximumWaitTimeForCamera", 2000);
            startActivityForResult(intent, 1);
        }
    }

   @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
       return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }

    /**
     * Gesture detector.
     *
     * @param context
     *   The current context.
     *
     * @return FALSE|gestureDetector
     */
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    openOptionsMenu();
                    return true;
                }
                return false;
            }
        });

        return gestureDetector;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                finish();
                return true;
            case R.id.share:

                tvResult.setText(getString(R.string.sending));

                // Setup params.
                RequestParams params = new RequestParams();
                params.put("title", Config.imageTitle);
                params.put("glass_secret", Config.glassSecret);
                params.put("account_name", Config.drupalAccountName);
                File myFile = new File(IMAGE_FILE_NAME);
                try {
                    params.put("image", myFile);
                } catch(FileNotFoundException ignored) {}

                // Send file.
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Config.glassEndPoint, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        tvResult.setText(getString(R.string.send));
                        printOptionsText();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        tvResult.setText(getString(R.string.failed));
                        printOptionsText();
                    }

                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Print options text.
     */
    private void printOptionsText() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(getString(R.string.tapforoptions));
            }

        }, 3000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        picTaken = true;
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    File f = new File(IMAGE_FILE_NAME);
                    if (f.exists()) {
                        Bitmap b = BitmapFactory.decodeFile(IMAGE_FILE_NAME);
                        ImageView image = (ImageView) findViewById(R.id.photo);
                        image.setImageBitmap(b);
                        TextView tap = (TextView) findViewById(R.id.tap_instruction);
                        tap.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Close the Text to Speech Library.
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        }
        super.onDestroy();
    }

}