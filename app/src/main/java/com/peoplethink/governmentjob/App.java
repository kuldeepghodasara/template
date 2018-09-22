package com.peoplethink.governmentjob;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONObject;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //OneSignal Push
        if (!TextUtils.isEmpty(getString(R.string.onesignal_app_id)))
            OneSignal.init(this, getString(R.string.onesignal_google_project_number), getString(R.string.onesignal_app_id), new NotificationHandler());

    }

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    private class NotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                JSONObject data = result.notification.payload.additionalData;

                String webViewUrl = (data != null) ? data.optString("url", null) : null;
                String browserUrl = result.notification.payload.launchURL;

                if (webViewUrl != null || browserUrl != null) {
                    if (webViewUrl != null){
                        HolderActivity.startWebViewActivity(App.this, webViewUrl, false, false, null, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        HolderActivity.startWebViewActivity(App.this, browserUrl, true, false, null, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                } else if (!result.notification.isAppInFocus) {
                    Intent mainIntent;
                    mainIntent = new Intent(App.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }
}