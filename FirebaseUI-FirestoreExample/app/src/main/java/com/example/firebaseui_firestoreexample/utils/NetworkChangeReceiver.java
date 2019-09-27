package com.example.firebaseui_firestoreexample.utils;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.firebaseui_firestoreexample.activities.EditNoteActivity;
import com.example.firebaseui_firestoreexample.activities.LoginActivity;
import com.example.firebaseui_firestoreexample.activities.MainActivity;
import com.example.firebaseui_firestoreexample.MyActivityLifecycleCallbacks;
import com.example.firebaseui_firestoreexample.activities.SettingsActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {


    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Activity activity = getActivity(context);
        int status = NetworkUtil.getConnectivityStatusString(context);
        if (!MyApp.internetDisabledInternally)
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    MyApp.lastTrafficLightState = TrafficLight.OFFLINE;
                } else {
                    if (status == NetworkUtil.NETWORK_STATUS_MOBILE) {
                        if (NetworkUtil.networkType == TelephonyManager.NETWORK_TYPE_EDGE) {
                            MyApp.lastTrafficLightState = TrafficLight.MAYBE_CONNECTED;
                            if (MyApp.autoInternInternetOffWhenE)
                                MyApp.internetDisabledInternally = true;
                        } else {
                            MyApp.lastTrafficLightState = TrafficLight.ONLINE;
                            if (activity instanceof EditNoteActivity) MyApp.updateFromServer = true;
//                      if(MyApp.updateLoadToCacheOnMobileData)
//                      if (MyApp.isBackUpFailed()) MyApp.loadToCache();
                        }

                    } else {
                        MyApp.lastTrafficLightState = TrafficLight.ONLINE;
                        Toast.makeText(context, "back online wifi", Toast.LENGTH_SHORT).show();
                        if (activity instanceof EditNoteActivity) {
                            MyApp.updateFromServer = true;
                        }
                        if (MyApp.isBackUpFailed()) {
                            MyApp.loadToCache();
                        }
                    }
                }

                if (activity instanceof EditNoteActivity) {
                    if (MyApp.isActivityEditNoteVisible()) activity.recreate();
                }
                if (activity instanceof MainActivity) {
                    if (MyApp.isActivityMainVisible()) activity.recreate();
                }
                if (activity instanceof SettingsActivity) {
                    if (MyApp.isActivitySettingsVisible()) activity.recreate();
                }
                if (activity instanceof LoginActivity) {
                    if (MyApp.isActivityLoginVisible()) activity.recreate();
                }
            }
    }


    public Activity getActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else {
                //noinspection AccessStaticViaInstance
                return new MyActivityLifecycleCallbacks().getCurrentActivity();
            }
        }

        return null;
    }
}