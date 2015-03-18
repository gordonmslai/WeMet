// include following in manifest
// dependencies {
//       compile 'com.loopj.android:android-async-http:1.4.5'
// }
package com.combativesornah.wemet;

import android.util.Log;

import com.loopj.android.http.*;

public class WeMetAPIClient {
  private static final String BASE_URL = "http://192.168.2.7/";

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(getAbsoluteUrl(url), params, responseHandler);
  }
  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  public static void get_profile(String username, AsyncHttpResponseHandler responseHandler) {
      Log.e(">>>>", "In get_profile");
      WeMetAPIClient.get("profiles/" + username, null, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
  }
}
