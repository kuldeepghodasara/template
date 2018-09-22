package com.peoplethink.governmentjob.providers.instagram;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * This activity is used to display a list of instagram photos
 */

public class InstagramFragment extends Fragment  implements InfiniteRecyclerViewAdapter.LoadMoreListener {

	private RecyclerView photosListView = null;
	private ArrayList<InstagramPhoto> photosList;
	private InstagramPhotosAdapter photosListAdapter = null;

	private Activity mAct;
	private RelativeLayout ll;

	private String nextpageurl;
	String username;
	Boolean isLoading = false;

	private static String API_URL = "https://api.instagram.com/v1/users/";
	private static String API_URL_END = "/media/recent?access_token=";

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		username = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

		photosListView = ll.findViewById(R.id.list);
		photosList = new ArrayList<>();
		photosListAdapter = new InstagramPhotosAdapter(getContext(), photosList, this);
		photosListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		photosListView.setAdapter(photosListAdapter);
		photosListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		refreshItems();
	}


	public void updateList(ArrayList<InstagramPhoto> photosList) {
		if (photosList.size() > 0) {
			this.photosList.addAll(photosList);
		}

		if (nextpageurl == null || photosList.size() == 0)
			photosListAdapter.setHasMore(false);
		photosListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);

	}

	@Override
	public void onMoreRequested() {
		if (!isLoading && nextpageurl != null) {
			new DownloadFilesTask(false).execute();
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, ArrayList<InstagramPhoto>> {

		boolean initialload;

		DownloadFilesTask(boolean firstload) {
			this.initialload = firstload;
		}

		@Override
		protected void onPreExecute() {
			if (isLoading) {
				this.cancel(true);
			} else {
				isLoading = true;
			}
			if (initialload) {
				nextpageurl = (API_URL + username + API_URL_END  + getResources().getString(R.string.instagram_access_token));
			}
		}

		@Override
		protected void onPostExecute(ArrayList<InstagramPhoto> result) {
			if (null != result && result.size() > 0) {
				updateList(result);
			} else {
				Helper.noConnection(mAct);
				photosListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}

			isLoading = false;
		}

		@Override
		protected ArrayList<InstagramPhoto> doInBackground(String... params) {
			//Getting data from url and parsing JSON
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			return parseJson(json);
		}
	}

	public ArrayList<InstagramPhoto> parseJson(JSONObject json) {
		ArrayList<InstagramPhoto> photosList = new ArrayList<>();

		try {
			if (json.has("pagination") && json.getJSONObject("pagination").has("next_url")) {
				nextpageurl = json.getJSONObject("pagination").getString("next_url");
			} else {
				nextpageurl = null;
			}

			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 InstagramPhoto photo = new InstagramPhoto();
                 photo.id = photoJson.getString("id");
                 photo.type = photoJson.getString("type");
                 photo.username = photoJson.getJSONObject("user").getString("username");
                 photo.profilePhotoUrl = photoJson.getJSONObject("user").getString("profile_picture");
                 if (photoJson.has("caption") && !photoJson.isNull("caption")){
                	 photo.caption = photoJson.getJSONObject("caption").getString("text");
                 }
                 photo.imageUrl = photoJson.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                 photo.createdTime = new Date(photoJson.getLong("created_time") * 1000);
                 photo.likesCount = photoJson.getJSONObject("likes").getInt("count");
                 photo.link = photoJson.getString("link");
				 photo.commentsJson = photoJson.getJSONObject("comments").toString();
                 
                 if (photo.type.equals("video")) {
                     photo.videoUrl = photoJson.getJSONObject("videos").getJSONObject("standard_resolution").getString("url");
                 }

                 photo.commentsCount = photoJson.getJSONObject("comments").getInt("count");

                 // Add to array list
                 photosList.add(photo);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
		}

        return photosList;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);
	}

	public void refreshItems(){
		photosList.clear();
		photosListAdapter.setHasMore(true);
		photosListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		new DownloadFilesTask(true).execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:
			if (!isLoading) {
				refreshItems();
			} else {
				Toast.makeText(mAct, getString(R.string.already_loading),
						Toast.LENGTH_LONG).show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
