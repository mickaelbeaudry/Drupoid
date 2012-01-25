package com.swentel.drupoid;

import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DrupoidSettings extends Activity {

  String drupoidUser = "";
  String drupoidPass = "";
  String drupoidEndpoint = "";

  /**
   * Settings layout.
   */
  protected void onCreate(Bundle savedInstanceState) {

    // Start settings activity.
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    // Set endpoint if available.
    EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
    drupoid_url.setText(Common.getPref(getBaseContext(), "drupoidEndpoint", ""));

    // Add listener on login button.
    Button save = (Button) findViewById(R.id.save_preferences);
    save.setOnClickListener(onSavePress);
  }

  /**
   * OnClickListener on login button.
   */
  private final View.OnClickListener onSavePress = new View.OnClickListener() {
    public void onClick(View v) {
      EditText drupoid_username = (EditText) findViewById(R.id.drupoid_username);
      EditText drupoid_password = (EditText) findViewById(R.id.drupoid_password);
      EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
      drupoidUser = drupoid_username.getText().toString();
      drupoidPass = drupoid_password.getText().toString();
      drupoidEndpoint = drupoid_url.getText().toString();

      if (drupoidUser.length() > 0 && drupoidPass.length() > 0 && drupoidEndpoint.length() > 0) {

        // @todo add async task
        // @todo rename to login and use the pattern that barbarosso send me!
        // instead of showing the pick screen, show login screen if we
        // don't have a cookie and change.
        // @add logout request.
        String sResponse = "";
        HashMap<String, String> Params = new HashMap<String, String>();
        Params.put("request_type", "authenticate");
        Params.put("drupoid_username", drupoidUser);
        Params.put("drupoid_password", drupoidPass);
        try {
          sResponse = HttpMultipartRequest.execute(getBaseContext(), drupoidEndpoint, Params, Common.SAVE_COOKIE, "", "");
        }
        catch (IOException e) {
        }

        Common.setPref(getBaseContext(), "drupoidEndpoint", drupoidEndpoint);
        Toast.makeText(getBaseContext(), sResponse, Toast.LENGTH_LONG).show();
      }
      else {
        Toast.makeText(getBaseContext(), R.string.missing_prefs, Toast.LENGTH_LONG).show();
      }
    }
  };
}
