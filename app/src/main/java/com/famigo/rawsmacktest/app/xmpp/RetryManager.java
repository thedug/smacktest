package com.famigo.rawsmacktest.app.xmpp;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.famigo.rawsmacktest.app.BusProvider;
import com.famigo.rawsmacktest.app.xmpp.command.RetryCommand;
import com.famigo.rawsmacktest.app.xmpp.event.FailedCommandEvent;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class RetryManager implements Runnable{

    private static final String TAG = RetryManager.class.getSimpleName();
    public static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY = 10000;

    private final WeakReference<Handler> weakHandler;

    private Map<String, RetryCommand> retriesOutstanding = new ConcurrentHashMap<String, RetryCommand>();

    public RetryManager(Handler handler){
        this.weakHandler = new WeakReference<Handler>(handler);

        BusProvider.getBus().register(this);

    }

    @Subscribe
    public void onFailedCommandEvent( FailedCommandEvent event ){
        RetryCommand retryCommand = null;
        if ( event.command instanceof RetryCommand ){
            retryCommand = (RetryCommand) event.command;
        } else {
            retryCommand = new RetryCommand(event.command);
        }

        retryCommand.tries++;
        retryCommand.retryAfter = SystemClock.uptimeMillis() + BASE_DELAY * retryCommand.tries;

        if ( retryCommand.tries < MAX_RETRIES ){
            retriesOutstanding.put(retryCommand.getId(), retryCommand);
        }

    }

    public void removeLateBloomer(String id){
        retriesOutstanding.remove(id);
    }

    @Override
    public void run() {
        Log.i(TAG, String.format("we have %d potential retriesOutstanding scheduled", retriesOutstanding.size()));
        Iterator<Map.Entry<String, RetryCommand>> iter = retriesOutstanding.entrySet().iterator();
        while (iter.hasNext()) {
            RetryCommand cmd = iter.next().getValue();
            if (cmd.retryAfter <= SystemClock.uptimeMillis()) {
                BusProvider.getBus().post(cmd);
                iter.remove();
            }
        }

        Handler handler = weakHandler.get();
        if (handler != null) {
            handler.postDelayed(this, BASE_DELAY / 2);
        }
    }

    public void shutdown(){
        Handler handler = weakHandler.get();
        if ( handler != null ) {
            handler.removeCallbacks(this);
        }
        BusProvider.getBus().unregister(this);
        retriesOutstanding.clear();
    }
}
