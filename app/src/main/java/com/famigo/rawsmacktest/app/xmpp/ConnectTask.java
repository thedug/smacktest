package com.famigo.rawsmacktest.app.xmpp;

import android.util.Log;

import com.famigo.rawsmacktest.app.R;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.concurrent.Callable;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class ConnectTask implements Runnable {

    private static final String TAG = ConnectTask.class.getSimpleName();

    private static final String HOST = "test-xmpp-1.fam.io";
    private static final int PORT = 5222;
    public static final String VHOST = "test-xmpp-1";

    private final String password;
    private final String username;
    private final XMPPConnection activeConnection;
    private ConnectionListener connectionListener;

    public ConnectTask(String username, String password, ConnectionListener connectionListener, XMPPConnection activeConnection) {
        this.password = password;
        this.username = username;
        this.connectionListener = connectionListener;
        this.activeConnection = activeConnection;
    }


    @Override
    public void run() {

        if ( activeConnection != null && activeConnection.isConnected() ){
            try {
                activeConnection.disconnect();
            } catch (SmackException.NotConnectedException e) {
                Log.i(TAG, e.getMessage(), e);
            }
        }

        ConnectionConfiguration configuration = new ConnectionConfiguration(HOST, PORT, VHOST);
        configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configuration.setDebuggerEnabled(true);
        configuration.setReconnectionAllowed(true);

        XMPPConnection connection = new XMPPTCPConnection(configuration);
        connection.addConnectionListener(connectionListener);

        try {
            connection.connect();
            connection.login(username, password);
            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);

        } catch ( Exception e ){
            Log.e(TAG, e.getMessage(), e);
        }
    }

}