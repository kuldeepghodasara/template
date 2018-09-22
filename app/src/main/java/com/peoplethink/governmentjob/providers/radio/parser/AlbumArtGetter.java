package com.peoplethink.governmentjob.providers.radio.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * This class is used to get Album Art of a song based on search query. Uses the spotify API.
 */
public class AlbumArtGetter {

    public static void getImageForQuery(final String query, final AlbumCallback callback, final Context context){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... unused) {

                if (!query.equals("null+null")) {
                    JSONObject o = Helper.getJSONObjectFromUrl("https://itunes.apple.com/search?term=" + URLEncoder.encode(query) + "&media=music&limit=1");

                    try {
                        if (o != null
                                && o.has("results")
                                && o.getJSONArray("results").length() > 0) {
                            JSONObject track = o.getJSONArray("results").getJSONObject(0);
                            String url = track.getString("artworkUrl100");
                            return url.replace("100x100bb.jpg", "500x500bb.jpg");
                        } else {
                            Log.v("INFO", "No items in Album Art Request");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(final String imageurl){
                if (imageurl != null)
                    Picasso.with(context)
                    .load(imageurl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                            callback.finished(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            callback.finished(null);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                else
                    callback.finished(null);
            }
        }.execute();
    }

    public interface AlbumCallback {
        void finished(Bitmap b);
    }
}
