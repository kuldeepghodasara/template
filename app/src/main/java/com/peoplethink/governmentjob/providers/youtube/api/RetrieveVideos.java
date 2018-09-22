package com.peoplethink.governmentjob.providers.youtube.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;

import com.peoplethink.governmentjob.providers.youtube.api.object.ReturnItem;
import com.peoplethink.governmentjob.providers.youtube.api.object.Video;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * This is class gets the videos from youtube and parses the result
 */
public class RetrieveVideos {

    private static int PER_PAGE = 20;

    private static String API_BASE = "https://www.googleapis.com/youtube/v3";
    private static String API_TYPE_SEARCH = "/search";
    private static String API_TYPE_PLAYLIST = "/playlistItems";

    private String serverKey;
    private Context mContext;

    public RetrieveVideos(Context mContext, String serverKey) {
        this.serverKey = serverKey;
        this.mContext = mContext;
    }

    public ReturnItem getLiveVideos(String channelId, String nextPageToken) {
        String retrievalUrl = API_BASE + API_TYPE_SEARCH + "?part=snippet&type=video&channelId=" + channelId + "&eventType=live&maxResults=" + PER_PAGE + "&key=" + serverKey;

        if (nextPageToken != null)
            retrievalUrl += ("&pageToken=" + nextPageToken);

        return getVideos(retrievalUrl, mContext);
    }

    public ReturnItem getUserVideos(String username) {
        return getUserVideos(username, null);
    }

    public ReturnItem getUserVideos(String username, String nextPageToken) {
        String retrievalUrl = API_BASE + API_TYPE_SEARCH + "?part=snippet&order=date&channelId=" + username + "&maxResults=" + PER_PAGE + "&key=" + serverKey;
        if (nextPageToken != null)
            retrievalUrl += ("&pageToken=" + nextPageToken);

        return getVideos(retrievalUrl, mContext);
    }

    public ReturnItem getPlaylistVideos(String playlist) {
        return getPlaylistVideos(playlist, null);
    }

    public ReturnItem getPlaylistVideos(String username, String nextPageToken) {
        String retrievalUrl = API_BASE + API_TYPE_PLAYLIST + "?part=snippet&playlistId=" + username + "&maxResults=" + PER_PAGE + "&key=" + serverKey;
        if (nextPageToken != null)
            retrievalUrl += ("&pageToken=" + nextPageToken);

        return getVideos(retrievalUrl, mContext);
    }


    public ReturnItem getSearchVideos(String query, String channel) {
        return getSearchVideos(query, channel, null);
    }

    public ReturnItem getSearchVideos(String query, String channel, String nextPageToken) {//start video retrieval process
        //Decode the parameter
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //We know for a fact that this encoding is supported
        }

        String retrievalUrl = API_BASE + API_TYPE_SEARCH + "?part=snippet&type=video&channelId=" + channel + "&q=" + query + "&maxResults=" + PER_PAGE + "&key=" + serverKey;

        if (nextPageToken != null)
            retrievalUrl += ("&pageToken=" + nextPageToken);

        return getVideos(retrievalUrl, mContext);
    }

    public static ReturnItem getVideos(String apiUrl, Context context) {
        ArrayList<Video> videos = null;
        String pagetoken = null;
        // Making HTTP request

        JSONObject json = Helper.getJSONObjectFromUrl(apiUrl);

        if (json == null) {
            return (new ReturnItem(null, null));
        }

        try {
            if (json.getString("kind").contains("youtube")) {
                videos = new ArrayList<>();
            }

            if (json.has("nextPageToken"))
                pagetoken = json.getString("nextPageToken");

            JSONArray jsonArray = json.getJSONArray("items");

            // Create a list to store the videos in
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject jsonSnippet = jsonArray.getJSONObject(i).getJSONObject("snippet");
                    String title = jsonSnippet.getString("title");
                    String updated = formatData(jsonSnippet.getString("publishedAt"), context);
                    String description = jsonSnippet.getString("description");
                    String channel = jsonSnippet.getString("channelTitle");
                    String id;
                    try {
                        id = jsonSnippet.getJSONObject("resourceId").getString("videoId");
                    } catch (Exception e) {
                        id = jsonObject.getJSONObject("id").getString("videoId");
                    }
                    // For a sharper thumbnail change sq to hq, this will make the app slower though
                    String thumbUrl = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                    String image = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                    // save the video to the list
                    videos.add(new Video(title, id, updated, description, thumbUrl, image, channel));
                } catch (JSONException e) {
                    Log.v("INFO", "JSONException: " + e);
                }
            }

        } catch (JSONException e) {
            Log.v("INFO", "JSONException: " + e);
        }
        Log.v("INFO", "Token: " + pagetoken);
        return (new ReturnItem(videos, pagetoken));
    }

    @SuppressLint("SimpleDateFormat")
    private static String formatData(String data, Context context) {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date date;
        String strData = "";
        try {
            date = parser.parse(data);
            strData = DateUtils.getRelativeDateTimeString(context, date.getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
        } catch (ParseException e) {
            Log.printStackTrace(e);
        }

        return strData;
    }

}