package com.swentel.drupoid;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Main Activity.
 */
public class DrupoidActivity extends Activity {

  private int result;
  private String message;
  private Bitmap bitmap;
  private String image_title;
  private String selectedImagePath;
  private ProgressDialog dialog;
  private final int SELECT_PICTURE = 1;
  InputStream inputStream;
  String drupoidUser = "";
  String drupoidPass = "";
  String drupoidEndpoint = "";

  /**
   * Main onCreate.
   */
  public void onCreate(Bundle savedInstanceState) {

    // Start main activity.
    super.onCreate(savedInstanceState);

    // Verify we have a DrupoidURL and a DrupoidCookie. If not, go to
    // the authentication screen.
    String drupoidEndpoint = Common.getPref(getBaseContext(), "drupoidEndpoint", "");
    String drupoidCookie = Common.getPref(getBaseContext(), "drupoidCookie", "");
    if (drupoidEndpoint.length() == 0 || drupoidCookie.length() == 0) {
      DrupoidSetAuthLayout();
    }
    else {
      // Authenticated.
      Common.drupoidAuthenticated = true;

      // Set upload layout.
      DrupoidSetUploadLayout();

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
    }
  }

  /**
   * Create options menu.
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    if (Common.drupoidAuthenticated) {
      menu.add(Menu.NONE, 0, 0, getString(R.string.logout)).setIcon(android.R.drawable.ic_lock_power_off);
      return super.onCreateOptionsMenu(menu);
    }

    return false;
  }

  /**
   * Menu selection.
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    if (!DrupoidIsOnline()) {
      DrupoidDialog(getString(R.string.no_connection), getString(R.string.check_internet_settings));
      return false;
    }

    dialog = ProgressDialog.show(DrupoidActivity.this, getString(R.string.logging_out), getString(R.string.please_wait), true);
    new DrupoidLogoutTask().execute();

    return true;
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
   * OnClickListener on upload button.
   */
  private final View.OnClickListener onUploadPress = new View.OnClickListener() {
    public void onClick(View v) {

      if (!DrupoidIsOnline()) {
        DrupoidDialog(getString(R.string.no_connection), getString(R.string.check_internet_settings));
        return;
      }

      EditText title = (EditText) findViewById(R.id.title);
      if (title.getText().toString().length() > 0 && selectedImagePath.toString().length() > 0) {
        image_title = title.getText().toString();
        dialog = ProgressDialog.show(DrupoidActivity.this, getString(R.string.uploading), getString(R.string.please_wait), true);
        new DrupoidUploadTask().execute();
      }
      else {
        DrupoidDialog(getString(R.string.warning), getString(R.string.missing_data));
      }
    }
  };

  /**
   * OnClickListener on login button.
   */
  private final View.OnClickListener onLoginPress = new View.OnClickListener() {
    public void onClick(View v) {

      if (!DrupoidIsOnline()) {
        DrupoidDialog(getString(R.string.no_connection), getString(R.string.check_internet_settings));
        return;
      }

      EditText drupoid_username = (EditText) findViewById(R.id.drupoid_username);
      EditText drupoid_password = (EditText) findViewById(R.id.drupoid_password);
      EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
      drupoidUser = drupoid_username.getText().toString();
      drupoidPass = drupoid_password.getText().toString();
      drupoidEndpoint = drupoid_url.getText().toString();

      if (drupoidUser.length() > 0 && drupoidPass.length() > 0 && drupoidEndpoint.length() > 0) {
        Common.setPref(getBaseContext(), "drupoidEndpoint", drupoidEndpoint);
        dialog = ProgressDialog.show(DrupoidActivity.this, getString(R.string.authenticating), getString(R.string.please_wait), true);
        new DrupoidLoginTask().execute();
      }
      else {
        DrupoidDialog(getString(R.string.warning), getString(R.string.missing_cred));
      }
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
   * Parse response into JSON object.
   */
  public void DrupoidParseResponse(String sResponse) {
    try {
      JSONObject jObject = new JSONObject(sResponse);
      result = jObject.getInt("result");
      message = jObject.getString("message");
    }
    catch (JSONException e1) {
      result = Common.JSON_PARSE_ERROR;
      DrupoidDialog(getString(R.string.warning), getString(R.string.parse_json_error));
    }
    catch (ParseException e1) {
      result = Common.SERVER_PARSE_ERROR;
      DrupoidDialog(getString(R.string.warning), getString(R.string.parse_server_error));
    }
  }

  /**
   * Check if we are connected.
   */
  public boolean DrupoidIsOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    // Test for connection
    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
      return true;
    }
    return false;
  }

  /**
   * Set upload layout and add listeners.
   */
  public void DrupoidSetUploadLayout() {
    setContentView(R.layout.main);

    Button upload = (Button) findViewById(R.id.upload_button);
    upload.setOnClickListener(onUploadPress);
    ImageView imgView = (ImageView) findViewById(R.id.image_preview);
    imgView.setOnClickListener(onSelectPress);
    DrupoidCloseKeyboard();
  }

