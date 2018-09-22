package com.peoplethink.governmentjob.providers.youtube.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.comments.CommentsActivity;
import com.peoplethink.governmentjob.providers.fav.FavDbAdapter;
import com.peoplethink.governmentjob.providers.youtube.api.object.Video;
import com.peoplethink.governmentjob.providers.youtube.player.YouTubePlayerActivity;
import com.peoplethink.governmentjob.util.DetailActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.WebHelper;
import com.squareup.picasso.Picasso;

/**
 * This activity is used to display the details of a video
 */

public class YoutubeDetailActivity extends DetailActivity {

	private FavDbAdapter mDbHelper;
	private TextView mPresentation;
	private Video video;

    public static final String EXTRA_VIDEO = "videoitem";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Use the general detaillayout and set the viewstub for youtube
		setContentView(R.layout.activity_details);
		ViewStub stub = findViewById(R.id.layout_stub);
		stub.setLayoutResource(R.layout.activity_youtube_detail);
		View inflated = stub.inflate();

		mToolbar = findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		mPresentation = findViewById(R.id.youtubetitle);
		TextView detailsDescription = findViewById(R.id.youtubedescription);
		TextView detailsSubTitle = findViewById(R.id.youtubesubtitle);

        video = (Video) getIntent().getSerializableExtra(EXTRA_VIDEO);

        detailsDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                WebHelper.getTextViewFontSize(this));

		mPresentation.setText(video.getTitle());
		detailsDescription.setText(video.getDescription());
		
		String subText = getResources().getString(R.string.video_subtitle_start) + 
				video.getUpdated() +
				getResources().getString(R.string.video_subtitle_end) + 
				video.getChannel();
		detailsSubTitle.setText(subText);

		if (Config.ADMOB_YOUTUBE)
			Helper.admobLoader(this, findViewById(R.id.adView));
		else
			findViewById(R.id.adView).setVisibility(View.GONE);

		thumb = findViewById(R.id.image);
		coolblue = findViewById(R.id.coolblue);

		Picasso.with(this).load(video.getImage()).into(thumb);

		setUpHeader(video.getImage());

		FloatingActionButton btnPlay = findViewById(R.id.playbutton);
		btnPlay.bringToFront();
		// Listening to button event
		btnPlay.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(YoutubeDetailActivity.this,
						YouTubePlayerActivity.class);
				intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, video.getId());
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

		Button btnFav = findViewById(R.id.favorite);

		// Listening to button event
		btnFav.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mDbHelper = new FavDbAdapter(YoutubeDetailActivity.this);
				mDbHelper.open();

				if (mDbHelper.checkEvent(video.getTitle(), video, FavDbAdapter.KEY_YOUTUBE)) {
					// Item is new
					mDbHelper.addFavorite(video.getTitle(), video, FavDbAdapter.KEY_YOUTUBE);
					Toast toast = Toast
							.makeText(YoutubeDetailActivity.this, getResources()
									.getString(R.string.favorite_success),
									Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(
							YoutubeDetailActivity.this,
							getResources().getString(
									R.string.favorite_duplicate),
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		Button btnComment = findViewById(R.id.comments);

		// Listening to button event
		btnComment.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// Start NewActivity.class
				Intent commentIntent = new Intent(YoutubeDetailActivity.this,
						CommentsActivity.class);
				commentIntent.putExtra(CommentsActivity.DATA_TYPE,
						CommentsActivity.YOUTUBE);
				commentIntent.putExtra(CommentsActivity.DATA_ID,
						video.getId());
				startActivity(commentIntent);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_share:
			String applicationName = getResources()
					.getString(R.string.app_name);
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);

			String urlvalue = getResources().getString(
					R.string.video_share_begin);
			String seenvalue = getResources().getString(
					R.string.video_share_middle);
			String appvalue = getResources()
					.getString(R.string.video_share_end);
			// this is the text that will be shared
			sendIntent.putExtra(Intent.EXTRA_TEXT, (urlvalue
					+ "http://youtube.com/watch?v=" + video.getId() + seenvalue
					+ applicationName + appvalue));
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, video.getTitle());
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources()
					.getString(R.string.share_header)));

			return true;
		case R.id.menu_view:
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("vnd.youtube:" + video.getId()));
				startActivity(intent);
			} catch (ActivityNotFoundException ex) {
				HolderActivity.startWebViewActivity(YoutubeDetailActivity.this, "http://www.youtube.com/watch?v=" + video.getId(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);

			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.youtube_detail_menu, menu);
		return true;
	}

}
