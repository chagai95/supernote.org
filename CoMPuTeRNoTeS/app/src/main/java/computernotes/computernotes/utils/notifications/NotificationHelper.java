package computernotes.computernotes.utils.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

public class NotificationHelper {

    Context mContext;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    public NotificationHelper(Context mContext) {
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
    public void initNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        for (NotificationChannel channel : NotificationChannels.getChannels()) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public NotificationHelper createNotification(String channel,
                                                 String title,
                                                 String content,
                                                 PendingIntent notifyIntent,
                                                 int smallIcon,
                                                 Bitmap largeIcon) {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(mContext, MyBroadcastReceiver.class);
        intent.putExtra("swiped",true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, intent, 0);


        mBuilder = new NotificationCompat.Builder(mContext, channel);
        mBuilder
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notifyIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setDeleteIntent(pendingIntent) ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(largeIcon);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(channel);
        }
        return this;
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public NotificationHelper setVibration(long[] pattern) {
        if (pattern == null || pattern.length == 0) {
            pattern = new long[]{500, 500};
        }
        mBuilder.setVibrate(pattern);
        return this;
    }

    public void show(int id) {
        mNotificationManager.notify(id, mBuilder.build());
    }
}
