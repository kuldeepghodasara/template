package com.peoplethink.governmentjob.providers.tumblr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.tumblr.Constants.Extra;
import com.peoplethink.governmentjob.providers.tumblr.TumblrItem;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 *  This activity is used to show a swipable viewpager of the selected tumblr items
 *
 *  Contains code from: Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */

public class TumblrPagerActivity extends Activity {

	private static final String STATE_POSITION = "STATE_POSITION";

	ViewPager imagePager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tumblr_pager);

		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		ArrayList<TumblrItem> tumblrItems = bundle.getParcelableArrayList(Extra.IMAGES);
		int pagerPosition = bundle.getInt(Extra.IMAGE_POSITION, 0);

		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		imagePager = findViewById(R.id.pager);
		if (null != tumblrItems){
			imagePager.setAdapter(new ImagePagerAdapter(tumblrItems));
			imagePager.setCurrentItem(pagerPosition);
		}
		
		Helper.admobLoader(this, findViewById(R.id.adView));
    
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, imagePager.getCurrentItem());
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private ArrayList<TumblrItem> images;
		private LayoutInflater inflater;

		ImagePagerAdapter(ArrayList<TumblrItem> images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			View imageLayout = inflater.inflate(R.layout.activity_tumblr_pager_image, view, false);
			assert imageLayout != null;
			final ImageView imageView = imageLayout.findViewById(R.id.image);
			
			final Button btnShare = imageLayout.findViewById(R.id.btnShare);
			Button btnSet = imageLayout.findViewById(R.id.btnSet);
			final Button btnSave = imageLayout.findViewById(R.id.btnSave);
			
			final ProgressBar spinner = imageLayout.findViewById(R.id.loading);

            spinner.setVisibility(View.VISIBLE);
            Picasso.with(TumblrPagerActivity.this).load(images.get(position).getUrl()).into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    spinner.setVisibility(View.GONE);
					new PhotoViewAttacher(imageView);
                    // close button click event
                    btnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String path = Environment.getExternalStorageDirectory().toString();
                            OutputStream fOut = null;
                            File file = new File(path, "tumblr_"+images.get(position).getId()+".jpg");
                            try {
                                fOut = new FileOutputStream(file);
                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fOut);
                                fOut.flush();
                                fOut.close();

                                MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());

                                String saved = getResources().getString(R.string.saved);
                                Toast.makeText(getBaseContext(), saved + " " + file.toString(), Toast.LENGTH_LONG).show();
                            } catch (FileNotFoundException e) {
                                Log.printStackTrace(e);
                            } catch (IOException e) {
                                Log.printStackTrace(e);
                            }
                        }
                    });

                    // close button click event
                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String appvalue = getResources().getString(R.string.tumblr_share_begin);
                            String applicationName = getResources().getString(R.string.app_name);

                            Intent shareIntent = new Intent();
                            shareIntent.setType("text/plain");
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, appvalue + " " + applicationName + ": " + images.get(position).getLink());
                            TumblrPagerActivity.this.startActivity(Intent.createChooser(shareIntent, "Share"));
                        }
                    });
                }

                @Override
                public void onError() {
                    spinner.setVisibility(View.GONE);
                }
            });

	        btnSet.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(TumblrPagerActivity.this);
		    		builder.setMessage(getResources().getString(R.string.set_confirm))
		    		   .setCancelable(true)
		    		   .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
		    		       public void onClick(DialogInterface dialog, int id) {
		    		    	   try {
		    		    	           Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
										WallpaperManager myWallpaperManager = WallpaperManager.getInstance(TumblrPagerActivity.this);
										myWallpaperManager.setBitmap(bitmap);
										Toast.makeText(TumblrPagerActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT).show();
								} catch (IOException e) {
										Log.printStackTrace(e);
										Log.v("ERROR", "Wallpaper not set");
								}  
									
		    		       }
		    		   });
		    		AlertDialog alert = builder.create();   
		    		alert.show();
				}
			}); 

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}