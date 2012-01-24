package com.swentel.drupoid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DrupoidSettings extends Activity {

  String duser = "";
  String dpass = "";
  String durl = "";
  private SharedPreferences mPref;

  /**
   * Settings layout.
   */
  protected void onCreate(Bundle savedInstanceState) {

    // @todo instead of saving the password, use a token auth method.
    mPref = this.getSharedPreferences("mPref", MODE_PRIVATE);

    // Start settings activity.
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    EditText drupoid_username = (EditText) findViewById(R.id.drupoid_username);
    EditText drupoid_password = (EditText) findViewById(R.id.drupoid_password);
    EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
    drupoid_username.setText(mPref.getString("drupoid_username", ""));
    drupoid_password.setText(mPref.getString("drupoid_password", ""));
    drupoid_url.setText(mPref.getString("drupoid_url", ""));

    // Add listener on upload button.
    Button save = (Button) findViewById(R.id.save_preferences);
    save.setOnClickListener(onSavePress);
  }

  /**
   * OnClickListener on upload button.
   */
  private final View.OnClickListener onSavePress = new View.OnClickListener() {
    public void onClick(View v) {
      EditText drupoid_username = (EditText) findViewById(R.id.drupoid_username);
      EditText drupoid_password = (EditText) findViewById(R.id.drupoid_password);
      EditText drupoid_url = (EditText) findViewById(R.id.drupoid_url);
      duser = drupoid_username.getText().toString();
      dpass = drupoid_password.getText().toString();
      durl = drupoid_url.getText().toString();
      if (duser.length() > 0 && dpass.length() > 0 && durl.length() > 0) {
        Editor editor = mPref.edit();
        editor.putString("drupoid_username", duser);
        editor.putString("drupoid_password", dpass);
        editor.putString("drupoid_url", durl);
        editor.apply();
        Toast.makeText(getBaseContext(), R.string.saved_prefs, Toast.LENGTH_LONG).show();
      }
      else {
        Toast.makeText(getBaseContext(), R.string.missing_prefs, Toast.LENGTH_LONG).show();
      }
    }
  };
}