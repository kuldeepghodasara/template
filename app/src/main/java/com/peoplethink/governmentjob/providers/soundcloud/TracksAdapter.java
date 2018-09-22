package com.peoplethink.governmentjob.providers.soundcloud;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.soundcloud.api.object.TrackObject;
import com.peoplethink.governmentjob.providers.soundcloud.player.player.CheerleaderPlayerListener;
import com.peoplethink.governmentjob.providers.soundcloud.ui.views.TrackView;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;

import java.util.List;

/**
 * Simple adapter used to display artist tracks in a list with an optional header.
 */
public class TracksAdapter extends InfiniteRecyclerViewAdapter
    implements CheerleaderPlayerListener {

    /**
     * View types.
     */
    private static final int VIEW_TYPE_TRACK = 1;
    private static final int VIEW_TYPE_HEADER = 2;
    private static final int VIEW_TYPE_FOOTER = 3;

    /**
     * Current played track playlist position used to display an indicator.
     */
    private int mPlayedTrackPosition;

    /**
     * Adapted tracks.
     */
    private List<TrackObject> mTracks;

    /**
     * view header
     */
    private View mHeaderView;

    private View mFooterView;

    /**
     * listener used to catch event on the raw view.
     */
    private TrackView.Listener mListener;

    /**
     * Simple adapter used to display tracks in a list.
     *
     * @param listener listener used to catch event on the raw view.
     * @param tracks   tracks.
     */
    public TracksAdapter(Context context, TrackView.Listener listener, List<TrackObject> tracks) {
        super(context, null);
        mTracks = tracks;
        mPlayedTrackPosition = -1;
        mListener = listener;
    }

    @Override
    protected int getViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return VIEW_TYPE_HEADER;
        } else if (position == getItemCount() - 1 && mFooterView != null) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_TRACK;
        }
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        Holder holder;
        switch (viewType) {
            case VIEW_TYPE_TRACK:
                TrackView v = new TrackView(parent.getContext());
                v.setListener(mListener);
                v.setLayoutParams(
                        new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT)
                );
                holder = new TrackHolder(v);
                break;
            case VIEW_TYPE_HEADER:
                holder = new HeaderHolder(mHeaderView);
                break;
            case VIEW_TYPE_FOOTER:
                holder = new FooterHolder(mFooterView);
                break;
            default:
                throw new IllegalStateException("View type not handled : " + viewType);
        }
        return holder;
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_TRACK:
                int offset = mHeaderView != null ? 1 : 0;
                ((TrackHolder) holder).trackView.setModel(mTracks.get(position - offset));
                if (position == mPlayedTrackPosition) {
                    ((TrackHolder) holder).trackView
                            .setBackgroundResource(R.drawable.soundcloud_selectable_background_selected);
                    ((TrackHolder) holder).trackView.setSelected(true);
                } else {
                    ((TrackHolder) holder).trackView
                            .setBackgroundResource(R.drawable.soundcloud_selectable_background_white);
                    ((TrackHolder) holder).trackView.setSelected(false);
                }

                //Hide the divider for the top view
                if (position == 0)
                    ((TrackHolder) holder).trackView.findViewById(R.id.divider).setVisibility(View.GONE);
                break;
            case VIEW_TYPE_HEADER:
                // do nothing
                break;
            case VIEW_TYPE_FOOTER:
                // do nothing
                break;
            default:
                throw new IllegalStateException("Unhandled view type : " + holder.getItemViewType());
        }
    }

    @Override
    protected int getCount() {
        int header = mHeaderView == null ? 0 : 1;
        int footer = mFooterView == null ? 0 : 1;
        return header + mTracks.size() + footer;
    }

    ////////////////////////////////////////////////////////////
    ///// Player listener used to keep played track updated ////
    ////////////////////////////////////////////////////////////

    @Override
    public void onPlayerPlay(TrackObject track, int position) {
        int offset = mHeaderView == null ? 0 : 1;
        mPlayedTrackPosition = position + offset;
        notifyDataSetChanged();
    }

    @Override
    public void onPlayerPause() {

    }

    @Override
    public void onPlayerSeekTo(int milli) {

    }

    @Override
    public void onPlayerDestroyed() {

    }

    @Override
    public void onBufferingStarted() {

    }

    @Override
    public void onBufferingEnded() {

    }

    @Override
    public void onProgressChanged(int milli) {

    }

    /**
     * Set the header view.
     *
     * @param v header view.
     */
    public void setHeaderView(View v) {
        mHeaderView = v;
    }

    public void setFooterView(View v){
        mFooterView = v;
    }

    /**
     * View holder pattern.
     */
    public static abstract class Holder extends RecyclerView.ViewHolder {
        private int viewType;

        public Holder(View v, int viewType) {
            super(v);
            this.viewType = viewType;
        }
    }

    /**
     * View holder for a track view.
     */
    private static class TrackHolder extends Holder {
        private TrackView trackView;

        private TrackHolder(TrackView v) {
            super(v, VIEW_TYPE_TRACK);
            this.trackView = v;
        }
    }

    /**
     * View holder for the view header.
     */
    private static class FooterHolder extends Holder {

        private FooterHolder(View v) {
            super(v, VIEW_TYPE_FOOTER);
        }
    }

    /**
     * View holder for the view header.
     */
    private static class HeaderHolder extends Holder {

        private HeaderHolder(View v) {
            super(v, VIEW_TYPE_HEADER);
        }
    }
}
