package com.peoplethink.governmentjob.providers.overview.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.drawer.NavItem;
import com.peoplethink.governmentjob.providers.overview.CategoryAdapter;
import com.peoplethink.governmentjob.providers.overview.OverviewParser;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class OverviewFragment extends Fragment implements CategoryAdapter.OnOverViewClick {

    //Views
    private RelativeLayout rl;
    private RecyclerView mRecyclerView;
    private ArrayList<NavItem> items;

    private String overviewString;
    private DividerItemDecoration horizontalDec;

    //List
    private CategoryAdapter multipleItemAdapter;

    private ViewTreeObserver.OnGlobalLayoutListener recyclerListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rl = (RelativeLayout) inflater.inflate(R.layout.fragment_list,null);
        setHasOptionsMenu(true);
        mRecyclerView = rl.findViewById(R.id.list);

        final StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        items = new ArrayList<>();
        multipleItemAdapter = new CategoryAdapter(items, getContext(), OverviewFragment.this);
        mRecyclerView.setAdapter(multipleItemAdapter);
        multipleItemAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);

        overviewString = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

        recyclerListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Get the view width, and check if it could be valid
                int viewWidth = mRecyclerView.getMeasuredWidth();
                if (viewWidth <= 0 ) return;

                //Remove the VTO
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                //Calculate and update the span
                float cardViewWidth = getResources().getDimension(R.dimen.card_width_overview);
                int newSpanCount = Math.max(1, (int) Math.floor(viewWidth / cardViewWidth));
                mLayoutManager.setSpanCount(newSpanCount);
                mLayoutManager.requestLayout();

                if (newSpanCount > 1){
                    mRecyclerView.addItemDecoration(horizontalDec);
                } else {
                    mRecyclerView.removeItemDecoration(horizontalDec);
                }
            }
        };
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(recyclerListener);

        horizontalDec = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.HORIZONTAL);

        DividerItemDecoration verticalDec = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(verticalDec);


        //Load items
        loadItems();

        return rl;
    }

    public void loadItems() {
        new OverviewParser(overviewString, getActivity(), new OverviewParser.CallBack() {
            @Override
            public void categoriesLoaded(ArrayList<NavItem> result, boolean failed) {
                if (failed) {
                    //If it failed; show an error if we're using a local file, or if we are online & using a remote overview.
                    if (!overviewString.contains("http") || Helper.isOnlineShowDialog(getActivity())) {
                        Toast.makeText(getActivity(), R.string.invalid_configuration, Toast.LENGTH_LONG).show();
                        multipleItemAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
                    }
                } else {
                    //Add all the new posts to the list and notify the adapter
                    items.addAll(result);
                    multipleItemAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
                }
            }
        }).execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(recyclerListener);
    }

    @Override
    public void onOverViewSelected(NavItem item) {
        HolderActivity.startActivity(getActivity(), item.getFragment(), item.getData());
    }
}
