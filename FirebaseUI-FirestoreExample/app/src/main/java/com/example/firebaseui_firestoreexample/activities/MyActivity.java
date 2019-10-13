package com.example.firebaseui_firestoreexample.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

abstract public class MyActivity extends AppCompatActivity {

    FirebaseFirestore db;

    private TrafficLight lastTrafficLightState;

    private Context c = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        onCreateCalled = true;

    }

    Resources.Theme getTrafficLightTheme() {
        Resources.Theme theme = super.getTheme();
//        skip this for now because it does not work. - tried to check if there can be a connection with google established.
//        new Online().run();
        if (MyApp.internetDisabledInternally) {
            theme.applyStyle(R.style.InternOffline, true);
            MyApp.currentTrafficLightState = TrafficLight.INTERN_OFFLINE;
            lastTrafficLightState = TrafficLight.INTERN_OFFLINE;
        } else if (isNetworkAvailable()) {
            if (NetworkUtil.getConnectivityStatusString(this) == NetworkUtil.NETWORK_STATUS_MOBILE && !NetworkUtil.fastConnection(NetworkUtil.networkType)) {
                theme.applyStyle(R.style.MaybeConnected, true);
                MyApp.currentTrafficLightState = TrafficLight.MAYBE_CONNECTED;
                lastTrafficLightState = TrafficLight.MAYBE_CONNECTED;
            } else {
                theme.applyStyle(R.style.Online, true);
                MyApp.currentTrafficLightState = TrafficLight.ONLINE;
                lastTrafficLightState = TrafficLight.ONLINE;
            }
        } else {
            theme.applyStyle(R.style.Offline, true);
            MyApp.currentTrafficLightState = TrafficLight.OFFLINE;
            lastTrafficLightState = TrafficLight.OFFLINE;
        }

        return theme;
    }

    private void askAboutDisablingInternalInternet() {

        // add a cancel button(do nothing) and a button for disabling internal internet but not setting it to auto turn off.
        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        alert.setTitle("disabling internal internet");
        alert.setMessage("internet might be slow \n \n would you like to disable internet internally?" +
                "\n \n if not enter how long you want to ignore this warning for:");

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final EditText ignoreSlowInternetWarning = new EditText(c);
        ignoreSlowInternetWarning.setText("6");

        layout.addView(ignoreSlowInternetWarning);

        Spinner dropdownTimeType = new Spinner(c);
//      create a list of items for the spinner.
        String[] timeType = new String[]{"minutes", "hours", "days", "weeks"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeType);
        dropdownTimeType.setAdapter(adapter);
        dropdownTimeType.setSelection(1, true);

        layout.addView(dropdownTimeType);


        alert.setView(layout);

        alert.setPositiveButton("yes", (dialog, whichButton) -> {
            disableInternalInternet();
            recreate();
        });

        alert.setNegativeButton("no", (dialog, whichButton) -> {
            Toast.makeText(c, "ignoring warning for " + ignoreSlowInternetWarning.getText().toString()
                    + " " + dropdownTimeType.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
            MyApp.showDialogWhenInternetSlow = false;
            long timeTypeLong = 1;
            switch (dropdownTimeType.getSelectedItem().toString()){
                case "minutes":
                    timeTypeLong = 1000*60;
                    break;
                case "hours":
                    timeTypeLong = 1000*60*60;
                    break;
                case "days":
                    timeTypeLong = 1000*60*60*24;
                    break;
                case "weeks":
                    timeTypeLong = 1000*60*60*24*7;
                    break;
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    MyApp.showDialogWhenInternetSlow = true;
                }
            },timeTypeLong*Integer.parseInt(ignoreSlowInternetWarning.getText().toString()));
        });
        AlertDialog alertDialog = alert.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
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

    void enableInternalInternet() {
        MyApp.internetDisabledInternally = false;
        db.enableNetwork();
    }

    void disableInternalInternet() {
        MyApp.internetDisabledInternally = true;
        db.disableNetwork();
    }

    void hideKeyboard() {
        Activity activity = this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    boolean onCreateCalled;

    @Override
    protected void onPause() {
        super.onPause();
//        check how the onPause works, and maybe use this for all activities.
//        MyApp.activityPaused();
        onCreateCalled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MyApp.currentTrafficLightState==TrafficLight.MAYBE_CONNECTED && MyApp.showDialogWhenInternetSlow)
            askAboutDisablingInternalInternet();
        // add in MyApp and in NetworkChangeReciever for the traffic light
        MyApp.activityResumed();
        if (!onCreateCalled && MyApp.currentTrafficLightState != lastTrafficLightState)
            recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.activityStopped();
    }


    /*@Override
    public Resources.Theme getTheme() {
        return getTrafficLightTheme();
    }*/

    void swipeToRefresh(SwipeRefreshLayout pullToRefresh) {
        if (MyApp.userSkippedLogin) {
            pullToRefresh.setRefreshing(false);
            pullToRefresh.setEnabled(false);
        } else
            pullToRefresh.setOnRefreshListener(() -> {
                if (MyApp.internetDisabledInternally)
                    askAboutActivatingInternalInternet();
                else
                    recreate();
                pullToRefresh.setRefreshing(false);
            });
    }

    void askAboutActivatingInternalInternet() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        alert.setTitle("activating internal internet");
        alert.setMessage("internet is disabled internally \n \n would you like to enable it?");

        final TextView input = new TextView(c);
        alert.setView(input);

        alert.setPositiveButton("yes", (dialog, whichButton) -> {
            enableInternalInternet();
            recreate();
        });

        alert.setNegativeButton("no", (dialog, whichButton) -> {
            // cancel
        });
        AlertDialog alertDialog = alert.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }
}
