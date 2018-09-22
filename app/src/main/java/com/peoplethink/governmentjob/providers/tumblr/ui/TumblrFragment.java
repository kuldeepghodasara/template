package com.peoplethink.governmentjob.providers.tumblr.ui;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.peoplethink.governmentjob.inherit.PermissionsFragment;
import com.peoplethink.governmentjob.providers.tumblr.ImageAdapter;
import com.peoplethink.governmentjob.providers.tumblr.TumblrItem;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.Log;
import com.peoplethink.governmentjob.util.layout.StaggeredGridSpacingItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *  This activity is used to display a list of tumblr imagess
 */

public class TumblrFragment extends Fragment implements PermissionsFragment, InfiniteRecyclerViewAdapter.LoadMoreListener {

    private RecyclerView listView;
    ArrayList<TumblrItem> tumblrItems;
	private ImageAdapter imageAdapter = null;
	
	private Activity mAct;
	private RelativeLayout ll;

	String perpage = "25";
	Integer curpage = 0;
	Integer total_posts;
	
	String baseurl;
	
	Boolean isLoading = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list, container, false);
        return ll;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

		String username = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		baseurl = "https://"+username+".tumblr.com/api/read/json?type=photo&num=" + perpage + "&start=";

		listView = ll.findViewById(R.id.list);
        tumblrItems = new ArrayList<>();
        imageAdapter = new ImageAdapter(getContext(), tumblrItems, this);
        imageAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        listView.setAdapter(imageAdapter);

        //TODO dynamically change grid span count
        listView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addItemDecoration(new StaggeredGridSpacingItemDecoration((int) getResources().getDimension(R.dimen.woocommerce_padding), true));
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		
        refreshItems();
	}
	
	public void updateList(ArrayList<TumblrItem> result) {
        if (result.size() > 0)
            tumblrItems.addAll(result);

        if ((curpage * Integer.parseInt(perpage)) > total_posts || result.size() == 0)
            imageAdapter.setHasMore(false);

        imageAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
    }

	@Override
	public String[] requiredPermissions() {
		return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
	}

    @Override
    public void onMoreRequested() {
        if (!isLoading && (curpage * Integer.parseInt(perpage)) <= total_posts) {
            // It is time to add new data. We call the listener
            isLoading = true;
            new InitialLoadGridView().execute(baseurl);
        }
    }

    void refreshItems(){
        isLoading = true;
        curpage = 0;
        tumblrItems.clear();
        imageAdapter.setHasMore(true);
        imageAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        new InitialLoadGridView().execute(baseurl);
    }

    private class InitialLoadGridView extends AsyncTask<String, Void, ArrayList<TumblrItem>> {

		@Override
		protected ArrayList<TumblrItem> doInBackground(String... params) {
			String geturl = params[0];
			geturl = geturl + Integer.toString((curpage) *  Integer.parseInt(perpage));
            curpage = curpage + 1;

			String jsonString = Helper.getDataFromUrl(geturl);

			System.out.println("Return: " + jsonString);
			JSONObject json= null;
			// try parse the string to a JSON object
			try {
				jsonString = jsonString.replace("var tumblr_api_read = ", "");
				//Log.v("INFO", json);
				json = new JSONObject(jsonString);
			} catch (JSONException e) {
				Log.printStackTrace(e);
			}
			
			ArrayList<TumblrItem> images = null;

			try {
				// Checking for SUCCESS TAG
				String success = json.getString("posts-total");
				total_posts = Integer.parseInt(success);

				if (0 < Integer.parseInt(success)) {
					// products found
					// Getting Array of Products
					JSONArray products;
					
					products = json.getJSONArray("posts");
                    images = new ArrayList<TumblrItem>();

					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString("id");
						String link = c.getString("url");
						String url;
						try {
						   url = c.getString("photo-url-1280");					
						} catch (JSONException e){
						   try {
								url = c.getString("photo-url-500");					
						   } catch (JSONException r){
							   try {
									url = c.getString("photo-url-250");					
							   } catch (JSONException l){
										url = null;
							   }
						   }
						}

						// creating new HashMap
						//HashMap<String, String> map = new HashMap<String, String>();

						// adding items to arraylist
						if (url != null){
							TumblrItem item = new TumblrItem(id, link, url);
							images.add(item);
						}
					}
				} else {
					Log.v("INFO", "No items found");
				}
			} catch (JSONException e) {
				Log.printStackTrace(e);
			} catch (NullPointerException e) {
				Log.printStackTrace(e);
			}
			
			return images;
		}
		
        @Override
		protected void onPostExecute(ArrayList<TumblrItem> results) {
			if (null != results) {
				updateList(results);
			} else {
				Helper.noConnection(mAct);
                imageAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}
			isLoading = false;
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
        	if (!isLoading){
        		refreshItems();
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
}