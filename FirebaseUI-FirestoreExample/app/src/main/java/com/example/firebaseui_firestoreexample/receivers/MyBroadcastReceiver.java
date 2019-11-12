package com.example.firebaseui_firestoreexample.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.TaskStackBuilder;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.utils.NotificationHelper;
import com.example.firebaseui_firestoreexample.activities.EditNoteActivity;
import com.example.firebaseui_firestoreexample.firestore_data.NoteData;
import com.google.firebase.firestore.DocumentReference;


//should I be using two broadcasts one for the alarm manager and one for the notification?
public class MyBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("reminder")) {
            if (intent.getStringExtra("reminderID") != null && intent.getStringExtra("noteID") != null) {
                String reminderID = intent.getStringExtra("reminderID");
                String noteID = intent.getStringExtra("noteID");

                ReminderData reminderData = null;
                if (MyApp.timeReminders.containsKey(reminderID))
                    reminderData = MyApp.timeReminders.get(reminderID);
                if (MyApp.locationReminders.containsKey(reminderID))
                    reminderData = MyApp.locationReminders.get(reminderID);
                if (reminderData != null)
                    createNotification(context, noteID, reminderData.getDocumentReference());
            }
        }
    }

    public static void createNotification(Context mContext, String noteID, DocumentReference reminderDocumentReference) {
        ReminderData reminderData = null;
        if (MyApp.timeReminders.containsKey(reminderDocumentReference.getId()))
            reminderData = MyApp.timeReminders.get(reminderDocumentReference.getId());
        if (MyApp.locationReminders.containsKey(reminderDocumentReference.getId()))
            reminderData = MyApp.locationReminders.get(reminderDocumentReference.getId());

        NoteData noteData = MyApp.allNotes.get(noteID);
        Note note = null;
        if (noteData != null) {
            note = noteData.getNote();
        }
        String title = null;
        String preview = "";
        if (note != null) {
            title = note.getTitle();

            preview = note.getDescription();
            if (reminderData != null)
                if (!reminderData.getReminder().getPreview().equals(""))
                    preview = reminderData.getReminder().getPreview();
        }


        Intent intent = new Intent(mContext, EditNoteActivity.class);
        intent.setAction("reminder");
        intent.putExtra("noteID", noteID);
        intent.putExtra("reminderID", reminderDocumentReference.getId());


        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationHelper notificationHelper = new NotificationHelper(mContext);
        notificationHelper.createNotification(title, preview, pendingIntent, reminderDocumentReference);
        // set the random to be high so it does not interfere with the id's
        int notificationID = 0;
        if (reminderData != null) {
            if(reminderData.getNotificationID()==0){
                notificationID = MyApp.createNotificationID();
                reminderData.setNotificationID(notificationID);
            }
            else
                notificationID = reminderData.getNotificationID();
        }
        notificationHelper.show(notificationID);
    }

}
