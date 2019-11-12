package com.example.firebaseui_firestoreexample.receivers;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.widget.Toast;

import com.example.firebaseui_firestoreexample.CloudUser;
import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.activities.EditNoteActivity;
import com.example.firebaseui_firestoreexample.activities.LoginActivity;
import com.example.firebaseui_firestoreexample.activities.MainActivity;
import com.example.firebaseui_firestoreexample.activities.SettingsActivity;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.utils.MyActivityLifecycleCallbacks;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

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
                    MyApp.currentTrafficLightState = TrafficLight.OFFLINE;
                } else {
                    addOfflineUsernamesToBeAddedToFriendsWhenOnline(context);
                    if (status == NetworkUtil.NETWORK_STATUS_MOBILE) {
                        NetworkUtil.connectionIsFast = NetworkUtil.fastConnection(NetworkUtil.networkType);
                        if (!NetworkUtil.fastConnection(NetworkUtil.networkType)) {
                            MyApp.currentTrafficLightState = TrafficLight.MAYBE_CONNECTED;
                            if (MyApp.autoInternInternetOffWhenSlow)
                                MyApp.internetDisabledInternally = true;
                        } else {
                            MyApp.currentTrafficLightState = TrafficLight.ONLINE;
                            if (activity instanceof EditNoteActivity) MyApp.updateFromServer = true;
//                      if(MyApp.updateLoadToCacheOnMobileData)
//                      if (MyApp.isBackUpFailed()) MyApp.loadToCache();
                        }

                    } else {
                        MyApp.currentTrafficLightState = TrafficLight.ONLINE;
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

    private void addOfflineUsernamesToBeAddedToFriendsWhenOnline(Context context) {
        ArrayList<String> handledUsernames = new ArrayList<>();
        for (String username :
                MyApp.offlineUsernamesToBeAddedToFriendsWhenOnline) {
            // has to be from server so we don't add invalid usernames.
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("username", username).get(Source.SERVER).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot result = task.getResult();
                    assert result != null;
                    List<DocumentSnapshot> documentSnapshots = result.getDocuments();
                    if (!documentSnapshots.isEmpty()) {

                        DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                        assert documentSnapshot != null;
                        CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                        assert cloudUser != null;
                        MyApp.myCloudUserData.getDocumentReference().update(
                                "friends", FieldValue.arrayUnion(cloudUser.getUid()))
                                .addOnSuccessListener(aVoid -> {
                                            handledUsernames.add(username);
                                            MyApp.friends.put(cloudUser.getUid(), new CloudUserData(cloudUser, documentSnapshot.getReference()));
                                            Toast.makeText(context, cloudUser.getUsername() + " added", Toast.LENGTH_LONG).show();
                                        }
                                );
                    } else
                        Toast.makeText(context, username + " does not exist", Toast.LENGTH_LONG).show();
                        // add this username to a list the user can deal with sometime.
                } else {
                    Toast.makeText(context, "username " + username + " will be added when internet is available", Toast.LENGTH_SHORT).show();
                    // make a list of usernames and add them as soon as there is internet then delete them from the list.
                }
            });
        }

        for (String s :
                handledUsernames) {
            MyApp.offlineUsernamesToBeAddedToFriendsWhenOnline.remove(s);
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

//  check wifi strength - find a listener for this as well.   https://stackoverflow.com/questions/13932724/getting-wifi-signal-strength-in-android


}