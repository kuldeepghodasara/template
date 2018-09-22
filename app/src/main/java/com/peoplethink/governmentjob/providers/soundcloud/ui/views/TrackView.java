package com.peoplethink.governmentjob.providers.soundcloud.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.soundcloud.api.object.TrackObject;
import com.peoplethink.governmentjob.providers.soundcloud.helpers.SoundCloudArtworkHelper;
import com.squareup.picasso.Picasso;

/**
 * Simple View used to render a track.
 */
public class TrackView extends FrameLayout implements View.OnClickListener {

    private ImageView mArtwork;
    private TextView mTitle;
    private TextView mArtist;
    private TextView mDuration;
    private ImageView mMore;

    private TrackObject mModel;
    private Listener mListener;

    private int mTrackColor;
    private int mArtistColor;
    private int mDurationColor;
    private int mTrackColorSelected;
    private int mArtistColorSelected;
    private int mDurationColorSelected;

    /**
     * Simple View used to render a track.
     *
     * @param context calling context.
     */
    public TrackView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple View used to render a track.
     *
     * @param context calling context.
     * @param attrs   attr from xml.
     */
    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple View used to render a track.
     *
     * @param context      calling context.
     * @param attrs        attr from xml.
     * @param defStyleAttr style from xml.
     */
    public TrackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mDuration.setTextColor(mDurationColorSelected);
            mArtist.setTextColor(mArtistColorSelected);
            mTitle.setTextColor(mTrackColorSelected);
        } else {
            mDuration.setTextColor(mDurationColor);
            mArtist.setTextColor(mArtistColor);
            mTitle.setTextColor(mTrackColor);
        }
    }

    /**
     * Set the track which must be displayed.
     *
     * @param track view model.
     */
    public void setModel(TrackObject track) {
        mModel = track;
        if (mModel != null) {

            String artworkUrl = SoundCloudArtworkHelper.getArtworkUrl(mModel, SoundCloudArtworkHelper.XLARGE);
            if (artworkUrl != null) {
                mArtwork.setVisibility(View.VISIBLE);
                Picasso.with(this.getContext()).load(artworkUrl).into(mArtwork);
            } else {
                mArtwork.setVisibility(View.GONE);
            }

            mArtist.setText(mModel.getUsername());
            mTitle.setText(mModel.getTitle());
            long min = mModel.getDuration() / 60000;
            long sec = (mModel.getDuration() % 60000) / 1000;
            mDuration.setText(String.format(getResources().getString(R.string.duration), min, sec));
        }
    }

    /**
     * Set a listener to catch the view events.
     *
     * @param listener listener to register.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void init(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.soundcloud_track_view, this);

        mArtwork = findViewById(R.id.track_view_artwork);
        mTitle = findViewById(R.id.track_view_title);
        mArtist = findViewById(R.id.track_view_artist);
        mDuration = findViewById(R.id.track_view_duration);
        mMore = findViewById(R.id.track_more);

        setBackgroundResource(R.drawable.soundcloud_selectable_background_white);

        this.setOnClickListener(this);

        Resources res = getResources();
        mTrackColor = ContextCompat.getColor(context, R.color.track_view_track);
        mArtistColor = ContextCompat.getColor(context, R.color.track_view_artist);
        mDurationColor = ContextCompat.getColor(context, R.color.track_view_duration);
        mArtistColorSelected = ContextCompat.getColor(context, R.color.track_view_artist_selected);
        mTrackColorSelected = ContextCompat.getColor(context, R.color.track_view_track_selected);
        mDurationColorSelected = ContextCompat.getColor(context, R.color.track_view_duration_selected);

        mMore.setOnClickListener( new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mListener != null) {
                    mListener.onMoreClicked(mModel, v);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onTrackClicked(mModel);
        }
    }

    /**
     * Interface used to catch view events.
     */
    public interface Listener {

        /**
         * Called when the user clicked on the track view.
         *
         * @param track model of the view.
         */
        void onTrackClicked(TrackObject track);

        void onMoreClicked(TrackObject track, View source);
    }
}
