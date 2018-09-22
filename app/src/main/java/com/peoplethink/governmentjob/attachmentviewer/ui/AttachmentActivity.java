package com.peoplethink.governmentjob.attachmentviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.attachmentviewer.loader.DefaultAudioLoader;
import com.peoplethink.governmentjob.attachmentviewer.loader.DefaultFileLoader;
import com.peoplethink.governmentjob.attachmentviewer.loader.DefaultVideoLoader;
import com.peoplethink.governmentjob.attachmentviewer.loader.MediaLoader;
import com.peoplethink.governmentjob.attachmentviewer.loader.PicassoImageLoader;
import com.peoplethink.governmentjob.attachmentviewer.model.Attachment;
import com.peoplethink.governmentjob.attachmentviewer.model.MediaAttachment;
import com.peoplethink.governmentjob.attachmentviewer.widgets.ScrollGalleryView;
import com.peoplethink.governmentjob.util.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This file is part of the Modulio template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class AttachmentActivity extends AppCompatActivity {

    private ScrollGalleryView scrollGalleryView;
    private List<MediaLoader> mediaList;

    public static String IMAGES = "images";

    public static void startActivity(Context source, MediaAttachment image){
        Intent intent = new Intent(source, AttachmentActivity.class);
        intent.putExtra(IMAGES, new ArrayList<>(Collections.singleton(image)));
        source.startActivity(intent);
    }

    public static void startActivity(Context source, ArrayList<MediaAttachment> images){
        Intent intent = new Intent(source, AttachmentActivity.class);
        intent.putExtra(IMAGES, images);
        source.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Helper.setStatusBarColor(this, R.color.black);

        ArrayList<Attachment> images = (ArrayList<Attachment>) getIntent().getSerializableExtra(IMAGES);
        scrollGalleryView = findViewById(R.id.scroll_gallery_view);

        Helper.admobLoader(this, findViewById(R.id.adView));

        mediaList = easyInitView(scrollGalleryView, images, getSupportFragmentManager());
    }

    public static List<MediaLoader> easyInitView(ScrollGalleryView view, ArrayList<Attachment> images, FragmentManager fm){

        List<MediaLoader> infos = new ArrayList<>(images.size());
        for (Attachment attachment : images) {
            if (attachment instanceof MediaAttachment) {
                MediaAttachment mediaAttachment = ((MediaAttachment) attachment);
                if (mediaAttachment.getMime().contains(MediaAttachment.MIME_PATTERN_IMAGE))
                    infos.add(new PicassoImageLoader(mediaAttachment));
                else if (mediaAttachment.getMime().contains(MediaAttachment.MIME_PATTERN_VID))
                    infos.add(new DefaultVideoLoader(mediaAttachment));
                else if (mediaAttachment.getMime().contains(MediaAttachment.MIME_PATTERN_AUDIO))
                    infos.add(new DefaultAudioLoader(mediaAttachment));
                else
                    infos.add(new DefaultFileLoader(mediaAttachment));
            }
        }

        view.setThumbnailSize((int) view.getContext().getResources().getDimension(R.dimen.thumbnail_height))
                .setZoom(true)
                .setFragmentManager(fm)
                .addMedia(infos);

        if (infos.size() == 1){
            view.hideThumbnails(true);
        }

        return infos;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
