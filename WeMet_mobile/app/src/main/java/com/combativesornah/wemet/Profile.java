package com.combativesornah.wemet;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.widget.ArrayAdapter;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Gordon Lai on 2/22/2015.
 */
public class Profile {
    String first;
    String last;
    String pic;
    String email;
    public Profile(String f, String l, String p) {
        first = f;
        last = l;
        pic = p;
    }
}

class ProfileList {
    ArrayList<Profile> profiles = new ArrayList<Profile>();
    String first = "First";
    String last = "Last";
    String pic;
//    String email;
    public ProfileList(int num) {
        for (int i = 0; i < num; i++) {
            Profile p = new Profile(first + i, last + i, "mypic3");
            profiles.add(p);
        }
    }
    public ProfileList() {
    }




    void refresh(final ArrayAdapter<Profile> p_adapter) {
        String myusername = "gordonmslai";

        WeMetAPIClient.get_profile(myusername, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.e("Response", response.toString());
                    JSONArray my_wemets = (JSONArray) response.get("matches");
                    for (int i = 0; i < my_wemets.length(); i++) {
//                        JSONObject jprof = my_wemets.getJSONObject(i);
//                        Log.e("Response", jprof.toString());
//                        Profile p = new Profile(jprof.get("firstname").toString(),
//                                jprof.get("lastname").toString(), jprof.get("image").toString());
//                        profiles.add(p);
//                        Log.e("Response", "added to profiles");
                        WeMetAPIClient.get_profile(my_wemets.getString(i), new JsonHttpResponseHandler() {
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    Profile p = new Profile(response.get("firstname").toString(),
                                                response.get("lastname").toString(), response.get("image").toString());
                                    profiles.add(p);
                                    Log.e("Response", "added " + response.get("username") + " to profiles");
                                    Log.e("Response", "notify data change");
                                    p_adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Log.e(">>> API", e.toString());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(">>> API", e.toString());
                }
            }
        });
    }
}
