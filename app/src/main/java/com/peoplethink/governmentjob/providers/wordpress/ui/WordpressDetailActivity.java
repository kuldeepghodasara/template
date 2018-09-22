package com.peoplethink.governmentjob.providers.wordpress.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.attachmentviewer.model.MediaAttachment;
import com.peoplethink.governmentjob.attachmentviewer.ui.AttachmentActivity;
import com.peoplethink.governmentjob.comments.CommentsActivity;
import com.peoplethink.governmentjob.providers.fav.FavDbAdapter;
import com.peoplethink.governmentjob.providers.wordpress.PostItem;
import com.peoplethink.governmentjob.providers.wordpress.WordpressListAdapter;
import com.peoplethink.governmentjob.providers.wordpress.api.JsonApiPostLoader;
import com.peoplethink.governmentjob.providers.wordpress.api.RestApiPostLoader;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressGetTaskInfo;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressPostsLoader;
import com.peoplethink.governmentjob.providers.wordpress.api.providers.JetPackProvider;
import com.peoplethink.governmentjob.providers.wordpress.api.providers.RestApiProvider;
import com.peoplethink.governmentjob.util.DetailActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;
import com.peoplethink.governmentjob.util.WebHelper;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */

public class WordpressDetailActivity extends DetailActivity implements JsonApiPostLoader.BackgroundPostCompleterListener {

    //By default, we remove the first image, however, you can disable this
    private static final boolean REMOVE_FIRST_IMG = true;
    //Preload all posts for faster loading, increases API requests
    public static final boolean PRELOAD_POSTS = true;

    //Utilties
    private FavDbAdapter mDbHelper;
    private WebView htmlTextView;
    private TextView mTitle;

    //Extra's
    public static final String EXTRA_POSTITEM = "postitem";
    public static final String EXTRA_API_BASE = "apiurl";
    public static final String EXTRA_DISQUS = "disqus";

