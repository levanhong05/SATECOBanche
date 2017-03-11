package com.dfm.honglv.satecobanche.functions;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by honglv on 11/03/2017.
 */

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private final static String TAG = "NetworkChecker";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Explicitly specify that which service class will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), NetworkService.class.getName());
        intent.putExtra("isNetworkConnected", isConnected(context));

        context.startService((intent.setComponent(comp)));
    }

    public boolean isConnected(Context context) {
        boolean val = false;

        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Toast.makeText(context, "Found WI-FI Network.", Toast.LENGTH_SHORT).show();
                val = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Toast.makeText(context, "Found Mobile Internet Network.", Toast.LENGTH_SHORT).show();
                val = true;
            }
        } else {
            // not connected to the internet
            Toast.makeText(context, "No Wifi or 3G enabled.", Toast.LENGTH_SHORT).show();
        }

        return val;
    }
}
