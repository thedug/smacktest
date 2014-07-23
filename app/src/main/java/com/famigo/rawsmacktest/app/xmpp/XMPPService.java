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

import com.famigo.rawsmacktest.app.BusProvider;
import com.famigo.rawsmacktest.app.MainActivity;
import com.famigo.rawsmacktest.app.R;
import com.famigo.rawsmacktest.app.xmpp.event.XMPPStatusEvent;
import com.famigo.rawsmacktest.app.xmpp.packet_handler.ChatMessageHandler;
import com.squareup.otto.Subscribe;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.util.Collection;
import java.util.Collections;
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
public class XMPPService extends Service implements ConnectionListener, IXMPPContext, ReceiptReceivedListener {

    private static final String USER = "luser";
    private static final String PASS = "passwd";
    private static final String TAG = XMPPService.class.getSimpleName();

    /**
     * Delay between sending a packet and calling it failed and ready for retry
     */
    private static final long CHECK_DELAY = 10000;
    /**
     * number of recently packets we keep so that we can detect duplicate packets
     */
    private static final int MAX_HISTORY = 1000;

    /*
     * EXTENSION NOTE:
     * if you need to handle more packet types create a
     * new AbsPacketHandler implementation for it and add it here
     */
    private final AbsPacketHandler[] mPacketHandlers = {new ChatMessageHandler(this)};

    public static void start(Context ctx, String username, String password){
        ctx.startService(
                new Intent(ctx, XMPPService.class)
                        .putExtra(USER, username)
                        .putExtra(PASS, password));
    }

    private XMPPConnection mActiveConnection = null;

    private Handler mHandler = new Handler();
    private RetryManager mRetryManager;

    private Notification mNotification;
    
    private Map<String, AbsXMPPCommand> mOutStandingCommands = new ConcurrentHashMap<String, AbsXMPPCommand>();

    private Set<String> mServicedPackets = Collections.synchronizedSet(Collections.newSetFromMap(new LinkedHashMap<String, Boolean>(){
        @Override
        protected boolean removeEldestEntry(Entry<String, Boolean> eldest) {
            return this.size() > MAX_HISTORY;
        }
    }));

    private Runnable mOutStandingKickoffRunnable = new Runnable() {
        @Override
        public void run() {
            mExecutor.submit(new OutstandingCheckTask(XMPPService.this));
        }
    };

    private ExecutorService mExecutor = Executors.newFixedThreadPool(
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

        mExecutor.submit(new ConnectTask(user, pass, this, mActiveConnection));

        mNotification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("XMPP active")
                .setContentText("XMPP client service is active and killin' yo battery")
                .setContentIntent( PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0) )
                .build();

        startForeground(0xBADCAFE, mNotification);

        watchOutstandingCommands();

        return Service.START_REDELIVER_INTENT;

    }

    @Override
    public void watchOutstandingCommands() {
        mHandler.postDelayed(mOutStandingKickoffRunnable, CHECK_DELAY / 2);
    }

    @Override
    public Collection<String> getServicedPackets() {
        return mServicedPackets;
    }

    @Override
    public void onCreate() {
        BusProvider.getBus().register(this);
        mRetryManager = new RetryManager(mHandler);
        /*
         * USAGE NOTE:
         * comment out to disable retry
         */
        mRetryManager.run();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        BusProvider.getBus().unregister(this);
        BusProvider.getBus().post(XMPPStatusEvent.UNINITIALIZED);

        mHandler.removeCallbacks(mOutStandingKickoffRunnable);
        mRetryManager.shutdown();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void connected(XMPPConnection xmppConnection) {
        mActiveConnection = xmppConnection;
        postOnMain(XMPPStatusEvent.CONNECTED);
    }

    @Override
    public void authenticated(XMPPConnection xmppConnection) {
        mActiveConnection = xmppConnection;
        attachPacketHandlers();
        postOnMain(XMPPStatusEvent.AUTHENTICATED);

        DeliveryReceiptManager.getInstanceFor(mActiveConnection).enableAutoReceipts();
        DeliveryReceiptManager.getInstanceFor(mActiveConnection).addReceiptReceivedListener(this);

    }

    private void attachPacketHandlers() {
        for ( AbsPacketHandler ph: mPacketHandlers) {
            mActiveConnection.addPacketListener(ph, ph.getFilter());
        }
    }

    private void detachPacketHandlers() {
        for ( AbsPacketHandler ph: mPacketHandlers) {
            mActiveConnection.removePacketListener(ph);
        }
    }

    @Override
    public void connectionClosed() {
        detachPacketHandlers();
        mActiveConnection = null;
        postOnMain(XMPPStatusEvent.CONNECTION_CLOSED);
        this.stopSelf();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        detachPacketHandlers();
        mActiveConnection = null;
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
        mActiveConnection = null;
        postOnMain(XMPPStatusEvent.RECONNECT_FAILED);
    }

    @Subscribe
    public void onCommand(AbsXMPPCommand command){
        command.initialize(this);
        mExecutor.submit(command);
    }

    @Override
    public void postOnMain(final Object event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getBus().post(event);
            }
        });
    }

    @Override
    public XMPPConnection getActiveConnection() {
        return mActiveConnection;
    }

    @Override
    public void addOutstandingCommand(AbsXMPPCommand command) {
        command.mExpiration = SystemClock.uptimeMillis()+ CHECK_DELAY;
        mOutStandingCommands.put(command.getId(), command);
    }

    @Override
    public Map<String, AbsXMPPCommand> getOutStandingCommands() {
        return mOutStandingCommands;
    }

    @Override
    public void onReceiptReceived(String arg0, String arg1, String receiptId){
        mOutStandingCommands.remove(receiptId);
        mRetryManager.removeLateBloomer(receiptId);
    }
}
