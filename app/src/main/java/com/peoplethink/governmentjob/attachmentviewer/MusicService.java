package com.peoplethink.governmentjob.attachmentviewer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.attachmentviewer.ui.AudioPlayerActivity;
import com.peoplethink.governmentjob.providers.radio.player.MediaNotificationManager;
import com.peoplethink.governmentjob.util.Log;

import java.io.IOException;

/**
 * This file is part of the Modulio template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private final String TAG = "MusicService";
    private int mNid = 123;
    private String url;
    private String title;

    private MediaPlayer mediaPlayer;
    private boolean paused = false;

    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            Log.v(TAG, "MusicServiceBinder: getService() called");
            return MusicService.this;
        }
    }

    private final IBinder MusicServiceBinder = new MusicServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "MusicService: onBind() called");
        return MusicServiceBinder;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "MusicService: onCreate() called");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "MusicService: onStart() called, instance=" + this.hashCode());
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "MusicService: onDestroy() called");
        release();
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        release();
    }

    private void release() {
        if (mediaPlayer == null) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void play(String url, String title) {
        this.url = url;
        this.title = title;
        if (mediaPlayer != null && paused) {
            start();
            paused = false;
            return;
        } else if (mediaPlayer != null) {
            release();
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            start();
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException ioe) {
            Log.e(TAG, "error trying to play " + url, ioe);
            String message = "error trying to play track: " + url
                    + ".\nError: " + ioe.getMessage();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getUrl() {
        return url;
    }

    public void stop() {
        release();
        hideNotification();
    }

    public int elapsed() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    public void seek(int timeInMillis) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(timeInMillis);
        }
    }

    // --MediaPlayerControl
    // methods----------------------------------------------------
    public void start() {
        mediaPlayer.start();
        showNotification();
    }

    public void pause() {
        mediaPlayer.pause();
        hideNotification();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void showNotification(){
        String contentTitle = (title == null || title.isEmpty()) ? getResources().getString(R.string.background_audio) : title;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(MediaNotificationManager.NOTIFICATION_CHANNEL_ID,
                    getString(R.string.audio_notification),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, MediaNotificationManager.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_radio_playing)
                        .setContentTitle(contentTitle)
                        .setContentText(getResources().getString(R.string.background_audio));

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, AudioPlayerActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0,
                        resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mNid, mBuilder.build());
    }

    public void hideNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mNid);
    }
}