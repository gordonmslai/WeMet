/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kklin.weartest2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCM Intent";

    private static final int MY_NOTIFICATION_ID = 1;
    NotificationManager notificationManager;
    Notification myNotification;

    // public static final String ACTION_MyIntentService = "com.example.androidintentservice.RESPONSE";
    // public static final String ACTION_MyUpdate = "com.example.androidintentservice.UPDATE";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                process(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        kklin.weartest2.GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void process(Bundle msg) {
        boolean valid = Boolean.parseBoolean(msg.getString("valid"));
        String other_username = msg.getString("other");
        if (valid) {
            notify("Meeting?", "Yup, you met " + other_username);
        } else {
            notify("Meeting?", "nah");
        }
        // TODO: other response processing code
    }

    private void notify(String title, String body) {
        //send update
        // Intent intentUpdate = new Intent();
        // intentUpdate.setAction(ACTION_MyUpdate);
        // intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        // // intentUpdate.putExtra(EXTRA_KEY_UPDATE, i);
        // sendBroadcast(intentUpdate);

        //generate notification
        // String notificationText = String.valueOf((int)(100 * i / 10)) + " %";
        myNotification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);

        // Intent intentResponse = new Intent();
        // intentResponse.setAction(ACTION_MyIntentService);
        // intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
        // // intentResponse.putExtra(EXTRA_KEY_OUT, extraOut);
        // sendBroadcast(intentResponse);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
