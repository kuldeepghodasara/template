package com.peoplethink.governmentjob.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.util.layout.TrackingScrollView;

public abstract class DetailActivity extends AppCompatActivity {

    protected RelativeLayout coolblue;
    protected Toolbar mToolbar;
    protected ImageView thumb;

    boolean FadeBar = true;

    protected int mScrollableHeaderHeight;
    protected int latestAlpha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setUpHeader(String imageurl){
        if (isTablet()){
            FadeBar = false;
        } else {
            coolblue.setVisibility(View.GONE);
        }

        if ((null != imageurl && !imageurl.equals("") && !imageurl.equals("null"))) {
            setParralaxHeader();
        } else if (!isTablet()){
            thumb.getLayoutParams().height = getActionBarHeight();
            FadeBar = false;
        } else if (isTablet()){
            setParralaxHeader();
            thumb.getLayoutParams().height = 0;
        }

        if (FadeBar) {
            mToolbar.getBackground().mutate().setAlpha(0);
            Helper.setStatusBarColor(DetailActivity.this,
                    ContextCompat.getColor(this, R.color.black));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setParralaxHeader(){
        if(isTablet()){
            mScrollableHeaderHeight = coolblue.getLayoutParams().height;
        } else {
            mScrollableHeaderHeight = thumb.getLayoutParams().height;
            thumb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    thumb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScrollableHeaderHeight= thumb.getHeight(); //height is ready
                }
            });
        }

        ((TrackingScrollView) findViewById(R.id.scroller))
                .setOnScrollChangedListener(new TrackingScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(
                            TrackingScrollView source, int left,
                            int top, int oldleft, int oldtop) {
                        handleScroll(top);
                    }
                });
    }

    private void handleScroll(int top) {
        int scrolledHeaderHeight = top;
        if (!isTablet()){
            scrolledHeaderHeight = Math.min(mScrollableHeaderHeight, Math.max(0, top));
        }

        ViewGroup.MarginLayoutParams headerParams = null;
        int newHeaderHeight = 0;
        if (isTablet()){
            headerParams = (ViewGroup.MarginLayoutParams) coolblue
                    .getLayoutParams();
            newHeaderHeight = mScrollableHeaderHeight - (scrolledHeaderHeight) / 2;
        } else {
            headerParams = (ViewGroup.MarginLayoutParams) thumb
                    .getLayoutParams();
            newHeaderHeight = mScrollableHeaderHeight - scrolledHeaderHeight;
        }

        if (headerParams.height != newHeaderHeight) {
            // Transfer image height to margin top
            headerParams.height = newHeaderHeight;
            if (!isTablet()){
                headerParams.topMargin = scrolledHeaderHeight;

                // Invalidate view
                thumb.setLayoutParams(headerParams);
            } else {
                coolblue.setLayoutParams(headerParams);
            }
        }

        if (FadeBar) {
            final int imageheaderHeight = thumb.getHeight()
                    - getSupportActionBar().getHeight();
            // t=how far you scrolled
            // ratio is from 0,0.1,0.2,...1
            final float ratio = (float) Math.min(Math.max(top, 0),
                    imageheaderHeight) / imageheaderHeight;
            // setting the new alpha value from 0-255 or transparent to opaque
            final int newAlpha = (int) (ratio * 255);
            if (newAlpha != latestAlpha) {
                mToolbar.getBackground().mutate().setAlpha(newAlpha);
                Helper.setStatusBarColor(DetailActivity.this,
                        blendColors(ratio, this));
            }

            latestAlpha = newAlpha;
        }
    }

    private boolean isTablet(){
        return getResources().getBoolean(R.bool.isTablet);
    }

    private int getActionBarHeight() {
        int actionBarHeight = getSupportActionBar().getHeight();
        if (actionBarHeight != 0)
            return actionBarHeight;
        final TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    getResources().getDisplayMetrics());
        return actionBarHeight;
    }

    private static int blendColors(float ratio, Context c) {
        int color1 = ContextCompat.getColor(c, R.color.myPrimaryDarkColor);
        int color2 = ContextCompat.getColor(c, R.color.black);
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio)
                + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio)
                + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio)
                + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    @Override
    public void onPause() {
        super.onPause();
        mToolbar.getBackground().mutate().setAlpha(255);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FadeBar)
            mToolbar.getBackground().mutate().setAlpha(latestAlpha);
    }

}