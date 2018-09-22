package com.peoplethink.governmentjob.providers.web;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.inherit.BackPressFragment;
import com.peoplethink.governmentjob.inherit.CollapseControllingFragment;
import com.peoplethink.governmentjob.providers.fav.FavDbAdapter;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This activity is used to display webpages
 */

public class WebviewFragment extends Fragment implements BackPressFragment, CollapseControllingFragment {

	//Static
	public static final String HIDE_NAVIGATION = "hide_navigation";
	public static final String LOAD_DATA = "loadwithdata";

	//File upload
	public static final int INPUT_FILE_REQUEST_CODE = 1;
	public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

	private ValueCallback<Uri[]> mFilePathCallback;
	private String mCameraPhotoPath;

	//References
	private Activity mAct;
	private FavDbAdapter mDbHelper;

	//Layout with interaction
	private WebView browser;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	//Layouts
	private ImageButton webBackButton;
	private ImageButton webForwButton;
	private LinearLayout ll;

	//HTML5 video
	private View mCustomView;
	private int mOriginalSystemUiVisibility;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		//Return the existing layout if there is a savedInstance of this fragment
		if (savedInstanceState != null) { return ll; }

		ll = (LinearLayout) inflater.inflate(R.layout.fragment_webview,
				container, false);

		setHasOptionsMenu(true);

		browser = ll.findViewById(R.id.webView);
		mSwipeRefreshLayout = ll.findViewById(R.id.refreshlayout);

		// settings some settings like zooming etc in seperate method for
		// suppresslint
		browserSettings();

		browser.setWebViewClient(new WebViewClient() {

			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return handleUri(url);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return handleUri(request.getUrl().toString());
			}

			// handeling errors
			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView view, int errorCode,
										String description, String failingUrl) {

				if (failingUrl.startsWith("file:///android_asset/") || checkConnectivity()) {
                    //It is a local error, or a we have connectivity
                } else {
                    browser.loadUrl("about:blank");
				}
			}

