package com.peoplethink.governmentjob.providers.woocommerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.woocommerce.model.RestAPI;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class CheckoutActivity extends AppCompatActivity {

    private static String COOKIE_LIST = "LIST";

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;

    public static void startActivity(Activity origin, List<Cookie> cookies){
        Intent intent = new Intent(origin, CheckoutActivity.class);
        Bundle b = new Bundle();

        ArrayList<String> cookieList = new ArrayList<String>();
        for (Cookie cookie: cookies){
            cookieList.add(cookie.toString());
        }
        b.putStringArrayList(CheckoutActivity.COOKIE_LIST, cookieList);
        intent.putExtras(b);
        origin.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_woocommerce_checkout);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_actionbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.checkout);

        final RestAPI api = new RestAPI(this);
        String baseUrl = api.getHost() + api.getCheckout();

        //This ensures that the shop cart (only) contains items from in-app cart.
        //If the user is logged in, he will remain logged in.
        ArrayList<String> cookieList = getIntent().getExtras().getStringArrayList(COOKIE_LIST);
        for (String cookie : cookieList){
            CookieManager.getInstance().setCookie(baseUrl, cookie);
        }

        mRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mRefreshLayout.setEnabled(false);
        mWebView = findViewById(R.id.webView);
        mWebView.loadUrl(baseUrl);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(api.getHost() + api.getCheckoutComplete())) {
                    findViewById(R.id.finished_view).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                }

                if (url.equals(api.getHost())){
                    finish();
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mRefreshLayout.setRefreshing(false);
                findViewById(R.id.loading_view).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
