package com.example.firebaseui_firestoreexample.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import com.example.firebaseui_firestoreexample.receivers.MyBroadcastReceiver;
import com.example.firebaseui_firestoreexample.utils.MyDatePickerFragment;
import com.example.firebaseui_firestoreexample.R;

import java.util.Date;
import java.util.Objects;

import static com.example.firebaseui_firestoreexample.receivers.MyBroadcastReceiver.REPORT_BUG_WHATSAPP_REMINDER;
import static com.example.firebaseui_firestoreexample.receivers.MyBroadcastReceiver.WHATSAPP_TIME_REMINDER;

public class WhatsappActivity extends MyActivity {

    String whatsappMessage;
    String whatsappNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whatsapp);
        whatsappMessage = getIntent().getStringExtra("whatsappMessage");
        whatsappNumber = getIntent().getStringExtra("whatsappNumber");
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(REPORT_BUG_WHATSAPP_REMINDER);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(WHATSAPP_TIME_REMINDER);
        if (getIntent().getAction() != null)
            switch (getIntent().getAction()) {
                case "snoozeWhatsapp":
                    snoozeWhatsapp();
                    break;
                case "swiped":
                    swiped();
                    break;
                default:
                    sendWhatsapp();
            }
    }

    private void swiped() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(this, MyBroadcastReceiver.class);
            myIntent.setAction("WhatsappTimeReminder");
        myIntent.putExtra("whatsappMessage", whatsappMessage);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, new Date().getTime()+1000*20, pendingIntent);

        finish();
    }

    private void snoozeWhatsapp() {
        new MyDatePickerFragment(
                null, this, "", whatsappMessage, null, null)
                .show(getSupportFragmentManager(), "date picker");
    }

    private void sendWhatsapp() {
        if (isNetworkAvailable()) {
            String textWhatsapp = whatsappMessage.replace(" ", "%20");

            String link = "https://api.whatsapp.com/send?phone=" + whatsappNumber + "&text=" + textWhatsapp + "&source=&data=%20";

            Uri uriUrl = Uri.parse(link);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        } else snoozeWhatsapp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
