package com.example.firebaseui_firestoreexample;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.FutureTask;

public class SettingsActivity extends AppCompatActivity {
    boolean startAppOffline;
    private DocumentReference documentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ToggleButton toggleButton = findViewById(R.id.simpleToggleButton);
        toggleButton.setChecked(MyApp.autoInternInternetOffWhenE);
        toggleButton.setOnClickListener(view -> {
            MyApp.autoInternInternetOffWhenE = !MyApp.autoInternInternetOffWhenE;
            MyApp.lastTrafficLightState = TrafficLight.UNKNOWN;
            int status = NetworkUtil.getConnectivityStatusString(this);
            if (NetworkUtil.networkType == TelephonyManager.NETWORK_TYPE_EDGE && status == NetworkUtil.NETWORK_STATUS_MOBILE)
                if (MyApp.autoInternInternetOffWhenE) MyApp.appInternInternetOffToggle = true;
        });

        ToggleButton toggleButton2 = findViewById(R.id.simpleToggleButton2);
        documentRef = FirebaseFirestore.getInstance().collection("utils").document("startAppOffline");
        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startAppOffline = (boolean) Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getData()).get("startAppOffline");
                toggleButton2.setChecked(startAppOffline);
                toggleButton2.setOnClickListener(view -> documentRef.update("startAppOffline", !startAppOffline));

            }
        });
    }
}