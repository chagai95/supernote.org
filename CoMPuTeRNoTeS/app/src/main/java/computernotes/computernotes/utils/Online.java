package computernotes.computernotes.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import computernotes.computernotes.activities.MainActivity;

public class Online implements Runnable {

        @Override
    public void run() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            MainActivity.networkWorking = (urlc.getResponseCode() == 200);
        } catch (Exception e) {
            Log.e("", "Error: ", e);
        }
    }
}
