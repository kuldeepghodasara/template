package com.peoplethink.governmentjob.attachmentviewer.loader;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.attachmentviewer.model.MediaAttachment;
import com.peoplethink.governmentjob.attachmentviewer.ui.AttachmentFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * This file is part of the Modulio template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class PicassoImageLoader extends MediaLoader {

    public PicassoImageLoader(MediaAttachment attachment) {
        super(attachment);
    }

    @Override
    public boolean isImage() {
        return true;
    }

    @Override
    public void loadMedia(AttachmentFragment context, final ImageView imageView, View rootView, final MediaLoader.SuccessCallback callback) {
        Picasso.with(context.getContext())
                .load(((MediaAttachment) getAttachment()).getUrl())
                .into(imageView, new ImageCallback(callback));
    }

    @Override
    public void loadThumbnail(Context context, ImageView thumbnailView, MediaLoader.SuccessCallback callback) {
        MediaAttachment att = ((MediaAttachment) getAttachment());
        String imageUrl = att.getThumbnailUrl() == null ? att.getThumbnailUrl() : att.getUrl();
        Picasso.with(context)
                .load(imageUrl)
                .resize(100, 100)
                .placeholder(R.drawable.placeholder)
                .centerInside()
                .into(thumbnailView, new ImageCallback(callback));
    }

    private static class ImageCallback implements Callback {

        private final MediaLoader.SuccessCallback callback;

        public ImageCallback(SuccessCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess() {
            callback.onSuccess();
        }

        @Override
        public void onError() {

        }
    }

}

