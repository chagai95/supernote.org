package com.example.firebaseui_firestoreexample.utils;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.firebaseui_firestoreexample.EditNoteActivity;
import com.example.firebaseui_firestoreexample.MainActivity;
import com.example.firebaseui_firestoreexample.MyActivityLifecycleCallbacks;

public class NetworkChangeReceiver extends BroadcastReceiver {


    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Activity activity = getActivity(context);
        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                MyApp.lastTrafficLightState = TrafficLight.OFFLINE;
                Toast.makeText(context, "gone offline", Toast.LENGTH_SHORT).show();
            } else {
                if (status == NetworkUtil.NETWORK_STATUS_MOBILE) {
                    if(NetworkUtil.networkType== TelephonyManager.NETWORK_TYPE_EDGE){
                        MyApp.lastTrafficLightState = TrafficLight.MAYBE_CONNECTED;
                        if(MyApp.autoInternInternetOffWhenE) MyApp.appInternInternetOffToggle = true;
                        Toast.makeText(context, "bad internet", Toast.LENGTH_SHORT).show();
                    } else {
                        MyApp.lastTrafficLightState = TrafficLight.ONLINE;
                        Toast.makeText(context, "back online", Toast.LENGTH_SHORT).show();
                        if (activity instanceof EditNoteActivity) {
                            MyApp.updateFromServer = true;
                        }
                        if (MyApp.isBackUpFailed()) {
                            MyApp.loadToCache();
                        }
                    }

                }

            }

            if (activity instanceof EditNoteActivity) {
                if (MyApp.isActivityEditNoteVisible()) activity.recreate();
            }
            if (activity instanceof MainActivity) {
//                activity.getIntent().putExtra("networkChangeReciever", true);
                if (MyApp.isActivityVisible()) activity.recreate();
            }
            if(MyApp.appInternInternetOffToggle) MyApp.lastTrafficLightState = TrafficLight.INTERN_OFFLINE;

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