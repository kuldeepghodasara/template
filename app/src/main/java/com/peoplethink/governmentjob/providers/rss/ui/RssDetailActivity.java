package com.peoplethink.governmentjob.providers.rss.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.peoplethink.governmentjob.attachmentviewer.ui.AudioPlayerActivity;
import com.peoplethink.governmentjob.attachmentviewer.ui.VideoPlayerActivity;
import com.peoplethink.governmentjob.providers.fav.FavDbAdapter;
import com.peoplethink.governmentjob.providers.rss.RSSItem;
import com.peoplethink.governmentjob.util.DetailActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.WebHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 *  This activity is used to display details of a rss item
 */

public class RssDetailActivity extends DetailActivity {

	private WebView wb;
	private FavDbAdapter mDbHelper;

	private RSSItem item;
	public static final String EXTRA_RSSITEM = "postitem";

	@SuppressLint("SetJavaScriptEnabled")@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Use the general detaillayout and set the viewstub for wordpress
		setContentView(R.layout.activity_details);
		ViewStub stub = findViewById(R.id.layout_stub);
		stub.setLayoutResource(R.layout.activity_rss_details);
		View inflated = stub.inflate();

		mToolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		thumb = findViewById(R.id.image);
		coolblue = findViewById(R.id.coolblue);
        
		TextView detailsTitle = findViewById(R.id.detailstitle);
		TextView detailsPubdate = findViewById(R.id.detailspubdate);

		Bundle bundle = this.getIntent().getExtras();
		item = (RSSItem) getIntent().getSerializableExtra(EXTRA_RSSITEM);

		detailsTitle.setText(item.getTitle());
		detailsPubdate.setText(item.getPubdate());

		setUpHeader(null);

		wb = findViewById(R.id.descriptionwebview);

		//parse the html and apply some styles
		Document doc = Jsoup.parse(item.getDescription());
		String html = WebHelper.docToBetterHTML(doc, this);

		wb.getSettings().setJavaScriptEnabled(true);
		wb.loadDataWithBaseURL(item.getLink(), html , "text/html", "UTF-8", "");
		wb.setBackgroundColor(Color.argb(1, 0, 0, 0));
		wb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		wb.getSettings().setDefaultFontSize(WebHelper.getWebViewFontSize(this));
		wb.setWebViewClient(new WebViewClient(){
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	if (url != null
						&& (url.endsWith(".png") || url
								.endsWith(".jpg")|| url
								.endsWith(".jpeg"))) {
					AttachmentActivity.startActivity(RssDetailActivity.this, MediaAttachment.withImage(
							url
					));
                	return true;
		    	} else if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
					HolderActivity.startWebViewActivity(RssDetailActivity.this, url, Config.OPEN_INLINE_EXTERNAL, false, null);
					return true;
		        } else {
		        	Uri uri = Uri.parse(url);
		        	Intent ViewIntent = new Intent(Intent.ACTION_VIEW, uri);

		        	// Verify it resolves
		        	PackageManager packageManager = getPackageManager();
		        	List<ResolveInfo> activities = packageManager.queryIntentActivities(ViewIntent, 0);
		        	boolean isIntentSafe = activities.size() > 0;

		        	// Start an activity if it's safe
		        	if (isIntentSafe) {
		        	    startActivity(ViewIntent);
		        	}
		        	return true;
		        }
		    }
		});

		Helper.admobLoader(this, findViewById(R.id.adView));
		
		Button btnMedia = findViewById(R.id.mediabutton);
		final String videoUrl = item.getVideourl();
		final String audioUrl = item.getAudiourl();

		if (videoUrl != null)
			btnMedia.setText(getResources().getString(R.string.btn_video));
		else if (audioUrl != null)
			btnMedia.setText(getResources().getString(R.string.btn_audio));
		else
			btnMedia.setVisibility(View.GONE);
		
		//Listening to button event
		btnMedia.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0){
				if (videoUrl != null){
					VideoPlayerActivity.startActivity(RssDetailActivity.this, videoUrl);
				} else if (audioUrl != null){
					AudioPlayerActivity.startActivity(RssDetailActivity.this, audioUrl, item.getTitle());
				}
			}
		});
		
		Button btnOpen = findViewById(R.id.openbutton);

		//Listening to button event
		btnOpen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				HolderActivity.startWebViewActivity(RssDetailActivity.this, item.getLink(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);


			}
		});

		Button btnFav = findViewById(R.id.favoritebutton);

		//Listening to button event
		btnFav.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mDbHelper = new FavDbAdapter(RssDetailActivity.this);
				mDbHelper.open();

				if (mDbHelper.checkEvent(item.getTitle(), item, FavDbAdapter.KEY_RSS)) {
					// Item is new
					mDbHelper.addFavorite(item.getTitle(), item, FavDbAdapter.KEY_RSS);
					Toast toast = Toast.makeText(RssDetailActivity.this, getResources().getString(R.string.favorite_success), Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(RssDetailActivity.this, getResources().getString(R.string.favorite_duplicate), Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

	}
	
	@Override
	public void onPause(){
		super.onPause();
		wb.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();
		wb.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {
		switch (menuitem.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_item_share:

				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				//this is the text that will be shared
				sendIntent.putExtra(Intent.EXTRA_TEXT, (item.getTitle() + "\n" + item.getLink()));
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share_header)));
				return true;
			default:
				return super.onOptionsItemSelected(menuitem);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_share, menu);
		return true;
	}

}