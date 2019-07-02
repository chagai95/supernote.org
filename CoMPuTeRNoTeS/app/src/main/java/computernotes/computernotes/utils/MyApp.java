package computernotes.computernotes.utils;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import computernotes.computernotes.ServerCommunicator;

public class MyApp extends Application {
    private static MyApp firstInstance;

    public MyApp() {
    }

    public static synchronized MyApp getFirstInstance() {
        if (firstInstance == null) {
            firstInstance = new MyApp();
            synchronized (MyApp.class) {
                if (firstInstance == null)
                    firstInstance = new MyApp();
            }
        }
        return firstInstance;
    }

    public static Context getContext(){
        return firstInstance;
        // or return firstInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        firstInstance = this;
        super.onCreate();
        firstInstance.registerReceiver(new NetworkChangeReceiver(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}