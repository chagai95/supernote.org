package com.example.firebaseui_firestoreexample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.TaskStackBuilder;

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
        if(Objects.requireNonNull(intent.getAction()).equals("TimeReminder")){
            OfflineNoteData offlineNoteData = MyApp.allNotesOfflineNoteData.get(intent.getStringExtra("noteID"));
            if (offlineNoteData != null)
                documentReference = offlineNoteData.getDocumentReference();
            Log.i("MyBroadcastReceiver", "notification triggered");
            createNotification(context);
        }
        if(Objects.requireNonNull(intent.getAction()).equals("swiped")){
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
}
