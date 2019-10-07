package com.example.firebaseui_firestoreexample.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.activities.WhatsappActivity;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;

import java.util.Date;
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("swiped")) {
                if (intent.getStringExtra("whatsappNumber").equals("4915905872952"))
                    swiped(context, null, "whatsapp time", intent);
                if (intent.getStringExtra("reminderID") != null) {
                    TimeReminderData timeReminderData = MyApp.timeReminders.get(intent.getStringExtra("reminderID"));
                    assert timeReminderData != null;
                    swiped(context, intent.getStringExtra("noteID"), timeReminderData.getTimeReminder().getType(), intent);
                }
            } else {
                Intent newIntent = new Intent(context, WhatsappActivity.class);
                newIntent.setAction(intent.getAction());
                newIntent.putExtra("whatsappMessage", intent.getStringExtra("whatsappMessage"));
                newIntent.putExtra("whatsappNumber", intent.getStringExtra("whatsappNumber"));
                context.startActivity(newIntent);
            }
        }
    }

    private void swiped(Context context, String noteID, String reminderType, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, MyBroadcastReceiver.class);
        if (noteID == null || reminderType.equals("whatsapp time")) {
            myIntent.setAction("WhatsappTimeReminder");
            myIntent.putExtra("whatsappMessage", intent.getStringExtra("whatsappMessage"));
            myIntent.putExtra("whatsappNumber", intent.getStringExtra("whatsappNumber"));
        } else {

            myIntent.setAction("TimeReminder");
            myIntent.putExtra("noteID", noteID);
        }


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);

        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, new Date().getTime() + 1000 * 20, pendingIntent);

    }

}
