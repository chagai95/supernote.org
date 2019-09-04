package com.example.firebaseui_firestoreexample;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

public class NotificationHelper {

    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    NotificationHelper(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

    }

    /**
     * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in
     * the support library
     */
    @TargetApi(Build.VERSION_CODES.O)
    void initNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        for (NotificationChannel channel : NotificationChannels.getChannels()) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    void createNotification(String title,
                            String content,
                            PendingIntent notifyIntent) {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(mContext, MyBroadcastReceiver.class);
        intent.setAction("swiped");//we use the action to check if the notification has been dismissed in MyBroadcastReceiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, intent, 0);


        mBuilder = new NotificationCompat.Builder(mContext, "CHANNEL_ID");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notifyIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setDeleteIntent(pendingIntent) ; // this gets triggered when we dismiss (swipe away) the notification or click clear all in the notification panel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId("CHANNEL_ID");
        }
    }

    @SuppressWarnings("unused")
    public Builder getBuilder() {
        return mBuilder;
    }

    @SuppressWarnings("unused")
    public NotificationHelper setVibration(long[] pattern) {
        if (pattern == null || pattern.length == 0) {
            pattern = new long[]{500, 500};
        }
        mBuilder.setVibrate(pattern);
        return this;
    }

    void show(int id) {
        mNotificationManager.notify(id, mBuilder.build());
    }
}
