package com.example.firebaseui_firestoreexample.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.activities.OpenFragmentActivity;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {

    private String reminderID;
    private String noteID;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getStringExtra("reminderID") != null && intent.getStringExtra("noteID") != null) {
                reminderID = intent.getStringExtra("reminderID");
                noteID = intent.getStringExtra("noteID");

                ReminderData reminderData = null;
                if (MyApp.timeReminders.containsKey(reminderID))
                    reminderData = MyApp.timeReminders.get(reminderID);
                if (MyApp.locationReminders.containsKey(reminderID))
                    reminderData = MyApp.locationReminders.get(reminderID);
                if (reminderData != null)
                    switch (intent.getAction()) {
                        case "swiped":
                            swiped(context);
                            break;
                        case "done":
                            clearNotification(context, reminderData.getNotificationID());
                            reminderData.getDocumentReference().update("done", true);
                            break;
                        case "next time":
                            clearNotification(context, reminderData.getNotificationID());
                            if(!(reminderData.getReminder().getAmountOfRepeats()==1))
                                reminderData.getReminder().decrementAmountOfRepeats();
                            break;
                        case "snooze":
                            Intent openFragmentActivityIntent = new Intent(context, OpenFragmentActivity.class);
                            openFragmentActivityIntent.setAction(intent.getAction());
                            openFragmentActivityIntent.putExtra("reminderID", reminderID);
                            openFragmentActivityIntent.putExtra("noteID", noteID);
                            context.startActivity(openFragmentActivityIntent);
                            clearNotification(context, reminderData.getNotificationID());
                            break;
                        case "send whatsapp":
                            if (isNetworkAvailable(context)) {
                                String textWhatsapp = reminderData.getReminder().getWhatsappMessage().replace(" ", "%20");
                                String link = "https://api.whatsapp.com/send?phone=" + reminderData.getReminder().getWhatsappNumber() + "&text=" + textWhatsapp + "&source=&data=%20";
                                Uri uriUrl = Uri.parse(link);
                                context.startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
                                clearNotification(context, reminderData.getNotificationID());
                            } else
                                Toast.makeText(context, "no internet connection", Toast.LENGTH_SHORT).show();
                            break;
                    }
            }
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(manager).getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    private void swiped(Context context) {
        Intent myBroadcastReceiverIntent = new Intent(context, MyBroadcastReceiver.class);
        myBroadcastReceiverIntent.setAction("reminder");
        myBroadcastReceiverIntent.putExtra("reminderID", reminderID);
        myBroadcastReceiverIntent.putExtra("noteID", noteID);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myBroadcastReceiverIntent, 0);
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, new Date().getTime() + 1000 * 20, pendingIntent);
    }

    public void clearNotification(Context mContext, int notificationID) {
            NotificationManager notificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationID);
    }

}
