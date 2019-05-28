package computernotes.computernotes.utils.notifications;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.O)
public class NotificationChannels {
    public static ArrayList<NotificationChannel> channels = new ArrayList<NotificationChannel>() {{
        add(new NotificationChannel("CHANNEL_ID", "Test Channel", NotificationManager.IMPORTANCE_HIGH));
    }};


    public static ArrayList<NotificationChannel> getChannels() {
        return channels;
    }
}
