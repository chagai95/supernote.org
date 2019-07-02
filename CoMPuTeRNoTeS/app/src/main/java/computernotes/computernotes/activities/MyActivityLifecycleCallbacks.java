package computernotes.computernotes.activities;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static Activity currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i(activity.getClass().getSimpleName(), "onCreate(Bundle)");
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onStart()");
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onResume()");
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onPause()");
        currentActivity = activity;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i(activity.getClass().getSimpleName(), "onSaveInstanceState(Bundle)");
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onStop()");
        currentActivity = activity;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onDestroy()");
        currentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }
}
