package com.peoplethink.governmentjob.attachmentviewer.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.MediaController;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;

/**
 * This file is part of the Modulio template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class AudioPlayerActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {
    private static String TAG="PlayQueueActivity";
    static final int UPDATE_INTERVAL = 250;

    private ServiceConnection serviceConnection = new MusicServiceServiceConnection();
    private com.peoplethink.governmentjob.attachmentviewer.MusicService MusicService;
    private MediaController mediaController;
    private Intent MusicServiceIntent;
    private Handler handler = new Handler();
    private String url = "";
    private String title = "";
    public static final String SERVICE = "service";
    public static final String URL = "url";
    
    public static void startActivity(Context context, String url, String title){
        Intent intent = new Intent(context, AudioPlayerActivity.class);
        intent.putExtra(URL, url);
        intent.putExtra(SERVICE, title);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_audio);

        Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mediaController = new MediaController(this);
        url = getIntent().getStringExtra(URL);
        title = getIntent().getStringExtra(SERVICE);
        ((TextView) findViewById(R.id.now_playing_text)).setText(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download:
                Helper.download(this, url);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_download, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //bind to service
        MusicServiceIntent = new Intent(this, com.peoplethink.governmentjob.attachmentviewer.MusicService.class);
        bindService(MusicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // the MediaController will hide after 3 seconds - tap the screen to
        // make it appear again
        mediaController.show();
        return false;
    }

    @Override
    protected void onPause() {

        unbindService(serviceConnection);
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final class MusicServiceServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            Log.d(TAG,"MusicServiceServiceConnection: Service connected");
            MusicService = ((com.peoplethink.governmentjob.attachmentviewer.MusicService.MusicServiceBinder) baBinder).getService();
            startService(MusicServiceIntent);
            if(MusicService.getMediaPlayer() == null && url != null) {
                MusicService.play(url, title);
            } else if(url != null && !url.equals(MusicService.getUrl())){
                MusicService.play(url, title);
            }
            connectMediaControl();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"MusicServiceServiceConnection: Service disconnected");
            MusicService = null;
        }
    }

    public void connectMediaControl() {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.main_audio_view));

        handler.post(new Runnable() {
            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }

    // --MediaPlayerControl
    // methods----------------------------------------------------
    @Override
    public void start() {
        MusicService.start();
    }

    @Override
    public void pause() {
        MusicService.pause();
    }

    @Override
    public int getDuration() {
        return MusicService.getMediaPlayer().getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return MusicService.getMediaPlayer().getCurrentPosition();
    }

    @Override
    public void seekTo(int i) {
        MusicService.getMediaPlayer().seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return MusicService.getMediaPlayer().isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}