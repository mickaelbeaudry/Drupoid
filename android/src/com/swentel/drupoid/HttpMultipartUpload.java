package com.swentel.drupoid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Based upon
 * http://stackoverflow.com/questions/4966910/androidhow-to-upload-mp3
 * -file-to-http-server.
 */
public class HttpMultipartUpload {
  static String lineEnd = "\r\n";
  static String twoHyphens = "--";
  static String boundary = "AaB03x87yxdkjnxvi7";

  public static String upload(String urlString, String filePath, String fileParameterName, HashMap<String, String> parameters) throws IOException {
    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    DataInputStream dis = null;
    FileInputStream fileInputStream = null;
    File file = new File(filePath);
    URL url = new URL(urlString);

    byte[] buffer;
    int maxBufferSize = 20 * 1024;
    try {

      // Client request.
      fileInputStream = new FileInputStream(file);

      // Open a HTTP connection to the URL
      conn = (HttpURLConnection) url.openConnection();
      // Allow Inputs
      conn.setDoInput(true);
      // Allow Outputs
      conn.setDoOutput(true);
      // Don't use a cached copy.
      conn.setUseCaches(false);
      // Use a post method.
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

      dos = new DataOutputStream(conn.getOutputStream());

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
      // @todo this should read in a jSon response.
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
