package com.example.firebaseui_firestoreexample.utils;

import android.content.Context;
import android.net.TrafficStats;

import com.example.firebaseui_firestoreexample.MyApp;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class InternetThread extends Thread {
    private Context context;
    public InternetThread(Context context) {
        this.context = context;
    }
    @Override
    public void run() {
        long bytesBefore = TrafficStats.getMobileRxBytes();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long bytesAfter = TrafficStats.getMobileRxBytes();

        long speed = (bytesAfter - bytesBefore);

        try {
            try {
                InputStream is = new URL("http://www.domain.com/ubuntu-linux.iso").openStream();
                byte[] buf = new byte[1024];
                int n = 0;
                long startBytes = TrafficStats.getTotalRxBytes(); /*gets total bytes received so far*/
                long startTime = System.nanoTime();
                while (n < 200) {
                    //noinspection ResultOfMethodCallIgnored
                    is.read(buf);
                    n++;
                }
                long endTime = System.nanoTime();
                long endBytes = TrafficStats.getTotalRxBytes(); /*gets total bytes received so far*/
                MyApp.totalTime = endTime - startTime;
                long totalBytes = endBytes - startBytes;


            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
