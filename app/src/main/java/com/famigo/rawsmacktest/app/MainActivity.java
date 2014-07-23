package com.famigo.rawsmacktest.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.famigo.rawsmacktest.app.view.DrawView;
import com.famigo.rawsmacktest.app.view.StrokeEvent;
import com.famigo.rawsmacktest.app.xmpp.AbsXMPPCommand;
import com.famigo.rawsmacktest.app.xmpp.ConnectTask;
import com.famigo.rawsmacktest.app.xmpp.RetryManager;
import com.famigo.rawsmacktest.app.xmpp.command.RetryCommand;
import com.famigo.rawsmacktest.app.xmpp.command.ShutdownCommand;
import com.famigo.rawsmacktest.app.xmpp.event.FailedCommandEvent;
import com.famigo.rawsmacktest.app.xmpp.XMPPService;
import com.famigo.rawsmacktest.app.xmpp.command.SendMessageCommand;
import com.famigo.rawsmacktest.app.xmpp.event.IncomingMessage;
import com.famigo.rawsmacktest.app.xmpp.event.XMPPStatusEvent;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import org.jivesoftware.smack.packet.Message;


public class MainActivity extends Activity implements DrawView.OnStrokeEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String to = null;

    private Gson gson = new Gson();
    private DrawView drawView;
    private AlertDialog chooseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawView)findViewById(R.id.draw_view);
        drawView.setOnStrokeEventListener(this);

        chooseDialog = new AlertDialog.Builder(this)
                .setTitle("Startup")
                .setMessage("Choose your user")
                .setPositiveButton("user1", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        to = "user2@" + ConnectTask.VHOST;
                        /*
                         * USAGE NOTE:
                         * this is how to start the service!
                         */
                        XMPPService.start(getApplicationContext(), "user1", "user1");
                    }
                })
                .setNegativeButton("user2", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        to = "user1@" + ConnectTask.VHOST;
                        XMPPService.start(getApplicationContext(), "user2", "user2");
                    }
                })
                .create();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
           switch (item.getItemId()){
               case R.id.disconnect:
                   /*
                    * USAGE NOTE:
                    * this is how to shut the service down.
                    * stopService will work but will pepper the log with
                    * exceptions as it will be unable to do it in the
                    * most graceful manner
                    */
                   BusProvider.getBus().post(new ShutdownCommand());
                   return true;
           }
        return false;
    }

    /*
     * USAGE NOTE:
     * this is how updates to the status of the XMPP
     * service are received
     *
     */
    @Subscribe
    public void onXMPPStatusUpdate( XMPPStatusEvent event ){
        if ( event == XMPPStatusEvent.UNINITIALIZED ){
            if ( !chooseDialog.isShowing() ) {
                chooseDialog.show();
            }
        }
    }

    /*
     * USAGE NOTE:
     * This is how incoming messages are received
     *
     */
    @Subscribe
    public void onMessage( IncomingMessage incomingMessage){
        StrokeEvent strokeEvent = gson.fromJson(incomingMessage.getMessage().getBody(), StrokeEvent.class);
        drawView.addRemoteEvent(strokeEvent);

    }

    /*
     * USAGE NOTE:
     * This is how the UI is notified of a command failure
     *
     * On first failure the type of event.mCommand will match the original commands.
     * As it is retried and subsequently fails, event.mCommand will come in as type RetryCommand.
     *
     * If needed the original command can be accessed via getOriginalCommand on the RetryCommand object.
     *
     * When retry.mTries == RetryManager.MAX_RETRIES the system has finally given up on this command
     * and will no longer be retried.
     *
     */
    @Subscribe
    public void onCmdFailed(FailedCommandEvent event){
        AbsXMPPCommand cmd = event.mCommand;

        if ( cmd instanceof RetryCommand ) {
            RetryCommand retry = (RetryCommand)cmd;

            if ( retry.mTries != RetryManager.MAX_RETRIES ){
                return;
            }

            retry.getOriginalCommand();

            Toast.makeText(this,
                    String.format("Cmd failed, type: %s id: %s", cmd.getClass().getSimpleName(), cmd.getId()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void newStrokeEvent(StrokeEvent strokeEvent) {
        /*
         * USAGE NOTE:
         * This is how a message is sent via XMPP
         */
        Message message = new Message(to, Message.Type.chat);
        message.setBody(gson.toJson(strokeEvent));
        BusProvider.getBus().post(new SendMessageCommand(message));
    }
}
