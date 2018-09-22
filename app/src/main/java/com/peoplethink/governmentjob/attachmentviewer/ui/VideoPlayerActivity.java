package com.peoplethink.governmentjob.attachmentviewer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.peoplethink.governmentjob.R;

/**
 * This file is part of the Modulio template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class VideoPlayerActivity extends Activity {

    private static String URL = "url";

    public static void startActivity(Context fromActivity, String url){
        Intent intent = new Intent(fromActivity, VideoPlayerActivity.class);
        intent.putExtra(URL, url);
        fromActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getExtras().getString(URL);
        setContentView(R.layout.activity_attachment_video);
        final VideoView videoView = findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                View progress = findViewById(R.id.videoProgress);
                progress.setVisibility(View.GONE);

                videoView.requestFocus();
                MediaController vidControl = new MediaController(VideoPlayerActivity.this);
                vidControl.setAnchorView(videoView);
                videoView.setMediaController(vidControl);
                videoView.start();
            }
        });
        videoView.setVideoURI(Uri.parse(url));
    }
}
