package com.swentel.drupoid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Common class with various variables, getters, setters and helper methods.
 */
public class Common {
  public static final int SEND_COOKIE = 1;
  public static final int SAVE_COOKIE = 2;

  /**
   * Get a preference.
   * 
   * @param ctxt
   *        The current context.
   * @param pref
   *        The name of the preference
   * @param DefaultValue
   *        The default value
   * @return The value of the preference.
   */
  public static String getPref(Context ctxt, String pref, String DefaultValue) {
    SharedPreferences sharedPreferences = ctxt.getSharedPreferences("drupoid", Context.MODE_PRIVATE);
    return sharedPreferences.getString(pref, DefaultValue);
  }

  /**
   * Set a preference.
   * 
   * @param ctxt
   *        The current context.
   * @param pref
   *        The name of the preference
   * @param value
   *        The value
   */
  public static void setPref(Context ctxt, String pref, String value) {
    SharedPreferences sharedPreferences = ctxt.getSharedPreferences("drupoid", Context.MODE_PRIVATE);
    Editor editor = sharedPreferences.edit();
    editor.putString(pref, value);
    editor.apply();
  }
}