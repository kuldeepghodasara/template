package com.peoplethink.governmentjob.providers.facebook;

import android.annotation.SuppressLint;
import android.app.Activity;
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
 * This fragment is used to display a list of facebook posts
 */

public class FacebookFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener {

	private RecyclerView listView = null;
	private ArrayList<FacebookItem> postsList;
	private FacebookAdapter postListAdapter = null;

	private Activity mAct;

	private RelativeLayout ll;

	String nextpageurl;
	String username;
	Boolean isLoading = false;

	private static String API_URL_BEGIN = "https://graph.facebook.com/";
	private static String API_URL_MIDDLE = "/posts/?access_token=";
	private static String API_URL_END = "&date_format=U&fields=comments.limit(50).summary(1),likes.limit(0).summary(1),from,picture,message,story,name,link,id,created_time,full_picture,source,type&limit=10";

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		username = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

		listView = ll.findViewById(R.id.list);
		postsList = new ArrayList<>();
		postListAdapter = new FacebookAdapter(getContext(), postsList, this);
		postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		listView.setAdapter(postListAdapter);
		listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		refreshItems();
	}


	public void updateList(ArrayList<FacebookItem> posts) {
		if (posts.size() > 0) {
			postsList.addAll(posts);
		}

		if (nextpageurl == null)
			postListAdapter.setHasMore(false);
		postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
	}

	@Override
	public void onMoreRequested() {
		if (!isLoading && nextpageurl != null) {
			new DownloadFilesTask(false).execute();
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, ArrayList<FacebookItem>> {

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
				nextpageurl = (API_URL_BEGIN + username + API_URL_MIDDLE  + getResources().getString(R.string.facebook_access_token) + API_URL_END);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<FacebookItem> posts) {
			
			if (null != posts && posts.size() > 0) {
				updateList(posts);
			} else if (posts == null){
				String token = getResources().getString(R.string.facebook_access_token);
				String message = null;
				if (token.equals("YOURFACEBOOKTOKENHERE") || token.equals("")){
					message = "Debug info: You have not entered a (valid) ACCESS token.";
				}
				Helper.noConnection(mAct, message);

				postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}

			isLoading = false;
		}

		@Override
		protected ArrayList<FacebookItem> doInBackground(String... params) {
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			return parseJson(json);
		}
	}


	public ArrayList<FacebookItem> parseJson(JSONObject json) {
		ArrayList<FacebookItem> postList = new ArrayList<FacebookItem>();
		try {
			if (json.has("paging") && json.getJSONObject("paging").has("next"))
				nextpageurl = json.getJSONObject("paging").getString("next");
			else
				nextpageurl = null;

			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
            	 try {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 FacebookItem post = new FacebookItem();
                 post.id = photoJson.getString("id");
                 post.type = photoJson.getString("type");
                 post.username = photoJson.getJSONObject("from").getString("name");
                 post.profilePhotoUrl = "https://graph.facebook.com/" + photoJson.getJSONObject("from").getString("id") + "/picture?type=large";
                 post.createdTime = new Date(photoJson.getLong("created_time") * 1000);
                 post.likesCount = photoJson.getJSONObject("likes").getJSONObject("summary").getInt("total_count");
                 if (photoJson.has("link"))
                	 post.link =  photoJson.getString("link");
                 else 
                	 post.link = "https://www.facebook.com/" + post.id;
                 
                 if (post.type.equals("video")) {
                     post.videoUrl = photoJson.getString("source");
                 }
                 
                 if (photoJson.has("message")){
                	 post.caption = photoJson.getString("message");
                 } else if (photoJson.has("story")){
                	 post.caption = photoJson.getString("story");
                 } else if (photoJson.has("name")){
                	 post.caption = photoJson.getString("name");
                 } else {
                	 post.caption = "";
                 }
                 
                 if (photoJson.has("full_picture")){
                	 post.imageUrl = photoJson.getString("full_picture");
                 }
                 
                 //post.captionUsername = photoJson.getJSONObject("caption").getJSONObject("from").getString("username");)
                	 
                 post.commentsCount = photoJson.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
                 post.commentsArray = photoJson.getJSONObject("comments").getJSONArray("data");

                 // Add to array list
                 postList.add(post);
            	 } catch (Exception e) {
         			Log.e("INFO", "Item " + i +" skipped because of exception");
         			Log.printStackTrace(e);
         		}
			}

			return postList;
		} catch (Exception e) {
			Log.printStackTrace(e);

			return null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);
	}

	public void refreshItems(){
		postsList.clear();
		postListAdapter.setHasMore(true);
		postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
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
