package com.example.firebaseui_firestoreexample.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.TaskStackBuilder;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.example.firebaseui_firestoreexample.utils.NotificationHelper;
import com.example.firebaseui_firestoreexample.activities.EditNoteActivity;
import com.example.firebaseui_firestoreexample.activities.WhatsappActivity;
import com.example.firebaseui_firestoreexample.firestore_data.OfflineNoteData;
import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;
import java.util.Random;


//should I be using two broadcasts one for the alarm manager and one for the notification?
public class MyBroadcastReceiver extends BroadcastReceiver {
    public static final int REPORT_BUG_WHATSAPP_REMINDER = 0;
    public static final int WHATSAPP_TIME_REMINDER = 1;
    DocumentReference noteDocumentReference;
    DocumentReference reminderDocumentReference;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null) {
            OfflineNoteData offlineNoteData=null;
            if(intent.getStringExtra("noteID")!= null)
             offlineNoteData = MyApp.allNotes.get(intent.getStringExtra("noteID"));
            if (offlineNoteData != null)
                noteDocumentReference = offlineNoteData.getDocumentReference();
            TimeReminderData timeReminderData=null;
            if(intent.getStringExtra("reminderID")!=null)
                timeReminderData = MyApp.timeReminders.get(intent.getStringExtra("reminderID"));
            if (timeReminderData != null)
                reminderDocumentReference = timeReminderData.getDocumentReference();

            switch (intent.getAction()) {
                case "TimeReminder":
                    createNotification(context, noteDocumentReference, reminderDocumentReference);
                    break;
                case "WhatsappTimeReminder":
                    createNotificationForWhatsapp(context, intent.getStringExtra("whatsappMessage"), intent.getStringExtra("whatsappNumber"));
                    break;
            }
        }
    }

    public static void createNotification(Context mContext, DocumentReference documentReference, DocumentReference reminderDocumentReference) {
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Note note = Objects.requireNonNull(task.getResult()).toObject(Note.class);
                String title = Objects.requireNonNull(note).getTitle();
                String description = note.getDescription();


                Intent intent = new Intent(mContext, EditNoteActivity.class);
                intent.putExtra("noteID", documentReference.getId());

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                stackBuilder.addNextIntentWithParentStack(intent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationHelper notificationHelper = new NotificationHelper(mContext);
                notificationHelper.createNotification(title, description, pendingIntent, reminderDocumentReference);
                notificationHelper.show(new Random(100).nextInt());
            }
        });
    }

    private void createNotificationForWhatsapp(Context mContext, String message, String number) {
        if (number.equals("4915905872952")) {
            Intent intent = new Intent(mContext, WhatsappActivity.class);
            intent.setAction("WhatsappTimeReminder");
            intent.putExtra("whatsappMessage", message);
            intent.putExtra("whatsappNumber", number);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            stackBuilder.addNextIntentWithParentStack(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationHelper notificationHelper = new NotificationHelper(mContext);
            notificationHelper.createNotificationForWhatsapp("send bug report", message, number, pendingIntent);
            notificationHelper.show(REPORT_BUG_WHATSAPP_REMINDER);
        } else noteDocumentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Note note = Objects.requireNonNull(task.getResult()).toObject(Note.class);
                String title = Objects.requireNonNull(note).getTitle();
                String description = note.getDescription();

                Intent intent = new Intent(mContext, EditNoteActivity.class);
                intent.setAction("WhatsappTimeReminder");
                intent.putExtra("whatsappMessage", message);
                intent.putExtra("whatsappNumber", number);
                intent.putExtra("noteID", noteDocumentReference.getId());

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                stackBuilder.addNextIntentWithParentStack(intent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationHelper notificationHelper = new NotificationHelper(mContext);
                notificationHelper.createNotificationForWhatsapp(title, message, number, pendingIntent);
                notificationHelper.show(WHATSAPP_TIME_REMINDER);
            }
        });
    }
}
