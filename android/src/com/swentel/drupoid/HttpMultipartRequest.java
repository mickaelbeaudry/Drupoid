package com.swentel.drupoid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.util.Log;

/**
 * Based upon
 * http://stackoverflow.com/questions/4966910/androidhow-to-upload-mp3
 * -file-to-http-server.
 */
public class HttpMultipartRequest {
  static String lineEnd = "\r\n";
  static String twoHyphens = "--";
  static String boundary = "AaB03x87yxdkjnxvi7";

  /**
   * @param urlString
   * @param filePath
   * @param fileParameterName
   * @param parameters
   * @param cookieType
   * 
   * @todo document better + set optionals to end.
   */
  public static String execute(String urlString, String filePath, String fileParameterName, HashMap<String, String> parameters, int cookieType) throws IOException {
    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    DataInputStream dis = null;
    FileInputStream fileInputStream = null;
    URL url = new URL(urlString);
    File file = null;

    byte[] buffer;
    int maxBufferSize = 20 * 1024;
    try {

      // Open a HTTP connection to the URL
      conn = (HttpURLConnection) url.openConnection();

      // Send cookie ?
      // Seriously, use a constant here.
      // currently hardcoded, next up, store this in myprefs ?
      // lookup info re: storing session cookies in android.
      // maybe use cookiemanager ?
      if (cookieType == 1) {
        String myCookie = "SESS779077e599724a8ee5233b8788a3e237=4ATqxJNvQ4MSee1NQ9Nl1aok_kPqVZd6x5IUdYW3Yyc";
        conn.setRequestProperty("Cookie", myCookie);
      }

      // Allow Inputs
      conn.setDoInput(true);
      // Allow Outputs
      conn.setDoOutput(true);
      // Don't use a cached copy.
      conn.setUseCaches(false);
      // Use a post method.
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
      conn.connect();

      dos = new DataOutputStream(conn.getOutputStream());

      // See if we need to upload a file.
      if (filePath.length() > 0) {
        file = new File(filePath);
        fileInputStream = new FileInputStream(file);
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + fileParameterName + "\"; filename=\"" + file.toString() + "\"" + lineEnd);
        dos.writeBytes("Content-Type: text/xml" + lineEnd);
        dos.writeBytes(lineEnd);

        // create a buffer of maximum size
        buffer = new byte[Math.min((int) file.length(), maxBufferSize)];
        int length;
        // read file and write it into form...
        while ((length = fileInputStream.read(buffer)) != -1) {
          dos.write(buffer, 0, length);
        }
      }

      // Basic data.
      for (String name : parameters.keySet()) {
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + lineEnd);
        dos.writeBytes(lineEnd);
        dos.writeBytes(parameters.get(name));
      }

      // Send multipart form data necessary after file data.
      dos.writeBytes(lineEnd);
      dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
      dos.flush();

      // @todo Seriously, use a constant here.
      if (cookieType == 0) {
        String headerName = null;
        for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
          if (headerName.equals("Set-Cookie")) {
            // @todo only session id cookie.
            String cookie = conn.getHeaderField(i);
            cookie = cookie.substring(0, cookie.indexOf(";"));
            String cookieName = cookie.substring(0, cookie.indexOf("="));
            String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
            Log.d("Name", cookieName);
            Log.d("value", cookieValue);
          }
        }
      }
    }
    finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
      if (dos != null) {
        dos.close();
      }
    }

    // Read the server response.
    String sResponse = "";
    try {
      dis = new DataInputStream(conn.getInputStream());
      StringBuilder response = new StringBuilder();

      String line;
      while ((line = dis.readLine()) != null) {
        response.append(line);
      }

      sResponse = response.toString();
    }
    finally {
      if (dis != null) {
        dis.close();
      }
    }

    return sResponse;
  }
}
