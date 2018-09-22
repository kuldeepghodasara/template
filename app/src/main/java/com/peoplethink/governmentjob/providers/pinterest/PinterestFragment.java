package com.peoplethink.governmentjob.providers.pinterest;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This activity is used to display a list of instagram photos
 */

public class PinterestFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener  {

	private ArrayList<Pin> pinList = null;
	private RecyclerView listView = null;
	private PinterestAdapter pinListAdapter = null;

	private Activity mAct;

	private RelativeLayout ll;

	String nextpageurl;

	String id;

	Boolean isLoading = false;

	private static String API_URL = "https://api.pinterest.com/v1/boards/";
	private static String API_URL_END = "/pins/?fields=id,original_link,note,image,media,attribution,created_at,creator(image,first_name),counts&limit=100&access_token=";

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		id = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

		listView = ll.findViewById(R.id.list);
		pinList = new ArrayList<>();
		pinListAdapter = new PinterestAdapter(getContext(), pinList, this);
		pinListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		listView.setAdapter(pinListAdapter);
		listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		refreshItems();
	}


	public void updateList(ArrayList<Pin> posts) {
		if (posts.size() > 0) {
			pinList.addAll(posts);
		}

		if (nextpageurl == null)
			pinListAdapter.setHasMore(false);

		pinListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);

	}

	@Override
	public void onMoreRequested() {
		if (!isLoading && nextpageurl != null) {
			new DownloadFilesTask(false).execute();
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, ArrayList<Pin>> {

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
				nextpageurl = (API_URL + id + API_URL_END  + getResources().getString(R.string.pinterest_access_token));
			}
		}

		@Override
		protected void onPostExecute(ArrayList<Pin> result) {
			if (null != result && result.size() > 0) {
				updateList(result);
			} else {
				Helper.noConnection(mAct);
				pinListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);

			}
			isLoading = false;
		}

		@Override
		protected ArrayList<Pin> doInBackground(String... params) {
			//Getting data from url and parsing JSON
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			return parseJson(json);
		}
	}

	public ArrayList<Pin> parseJson(JSONObject json) {
		ArrayList<Pin> result = new ArrayList<Pin>();
		try {
			if (json.getJSONObject("page").has("next") &&
                    json.getJSONObject("page").getString("next").contains("http"))
				nextpageurl = json.getJSONObject("page").getString("next");
			else
				nextpageurl = null;
			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 Pin pin = new Pin();
                 pin.id = photoJson.getString("id");
                 pin.type = photoJson.getJSONObject("media").getString("type");
                 pin.creatorName = photoJson.getJSONObject("creator").getString("first_name");
                 pin.creatorImageUrl = photoJson.getJSONObject("creator").getJSONObject("image").getJSONObject("60x60").getString("url");
                 pin.caption = photoJson.getString("note");

                 pin.imageUrl = photoJson.getJSONObject("image").getJSONObject("original").getString("url");

                 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                 pin.createdTime = format.parse(photoJson.getString("created_at"));
                 pin.repinCount = photoJson.getJSONObject("counts").getInt("saves");
                 pin.commentsCount = photoJson.getJSONObject("counts").getInt("comments");
                 
                 pin.link = photoJson.getString("original_link");
                 
                 if (pin.type.equals("video") && photoJson.getJSONObject("attribution").getString("url") != null) {
                     pin.videoUrl = photoJson.getJSONObject("attribution").getString("url");
                 }
                 
                 // Add to array list
                 result.add(pin);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
		return result;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);
	}

	public void refreshItems(){
		pinList.clear();
		pinListAdapter.setHasMore(true);
		pinListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
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
