package com.famigo.rawsmacktest.app.xmpp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.famigo.rawsmacktest.app.BusProvider;
import com.famigo.rawsmacktest.app.MainActivity;
import com.famigo.rawsmacktest.app.R;
import com.famigo.rawsmacktest.app.xmpp.event.IncommingMessage;
import com.famigo.rawsmacktest.app.xmpp.event.XMPPStatusEvent;
import com.squareup.otto.Subscribe;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class XMPPService extends Service implements ConnectionListener, PacketListener {

    private static final String USER = "luser";
    private static final String PASS = "passwd";
    private static final String TAG = XMPPService.class.getSimpleName();

    private XMPPConnection activeConnection = null;

    private Handler handler = new Handler();
    private Notification notification;

    private List<XMPPCommand> stalledCommands = new LinkedList<XMPPCommand>();

    public static void start(Context ctx, String username, String password){
        ctx.startService(
                new Intent(ctx, XMPPService.class)
                        .putExtra(USER, username)
                        .putExtra(PASS, password));
    }

    private ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()*2,
            new ThreadFactory() {
        public int count = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, String.format("%s-%d", TAG, count++));
        }
    });

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String user = intent.getStringExtra(USER);
        String pass = intent.getStringExtra(PASS);

        executor.submit(new ConnectTask(user, pass, this, activeConnection));

        notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("XMPP active")
                .setContentText("XMPP client service is active and killin' yo battery")
                .setContentIntent( PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0) )
                .build();

        startForeground(0xBADCAFE, notification);

        return Service.START_REDELIVER_INTENT;

    }

    @Override
    public void onCreate() {
        BusProvider.getBus().register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        BusProvider.getBus().unregister(this);
        try {
            activeConnection.disconnect();
        } catch ( Exception e ){
            Log.e(TAG, "cannot disconnect", e);
        } finally {
            BusProvider.getBus().post(XMPPStatusEvent.UNINITIALIZED);
            super.onDestroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void connected(XMPPConnection xmppConnection) {
        activeConnection = xmppConnection;
        postOnMain(XMPPStatusEvent.CONNECTED);
    }

    @Override
    public void authenticated(XMPPConnection xmppConnection) {
        activeConnection = xmppConnection;
        xmppConnection.addPacketListener(this, new MessageTypeFilter(Message.Type.chat));
        postOnMain(XMPPStatusEvent.AUTHENTICATED);

        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(activeConnection);
        try {
            Log.e(TAG, "Supported " + offlineMessageManager.supportsFlexibleRetrieval());
            for (Message message : offlineMessageManager.getMessages()) {
                processPacket(message);
            }
        } catch (SmackException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (XMPPException.XMPPErrorException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if ( stalledCommands.size() > 0 ){

            Iterator<XMPPCommand> iter = stalledCommands.iterator();
            while( iter.hasNext() ) {
                Log.i(TAG, String.format("size %d", stalledCommands.size()));
                XMPPCommand command = iter.next();
                if ( !activeConnection.isConnected() ){
                    break;
                }
                onCommand(command);
                iter.remove();
            }
        }
    }

    @Override
    public void connectionClosed() {
        activeConnection.removePacketListener(this);
        activeConnection = null;
        postOnMain(XMPPStatusEvent.CONNECTION_CLOSED);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        activeConnection.removePacketListener(this);
        activeConnection = null;
        postOnMain(XMPPStatusEvent.CONNECTION_CLOSED_ERROR);
    }

    @Override
    public void reconnectingIn(int i) {
        postOnMain(XMPPStatusEvent.RECONNECTING);
    }

    @Override
    public void reconnectionSuccessful() {
        postOnMain(XMPPStatusEvent.RECONNECTED);
    }

    @Override
    public void reconnectionFailed(Exception e) {
        activeConnection = null;
        postOnMain(XMPPStatusEvent.RECONNECT_FAILED);
    }

    @Subscribe
    public void onCommand(XMPPCommand command){
        if ( activeConnection != null ){
            if ( activeConnection.isConnected() && activeConnection.isAuthenticated() ) {
                command.applyConnection(activeConnection);
                executor.submit(command);
            } else {
                stalledCommands.add(command);
            }
        } else {
            stalledCommands.add(command);
        }
    }

    private void postOnMain(final Object event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getBus().post(event);
            }
        });
    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        postOnMain(new IncommingMessage((Message) packet));
    }
}
