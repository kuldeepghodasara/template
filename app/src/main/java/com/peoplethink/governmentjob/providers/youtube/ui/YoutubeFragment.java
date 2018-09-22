package com.peoplethink.governmentjob.providers.youtube.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.youtube.VideosAdapter;
import com.peoplethink.governmentjob.providers.youtube.api.RetrieveVideos;
import com.peoplethink.governmentjob.providers.youtube.api.object.ReturnItem;
import com.peoplethink.governmentjob.providers.youtube.api.object.Video;
import com.peoplethink.governmentjob.providers.youtube.player.YouTubePlayerActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * This activity is used to display a list of vidoes
 */
public class YoutubeFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener {
    //Layout references
    private RecyclerView listView;
    private RelativeLayout ll;
    private Activity mAct;

    //Stores information
    private ArrayList<Video> videoList;
    private VideosAdapter videoAdapter;
    private RetrieveVideos videoApiClient;

    //Keeping track of location & status
    private String upcomingPageToken;
    private boolean isLoading = true;
    private String currentType;
    private String searchQuery;

    private static String TYPE_PLAYLIST = "playlist";
    private static String TYPE_USER = "channel";
    private static String TYPE_LIVE = "live";

    private static String TYPE_SEARCH = "search";

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list, container, false);

        return ll;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        OnItemClickListener listener = (new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = videoList.get(position);
                Video video = (Video) o;

                if (currentType.equals(TYPE_LIVE)) {
                    Intent intent = new Intent(mAct,
                            YouTubePlayerActivity.class);
                    intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, video.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mAct, YoutubeDetailActivity.class);
                    intent.putExtra(YoutubeDetailActivity.EXTRA_VIDEO, video);
                    startActivity(intent);
                }
            }
        });

        listView = ll.findViewById(R.id.list);
        videoList = new ArrayList<>();
        videoAdapter = new VideosAdapter(getContext(), videoList, this, listener);
        videoAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        listView.setAdapter(videoAdapter);
        listView.setLayoutManager(new LinearLayoutManager(ll.getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        String apiKey = getResources().getString(R.string.google_server_key);
        videoApiClient = new RetrieveVideos(mAct, apiKey);

        //Set the default type
        currentType = getTypeBasedOnParameters();

        //Load the youtube videos
        refreshItems();
    }

    private void refreshItems(){
        videoList.clear();
        videoAdapter.setHasMore(true);
        videoAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        loadVideosInList(null);
    }

    public void updateList(ArrayList<Video> videos){
        if (videos.size() > 0) {
            videoList.addAll(videos);
        }

        if (upcomingPageToken == null || videos.size() == 0)
            videoAdapter.setHasMore(false);

        videoAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
    }

    //@param nextPageToken the token of the page to load, null if the first page
    //@param param the username or query
    //@param retrievaltype the type of retrieval to do, either TYPE_SEARCH, TYPE_PLAYLIST or TYPE_LIVE
    private void loadVideosInList(final String nextPageToken) {
        isLoading = true;

        if (nextPageToken == null) {
            upcomingPageToken = null;
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ReturnItem result = null;
                if (currentType.equals(TYPE_SEARCH)) {
                    result = videoApiClient.getSearchVideos(searchQuery, getIdBasedOnParameters(), nextPageToken);
                } else if (currentType.equals(TYPE_PLAYLIST)) {
                    result = videoApiClient.getPlaylistVideos(getIdBasedOnParameters(), nextPageToken);
                } else if (currentType.equals(TYPE_LIVE)) {
                    result = videoApiClient.getLiveVideos(getIdBasedOnParameters(), nextPageToken);
                } else if (currentType.equals(TYPE_USER)) {
                    result = videoApiClient.getUserVideos(getIdBasedOnParameters(), nextPageToken);
                }

                final ArrayList<Video> videos = result.getList();
                upcomingPageToken = result.getPageToken();

                mAct.runOnUiThread(new Runnable() {
                    public void run() {

                        isLoading = false;
                        
                    	if (!isAdded()) return;

                        if (videos != null) {
                            updateList(videos);

                            if (currentType.equals(TYPE_LIVE)) {
                                if (videos.size() > 0) {
                                    LayoutInflater.from(mAct).inflate(R.layout.fragment_youtube_livefooter, ll);
                                    View liveBottomView = ll.findViewById(R.id.youtube_live_bottom);
                                    if (videos.size() == 1) {
                                        liveBottomView.setVisibility(View.VISIBLE);
                                    } else if (liveBottomView.getVisibility() == View.VISIBLE) {
                                        liveBottomView.setVisibility(View.GONE);
                                    }
                                } else {
                                    //Emptyview set title
                                    videoAdapter.setEmptyViewText(
                                            getString(R.string.video_no_live_title),
                                            getString(R.string.video_no_live));
                                    videoAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
                                }
                            }

                        } else {
                            Helper.noConnection(mAct);
                            videoAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
                        }
                    }
                });

            }
        });

    }

    private String[] getPassedData() {
        return getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);

        //set & get the search button in the actionbar
        final SearchView searchView = new SearchView(mAct);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                currentType = TYPE_SEARCH;
                refreshItems();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        String[] parts = getPassedData();

        searchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View arg0) {
                if (!isLoading) {
                    currentType = getTypeBasedOnParameters();
                    searchQuery = null;
                    refreshItems();
                }
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                // search was opened
            }
        });


        if (!getTypeBasedOnParameters().equals(TYPE_PLAYLIST)) {
            menu.add(R.string.search)
                    .setIcon(R.drawable.ic_action_search)
                    .setActionView(searchView)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        //TODO make menu an xml item
    }

    public String getTypeBasedOnParameters() {
        if (getPassedData().length < 2 || (
                !getPassedData()[1].equals(TYPE_LIVE) &&
                        !getPassedData()[1].equals(TYPE_USER) &&
                        !getPassedData()[1].equals(TYPE_PLAYLIST))) {
            throw new RuntimeException("Your youtube configuration is incorrect, please check your documentation");
        }

        return getPassedData()[1];
    }

    public String getIdBasedOnParameters() {
        if (getPassedData().length < 2) {
            throw new RuntimeException("Your youtube configuration is incorrect, please check your documentation");
        }

        return getPassedData()[0];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh:
                if (!isLoading) {
                    refreshItems();
                } else {
                    Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMoreRequested() {
        if (null != upcomingPageToken && !isLoading) {
            loadVideosInList(upcomingPageToken);
        }
    }
}