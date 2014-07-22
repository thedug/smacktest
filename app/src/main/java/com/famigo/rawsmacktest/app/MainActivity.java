package com.famigo.rawsmacktest.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.famigo.rawsmacktest.app.view.DrawView;
import com.famigo.rawsmacktest.app.view.StrokeEvent;
import com.famigo.rawsmacktest.app.xmpp.ConnectTask;
import com.famigo.rawsmacktest.app.xmpp.RetryManager;
import com.famigo.rawsmacktest.app.xmpp.command.RetryCommand;
import com.famigo.rawsmacktest.app.xmpp.event.FailedCommandEvent;
import com.famigo.rawsmacktest.app.xmpp.XMPPCommand;
import com.famigo.rawsmacktest.app.xmpp.XMPPService;
import com.famigo.rawsmacktest.app.xmpp.command.SendMessageCommand;
import com.famigo.rawsmacktest.app.xmpp.event.IncommingMessage;
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
                        XMPPService.start(getApplicationContext(),"user1", "user1");
                    }
                })
                .setNegativeButton("user2", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        to = "user1@" + ConnectTask.VHOST;
                        XMPPService.start(getApplicationContext(),"user2", "user2");
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
                   stopService(new Intent(this, XMPPService.class));
                   return true;
           }
        return false;
    }

    @Subscribe
    public void onXMPPStatusUpdate( XMPPStatusEvent event ){
        if ( event == XMPPStatusEvent.UNINITIALIZED ){
            if ( !chooseDialog.isShowing() ) {
                chooseDialog.show();
            }
        }
    }

    @Subscribe
    public void onMessage( IncommingMessage incommingMessage ){
        StrokeEvent strokeEvent = gson.fromJson(incommingMessage.getMessage().getBody(), StrokeEvent.class);
        drawView.addRemoteEvent(strokeEvent);

    }

    @Subscribe
    public void onCmdFailed(FailedCommandEvent event){
        XMPPCommand cmd = event.command;

        if ( cmd instanceof RetryCommand ) {
            RetryCommand retry = (RetryCommand)cmd;

            if ( retry.tries != RetryManager.MAX_RETRIES ){
                return;
            }

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
        Message message = new Message(to, Message.Type.chat);
        message.setBody(gson.toJson(strokeEvent));
        BusProvider.getBus().post(new SendMessageCommand(message));
    }
}
