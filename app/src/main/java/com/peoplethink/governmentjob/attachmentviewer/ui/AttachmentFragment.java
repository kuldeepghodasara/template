package com.peoplethink.governmentjob.attachmentviewer.ui;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.attachmentviewer.loader.DefaultImageLoader;
import com.peoplethink.governmentjob.attachmentviewer.loader.MediaLoader;
import com.peoplethink.governmentjob.attachmentviewer.loader.PicassoImageLoader;
import com.peoplethink.governmentjob.attachmentviewer.model.MediaAttachment;
import com.peoplethink.governmentjob.attachmentviewer.widgets.HackyViewPager;
import com.peoplethink.governmentjob.attachmentviewer.widgets.ScrollGalleryView;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * This file is part of the Modulio template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class AttachmentFragment extends Fragment {

    //Constants
    public static final String MEDIALOADER = "loader";
    public static final String ZOOM = "zoom";

    private boolean systemUIVisible = true;

    //Media to load
    private MediaLoader mMediaLoader;

    //Views
    private HackyViewPager viewPager;
    private ImageView backgroundImage;
    private TextView descriptionView;
    private ScrollGalleryView scrollGalleryView;
    private View rootView;

    private PhotoViewAttacher photoViewAttacher;
    private AttachmentActivity activity;

    public void setMediaLoader(MediaLoader mediaLoader) {
        mMediaLoader = mediaLoader;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        rootView = inflater.inflate(R.layout.fragment_attachment, container, false);
        backgroundImage = rootView.findViewById(R.id.backgroundImage);
        descriptionView = rootView.findViewById(R.id.description);
        viewPager = getActivity().findViewById(R.id.viewPager);
        scrollGalleryView = getActivity().findViewById(R.id.scroll_gallery_view);

        if (savedInstanceState != null) {
            mMediaLoader = (MediaLoader) savedInstanceState.getSerializable(MEDIALOADER);
        }

        loadMediaToView(rootView, savedInstanceState);

        String description = mMediaLoader.getAttachment().getDescription();
        if (description != null && !description.isEmpty()) {
            descriptionView.setText(Html.fromHtml(description));
            descriptionView.setVisibility(View.VISIBLE);
        }

        if (scrollGalleryView.thumbnailsHidden()){
            rootView.findViewById(R.id.thumbnail_container_padding).setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (AttachmentActivity) getActivity();
    }

    private void loadMediaToView(final View rootView, Bundle savedInstanceState) {
        assert mMediaLoader != null;

        mMediaLoader.loadMedia(this, backgroundImage, rootView, new MediaLoader.SuccessCallback() {
            @Override
            public void onSuccess() {
                if (mMediaLoader instanceof PicassoImageLoader || mMediaLoader instanceof DefaultImageLoader)
                    createViewAttacher(getArguments());
                rootView.findViewById(R.id.attachmentProgress).setVisibility(View.GONE);
            }
        });
    }

    private void createViewAttacher(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(ZOOM)) {
            Log.v("INFO", "Attaching zoom stuff");
            photoViewAttacher = new PhotoViewAttacher(backgroundImage);
            photoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    if (systemUIVisible){
                        hideSystemUI();
                    } else {
                        showSystemUI();
                    }
                }

                @Override
                public void onOutsidePhotoTap() {

                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(MEDIALOADER, mMediaLoader);
        outState.putBoolean(ZOOM, photoViewAttacher != null);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_download:
                    Helper.download(getActivity(),
                            ((MediaAttachment) mMediaLoader.getAttachment()).getUrl());
                return true;
            case R.id.action_wallpaper:
                Picasso.with(getContext()).load(((MediaAttachment) mMediaLoader.getAttachment()).getUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
                        try {
                            wallpaperManager.setBitmap(bitmap);

                            Toast toast = Toast.makeText(getContext(), getString(R.string.wallpaper_success), Toast.LENGTH_SHORT);
                            toast.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (mMediaLoader.getAttachment() instanceof MediaAttachment) {
            inflater.inflate(R.menu.menu_download, menu);

            //If this media is an image, also inflate the wallpaper button.
            if (mMediaLoader.getAttachment() instanceof MediaAttachment &&
                    ((MediaAttachment) mMediaLoader.getAttachment()).getMime().contains(MediaAttachment.MIME_PATTERN_IMAGE))
                inflater.inflate(R.menu.menu_image, menu);
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    private void showSystemUI(){
        scrollGalleryView.hideThumbnails(false);
        if (!scrollGalleryView.thumbnailsHidden())
            rootView.findViewById(R.id.bottomHolder).setVisibility(View.VISIBLE);

        if (activity.getSupportActionBar() != null){
            if (!(android.os.Build.VERSION.SDK_INT <= 19)) {
                activity.getSupportActionBar().show();
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= 19)
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        systemUIVisible = true;
    }

    private void hideSystemUI(){
        scrollGalleryView.hideThumbnails(true);
        rootView.findViewById(R.id.bottomHolder).setVisibility(View.GONE);

        if (activity.getSupportActionBar() != null){
            if (!(android.os.Build.VERSION.SDK_INT <= 19)) {
                activity.getSupportActionBar().hide();
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        systemUIVisible = false;
    }

}
