package com.peoplethink.governmentjob.providers.woocommerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.fav.ui.FavFragment;
import com.peoplethink.governmentjob.providers.woocommerce.WooCommerceTask;
import com.peoplethink.governmentjob.providers.woocommerce.adapter.OrdersAdapter;
import com.peoplethink.governmentjob.providers.woocommerce.model.CredentialStorage;
import com.peoplethink.governmentjob.providers.woocommerce.model.orders.Order;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 *         Copyright 2017
 */
public class OrdersFragment extends Fragment implements WooCommerceTask.Callback<Order>, InfiniteRecyclerViewAdapter.LoadMoreListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrdersAdapter ordersAdapter;
    private List<Order> ordersList;
    private Activity mAct;

    private boolean awaitingLogin = false;

    private int page = 1;

    public OrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wc, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycleView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        ordersList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(getContext(), ordersList, this);
        ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        recyclerView.setAdapter(ordersAdapter);

        mAct = getActivity();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mAct);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (CredentialStorage.credentialsAvailable(mAct)) {
            refreshItems();
            loadHeader();
        } else {
            ordersAdapter.setEmptyViewText(
                    getString(R.string.no_user),
                    getString(R.string.no_user_subtitle));
            ordersAdapter.setEmptyViewButton(getResources().getString(R.string.common_signin_button_text), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(mAct, WooCommerceLoginActivity.class));
                    awaitingLogin = true;
                }
            });
            ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    private void loadHeader() {
        LayoutInflater layoutInflater = LayoutInflater.from(mAct);

        ViewGroup headerView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_order_header, null);
        TextView text = headerView.findViewById(R.id.order_header_text);
        View signOut = headerView.findViewById(R.id.user_sign_out_button);

        text.setText(String.format(getString(R.string.greeting), CredentialStorage.getName(mAct)));
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CredentialStorage.clearCredentials(mAct);
                Toast.makeText(mAct, R.string.action_sign_out_success, Toast.LENGTH_SHORT).show();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(OrdersFragment.this).attach(OrdersFragment.this).commit();
            }
        });

        ordersAdapter.setHeader(headerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (awaitingLogin) {
            awaitingLogin = false;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }
    }

    @Override
    public void success(ArrayList<Order> result) {
        if (result.size() > 0) {
            ordersList.addAll(result);
            ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
        } else if (result.size() == 0 && ordersList.size() == 0) {
            ordersAdapter.setEmptyViewText(
                    String.format(getString(R.string.greeting), CredentialStorage.getName(mAct)),
                    getString(R.string.no_order));
            ordersAdapter.setEmptyViewButton(getString(R.string.shop), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HolderActivity.startActivity(mAct, WooCommerceFragment.class, new String[]{});
                }
            });
            ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
        } else {
            ordersAdapter.setHasMore(false);
            ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void failed() {
        ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshItems() {
        // Load items
        page = 1;
        ordersList.clear();
        ordersAdapter.setHasMore(true);
        ordersAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        requestItems();
    }

    @Override
    public void onMoreRequested() {
        page = page + 1;
        requestItems();
    }

    private void requestItems() {
        WooCommerceTask.WooCommerceBuilder builder = new WooCommerceTask.WooCommerceBuilder(mAct);
        builder.getOrders(this, CredentialStorage.getId(mAct), page).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.woocommerce_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_cart:
                HolderActivity.startActivity(getActivity(), CartFragment.class, null);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}