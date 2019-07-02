package computernotes.computernotes.utils;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.widget.Toast;

import computernotes.computernotes.activities.MyActivityLifecycleCallbacks;

public class NetworkChangeReceiver extends BroadcastReceiver {



    public NetworkChangeReceiver(){}

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Toast.makeText(context,"gone offline",Toast.LENGTH_LONG).show();
                getActivity(context).recreate();
            } else {
                Toast.makeText(context,"back online",Toast.LENGTH_LONG).show();
                getActivity(context).recreate();
            }
        }
    }


    public Activity getActivity(Context context)
    {
        if (context == null)
        {
            return null;
        }
        else if (context instanceof ContextWrapper)
        {
            if (context instanceof Activity)
            {
                return (Activity) context;
            }
            else
            {
                return new MyActivityLifecycleCallbacks().getCurrentActivity();
            }
        }

        return null;
    }
}