package com.dfm.honglv.satecobanche.adapter;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by honglv on 28/02/2017.
 */

public class TCPClient {
    private static final String TAG = "TCPClient";
    public static final String SERVERIP = "192.168.30.188"; //your computer IP address
    public static final int SERVERPORT = 4444;

    private String incomingMessage, command;
    BufferedReader in;
    PrintWriter out;

    private MessageCallback listener = null;
    private boolean mRun = false;

    /**
     ** TCPClient class constructor, which is created in AsyncTasks after the button click.
     * @param command  Command passed as an argument, e.g. "shutdown -r" for restarting computer
     * @param listener Callback interface object
    */
    public TCPClient(String command, MessageCallback listener) {
        this.listener = listener;
        this.command = command ;
    }

    /**
     * Public method for sending the message via OutputStream object.
     * @param message Message passed as an argument and sent via OutputStream object.
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();

            Log.d(TAG, "Sent Message: " + message);
        }
    }

    /**
     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
     */
    public void stopClient(){
        Log.d(TAG, "Client stopped!");
        mRun = false;
    }

    public void run() {
        mRun = true;

        try {
            // Creating InetAddress object from ipNumber passed via constructor from IpGetter class.
            InetAddress serverAddress = InetAddress.getByName(SERVERIP);

            Log.d(TAG, "Connecting...");

            Socket socket = new Socket(serverAddress, SERVERPORT);

            try {
                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e(TAG, "C: Sent.");

                Log.e(TAG, "C: Done.");

                //Create BufferedReader object for receiving messages from server.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "In/Out created");

                //Sending message with command specified by AsyncTask
                this.sendMessage(command);

                //Listen for the incoming messages while mRun = true
                while (mRun) {
                    incomingMessage = in.readLine();

                    if (incomingMessage != null && listener != null) {
                        listener.callbackMessageReceiver(incomingMessage);
                    }

                    incomingMessage = null;
                }

                Log.d(TAG, "Received Message: " + incomingMessage);
            } catch (Exception e) {
                Log.d(TAG, "Error", e);
            } finally {
                out.flush();
                out.close();

                in.close();
                socket.close();

                Log.d(TAG, "Socket Closed");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
        }
    }
}
