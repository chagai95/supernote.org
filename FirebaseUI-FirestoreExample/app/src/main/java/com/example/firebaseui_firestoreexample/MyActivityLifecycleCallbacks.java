package com.example.firebaseui_firestoreexample;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static Activity currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }
}
