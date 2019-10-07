package com.example.firebaseui_firestoreexample.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.firebaseui_firestoreexample.services.SensorService;

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