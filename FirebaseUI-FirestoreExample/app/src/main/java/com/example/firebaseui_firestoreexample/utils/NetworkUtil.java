package com.example.firebaseui_firestoreexample.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.Objects;

public class NetworkUtil {
     private static final int TYPE_WIFI = 1;
     private static final int TYPE_MOBILE = 2;
     private static final int TYPE_NOT_CONNECTED = 0;
     static final int NETWORK_STATUS_NOT_CONNECTED = 0;
     private static final int NETWORK_STATUS_WIFI = 1;
     public static final int NETWORK_STATUS_MOBILE = 2;
     private static final int NETWORK_TYPE_WIFI = 30;

     public static int networkType;

    private static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                networkType = NETWORK_TYPE_WIFI;
                return TYPE_WIFI;
            }

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                networkType = Objects.requireNonNull(telephonyManager).getNetworkType();

                if ((Objects.requireNonNull(telephonyManager).getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA)) Toast.makeText(context, "3G enabled", Toast.LENGTH_SHORT).show();
                else if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP)) Toast.makeText(context, "4G enabled", Toast.LENGTH_SHORT).show();
                else if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE)) Toast.makeText(context, "2G enabled", Toast.LENGTH_SHORT).show();
                else if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE)) Toast.makeText(context, "LTE enabled", Toast.LENGTH_SHORT).show();
                else Toast.makeText(context, "unknown network: "+ (Objects.requireNonNull(telephonyManager).getNetworkType()), Toast.LENGTH_SHORT).show();

                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_1xRTT)   )Toast.makeText(context, "NETWORK_TYPE_1xRTT = 7", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA )   )Toast.makeText(context, "NETWORK_TYPE_CDMA = 4;", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE )   )Toast.makeText(context, "NETWORK_TYPE_EDGE = 2;", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EHRPD)   )Toast.makeText(context, "NETWORK_TYPE_EHRPD = 1", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0 ) )Toast.makeText(context, "NETWORK_TYPE_EVDO_0 = ", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A)  )Toast.makeText(context, "NETWORK_TYPE_EVDO_A = ", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_B)  )Toast.makeText(context, "NETWORK_TYPE_EVDO_B = ", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS )   )Toast.makeText(context, "NETWORK_TYPE_GPRS = 1;", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_GSM )    )Toast.makeText(context, "NETWORK_TYPE_GSM = 16;", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA)   )Toast.makeText(context, "NETWORK_TYPE_HSDPA = 8", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA )   )Toast.makeText(context, "NETWORK_TYPE_HSPA = 10", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP)   )Toast.makeText(context, "NETWORK_TYPE_HSPAP = 1", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA)   )Toast.makeText(context, "NETWORK_TYPE_HSUPA = 9", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_IDEN )   )Toast.makeText(context, "NETWORK_TYPE_IDEN = 11", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_IWLAN)   )Toast.makeText(context, "NETWORK_TYPE_IWLAN = 1", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE )    )Toast.makeText(context, "NETWORK_TYPE_LTE = 13;", Toast.LENGTH_SHORT).show();
//                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_NR )     )Toast.makeText(context, "NETWORK_TYPE_NR = 20;", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_TD_SCDMA))Toast.makeText(context, "NETWORK_TYPE_TD_SCDMA ", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS )   )Toast.makeText(context, "NETWORK_TYPE_UMTS = 3;", Toast.LENGTH_SHORT).show();
                if ((telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN ))Toast.makeText(context, "NETWORK_TYPE_UNKNOWN =", Toast.LENGTH_SHORT).show();
                return TYPE_MOBILE;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

//    could add the other changes in network type to and call this from the refresh method or from on create or from onResume
    public static int getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        int status = NETWORK_STATUS_NOT_CONNECTED;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = NETWORK_STATUS_WIFI;
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        }
        return status;
    }

}


