package com.peoplethink.governmentjob.providers.wordpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.peoplethink.governmentjob.providers.wordpress.PostItem;
import com.peoplethink.governmentjob.providers.wordpress.WordpressListAdapter;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressCategoriesLoader;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressGetTaskInfo;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressPostsLoader;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */

public class WordpressFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener {

    //Layout attributes
	private RecyclerView postList = null;
	private Activity mAct;
	private RelativeLayout ll;

    //Keeping track of the WP
    private WordpressGetTaskInfo mInfo;
    private String urlSession;

    //The arguments we started this fragment with
	private String[] arguments;
	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		arguments = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);

		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
									long id) {
				PostItem newsData = mInfo.posts.get(position);
				if (newsData.getPostType().equals(PostItem.PostType.SLIDER)) return;

				Intent intent = new Intent(mAct, WordpressDetailActivity.class);
				intent.putExtra(WordpressDetailActivity.EXTRA_POSTITEM, newsData);
				intent.putExtra(WordpressDetailActivity.EXTRA_API_BASE, arguments[0]);
				//If a disqus parse-able is provided, pass it to the detailActivity
				if (arguments.length > 2)
					intent.putExtra(WordpressDetailActivity.EXTRA_DISQUS, arguments[2]);

				startActivity(intent);
			}
		};

		postList = ll.findViewById(R.id.list);
		mInfo = new WordpressGetTaskInfo(postList, getActivity(), arguments[0], false);
		mInfo.adapter = new WordpressListAdapter(getContext(), mInfo.posts, this, listener, mInfo.simpleMode);
		postList.setAdapter(mInfo.adapter);
		postList.setLayoutManager(new LinearLayoutManager(ll.getContext(), LinearLayoutManager.VERTICAL, false));

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		getPosts();
	}

	public void getPosts() {
		if (arguments.length > 1 && !arguments[1].equals("")) {
            //Load category posts
			urlSession = WordpressPostsLoader.getCategoryPosts(mInfo, arguments[1]);
		} else {
            //Load recent posts
            urlSession = WordpressPostsLoader.getRecentPosts(mInfo);
            //Load category bubbles
            new WordpressCategoriesLoader(mInfo).load();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);

		// set & get the search button in the actionbar
		final SearchView searchView = new SearchView(mAct);
		searchView.setQueryHint(getResources().getString(
				R.string.search_hint));
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			//
			@Override
			public boolean onQueryTextSubmit(String query) {
				try {
					query = URLEncoder.encode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					Log.printStackTrace(e);
				}
				searchView.clearFocus();

				urlSession = WordpressPostsLoader.getSearchPosts(mInfo, query);

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}

		});

		searchView .addOnAttachStateChangeListener(
				new OnAttachStateChangeListener() {

					@Override
					public void onViewDetachedFromWindow(View arg0) {
						if (!mInfo.isLoading) {
							getPosts();
						}
					}

					@Override
					public void onViewAttachedToWindow(View arg0) {
						// search was opened
					}
				});

		//TODO make menu an xml item
		menu.add(R.string.search)
				.setIcon(R.drawable.ic_action_search)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:
			if (!mInfo.isLoading) {
				getPosts();
			} else {
				Toast.makeText(mAct, getString(R.string.already_loading),
						Toast.LENGTH_LONG).show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMoreRequested() {
		if (!mInfo.isLoading && mInfo.curpage < mInfo.pages) {
			//Load more and remember the position
			WordpressPostsLoader.loadMorePosts(mInfo, urlSession);
		}
	}
}
