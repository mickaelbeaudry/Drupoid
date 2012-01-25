package com.swentel.drupoid;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Main Activity.
 */
public class DrupoidActivity extends Activity {

  private ImageView imgView;
  private Bitmap bitmap;
  private String image_title;
  private String selectedImagePath;
  private ProgressDialog dialog;
  private final int SELECT_PICTURE = 1;
  private SharedPreferences mPref;
  InputStream inputStream;

  /**
   * Main onCreate.
   */
  public void onCreate(Bundle savedInstanceState) {

    mPref = this.getSharedPreferences("mPref", MODE_PRIVATE);

    // Start main activity.
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Listen to share menu.
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    String action = intent.getAction();
    if (Intent.ACTION_SEND.equals(action)) {
      if (extras.containsKey(Intent.EXTRA_STREAM)) {
        Uri selectedImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
        DrupoidSetPreview(selectedImageUri);
      }
    }

    // Add listener on image preview.
    imgView = (ImageView) findViewById(R.id.image_preview);
    imgView.setOnClickListener(onSelectPress);

    // Add listener on upload button.
    Button upload = (Button) findViewById(R.id.upload_button);
    upload.setOnClickListener(onUploadPress);
  }

  /**
   * Create options menu.
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    // @todo this shouldn't be a menu, but become a different layout.
    // based on authentication status, we either show login layout
    // or the app (with a logout button), for that we also need to
    // convert the response from the server into a json object
    // so we can return with a 'status' and 'result' key in the
    // the json.
    menu.add(Menu.NONE, 0, 0, getString(R.string.settings)).setIcon(android.R.drawable.ic_menu_preferences);
    return super.onCreateOptionsMenu(menu);
  }

  /**
   * Menu selection.
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case 0:
      startActivity(new Intent(this, DrupoidSettings.class));
      return true;
    }
    return false;
  }

  /**
   * OnClickListener on select button.
   */
  private final View.OnClickListener onSelectPress = new View.OnClickListener() {
    public void onClick(View v) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, getString(R.string.picture_select)), SELECT_PICTURE);
    }
  };

  /**
   * Start onActivityResult for image select.
   */
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == SELECT_PICTURE) {
        Uri selectedImageUri = data.getData();
        DrupoidSetPreview(selectedImageUri);
      }
    }
  }

  /**
   * OnClickListener on upload button.
   */
  private final View.OnClickListener onUploadPress = new View.OnClickListener() {
    public void onClick(View v) {

      if (!DrupoidIsOnline()) {
        AlertDialog alertDialog = new AlertDialog.Builder(DrupoidActivity.this).create();
        alertDialog.setMessage(getString(R.string.no_connection));
        alertDialog.setButton(getString(R.string.close), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
        alertDialog.show();
      }
      else {
        EditText title = (EditText) findViewById(R.id.title);
        if (title.getText().toString().length() > 0 && selectedImagePath.toString().length() > 0) {
          image_title = title.getText().toString();
          dialog = ProgressDialog.show(DrupoidActivity.this, getString(R.string.uploading), getString(R.string.please_wait), true);
          new DrupoidUploadTask().execute();
        }
        else {
          Toast.makeText(getBaseContext(), R.string.missing_data, Toast.LENGTH_LONG).show();
        }
      }
    }
  };

  /**
   * Check if we are connected.
   */
  private boolean DrupoidIsOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    // Test for connection
    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
      return true;
    }
    else {
      Log.v("Debug", "Internet Connection Not Present");
      return false;
    }
  }

  /**
   * Set preview in the imageView.
   */
  public void DrupoidSetPreview(Uri selectedImageUri) {

    // The selected image can either come from the Image gallery
    // or from the File manager.
    String fileManagerPath = selectedImageUri.getPath();
    String imageGalleryPath = getPath(selectedImageUri);
    if (imageGalleryPath != null) {
      selectedImagePath = imageGalleryPath;
    }
    else if (fileManagerPath != null) {
      selectedImagePath = fileManagerPath;
    }

    // Create preview.
    bitmap = DrupoidCalculateSize(selectedImagePath, 300);
    ImageView imageView = (ImageView) findViewById(R.id.image_preview);
    imageView.setImageBitmap(bitmap);
  }

  /**
   * Calculate size of preview.
   */
  private Bitmap DrupoidCalculateSize(String selectedImagePath, int maxSize) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(selectedImagePath, opts);
    int w = opts.outHeight, h = opts.outHeight;
    int maxDim = (w > h) ? w : h;

    int inSample = maxDim / maxSize;
    opts = new BitmapFactory.Options();
    opts.inSampleSize = inSample;
    bitmap = BitmapFactory.decodeFile(selectedImagePath, opts);

    return bitmap;
  }

  /**
   * Upload to Drupoid enabled server.
   */
  class DrupoidUploadTask extends AsyncTask<Void, Void, String> {

    protected String doInBackground(Void... unused) {
      String sResponse = "";

      // Get settings.
      String durl = mPref.getString("drupoid_url", "drupoid_url");

      // Parameters to send through.
      HashMap<String, String> Params = new HashMap<String, String>();
      Params.put("title", image_title);
      Params.put("request_type", "image_upload");

      // Perform request.
      try {
        sResponse = HttpMultipartRequest.execute(getBaseContext(), durl, Params, Common.SEND_COOKIE, selectedImagePath, "image");
      }
      catch (IOException e) {
      }

      return sResponse;
    }

    protected void onPostExecute(String sResponse) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      // Show message and reset application.
      Toast.makeText(getBaseContext(), sResponse, Toast.LENGTH_LONG).show();
      selectedImagePath = "";
      bitmap = null;
      EditText title = (EditText) findViewById(R.id.title);
      title.setText("");
      ImageView imageView = (ImageView) findViewById(R.id.image_preview);
      imageView.setImageResource(R.drawable.insert_image);
    }
  }

  /**
   * Get path of image.
   */
  public String getPath(Uri uri) {
    String[] projection = {
      MediaStore.Images.Media.DATA
    };
    Cursor cursor = managedQuery(uri, projection, null, null, null);
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }
}