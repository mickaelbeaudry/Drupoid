package com.swentel.drupoid;

import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DrupoidAuthentication extends Activity {

  String drupoidUser = "";
  String drupoidPass = "";
  String drupoidEndpoint = "";
  ProgressDialog dialog;

  /**
   * Settings layout.
   */
  protected void onCreate(Bundle savedInstanceState) {

    // Start settings activity.
    super.onCreate(savedInstanceState);
    setContentView(R.layout.authentication);

    // Set endpoint if available.
    EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
    drupoid_url.setText(Common.getPref(getBaseContext(), "drupoidEndpoint", ""));

    // Add listener on login button.
    Button login = (Button) findViewById(R.id.login);
    login.setOnClickListener(onLoginPress);
  }

  /**
   * OnClickListener on login button.
   */
  private final View.OnClickListener onLoginPress = new View.OnClickListener() {
    public void onClick(View v) {
      EditText drupoid_username = (EditText) findViewById(R.id.drupoid_username);
      EditText drupoid_password = (EditText) findViewById(R.id.drupoid_password);
      EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
      drupoidUser = drupoid_username.getText().toString();
      drupoidPass = drupoid_password.getText().toString();
      drupoidEndpoint = drupoid_url.getText().toString();

      if (drupoidUser.length() > 0 && drupoidPass.length() > 0 && drupoidEndpoint.length() > 0) {
        Common.setPref(getBaseContext(), "drupoidEndpoint", drupoidEndpoint);
        dialog = ProgressDialog.show(DrupoidAuthentication.this, getString(R.string.authenticating), getString(R.string.please_wait), true);
        new DrupoidAuthTask().execute();
      }
      else {
        Toast.makeText(getBaseContext(), R.string.missing_cred, Toast.LENGTH_LONG).show();
      }

      /*
       * if (Common.drupoidAuthenticated) { startActivity(new Intent(this,
       * DrupoidActivity.class)); }
       */
    }
  };

  /**
   * Authentication.
   */
  class DrupoidAuthTask extends AsyncTask<Void, Void, String> {

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
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      // Show message.
      Toast.makeText(getBaseContext(), sResponse, Toast.LENGTH_LONG).show();
      Common.drupoidAuthenticated = true;
    }
  }
}