    //Post information
    private PostItem post;
    private String disqusParseable;
    private String apiBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Use the general detaillayout and set the viewstub for wordpress
        setContentView(R.layout.activity_details);
        ViewStub stub = findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_wordpress_details);
        View inflated = stub.inflate();

        mToolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Views
        thumb = findViewById(R.id.image);
        coolblue = findViewById(R.id.coolblue);
        mTitle = findViewById(R.id.title);
        TextView mDateAuthorView = findViewById(R.id.dateauthorview);

        //Extras
        Bundle bundle = this.getIntent().getExtras();
        post = (PostItem) getIntent().getSerializableExtra(EXTRA_POSTITEM);
        disqusParseable = getIntent().getStringExtra(EXTRA_DISQUS);
        apiBase = getIntent().getStringExtra(EXTRA_API_BASE);

        //If we have a post and a bundle
        if (null != post && null != bundle) {

            String dateAuthorString;
            if (post.getDate() != null)
                dateAuthorString = getResources().getString(R.string.wordpress_subtitle_start) +
                        DateUtils.getRelativeDateTimeString(this, post.getDate().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
                        + getResources().getString(R.string.wordpress_subtitle_end)
                        + post.getAuthor();
            else
                dateAuthorString = post.getAuthor();

            mTitle.setText(post.getTitle());
            mDateAuthorView.setText(dateAuthorString);

            loadHeaderImage();
            configureFAB();
            setUpHeader(post.getImageCandidate());

            Helper.admobLoader(this, findViewById(R.id.adView));

            configureContentWebView();

            //If the post is completed, load the body. Else, retrieve the full body first
            if (post.getPostType() == PostItem.PostType.JSON && !post.isCompleted()) {
                new JsonApiPostLoader(post, getIntent().getStringExtra(EXTRA_API_BASE), this).start();
            } else if (post.getPostType() == PostItem.PostType.REST && !post.isCompleted()) {
                new RestApiPostLoader(post, getIntent().getStringExtra(EXTRA_API_BASE), this).start();
                loadPostBody(post);
            } else {
                loadPostBody(post);
            }

            configureFavoritesButton();
            loadRelatedPosts();

        }

    }

    private void configureContentWebView(){
        htmlTextView = findViewById(R.id.htmlTextView);
        htmlTextView.getSettings().setJavaScriptEnabled(true);
        htmlTextView.setBackgroundColor(Color.TRANSPARENT);
        htmlTextView.getSettings().setDefaultFontSize(
                WebHelper.getWebViewFontSize(this));
        htmlTextView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        htmlTextView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null
                        && (url.endsWith(".png") || url
                        .endsWith(".jpg") || url
                        .endsWith(".jpeg"))) {

                    AttachmentActivity.startActivity(WordpressDetailActivity.this, MediaAttachment.withImage(
                            url
                    ));

                    return true;
                } else if (url != null
                        && (url.startsWith("http://") || url
                        .startsWith("https://"))) {
                    HolderActivity.startWebViewActivity(WordpressDetailActivity.this, url, Config.OPEN_INLINE_EXTERNAL, false, null);
                    return true;
                } else {
                    Uri uri = Uri.parse(url);
                    Intent ViewIntent = new Intent(Intent.ACTION_VIEW, uri);

                    // Verify it resolves
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> activities = packageManager
                            .queryIntentActivities(ViewIntent, 0);
                    boolean isIntentSafe = activities.size() > 0;

                    // Start an activity if it's safe
                    if (isIntentSafe) {
                        startActivity(ViewIntent);
                    }
                    return true;
                }
            }
        });
    }

    private void configureFAB(){
        if (post.getAttachments() != null && post.getAttachments().size() > 1 && Config.WP_ATTACHMENTS_BUTTON){
            FloatingActionButton fb = findViewById(R.id.attachments_button);
            fb.setVisibility(View.VISIBLE);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AttachmentActivity.startActivity(WordpressDetailActivity.this, post.getAttachments());
                }
            });

            for (MediaAttachment att : post.getAttachments()){
                Log.v("INFO", att.toString());
            }
        }
    }

    private void loadHeaderImage(){
        String imageurl = post.getImageCandidate();
        if ((null != imageurl && !imageurl.equals("") && !imageurl.equals("null"))) {
            Picasso.with(this).load(imageurl).fit().centerCrop().into(thumb);
            thumb.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    if (post.getAttachments() != null) {

                        //Make sure that the featured attachment is (the first) in the list
                        String imageUrl = post.getImageCandidate();
                        ArrayList<MediaAttachment> attachmentList = new ArrayList<MediaAttachment>();
                        boolean inAttachments = false;
                        for (MediaAttachment attachment : post.getAttachments()){
                            if (imageUrl.equals(attachment.getUrl()) || imageUrl.equals(attachment.getThumbnailUrl())){
                                attachmentList.add(0, attachment);
                                inAttachments = true;
                            } else {
                                attachmentList.add(attachment);
                            }
                        }
                        if (!inAttachments){
                            attachmentList.add(0, MediaAttachment.withImage(imageUrl));
                        }

                        //Show attachments
                        AttachmentActivity.startActivity(WordpressDetailActivity.this, attachmentList);
                    }

                }
            });

            findViewById(R.id.scroller).setOnTouchListener(new View.OnTouchListener() {

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (findViewById(R.id.progressBar).getVisibility() == View.VISIBLE) && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN;
                }
            });
        }
    }

    private void configureFavoritesButton(){
        Button btnFav = findViewById(R.id.favorite);
        btnFav.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                mDbHelper = new FavDbAdapter(WordpressDetailActivity.this);
                mDbHelper.open();

                if (mDbHelper.checkEvent(post.getTitle(), post, FavDbAdapter.KEY_WORDPRESS)) {
                    // Item is new
                    mDbHelper.addFavorite(post.getTitle(), post, FavDbAdapter.KEY_WORDPRESS);
                    Toast toast = Toast
                            .makeText(
                                    WordpressDetailActivity.this,
                                    getResources().getString(
                                            R.string.favorite_success),
                                    Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(
                            WordpressDetailActivity.this,
                            getResources().getString(
                                    R.string.favorite_duplicate),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    private void loadRelatedPosts(){
        //If we have tags and a baseurl, load the related posts
        if (post.getTag() != null && getIntent().getStringExtra(EXTRA_API_BASE) != null) {
            final RecyclerView relatedList = findViewById(R.id.related_list);
            final WordpressGetTaskInfo mInfo = new WordpressGetTaskInfo(relatedList, this, getIntent().getStringExtra(EXTRA_API_BASE), true);
            mInfo.setListener(new WordpressGetTaskInfo.ListListener() {
                @Override
                public void completedWithPosts() {
                    findViewById(R.id.related).setVisibility(View.VISIBLE);
                }
            });
            OnItemClickListener listener = new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position,
                                        long id) {
                    Object o =  mInfo.posts.get(position);
                    PostItem newsData = (PostItem) o;

                    Intent intent = new Intent(WordpressDetailActivity.this, WordpressDetailActivity.class);
                    intent.putExtra(EXTRA_POSTITEM, newsData);
                    intent.putExtra(EXTRA_API_BASE, getIntent().getStringExtra(EXTRA_API_BASE));
                    if (disqusParseable != null)
                        intent.putExtra(WordpressDetailActivity.EXTRA_DISQUS, disqusParseable);
                    startActivity(intent);
                    finish();
                }
            };

            mInfo.adapter = new WordpressListAdapter(this, mInfo.posts, null, listener, mInfo.simpleMode);
            relatedList.setAdapter(mInfo.adapter);
            relatedList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

            WordpressPostsLoader.getTagPosts(mInfo, post.getTag());


        }
    }


    @Override
    public void onPause() {
        super.onPause();
        htmlTextView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (htmlTextView != null)
            htmlTextView.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wordpress_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_share:
                shareContent();
                return true;
            case R.id.menu_view:
                HolderActivity.startWebViewActivity(WordpressDetailActivity.this, post.getUrl(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareContent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, post.getTitle() + "\n" + post.getUrl());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share using"));
    }

    private void loadPostBody(final PostItem result) {
        if (null != result) {
            setHTML(result.getContent());

            //If we have a commentsArray or a disqus url, enable comments
            if ((result.getCommentCount() != null &&
                        result.getCommentCount() != 0 &&
                        result.getCommentsArray() != null) ||
                    disqusParseable != null ||
                    ((post.getPostType() == PostItem.PostType.JETPACK || post.getPostType() == PostItem.PostType.REST) &&
                            result.getCommentCount() != 0)) {

                Button btnComment = findViewById(R.id.comments);

                //Set the comments count if we have it available
                if (result.getCommentCount() == 0 || (result.getCommentCount() == 10 && post.getPostType() == PostItem.PostType.REST))
                    btnComment.setText(getResources().getString(R.string.comments));
                else
                    btnComment.setText(Helper.formatValue(result.getCommentCount()) + " " + getResources().getString(R.string.comments));

                btnComment.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {

                        Intent commentIntent = new Intent(WordpressDetailActivity.this, CommentsActivity.class);

                        if (disqusParseable != null) {
                            commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, disqusParseable);
                            commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.DISQUS);
                            commentIntent.putExtra(CommentsActivity.DATA_ID, post.getId().toString());
                        } else {
                            if (post.getPostType() == PostItem.PostType.JETPACK) {
                                commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, JetPackProvider.getPostCommentsUrl(apiBase, post.getId().toString()));
                                commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.WORDPRESS_JETPACK);
                            } else if (post.getPostType() == PostItem.PostType.REST){
                                    commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, RestApiProvider.getPostCommentsUrl(apiBase, post.getId().toString()));
                                    commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.WORDPRESS_REST);
                            } else if (post.getPostType() == PostItem.PostType.JSON) {
                                commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, result.getCommentsArray().toString());
                                commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.WORDPRESS_JSON);
                            }
                        }

                        startActivity(commentIntent);
                    }
                });
            }
        } else {
            findViewById(R.id.progressBar).setVisibility(View.GONE);

            Helper.noConnection(WordpressDetailActivity.this);
        }
    }

    public void setHTML(String source) {
        Document doc = Jsoup.parse(source);

        //Remove the first image to prevent a repetition of the header image (if enabled and present)
        if (REMOVE_FIRST_IMG) {
            if (doc.select("img") != null && doc.select("img").first() != null)
                doc.select("img").first().remove();
        }

        String html = WebHelper.docToBetterHTML(doc, this);

        htmlTextView.loadDataWithBaseURL(post.getUrl(), html, "text/html", "UTF-8", "");
        htmlTextView.setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }


    @Override
    public void completed(final PostItem item) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (item.getPostType() == PostItem.PostType.JSON)
                        loadPostBody(item);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
