package com.famigo.rawsmacktest.app.xmpp;

import android.util.Log;

import com.famigo.rawsmacktest.app.BuildConfig;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class ConnectTask implements Runnable {

    private static final String TAG = ConnectTask.class.getSimpleName();

    private static final String HOST = "test-xmpp-1.fam.io";
    private static final int PORT = 5222;
    public static final String VHOST = "test-xmpp-1";

    private final String mPassword;
    private final String mUsername;
    private final XMPPConnection mActiveConnection;
    private ConnectionListener mConnectionListener;

    public ConnectTask(String username, String password, ConnectionListener connectionListener, XMPPConnection activeConnection) {
        this.mPassword = password;
        this.mUsername = username;
        this.mConnectionListener = connectionListener;
        this.mActiveConnection = activeConnection;
    }


    @Override
    public void run() {

        if ( mActiveConnection != null && mActiveConnection.isConnected() ){
            try {
                mActiveConnection.disconnect();
            } catch (SmackException.NotConnectedException e) {
                Log.i(TAG, e.getMessage(), e);
            }
        }

        ConnectionConfiguration configuration = new ConnectionConfiguration(HOST, PORT, VHOST);
        configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configuration.setDebuggerEnabled(BuildConfig.DEBUG);
        configuration.setReconnectionAllowed(true);

        XMPPConnection connection = new XMPPTCPConnection(configuration);
        connection.addConnectionListener(mConnectionListener);

        try {
            connection.connect();
            connection.login(mUsername, mPassword);

        } catch ( Exception e ){
            Log.e(TAG, e.getMessage(), e);
        }
    }

}