package com.peoplethink.governmentjob.providers.soundcloud.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.inherit.BackPressFragment;
import com.peoplethink.governmentjob.inherit.CollapseControllingFragment;
import com.peoplethink.governmentjob.providers.soundcloud.TracksAdapter;
import com.peoplethink.governmentjob.providers.soundcloud.api.SoundCloudClient;
import com.peoplethink.governmentjob.providers.soundcloud.api.object.TrackObject;
import com.peoplethink.governmentjob.providers.soundcloud.helpers.EndlessRecyclerOnScrollListener;
import com.peoplethink.governmentjob.providers.soundcloud.player.player.CheerleaderPlayer;
import com.peoplethink.governmentjob.providers.soundcloud.player.player.CheerleaderPlaylistListener;
import com.peoplethink.governmentjob.providers.soundcloud.ui.views.PlaybackView;
import com.peoplethink.governmentjob.providers.soundcloud.ui.views.TrackView;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is used to display a list of SoundCloud tracks
 */

public class SoundCloudFragment extends Fragment implements
        PlaybackView.Listener, CheerleaderPlaylistListener, BackPressFragment, CollapseControllingFragment {

    // Static
    private static final int PER_PAGE = 20;

    // sound cloud
    private CheerleaderPlayer mCheerleaderPlayer;

    // tracks widget
    private RecyclerView mRetrieveTracksRecyclerView;
    private TrackView.Listener mRetrieveTracksListener;
    private ArrayList<TrackObject> mRetrievedTracks;
    private TracksAdapter mAdapter;

    // player widget
    private RecyclerView mPlaylistRecyclerView;
    private PlaybackView mPlaybackView;
    private TracksAdapter mPlaylistAdapter;
    private ArrayList<TrackObject> mPlaylistTracks;

    private TrackView.Listener mPlaylistTracksListener;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;

    //Fragment
    private Activity mAct;
    private FrameLayout ll;

    @SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ll = (FrameLayout) inflater.inflate(R.layout.fragment_soundcloud,
                container, false);
		setHasOptionsMenu(true);

        mRetrieveTracksRecyclerView = ll.findViewById(R.id.recyclerview);
        mPlaylistRecyclerView = ll.findViewById(R.id.activity_artist_playlist);

        return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, getArguments().getStringArray(MainActivity.FRAGMENT_DATA));
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, this.getClass());

        mCheerleaderPlayer = new CheerleaderPlayer.Builder()
                .from(mAct)
                .with(R.string.soundcloud_id)
                .notificationActivity(mAct)
                .notificationIcon(R.drawable.ic_radio_playing)
                .notificationBundle(bundle)
                .build();

        initRetrieveTracksRecyclerView();
        initPlaylistTracksRecyclerView();
        setTrackListPadding();

        // check if tracks are already loaded into the player.
        ArrayList<TrackObject> currentsTracks = mCheerleaderPlayer.getTracks();
        if (currentsTracks != null) {
            mPlaylistTracks.addAll(currentsTracks);
        }

        // synchronize the player view with the current player (loaded track, playing state, etc.)
        mPlaybackView.synchronize(mCheerleaderPlayer);

        // Load the tracks
        loadTracks(0);
	}

    @Override
    public void onResume() {
        super.onResume();
        mCheerleaderPlayer.registerPlayerListener(mPlaybackView);
        mCheerleaderPlayer.registerPlayerListener(mPlaylistAdapter);
        mCheerleaderPlayer.registerPlaylistListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mCheerleaderPlayer.unregisterPlayerListener(mPlaybackView);
            mCheerleaderPlayer.unregisterPlayerListener(mPlaylistAdapter);
            mCheerleaderPlayer.unregisterPlaylistListener(this);
        } catch (Exception e){
            //As a fault in 'un-registering' won't matter, we ignore it.
            Log.v("INFO", "Unable to unregister player listeners");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCheerleaderPlayer.destroy();
    }

    @Override
    public void onTogglePlayPressed() {
        mCheerleaderPlayer.togglePlayback();
    }

    @Override
    public void onPreviousPressed() {
        mCheerleaderPlayer.previous();
    }

    @Override
    public void onNextPressed() {
        mCheerleaderPlayer.next();
    }

    @Override
    public void onSeekToRequested(int milli) {
        mCheerleaderPlayer.seekTo(milli);
    }

    @Override
    public void onTrackAdded(TrackObject track) {
        if (mPlaylistTracks.isEmpty()) {
            mPlaylistRecyclerView.setVisibility(View.VISIBLE);
            mPlaylistRecyclerView.animate().translationY(0);
        }
        mPlaylistTracks.add(track);
        mPlaylistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTrackRemoved(TrackObject track, boolean isEmpty) {
        if (mPlaylistTracks.remove(track)) {
            mPlaylistAdapter.notifyDataSetChanged();
        }
        if (isEmpty) {
            mPlaylistRecyclerView.animate().translationY(mPlaybackView.getHeight());
            mPlaylistRecyclerView.setLayoutAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    mPlaylistRecyclerView.setVisibility(View.GONE);
                }
            });
        }
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:
            mRetrievedTracks.clear();
            mEndlessRecyclerOnScrollListener.reset();
            loadTracks(0);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    /**
     * Used to position the track list at the bottom of the screen.
     */
    private void setTrackListPadding() {
        mPlaylistRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPlaylistRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                int headerListHeight = getResources().getDimensionPixelOffset(R.dimen.playback_view_height);
                mPlaylistRecyclerView.setPadding(0, mPlaylistRecyclerView.getHeight() - headerListHeight, 0, 0);
                mPlaylistRecyclerView.setAdapter(mPlaylistAdapter);

                // hide if current play playlist is empty.
                if (mPlaylistTracks.isEmpty()) {
                    mPlaylistRecyclerView.setVisibility(View.GONE);
                    mPlaylistRecyclerView.setTranslationY(headerListHeight);
                }

                return true;
            }
        });
    }

    /**
     * Used to retrieved the tracks of the artist as well as artist details.
     */
    private void loadTracks(int page) {

        final int from = PER_PAGE * page;

        mAdapter.setFooterView(LayoutInflater.from(mAct)
                .inflate(R.layout.listview_footer, mPlaylistRecyclerView, false));
        mAdapter.notifyDataSetChanged();

        final long idToLoad = Long.parseLong(getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0]);
        boolean isPlaylist = false;
        if (getArguments().getStringArray(MainActivity.FRAGMENT_DATA).length > 1
                && getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[1].equals("playlist")){
            isPlaylist = true;
        }
        final boolean isPlaylistFinal = isPlaylist;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                SoundCloudClient api = new SoundCloudClient(getResources().getString(R.string.soundcloud_id));
                final List<TrackObject> tracks;
                if (!isPlaylistFinal) {
                    tracks = api.getListTrackObjectsOfUser(idToLoad, from, PER_PAGE);
                } else {
                    tracks = api.getListTrackObjectsOfPlaylist(idToLoad, from, PER_PAGE);
                }

                mAct.runOnUiThread(new Runnable() {
                    public void run() {

                        mAdapter.setFooterView(null);

                        if (tracks != null) {
                            mAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
                            if (tracks.size() > 0)
                                mRetrievedTracks.addAll(tracks);
                        } else {
                            Helper.noConnection(mAct);
                            mAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
                        }

                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

    }

    private void showTrackActionsPopup(final TrackObject track, View source){
        final PopupMenu popmenu = new PopupMenu(mAct, source);
        popmenu.getMenuInflater().inflate(R.menu.soundcloud_menu, popmenu.getMenu());

        popmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId()) {
                    case R.id.menu_download:
                        Helper.download(mAct, track.getLinkStream());
                        return true;
                    case R.id.menu_share:
                        Intent share = new Intent(android.content.Intent.ACTION_SEND);
                        share.setType("text/plain");

                        // Add data to the intent, the receiving app will decide
                        share.putExtra(Intent.EXTRA_TEXT, track.getPermalinkUrl());

                        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_header)));

                        return true;
                    default:
                        return false;
                }
            }
        });

        popmenu.show();
    }

    private void initRetrieveTracksRecyclerView() {
        mRetrieveTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(TrackObject track) {
                if (mCheerleaderPlayer.getTracks().contains(track)) {
                    mCheerleaderPlayer.play(track);
                } else {
                    boolean playNow = !mCheerleaderPlayer.isPlaying();

                    mCheerleaderPlayer.addTrack(track, playNow);
                    mPlaylistAdapter.notifyDataSetChanged();

                    if (!playNow) {
                        Toast.makeText(mAct, getResources().getString(R.string.toast_track_added), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onMoreClicked(TrackObject track, View source) {
                showTrackActionsPopup(track, source);
            }

        };

        mRetrievedTracks = new ArrayList<>();
        mAdapter = new TracksAdapter(getContext(), mRetrieveTracksListener, mRetrievedTracks);
        mAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        mRetrieveTracksRecyclerView.setAdapter(mAdapter);

        LinearLayoutManager mRetrieveTracksLayoutManager = new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false);
        mRetrieveTracksRecyclerView.setLayoutManager(mRetrieveTracksLayoutManager);
        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mRetrieveTracksLayoutManager) {
            @Override
            public void onLoadMore(final int current_page) {
                mRetrieveTracksRecyclerView.post(new Runnable() {
                    public void run() {
                        loadTracks(current_page);
                    }
                });
            }
        };
        mRetrieveTracksRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    private void initPlaylistTracksRecyclerView() {
        mPlaylistTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(TrackObject track) {
                mCheerleaderPlayer.play(track);
            }

            @Override
            public void onMoreClicked(TrackObject track, View source) {
                showTrackActionsPopup(track, source);
            }
        };

        mPlaybackView = new PlaybackView(mAct);
        mPlaybackView.setListener(this);

        mPlaylistTracks = new ArrayList<>();
        mPlaylistAdapter = new TracksAdapter(getContext(), mPlaylistTracksListener, mPlaylistTracks);
        mPlaylistAdapter.setHeaderView(mPlaybackView);

        mPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false));

        //Swipe dismiss listner
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                    @Override
                    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        if (viewHolder.getAdapterPosition() == 0) return 0;
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }

                    @Override
                    public boolean onMove(
                            final RecyclerView recyclerView,
                            final RecyclerView.ViewHolder viewHolder,
                            final RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            final RecyclerView.ViewHolder viewHolder,
                            final int swipeDir) {
                        //Get the track based on the position (minus the header)
                        TrackObject track = mCheerleaderPlayer.getTracks().get(viewHolder.getAdapterPosition() - 1);
                        if (mCheerleaderPlayer.getTracks().contains(track)) {
                            mCheerleaderPlayer.removeTrack(mPlaylistTracks.indexOf(track));
                        }
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            // Get RecyclerView item from the ViewHolder
                            View itemView = viewHolder.itemView;

                            Paint p = new Paint();
                            if (dX < 0) {
                                p.setColor(ContextCompat.getColor(mAct, R.color.grey));
                                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                        (float) itemView.getRight(), (float) itemView.getBottom(), p);
                            }

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                simpleItemTouchCallback
        );
        itemTouchHelper.attachToRecyclerView(mPlaylistRecyclerView);
    }


    @Override
    public boolean handleBackPress() {
        //If the playlist view is expanded, make sure a backpress closes it
        if (mPlaybackView.getTop() < mPlaylistRecyclerView.getHeight() - mPlaybackView.getHeight() &&
                mPlaylistTracks.size() > 0) { //There should be at least one track in the playlistview for it to be visible at all
            mPlaylistRecyclerView.getLayoutManager().smoothScrollToPosition(mPlaylistRecyclerView, null, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsCollapse() {
        return false;
    }
}
