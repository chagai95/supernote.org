package com.example.firebaseui_firestoreexample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.TaskStackBuilder;

import com.example.firebaseui_firestoreexample.activities.EditNoteActivity;
import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.OfflineNoteData;
import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;
import java.util.Random;


//should I be using two broadcasts one for the alarm manager and one for the notification?
public class MyBroadcastReceiver extends BroadcastReceiver {
    DocumentReference documentReference;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), "sendWhatsapp")){
            String message = intent.getStringExtra("bugReportMessage");
            String textWhatsapp = message.replace(" ", "%20");
            String link = "https://api.whatsapp.com/send?phone=4915905872952&text=" + textWhatsapp + "&source=&data=%20";

            Uri uriUrl = Uri.parse(link);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            context.startActivity(launchBrowser);
        }
        if (Objects.requireNonNull(intent.getAction()).equals("reportBugWhatsappReminder")) {
            createNotificationForWhatsapp(context,intent.getStringExtra("bugReportMessage"),"");
        } else {
            if (Objects.requireNonNull(intent.getAction()).equals("LocationReminder")) {
                OfflineNoteData offlineNoteData = MyApp.allNotesOfflineNoteData.get(intent.getStringExtra("noteID"));

            }
            if (Objects.requireNonNull(intent.getAction()).equals("TimeReminder")) {
                OfflineNoteData offlineNoteData = MyApp.allNotesOfflineNoteData.get(intent.getStringExtra("noteID"));
                if (offlineNoteData != null)
                    documentReference = offlineNoteData.getDocumentReference();
                Log.i("MyBroadcastReceiver", "notification triggered");
//            make this go off when a button get's pressed on the notification and make the button show no internet when there isn't none
                if (intent.getBooleanExtra("whatsappReminder", false)) {
                    String number = intent.getStringExtra("whatsappNumber");
                    Log.d("number", number);
                    String numberWhatsapp;
                    if (number.startsWith("00")) numberWhatsapp = number.substring(2);
                    else if (number.startsWith("0")) numberWhatsapp = "49" + number.substring(1);
                    else if (number.startsWith("+")) numberWhatsapp = number.substring(1);
                    else numberWhatsapp = number;
                    Log.d("numberWhatsapp", numberWhatsapp);

                    String message = intent.getStringExtra("whatsappMessage");
                    String textWhatsapp = message.replace(" ", "%20");
                    String link = "https://api.whatsapp.com/send?phone=" + numberWhatsapp + "&text=" + textWhatsapp + "&source=&data=%20";

                    Uri uriUrl = Uri.parse(link);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    context.startActivity(launchBrowser);
                }
            }
                createNotification(context);
        }
        if (Objects.requireNonNull(intent.getAction()).equals("swiped")) {
            Log.i("MyBroadcastReceiver", "swiped");
//            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//            Intent myIntent = new Intent(context, MyBroadcastReceiver.class);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
//            alarmManager.set(AlarmManager.RTC_WAKEUP,  new Date().getTime() + new Settings().swipedSnoozeTimeInSeconds*1000 , pendingIntent); // 10 seconds
        }
    }

    private void createNotification(Context mContext) {
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Note note = Objects.requireNonNull(task.getResult()).toObject(Note.class);
                String title = Objects.requireNonNull(note).getTitle();
                String content = note.getDescription();


                Intent intent = new Intent(mContext, EditNoteActivity.class);
                intent.putExtra("noteID", documentReference.getId());

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                stackBuilder.addNextIntentWithParentStack(intent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationHelper notificationHelper = new NotificationHelper(mContext);
                notificationHelper.createNotification(title, content, pendingIntent);
                notificationHelper.show(new Random(100).nextInt());
            }
        });
    }

    private void createNotificationForWhatsapp(Context mContext,String message,String number) {
        if(number.equals("")){
            Intent intent = new Intent(mContext, MyBroadcastReceiver.class);
            intent.putExtra("bugReportMessage",message);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            stackBuilder.addNextIntentWithParentStack(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationHelper notificationHelper = new NotificationHelper(mContext);
            notificationHelper.createNotificationForWhatsapp("send bug report", message, pendingIntent);
            notificationHelper.show(new Random(100).nextInt());
        }

        else documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Note note = Objects.requireNonNull(task.getResult()).toObject(Note.class);
                String title = Objects.requireNonNull(note).getTitle();
                String content = note.getDescription();


                Intent intent = new Intent(mContext, EditNoteActivity.class);
                intent.putExtra("noteID", documentReference.getId());

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                stackBuilder.addNextIntentWithParentStack(intent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationHelper notificationHelper = new NotificationHelper(mContext);
                notificationHelper.createNotification(title, content, pendingIntent);
                notificationHelper.show(new Random(100).nextInt());
            }
        });
    }
}
