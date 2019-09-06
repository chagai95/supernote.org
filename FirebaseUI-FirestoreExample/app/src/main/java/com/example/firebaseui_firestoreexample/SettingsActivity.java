package com.example.firebaseui_firestoreexample;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;

public class SettingsActivity extends AppCompatActivity {

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
            if(status== TelephonyManager.NETWORK_TYPE_EDGE)
                if(MyApp.autoInternInternetOffWhenE) MyApp.appInternInternetOffToggle = true;
        });

    }
}
