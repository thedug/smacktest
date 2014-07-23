package com.famigo.rawsmacktest.app.xmpp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
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
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class XMPPService extends Service implements ConnectionListener, PacketListener, ICommandContext, ReceiptReceivedListener {

    private static final String USER = "luser";
    private static final String PASS = "passwd";
    private static final String TAG = XMPPService.class.getSimpleName();

    private static final long CHECK_DELAY = 10000;
    private static final int MAX_HISTORY = 1000;


    private XMPPConnection activeConnection = null;

    private Handler handler = new Handler();
    private RetryManager retryManager;

    private Notification notification;
    
    private Map<String, XMPPCommand> outStandingCommands = new ConcurrentHashMap<String, XMPPCommand>();

    private Set<String> servicedPackets = Collections.synchronizedSet(Collections.newSetFromMap(new LinkedHashMap<String, Boolean>(){
        @Override
        protected boolean removeEldestEntry(Entry<String, Boolean> eldest) {
            return this.size() > MAX_HISTORY;
        }
    }));

    private Runnable outStandingKickoffRunnable = new Runnable() {
        @Override
        public void run() {
            executor.submit( new OutstandingCheckTask(XMPPService.this) );
        }
    };

    public static void start(Context ctx, String username, String password){
        ctx.startService(
                new Intent(ctx, XMPPService.class)
                        .putExtra(USER, username)
                        .putExtra(PASS, password));
    }

    private ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()*4,
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

        watchOutstandingCommands();

        return Service.START_REDELIVER_INTENT;

    }

    @Override
    public void watchOutstandingCommands() {
        handler.postDelayed( outStandingKickoffRunnable, CHECK_DELAY /2);
    }

    @Override
    public void onCreate() {
        BusProvider.getBus().register(this);
        retryManager = new RetryManager(handler);
        retryManager.run();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        BusProvider.getBus().unregister(this);
        BusProvider.getBus().post(XMPPStatusEvent.UNINITIALIZED);

        handler.removeCallbacks(outStandingKickoffRunnable);
        retryManager.shutdown();

        super.onDestroy();
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

        DeliveryReceiptManager.getInstanceFor(activeConnection).enableAutoReceipts();
        DeliveryReceiptManager.getInstanceFor(activeConnection).addReceiptReceivedListener(this);

    }

    @Override
    public void connectionClosed() {
        activeConnection.removePacketListener(this);
        activeConnection = null;
        postOnMain(XMPPStatusEvent.CONNECTION_CLOSED);
        this.stopSelf();
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
        command.initialize(this);
        executor.submit(command);
    }

    @Override
    public void postOnMain(final Object event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getBus().post(event);
            }
        });
    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        if ( !servicedPackets.contains(packet.getPacketID())) {
            servicedPackets.add(packet.getPacketID());
            postOnMain(new IncommingMessage((Message) packet));
        }
    }

    @Override
    public XMPPConnection getActiveConnection() {
        return activeConnection;
    }

    @Override
    public void addOutstandingCommand(XMPPCommand command) {
        command.expiration = SystemClock.uptimeMillis()+ CHECK_DELAY;
        outStandingCommands.put(command.getId(), command);
    }

    @Override
    public Map<String, XMPPCommand> getOutStandingCommands() {
        return outStandingCommands;
    }

    @Override
    public void onReceiptReceived(String arg0, String arg1, String receiptId){
        outStandingCommands.remove(receiptId);
        retryManager.removeLateBloomer(receiptId);
    }
}
