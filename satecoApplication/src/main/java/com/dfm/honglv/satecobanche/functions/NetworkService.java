package com.dfm.honglv.satecobanche.functions;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NetworkService extends IntentService {
    public NetworkService() {
        super("NetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        boolean isNetworkConnected = extras.getBoolean("isNetworkConnected");
        // your code

        if(isNetworkConnected){
            Log.e("TAG", "Network available.");
        } else {
            Log.e("TAG", "Network unavailable.");
        }

    }
}
