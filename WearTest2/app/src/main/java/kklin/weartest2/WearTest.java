package kklin.weartest2;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class WearTest extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    /* put this into your activity class */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private TextView accelTextView;
    private boolean startedHandshake = false;
    private long handshakeStartTime = -1; //millis

    public int MAX_HANDSHAKE_LENGTH = 1000; //millis
    public float HANDSHAKE_THRESHOLD = 7;

    private GoogleApiClient mGoogleApiClient;
    Location mostRecentLoc;

    AtomicInteger msgId = new AtomicInteger();

    private static final String SERVER_URL="http://wemet-demo.me";

    String SENDER_ID = "727552317391";

    GoogleCloudMessaging gcm;
    Context context;

    String regid;

    // TODO: remove after testing
    String username = "kklin";

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            // accelTextView.setText("Acceleration: " + mAccel);
            if (isHandshake(mAccel)) {
                Toast toast = Toast.makeText(getApplicationContext(), "Nice to meet you too.", Toast.LENGTH_SHORT);
                toast.show();
                // TODO: location doesn't work in Sutardja Dai, so we're just going to hard code it for now
                if (mostRecentLoc != null) {
                    //toast = Toast.makeText(getApplicationContext(), "We met at lat: " +
                    //        mostRecentLoc.getLatitude() + " long: " + mostRecentLoc.getLongitude(), Toast.LENGTH_SHORT);
                    //toast.show();
                }

                double latitude = 37.874864;
                double longitude = -122.258213;
                potentialHandshake(latitude, longitude);
            }
        }

        private void potentialHandshake(double latitude, double longitude) {
            notifyOfPotentialHandshake(latitude, longitude);
        }

        private void notifyOfPotentialHandshake(double latitude, double longitude) {
            Bundle data = new Bundle();
            data.putString("type", "meeting");
            // data.putString("username", username);
            // for testing
            data.putString("username", Integer.toString(msgId.addAndGet(1)));
            data.putString("latitude", Double.toString(latitude));
            data.putString("longitude", Double.toString(longitude));
            data.putString("time", Long.toString(System.currentTimeMillis()));
            messageServer(data);
        }

        private boolean isHandshake(float accel) {
            // check if we never finish the handshake (if we're doing something like raising our arm)
            return accel > HANDSHAKE_THRESHOLD;
            // if (startedHandshake && System.currentTimeMillis() - handshakeStartTime > MAX_HANDSHAKE_LENGTH) {
            //     startedHandshake = false;
            // }
            // if (accel < 0 && accel > HANDSHAKE_THRESHOLD) {
            //     startedHandshake = true;
            //     handshakeStartTime = System.currentTimeMillis();
            // } else if (startedHandshake && accel > 0 && accel > HANDSHAKE_THRESHOLD) {
            //     startedHandshake = false;
            //     return true;
            // }
            // return false;
        }

        private void messageServer(final Bundle data) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    // mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_test);
        // accelTextView = (TextView) findViewById(R.id.accelTextView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .build();

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);



        gcm = GoogleCloudMessaging.getInstance(this);
        // regid = getRegistrationId(context);
        regid = "";

        if (regid.isEmpty()) {
            registerInBackground();
        }

        // createAndQueryProfileExample();
    }

    private void createAndQueryProfileExample() {
        // create
        Bundle createData = new Bundle();
        createData.putString("type", "new_profile");
        createData.putString("real_email", "kaikai526@gmail.com");
        createData.putString("photo", "photo.jpeg");
        createData.putString("firstname", "Kevin");
        createData.putString("lastname", "Lin");
        // messageServer(createData);

        // query
        Bundle queryData = new Bundle();
        createData.putString("type", "get_profile");
        createData.putString("username", "kklin");
        // messageServer(queryData);
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    // storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        (new SendRegistrationIdTask(regid)).execute();
    }

    private final class SendRegistrationIdTask extends AsyncTask<String, Void, HttpResponse> {
        private String mRegId;

        public SendRegistrationIdTask(String regId) {
            mRegId = regId;
        }

        @Override
        protected HttpResponse doInBackground(String... regIds) {
            String url = SERVER_URL;
            HttpPost httppost = new HttpPost(url);

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("regid", mRegId));
                nameValuePairs.add(new BasicNameValuePair("username", username));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpClient httpclient = new DefaultHttpClient();
                return httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
                // Log.e(Constants.TAG, e.getMessage(), e);
            } catch (IOException e) {
                // Log.e(Constants.TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            if (response == null) {
                // Log.e(Constants.TAG, "HttpResponse is null");
                return;
            }

            //StatusLine httpStatus = response.getStatusLine();
            //if (httpStatus.getStatusCode() != 200) {
            //    Log.e(Constants.TAG, "Status: " + httpStatus.getStatusCode());
            //    mStatus.setText(httpStatus.getReasonPhrase());
            //    return;
            //}

            // String status = getString(R.string.server_registration, mRegId);
            // mStatus.setText(status);
        }
    }

//    protected void startLocationUpdates() {
//        LocationRequest mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
//    }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(ConnectionResult c) {}

    @Override
    public void onConnected(Bundle b) {
        // startLocationUpdates();
    }

    public void onProviderEnabled(String s) {}
    public void onProviderDisabled(String s) {}

    public void onStatusChanged(String s, int i, Bundle b) {}

    @Override
    public void onLocationChanged(Location location) {
        mostRecentLoc = location;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wear_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
