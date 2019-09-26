package com.example.firebaseui_firestoreexample;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;

import java.util.Objects;

abstract public class MyActivity extends AppCompatActivity {

    private TrafficLight lastTrafficLightState;
    int countGetThemeCalled =0;

    Resources.Theme getTrafficLightTheme(){
        Resources.Theme theme = super.getTheme();
//        skip this for now because it does not work. - tried to check if there can be a connection with google established.
//        new Online().run();
        countGetThemeCalled++;
        if (MyApp.internetDisabledInternally) {
            theme.applyStyle(R.style.InternOffline, true);
            MyApp.lastTrafficLightState = TrafficLight.INTERN_OFFLINE;
            lastTrafficLightState = TrafficLight.INTERN_OFFLINE;
        } else
        if (isNetworkAvailable()) {
            if (NetworkUtil.networkType == TelephonyManager.NETWORK_TYPE_EDGE) {
                theme.applyStyle(R.style.MaybeConnected, true);
                MyApp.lastTrafficLightState = TrafficLight.MAYBE_CONNECTED;
                lastTrafficLightState = TrafficLight.MAYBE_CONNECTED;
            } else {
                theme.applyStyle(R.style.Online, true);
                MyApp.lastTrafficLightState = TrafficLight.ONLINE;
                lastTrafficLightState = TrafficLight.ONLINE;
            }
        } else {
            theme.applyStyle(R.style.Offline, true);
            MyApp.lastTrafficLightState = TrafficLight.OFFLINE;
            lastTrafficLightState = TrafficLight.OFFLINE;
        }

        return theme;
    }

    boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(manager).getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    public void setLastTrafficLightState(TrafficLight lastTrafficLightState) {
        this.lastTrafficLightState = lastTrafficLightState;
    }

    public TrafficLight getLastTrafficLightState() {
        return lastTrafficLightState;
    }
}
