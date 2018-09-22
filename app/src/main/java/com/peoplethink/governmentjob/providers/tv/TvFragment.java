package com.peoplethink.governmentjob.providers.tv;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.apg.mobile.roundtextview.BadgeView;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.inherit.BackPressFragment;
import com.peoplethink.governmentjob.inherit.CollapseControllingFragment;
import com.peoplethink.governmentjob.inherit.ConfigurationChangeFragment;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;
import com.peoplethink.governmentjob.util.layout.CustomAppBarLayout;

/**
 * This fragment is used to play live video streams.
 */
public class TvFragment extends Fragment implements CollapseControllingFragment,
        BackPressFragment, ConfigurationChangeFragment, OnPreparedListener {

    private Activity mAct;
    private RelativeLayout rl;

    private VideoView videoView;
    private boolean systemUIHidden = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rl = (RelativeLayout) inflater.inflate(R.layout.fragment_tv, container, false);
        videoView = rl.findViewById(R.id.video_view);

        return rl;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        setRetainInstance(true);

        Helper.isOnlineShowDialog(mAct);

        String streamUrl = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
        initCustomControls();
        videoView.setOnPreparedListener(this);
        videoView.setVideoURI(Uri.parse(streamUrl));

        /**videoView.setId3MetadataListener(new MetadataListener(){

        @Override public void onMetadata(Metadata metadata) {
        Log.v("INFO", "Meta:" + metadata);
        }
        });**/

    }

    public void initCustomControls(){
        VideoControlsCustom controlsLeanback = new VideoControlsCustom(mAct);

        controlsLeanback.hideSeekBar();
        FrameLayout fullscreen = (FrameLayout) LayoutInflater.from(mAct).inflate(R.layout.listview_slider_chip, null);
        fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (systemUIHidden) {
                    showSystemUI();
                } else {
                    hideSystemUI();
                }
            }
        });
        ((BadgeView) fullscreen.findViewById(R.id.category_chip)).setBadgeMainText(getString(R.string.toggle_fullscreen));
        controlsLeanback.addExtraView(fullscreen);
        controlsLeanback.setVisibilityListener(new ControlsVisibilityListener());
        videoView.setControls(controlsLeanback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        showSystemUI();
    }

    @Override
    public boolean supportsCollapse() {
        return false;
    }

    @Override
    public boolean handleBackPress() {
        if (systemUIHidden) {
            showSystemUI();
            return true;
        }
        return false;
    }

    @Override
    public void onPrepared() {
        videoView.start();
    }

    private class ControlsVisibilityListener implements VideoControlsVisibilityListener {
        @Override
        public void onControlsShown() {
            //showSystemUI();
        }

        @Override
        public void onControlsHidden() {
            //hideSystemUI();
        }
    }

    private void hideSystemUI() {

        if (mAct instanceof MainActivity) {
            if (!((MainActivity) mAct).useTabletMenu()) {
                ((MainActivity) mAct).drawer.setFitsSystemWindows(false);

                //Workaround because setFitsSystemWindows isn't passed on to RelativeLayout
                RelativeLayout layout = mAct.findViewById(R.id.drawer_child);
                DrawerLayout.LayoutParams relativeParams = (DrawerLayout.LayoutParams) layout.getLayoutParams();
                relativeParams.setMargins(0, 0, 0, 0);
                layout.setLayoutParams(relativeParams);
            } else {
                mAct.findViewById(R.id.nav_view).setVisibility(View.GONE);
            }

        }

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View mDecorView = mAct.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 19) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else if (Build.VERSION.SDK_INT >= 16) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        systemUIHidden = true;
    }

    private void showSystemUI() {
        try {
            if (mAct instanceof MainActivity) {
                if (!((MainActivity) mAct).useTabletMenu())
                    ((MainActivity) mAct).drawer.setFitsSystemWindows(true);
                else {
                    mAct.findViewById(R.id.nav_view).setVisibility(View.VISIBLE);

                    //Hacky way to prevent overlay on navigationview:
                    //We show the status bar as translucent, and add padding to the toolbar to compensate
                    mAct.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    CustomAppBarLayout layout = (CustomAppBarLayout) ((MainActivity) mAct).mToolbar.getParent();
                    layout.setPadding(0, (int) Math.ceil(25 *  getResources().getDisplayMetrics().density), 0, 0);
                }
            }

            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (Build.VERSION.SDK_INT >= 16) {
                View mDecorView = mAct.getWindow().getDecorView();
                mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();

            systemUIHidden = false;
        } catch (NullPointerException e) {
            Log.e("INFO", e.toString());
        }
    }


}


