package com.example.firebaseui_firestoreexample.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.firebaseui_firestoreexample.activities.MainActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        startAppAndCloseMainActivity();
        return ret;
    }

    private void startAppAndCloseMainActivity() {
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.putExtra("startAppAndCloseMainActivity",true);
        FirebaseFirestore.getInstance().collection("utils").document("MyService").update(
                "MyService", FieldValue.arrayUnion(this.toString()));
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