  /**
   * Set the login layout and listeners.
   */
  public void DrupoidSetAuthLayout() {
    setContentView(R.layout.authentication);

    Button login = (Button) findViewById(R.id.login);
    login.setOnClickListener(onLoginPress);

    // Set endpoint if available.
    EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
    String drupoidEndpoint = Common.getPref(getBaseContext(), "drupoidEndpoint", "");
    drupoid_url.setText(drupoidEndpoint);
    DrupoidCloseKeyboard();
  }

  /**
   * Close keyboard
   */
  public void DrupoidCloseKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
  }

  /**
   * Show dialog.
   */
  public void DrupoidDialog(String title, CharSequence message) {
    AlertDialog alertDialog = new AlertDialog.Builder(DrupoidActivity.this).create();
    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
    alertDialog.setTitle(title);
    alertDialog.setMessage(message);
    alertDialog.setButton(getString(R.string.close), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });
    alertDialog.show();
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
  public Bitmap DrupoidCalculateSize(String selectedImagePath, int maxSize) {
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
   * Upload task.
   */
  class DrupoidUploadTask extends AsyncTask<Void, Void, String> {

    protected String doInBackground(Void... unused) {
      String sResponse = "";

      // Get endpoint.
      String drupoidEndpoint = Common.getPref(getBaseContext(), "drupoidEndpoint", "");

      // Parameters to send through.
      HashMap<String, String> Params = new HashMap<String, String>();
      Params.put("title", image_title);
      Params.put("request_type", "image_upload");

      // Perform request.
      try {
        sResponse = HttpMultipartRequest.execute(getBaseContext(), drupoidEndpoint, Params, Common.SEND_COOKIE, selectedImagePath, "image");
      }
      catch (IOException e) {
      }

      return sResponse;
    }

    protected void onPostExecute(String sResponse) {

      // Close dialog.
      if (dialog.isShowing()) {
        dialog.dismiss();
      }

      // Parse response.
      DrupoidParseResponse(sResponse);

      // Show message and reset application.
      if (result == Common.SUCCESS) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        selectedImagePath = "";
        bitmap = null;
        EditText title = (EditText) findViewById(R.id.title);
        title.setText("");
        ImageView imageView = (ImageView) findViewById(R.id.image_preview);
        imageView.setImageResource(R.drawable.insert_image);
      }
      // Go to login screen.
      else if (result == Common.NO_AUTH) {
        Common.drupoidAuthenticated = false;
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        DrupoidSetAuthLayout();
      }
      // Show warning.
      else if (result < Common.JSON_PARSE_ERROR) {
        DrupoidDialog(getString(R.string.warning), message);
      }
    }
  }

  /**
   * Login task.
   */
  class DrupoidLoginTask extends AsyncTask<Void, Void, String> {

    protected String doInBackground(Void... unused) {
      String sResponse = "";

      // Get endpoint.
      String drupoidEndpoint = Common.getPref(getBaseContext(), "drupoidEndpoint", "");
      HashMap<String, String> Params = new HashMap<String, String>();
      Params.put("request_type", "authenticate");
      Params.put("drupoid_username", drupoidUser);
      Params.put("drupoid_password", drupoidPass);
      try {
        sResponse = HttpMultipartRequest.execute(getBaseContext(), drupoidEndpoint, Params, Common.SAVE_COOKIE, "", "");
      }
      catch (IOException e) {
      }

      return sResponse;
    }

    protected void onPostExecute(String sResponse) {

      // Close dialog.
      if (dialog.isShowing()) {
        dialog.dismiss();
      }

      // Parse response.
      DrupoidParseResponse(sResponse);

      if (result == Common.SUCCESS) {
        Common.drupoidAuthenticated = true;
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        DrupoidSetUploadLayout();
      }
      else if (result < Common.JSON_PARSE_ERROR) {
        DrupoidDialog(getString(R.string.warning), message);
      }
    }
  }

  /**
   * Logout task.
   */
  class DrupoidLogoutTask extends AsyncTask<Void, Void, String> {

    protected String doInBackground(Void... unused) {
      String sResponse = "";

      // Get endpoint.
      String drupoidEndpoint = Common.getPref(getBaseContext(), "drupoidEndpoint", "");

      // Parameters to send through.
      HashMap<String, String> Params = new HashMap<String, String>();
      Params.put("request_type", "logout");
      // Perform request.
      try {
        sResponse = HttpMultipartRequest.execute(getBaseContext(), drupoidEndpoint, Params, Common.SEND_COOKIE, "", "");
      }
      catch (IOException e) {
      }

      return sResponse;
    }

    protected void onPostExecute(String sResponse) {

      // Close dialog.
      if (dialog.isShowing()) {
        dialog.dismiss();
      }

      // Parse response.
      DrupoidParseResponse(sResponse);

      // Show message and go to login screen.
      if (result == 1) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        Common.delPref(getBaseContext(), "drupoidCookie");
        Common.drupoidAuthenticated = false;
        DrupoidSetAuthLayout();
      }
      else {
        DrupoidDialog(getString(R.string.warning), message);
      }
    }
  }
}