package com.example.firebaseui_firestoreexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by fabio on 24/01/2016.
 */
public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
//        FirebaseFirestore.getInstance().collection("utils").document("sensorRestarterBroadcastReceiver").update(
//                "sensorRestarterBroadcastReceiver", FieldValue.arrayUnion(this.toString().substring(72)));
        context.startService(new Intent(context, SensorService.class));
    }

}