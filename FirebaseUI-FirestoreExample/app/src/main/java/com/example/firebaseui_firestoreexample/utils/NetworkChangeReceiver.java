package com.example.firebaseui_firestoreexample.utils;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
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
                Toast.makeText(context, "gone offline", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "back online", Toast.LENGTH_SHORT).show();
                if (activity instanceof EditNoteActivity) {
                    MyApp.updateFromServer = true;
                }
            }

                if (activity instanceof EditNoteActivity) {
                    if(MyApp.isActivityEditNoteVisible())activity.recreate();
                }
                if(activity instanceof MainActivity)
                    if(MyApp.isActivityVisible()) activity.recreate();

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