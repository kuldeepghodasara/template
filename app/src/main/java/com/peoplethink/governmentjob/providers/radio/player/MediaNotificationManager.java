package com.peoplethink.governmentjob.providers.radio.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.radio.ui.RadioFragment;

import com.peoplethink.governmentjob.providers.radio.metadata.Metadata;

public class MediaNotificationManager {

    public static final int NOTIFICATION_ID = 555;
    public static final String NOTIFICATION_CHANNEL_ID = "radio_channel";

    private RadioService service;

    private Metadata meta;
    private String strAppName, strLiveBroadcast;

    private Bitmap notifyIcon;
    private String playbackStatus;

    private Resources resources;

    public MediaNotificationManager(RadioService service) {

        this.service = service;
        this.resources = service.getResources();

        strAppName = resources.getString(R.string.app_name);
        strLiveBroadcast = resources.getString(R.string.notification_playing);

    }

    public void startNotify(String playbackStatus) {
        this.playbackStatus = playbackStatus;
        this.notifyIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);

        startNotify();
    }

    public void startNotify(Bitmap notifyIcon, Metadata meta) {

        this.notifyIcon = notifyIcon;
        this.meta = meta;
        startNotify();
    }

    private void startNotify(){
        if (playbackStatus == null) return;

        if (notifyIcon == null)
            notifyIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);

        NotificationManager notificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    service.getString(R.string.audio_notification),
                    NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }

        int icon = R.drawable.ic_pause_white;
        Intent playbackAction = new Intent(service, RadioService.class);
        playbackAction.setAction(RadioService.ACTION_PAUSE);
        PendingIntent action = PendingIntent.getService(service, 1, playbackAction, 0);

        if(playbackStatus.equals(PlaybackStatus.PAUSED)){
            icon = R.drawable.ic_play_white;
            playbackAction.setAction(RadioService.ACTION_PLAY);
            action = PendingIntent.getService(service, 2, playbackAction, 0);
        }

        Intent stopIntent = new Intent(service, RadioService.class);
        stopIntent.setAction(RadioService.ACTION_STOP);
        PendingIntent stopAction = PendingIntent.getService(service, 3, stopIntent, 0);

        Intent intent = new Intent(service, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, new String[]{service.getStreamUrl()});
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, RadioFragment.class);
        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManagerCompat.from(service)
                .cancel(NOTIFICATION_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID);


        String title = meta != null && meta.getArtist() != null ?
                meta.getArtist() : strLiveBroadcast;
        String subTitle = meta != null && meta.getSong() != null ?
                meta.getSong() : strAppName;

        builder.setContentTitle(title)
                .setContentText(subTitle)
                .setLargeIcon(notifyIcon)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_radio_playing)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_stop, "stop", stopAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.getMediaSession().getSessionToken())
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopAction));

        Notification notification = builder.build();
        service.startForeground(NOTIFICATION_ID, notification);

    }

    public void cancelNotify() {

        service.stopForeground(true);

    }

}