			// Make sure any url clicked is opened in webview
			boolean handleUri(String url) {
				if (url.contains("market://") || url.contains("mailto:")
						|| url.contains("play.google") || url.contains("tel:") || url
						.contains("vid:") || url.contains("youtube.com")) {
					// Load new URL Don't override URL Link
					startActivity(
							new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

					return true;
				}

				// Return true to override url loading (In this case do
				// nothing).
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(browser, url);

				adjustControls();
			}

		});

		// has all to do with progress bar
		browser.setWebChromeClient(new WebChromeClient() {
			public boolean onShowFileChooser(
					WebView webView, ValueCallback<Uri[]> filePathCallback,
					WebChromeClient.FileChooserParams fileChooserParams) {
				if(mFilePathCallback != null) {
					mFilePathCallback.onReceiveValue(null);
				}
				mFilePathCallback = filePathCallback;

				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
					// Create the File where the photo should go
					File photoFile = null;
					try {
						photoFile = createImageFile();
						takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
					} catch (IOException ex) {
						// Error occurred while creating the File
					}

					// Continue only if the File was successfully created
					if (photoFile != null) {
						mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(photoFile));
					} else {
						takePictureIntent = null;
					}
				}

				Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
				contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
				contentSelectionIntent.setType("image/*");

				Intent[] intentArray;
				if(takePictureIntent != null) {
					intentArray = new Intent[]{takePictureIntent};
				} else {
					intentArray = new Intent[0];
				}

				Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
				chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
				chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

				startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (mSwipeRefreshLayout.isRefreshing()) {
					if (progress == 100) {
						mSwipeRefreshLayout.setRefreshing(false);
					}
				} else if (progress < 100){
                    //If we do not hide the navigation, show refreshing
					if (navigationIsVisible())
					    mSwipeRefreshLayout.setRefreshing(true);
				}
			}

			@SuppressLint("InlinedApi")
			@Override
			public void onShowCustomView(View view,
										 WebChromeClient.CustomViewCallback callback) {
				// if a view already exists then immediately terminate the new one
				if (mCustomView != null) {
					onHideCustomView();
					return;
				}

				// 1. Stash the current state
				mCustomView = view;
                mCustomView.setBackgroundColor(Color.BLACK);
				mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();

				// 2. Stash the custom view callback
				mCustomViewCallback = callback;

				// 3. Add the custom view to the view hierarchy
				FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
				decor.addView(mCustomView, new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));


				// 4. Change the state of the window
				getActivity().getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
								View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
								View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
								View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
								View.SYSTEM_UI_FLAG_FULLSCREEN |
								View.SYSTEM_UI_FLAG_IMMERSIVE);
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}

			@Override
			public void onHideCustomView() {
				// 1. Remove the custom view
				FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
				decor.removeView(mCustomView);
				mCustomView = null;

				// 2. Restore the state to it's original form
				getActivity().getWindow().getDecorView()
						.setSystemUiVisibility(mOriginalSystemUiVisibility);

                //The user will come from landscape, so we'll first 'rotate' to portrait (rotation fixes a bug of the keybaord not showing)
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                //The we'll restore to the detected orientation (by immediately rotating back, the user should not notice any difference and/or flickering).
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

				// 3. Call the custom view callback
				mCustomViewCallback.onCustomViewHidden();
				mCustomViewCallback = null;


            }

			@Override
			public Bitmap getDefaultVideoPoster() {
				if(isAdded()){
					return BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
				} else {
					return null;
				}
			}

		});

		browser.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
										String contentDisposition, String mimetype,
										long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		// setting an on touch listener
		browser.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:
						if (!v.hasFocus()) {
							v.requestFocus();
						}
						break;
				}
				return false;
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				browser.reload();
			}
		});

		return ll;
	}// of oncreateview

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		setRetainInstance(true);

		String weburl = getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		String data = getArguments().containsKey(LOAD_DATA) ? getArguments().getString(LOAD_DATA) : null;
		if (weburl.startsWith("file:///android_asset/") || checkConnectivity()) {
			//If this is the first time, load the initial url, otherwise restore the view if necessairy
			if (savedInstanceState == null) {
				//If we have HTML data to load, do so, else load the url.
				if (data != null) {
					browser.loadDataWithBaseURL(weburl, data, "text/html", "UTF-8", "");
				} else {
					browser.loadUrl(weburl);
				}
			} else if (mCustomView != null){
				FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
				((ViewGroup) mCustomView.getParent()).removeView(mCustomView);
				decor.addView(mCustomView, new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();

        if (browser != null)
		    browser.onPause();
        else
            Log.e("INFO", "Browser is null");

        setMenuVisibility(false);
	}

	@Override
	public void setMenuVisibility(final boolean visible) {
		super.setMenuVisibility(visible);
		if (mAct == null) return;

		if (visible) {
            if (navigationIsVisible()){

                ActionBar actionBar = ((AppCompatActivity) mAct)
                        .getSupportActionBar();

                if (mAct instanceof HolderActivity) {
                    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
                } else {
                    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
                }

                View view = mAct.getLayoutInflater().inflate(R.layout.fragment_webview_actionbar, null);
                LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL);
                actionBar.setCustomView(view, lp);

                webBackButton = mAct.findViewById(R.id.goBack);
                webForwButton = mAct.findViewById(R.id.goForward);

                webBackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (browser.canGoBack())
                            browser.goBack();
                    }
                });
                webForwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (browser.canGoForward())
                            browser.goForward();
                    }
                });
            }
		} else {
            if (navigationIsVisible()
                    && getActivity() != null) {

                ActionBar actionBar = ((AppCompatActivity) getActivity())
                        .getSupportActionBar();
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            }
        }
	}

	@Override
	public void onResume() {
		super.onResume();
        if (browser != null) {
            browser.onResume();
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }

        if (this.getArguments().containsKey(HIDE_NAVIGATION) &&
                this.getArguments().getBoolean(HIDE_NAVIGATION)){
			mSwipeRefreshLayout.setEnabled(false);
		}

        adjustControls();
        if (isMenuVisible() || getUserVisibleHint())
            setMenuVisibility(true);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setMenuVisibility(true);
        }
    }

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
	}


	/**
	 * More info this method can be found at
	 * http://developer.android.com/training/camera/photobasics.html
	 *
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		return File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			shareURL();
			return true;
		case R.id.favorite:
			mDbHelper = new FavDbAdapter(mAct);
			mDbHelper.open();

			String title = browser.getTitle();
			String url = browser.getUrl();

			if (mDbHelper.checkEvent(title, url, FavDbAdapter.KEY_WEB)) {
				// This item is new
				mDbHelper.addFavorite(title, url, FavDbAdapter.KEY_WEB);
				Toast toast = Toast.makeText(mAct,
						getResources().getString(R.string.favorite_success),
						Toast.LENGTH_LONG);
				toast.show();
			} else {
				Toast toast = Toast.makeText(mAct,
						getResources().getString(R.string.favorite_duplicate),
						Toast.LENGTH_LONG);
				toast.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (!this.getArguments().containsKey(HIDE_NAVIGATION)  ||
				!this.getArguments().getBoolean(HIDE_NAVIGATION))
		inflater.inflate(R.menu.webview_menu, menu);

        //For local urls, we don't need a share item
        if (browser.getUrl() != null && browser.getUrl().startsWith("file:///android_asset/"))
            menu.findItem(R.id.share).setVisible(false);
	}

	// Checking for an internet connection
	private boolean checkConnectivity() {
		boolean enabled = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) mAct
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if ((info == null || !info.isConnected() || !info.isAvailable())) {
			enabled = false;
			
			Helper.noConnection(mAct);
		}
		
		return enabled;
	}

	public void adjustControls() {
		webBackButton = mAct.findViewById(R.id.goBack);
		webForwButton = mAct.findViewById(R.id.goForward);

		if (webBackButton == null || webForwButton == null || browser == null) return;

		if (browser.canGoBack()) {
			webBackButton.setColorFilter(Color.argb(255, 255, 255, 255));
		} else {
			webBackButton.setColorFilter(Color.argb(255, 0, 0, 0));
		}
		if (browser.canGoForward()) {
			webForwButton.setColorFilter(Color.argb(255, 255, 255, 255));
		} else {
			webForwButton.setColorFilter(Color.argb(255, 0, 0, 0));
		}
	}

	// sharing
	private void shareURL() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		String appname = getString(R.string.app_name);
		shareIntent.putExtra(Intent.EXTRA_TEXT,
				(getResources().getString(R.string.web_share_begin)) + appname
						+ getResources().getString(R.string.web_share_end)
						+ browser.getUrl());
		startActivity(Intent.createChooser(shareIntent, getResources()
				.getString(R.string.share)));
	}

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	private void browserSettings() {
		// set javascript and zoom and some other settings
		browser.getSettings().setJavaScriptEnabled(true);
		browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDisplayZoomControls(false);
		browser.getSettings().setAppCacheEnabled(true);
		browser.getSettings().setDatabaseEnabled(true);
		browser.getSettings().setDomStorageEnabled(true);
		browser.getSettings().setUseWideViewPort(true);
		browser.getSettings().setLoadWithOverviewMode(true);

		// enable all plugins (flash)
		browser.getSettings().setPluginState(PluginState.ON);
	}

	@Override
	public boolean handleBackPress() {
		if (browser.canGoBack()){
			browser.goBack();
			return true;
		}

		return false;
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		Uri[] results = null;

		// Check that the response is a good one
		if(resultCode == Activity.RESULT_OK) {
			if(data == null) {
				// If there is not data, then we may have taken a photo
				if(mCameraPhotoPath != null) {
					results = new Uri[]{Uri.parse(mCameraPhotoPath)};
				}
			} else {
				String dataString = data.getDataString();
				if (dataString != null) {
					results = new Uri[]{Uri.parse(dataString)};
				}
			}
		}

		mFilePathCallback.onReceiveValue(results);
		mFilePathCallback = null;
	}

	@Override
	public boolean supportsCollapse() {
		return false;
	}

	public boolean navigationIsVisible(){
		//If override is on, always hide
		if (Config.FORCE_HIDE_NAVIGATION) return false;

		//Only hide navigation if key is provided and is true
		return (!this.getArguments().containsKey(HIDE_NAVIGATION)  ||
				!this.getArguments().getBoolean(HIDE_NAVIGATION)
		);
	}
}
