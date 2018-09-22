package com.peoplethink.governmentjob.providers.tv;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile;
import com.peoplethink.governmentjob.R;

public class VideoControlsCustom extends VideoControlsMobile {

    public VideoControlsCustom(Context context) {
        super(context);
    }

    public VideoControlsCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoControlsCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public VideoControlsCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_tv_controls;
    }

    public void hideSeekBar(){
        this.seekBar.setVisibility(View.GONE);
        this.currentTimeTextView.setVisibility(View.GONE);
        this.endTimeTextView.setVisibility(View.GONE);
    }
}