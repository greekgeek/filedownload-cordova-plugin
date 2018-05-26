package com.greek.cordova.plugin;
// The native Toast API
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import android.os.*;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.net.URL;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileDownloader extends CordovaPlugin implements ActivityCompat.OnRequestPermissionsResultCallback {
  boolean DEBUG = false;
  final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
  final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

  boolean writePermission = false;
  boolean readPermission = false;

  String action = null;
  JSONArray args = null;
  CallbackContext callbackContext = null;

  public void DEBUGGER(String message){
    if (DEBUG) {
      System.out.println("DOWNLOADER LOGS::" + message);
    }
  }
  public int downloadFile() {
      URL url;
      File root;
      File dir;
      File file;
      InputStream in = null;
      String folder = "download";
      String fileURL = null;
      String filename = "download";
      FileOutputStream fos = null;
      try {
          JSONObject payload = args.getJSONObject(0);
          if (payload.has("folder")) {
              folder = payload.getString("folder");
          }
          if (payload.has("filename")) {
              filename = payload.getString("filename");
          }
          if(payload.has("fileurl")) {
              fileURL = payload.getString("fileurl");
          }
          if(filename == null || fileURL == null || filename == null) {
              callbackContext.error("Error in getting one of options: (filename, fileurl, filename)");
              // Error CB
          }
          try {
              DEBUGGER(payload.getString("url"));
              url = new URL(fileURL);
              HttpURLConnection connection = (HttpURLConnection)url.openConnection();
              connection.connect();
              int responseCode = connection.getResponseCode();
              if (responseCode != HttpURLConnection.HTTP_OK) {
                  callbackContext.error("Error in fetching the url" + fileURL + "with response code" + responseCode);
                  return false;
              }

              in = url.openStream();
              root = android.os.Environment.getExternalStorageDirectory();
              dir = new File (root.getAbsolutePath() + "/"+folder);
              dir.mkdirs();
              file = new File(dir, filename);
              fos = new FileOutputStream(file);
              int length = -1;
              byte[] buffer = new byte[1024];
              while ((length = in.read(buffer)) > -1) {
                  fos.write(buffer, 0, length);
              }
              fos.close();
              in.close();
          } catch(Exception e) {
              callbackContext.error("Error in plugin as " + e.getMessage());
          }
      } catch (Exception e) {
          // TODO Auto-generated catch block
          callbackContext.error("Error in plugin as " + e.getMessage());
      }
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
      callbackContext.sendPluginResult(pluginResult);
      return true;
  }
  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          this.readPermission = true;
            if(this.writePermission && this.readPermission) {
              this.downloadFile();
              }
          } else {
        }
          return;
      }
      case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          this.writePermission = true;
            if(this.writePermission && this.readPermission) {
              this.downloadFile();
            }
        } else {
        }
        return;
      }
    }
  }
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
      this.action = action;
      this.args = args;
      this.callbackContext = callbackContext;
      // Verify that the user sent a 'show' action
      if (!action.equals("download")) {
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
      }
      if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED) {

          ActivityCompat.requestPermissions(mainActivity,
                  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                  MY_PERMISSIONS_REQUEST_WRITE_STORAGE);

          if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                  != PackageManager.PERMISSION_GRANTED) {

              ActivityCompat.requestPermissions(mainActivity,
                      new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                      MY_PERMISSIONS_REQUEST_READ_STORAGE);

          } else {
                return downloadFile(action, args, callbackContext);
          }

          // Permission is not granted
      }

  }

}
