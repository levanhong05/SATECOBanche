package com.dfm.honglv.satecobanche.adapter;

/**
 * Created by honglv on 28/02/2017.
 */

public interface MessageCallback {
    /**
     * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
     * @param message Received message from server app.
     */
    public void callbackMessageReceiver(String message);
}
