package com.peoplethink.governmentjob.providers.twitter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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
import com.peoplethink.governmentjob.providers.twitter.Tweet;
import com.peoplethink.governmentjob.providers.twitter.TweetAdapter;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *  This activity is used to display a list of tweets
 */

public class TweetsFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener {

	private RecyclerView listView;
	private TweetAdapter tweetAdapter;
	private ArrayList<Tweet> tweets;
	
	private Activity mAct;
	private RelativeLayout ll;
	
	String searchValue;
	String latesttweetid;
	String perpage = "15";

	Boolean isLoading = true;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list, container, false);
		setHasOptionsMenu(true);
		
		searchValue =  this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		listView = ll.findViewById(R.id.list);

		listView = ll.findViewById(R.id.list);
		tweets = new ArrayList<>();
		tweetAdapter = new TweetAdapter(getContext(), tweets, this);
		tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		listView.setAdapter(tweetAdapter);
		listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

	    return ll;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		refreshItems();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.refresh:
	    	if (!isLoading){
	    		refreshItems();
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	public void refreshItems(){
		isLoading = true;
		latesttweetid = null;

		tweets.clear();
		tweetAdapter.setHasMore(true);
		tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		new SearchTweetsTask().execute(searchValue);
	}
	
	public void updateList(ArrayList<Tweet> result) {
		if (result.size() > 0) {
			tweets.addAll(result);
		}

		if (result.size() == 0)
			tweetAdapter.setHasMore(false);
		tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
	}

	@Override
	public void onMoreRequested() {
		if (!isLoading){
			new SearchTweetsTask().execute(searchValue);
		}
	}

	//Connect to twitter api and get values.
	private class SearchTweetsTask extends AsyncTask<String, Void, ArrayList<Tweet>>{

		private String URL_VALUE;
		private final String URL_BASE = "https://api.twitter.com";
		private final String URL_TIMELINE = URL_BASE + "/1.1/statuses/user_timeline.json?count="+perpage+"&screen_name=";
		private final String URL_SEARCH = URL_BASE + "/1.1/search/tweets.json?count="+perpage+"&q=";
		private final String URL_PARAM = "&max_id=";
		private final String URL_AUTH = URL_BASE + "/oauth2/token";

		private final String CONSUMER_KEY = getResources().getString(R.string.twitter_api_consumer_key);
		private final String CONSUMER_SECRET = getResources().getString(R.string.twitter_api_consumer_secret_key);

		private String authenticateApp(){

			HttpURLConnection connection = null;
			OutputStream os = null;
			BufferedReader br = null;
			StringBuilder reply = null;

			try {
				URL url = new URL(URL_AUTH);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);

				// Encoding keys
				String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
				String authorisation = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
				String parameter = "grant_type=client_credentials";

				// Sending credentials
				connection.addRequestProperty("Authorization", authorisation);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				connection.connect();
				
				// sending parameters to method
				os = connection.getOutputStream();
				os.write(parameter.getBytes());
				os.flush();
				os.close();

				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				reply = new StringBuilder();

				while ((line = br.readLine()) != null){            
					reply.append(line);	
				}

				Log.d("Post response", String.valueOf(connection.getResponseCode()));
				Log.d("Json response - tokenk", reply.toString());

			} catch (Exception e) {
				Log.e("INFO", "Exception: " + e.toString());
				
			}finally{
				if (connection != null) {
					connection.disconnect();
				}
			}
			if (reply == null) return null;
			return reply.toString();
		}
		

		//Showing the progressdialog while loading data in background
		@Override
		protected void onPreExecute(){
			super.onPreExecute();

			isLoading = true;
		}
		


		//Get the latest tweets from the timeline of the user
		@Override
		protected ArrayList<Tweet> doInBackground(String... param) {

			String searchValue = param[0];
			Boolean search = false;
			if (searchValue.startsWith("?")){
				URL_VALUE = URL_SEARCH;
				try {
					searchValue = URLEncoder.encode(searchValue.substring(1), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					Log.printStackTrace(e);
				}
				search = true;
			} else {
				URL_VALUE = URL_TIMELINE;
			}
			ArrayList<Tweet> result = null;
			HttpURLConnection connection = null;
			BufferedReader br = null;

			try {
				URL url;
				if (null != latesttweetid && !latesttweetid.equals("")) {
					Long fromid = Long.parseLong(latesttweetid) - 1;
					url = new URL(URL_VALUE + searchValue + URL_PARAM + Long.toString(fromid));
				}else {
					url = new URL(URL_VALUE + searchValue);
				}
				Log.v("INFO", "Requesting: " + url.toString());
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				String jsonString = authenticateApp();
				JSONObject jsonAccess = new JSONObject(jsonString);
				String tokenHolder = jsonAccess.getString("token_type") + " " + 
						jsonAccess.getString("access_token");

				connection.setRequestProperty("Authorization", tokenHolder);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.connect();

				// retrieve tweets from api
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String line;
				StringBuilder reply = new StringBuilder();

				while ((line = br.readLine()) != null){            
					reply.append(line);	
				}
				
				Log.d("GET response", String.valueOf(connection.getResponseCode()));
				Log.d("JSON response", reply.toString());
				
				JSONArray jsonArray;
				JSONObject jsonObject;
				
				if (search){
					JSONObject obj = new JSONObject(reply.toString());
					jsonArray = obj.getJSONArray("statuses");
				} else {	
					jsonArray = new JSONArray(reply.toString());
				}

				result = new ArrayList<Tweet>();

				for (int i = 0; i < jsonArray.length(); i++) {
					
					jsonObject = (JSONObject) jsonArray.get(i);
					Tweet tweet = new Tweet();

					tweet.setname(jsonObject.getJSONObject("user").getString("name"));
					tweet.setusername(jsonObject.getJSONObject("user").getString("screen_name"));
					tweet.seturlProfileImage(jsonObject.getJSONObject("user").getString("profile_image_url"));
					tweet.setmessage(jsonObject.getString("text"));
					tweet.setRetweetCount(jsonObject.getInt("retweet_count"));
					tweet.setData(jsonObject.getString("created_at"));
					tweet.setTweetId(jsonObject.getString("id"));
					
					try {
						if (jsonObject.has("extended_entities")){
							String mediaurl = ((JSONObject) jsonObject.getJSONObject("extended_entities").getJSONArray("media").get(0)).getString("media_url");
							if (((JSONObject) jsonObject.getJSONObject("extended_entities").getJSONArray("media").get(0)).getString("type").equalsIgnoreCase("photo"))
								tweet.setImageUrl(mediaurl);
						}
					} catch (JSONException e){
						Log.printStackTrace(e);
					}
					
					
					latesttweetid = jsonObject.getString("id");

					result.add(i, tweet);
				}

			} catch (Exception e) {
				Log.printStackTrace(e);
				Log.e("INFO", "Exception: GET " + e.toString());

			}finally {
				if(connection != null){
					connection.disconnect();
				}
			}
			return result;
		}

		//Populate listview with tweets after background task has been completed. If results are empty
		//then show error toast.
		@Override
		protected void onPostExecute(ArrayList<Tweet> result){
			isLoading = false;
			if (null != result) {
				updateList(result);
			} else {
				Helper.noConnection(mAct);
				tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}
		}

	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.refresh_menu, menu);
	}
	

}
