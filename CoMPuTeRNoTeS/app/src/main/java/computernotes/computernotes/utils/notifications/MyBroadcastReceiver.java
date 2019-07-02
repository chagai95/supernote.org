package computernotes.computernotes.utils.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import androidx.core.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;
import java.util.Random;

import computernotes.computernotes.R;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.Settings;
import computernotes.computernotes.activities.NoteActivity;
import computernotes.computernotes.note.Note;
import computernotes.computernotes.note.NoteMain;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getBooleanExtra("swiped",false)){
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent myIntent = new Intent(context, MyBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP,  new Date().getTime() + new Settings().swipedSnoozeTimeInSeconds*1000 , pendingIntent); // 10 seconds
        }

        int note_index = intent.getExtras().getInt("note_index");

        //temporary make new servervcommunicator, otherwise app will crash on
        //notification click when app was closed before

        //Edited by chagai on the 3/5/2019 at 5:57 PM
        // ServerCommunicator is a singleton now

        ServerCommunicator.getInstance();
        Note note=new NoteMain();
        if(ServerCommunicator.notes.size()!=0)
        note = ServerCommunicator.notes.get(note_index);
        createNotification(context, note);
    }

    private void createNotification(Context mContext, Note note) {

        String title = note.getTitle();
        String content = "insert what should be displayed as content";


        Intent intent = new Intent(mContext, NoteActivity.class);
        intent.putExtra("note_index",ServerCommunicator.notes.indexOf(note));
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationHelper notificationHelper = new NotificationHelper(mContext);
        notificationHelper.createNotification("CHANNEL_ID", title, content, pendingIntent, R.mipmap.ic_launcher, null);
        notificationHelper.show(new Random(100).nextInt());
    }
}
