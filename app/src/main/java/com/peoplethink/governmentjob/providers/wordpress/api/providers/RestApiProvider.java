package com.peoplethink.governmentjob.providers.wordpress.api.providers;

import android.text.Html;

import com.peoplethink.governmentjob.providers.wordpress.CategoryItem;
import com.peoplethink.governmentjob.providers.wordpress.PostItem;
import com.peoplethink.governmentjob.providers.wordpress.api.RestApiPostLoader;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressCategoriesTask;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressGetTaskInfo;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressPostsTask;
import com.peoplethink.governmentjob.providers.wordpress.ui.WordpressDetailActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This is a provider for the Wordpress Fragment over Wordpress REST API (as of WP V4.7).
 */
public class RestApiProvider implements WordpressProvider {

    //Rest
    private static final String REST_FIELDS = "&_embed=1";
    private static final SimpleDateFormat REST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Override
    public String getRecentPosts(WordpressGetTaskInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append("posts/?per_page=");
        builder.append(WordpressPostsTask.PER_PAGE);
        builder.append(REST_FIELDS);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getTagPosts(WordpressGetTaskInfo info, String tag) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append("posts/?per_page=");
        if (info.simpleMode)
            builder.append(WordpressPostsTask.PER_PAGE_RELATED);
        else
            builder.append(WordpressPostsTask.PER_PAGE);
        builder.append(REST_FIELDS);
        builder.append("&tags=");
        builder.append(tag);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getCategoryPosts(WordpressGetTaskInfo info, String category) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append("posts/?per_page=");
        builder.append(WordpressPostsTask.PER_PAGE);
        builder.append(REST_FIELDS);
        builder.append("&categories=");
        builder.append(category);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getSearchPosts(WordpressGetTaskInfo info, String query) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append("posts/?per_page=");
        builder.append(WordpressPostsTask.PER_PAGE);
        builder.append(REST_FIELDS);
        builder.append("&search=");
        builder.append(query);
        builder.append("&page=");

        return builder.toString();
    }

    public static String getPostCommentsUrl(String apiBase, String postId){
        StringBuilder builder = new StringBuilder();
        builder.append(apiBase);
        builder.append("comments/?post=");
        builder.append(postId);
        builder.append(REST_FIELDS);
        builder.append("&orderby=date_gmt&order=asc");
        builder.append("&per_page=50");

        return builder.toString();
    }

    public static String getPostMediaUrl(String apiBase, String postId){
        StringBuilder builder = new StringBuilder();
        builder.append(apiBase);
        builder.append("media?parent=");
        builder.append(postId);

        return builder.toString();
    }

    @Override
    public ArrayList<CategoryItem> getCategories(WordpressGetTaskInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append("categories");
        builder.append("?orderby=count&order=desc&per_page=" + WordpressCategoriesTask.NUMBER_OF_CATEGORIES);

        JSONArray response = Helper.getJSONArrayFromUrl(builder.toString());
        if (response == null || response.length() == 0)
            return null;

        ArrayList<CategoryItem> result = null;
        try {
            for (int i = 0; i < response.length(); i++) {
                if (result == null) result = new ArrayList<>();

                JSONObject category = response.getJSONObject(i);
                CategoryItem item = new CategoryItem(category.getString("id"), category.getString("name"), category.getInt("count"));
                result.add(item);
            }
        } catch (JSONException e){
            Log.printStackTrace(e);
        }

        return result;
    }

    @Override
    public ArrayList<PostItem> parsePostsFromUrl(WordpressGetTaskInfo info, String url) {
        ArrayList<PostItem> result = null;
        try {

            JSONArray posts = getJSONArrFromUrl(url, info);

            // parsing json object
            if (posts != null) {
                result = new ArrayList<PostItem>();

                for (int i = 0; i < posts.length(); i++) {
                    try {
                        JSONObject post = posts.getJSONObject(i);
                        PostItem item = itemFromJsonObject(post);

                        //Complete the post in the background (if enabled)
                        if (WordpressDetailActivity.PRELOAD_POSTS)
                            new RestApiPostLoader(item, info.baseurl, null).start();


                        if (!item.getId().equals(info.ignoreId)) {
                            result.add(item);
                        }
                    } catch (Exception e) {
                        Log.v("INFO", "Item " + i + " of " + posts.length()
                                + " has been skipped due to exception!");
                        Log.printStackTrace(e);
                    }
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }

        return result;
    }

    public static PostItem itemFromJsonObject(JSONObject post) throws JSONException {
        PostItem item = new PostItem(PostItem.PostType.REST);

        item.setId(post.getLong("id"));
        item.setAuthor(post.getJSONObject("_embedded").getJSONArray("author").getJSONObject(0).getString("name"));
        try {
            item.setDate(REST_DATE_FORMAT.parse(post.getString("date")));
        } catch (ParseException e) {
            Log.printStackTrace(e);
        }

        item.setTitle(Html.fromHtml(post.getJSONObject("title").getString("rendered"))
                .toString());
        item.setUrl(post.getString("link"));
        item.setContent(post.getJSONObject("content").getString("rendered"));

        if (post.getJSONObject("_embedded").has("replies")
                && post.getJSONObject("_embedded").getJSONArray("replies") != null) {
            item.setCommentCount((long) post.getJSONObject("_embedded").getJSONArray("replies").getJSONArray(0).length());
        } else {
            item.setCommentCount((long) 0);
        }

        if (post.getJSONObject("_embedded").has("wp:featuredmedia")
                && post.getJSONObject("_embedded").getJSONArray("wp:featuredmedia").length() > 0
                && post.getJSONObject("_embedded").getJSONArray("wp:featuredmedia").getJSONObject(0).getString("media_type").equals("image")){
            JSONObject sizes = post.getJSONObject("_embedded").getJSONArray("wp:featuredmedia").getJSONObject(0).getJSONObject("media_details").getJSONObject("sizes");
            if (sizes.has("large"))
                item.setFeaturedImageUrl(sizes.getJSONObject("large").getString("source_url"));
            else
                item.setFeaturedImageUrl(sizes.getJSONObject("full").getString("source_url"));
            item.setThumbnailUrl(sizes.getJSONObject("medium").getString("source_url"));
        }

        //If there are tags, save the first one
        JSONArray tags = post.getJSONArray("tags");
        if (tags != null && tags.length() > 0)
            item.setTag(Long.toString(tags.getLong(0)));

        return item;
    }

    /**
     * Custom method to get JSON to get header fields
     * @return JSON Array from url
     */
    private JSONArray getJSONArrFromUrl(String url, WordpressGetTaskInfo info){
        // Making HTTP request
        Log.v("INFO", "Requesting: " + url);

        StringBuffer chaine = new StringBuffer("");
        try {
            URL urlCon = new URL(url);

            //Open a connection
            HttpURLConnection connection = (HttpURLConnection) urlCon
                    .openConnection();
            connection.setRequestProperty("User-Agent", "Universal/2.0 (Android)");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            //Handle redirecti
            int status = connection.getResponseCode();
            if ((status != HttpURLConnection.HTTP_OK) && (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)){

                // get redirect url from "location" header field
                String newUrl = connection.getHeaderField("Location");
                // get the cookie if need, for login
                String cookies = connection.getHeaderField("Set-Cookie");

                // open the new connnection again
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", "Universal/2.0 (Android)");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                System.out.println("Redirect to URL : " + newUrl);
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                info.pages = connection.getHeaderFieldInt("X-WP-TotalPages", 1);
            }

            //Get the stream from the connection and read it
            InputStream inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            Log.printStackTrace(e);
        }

        String response = chaine.toString();
        try {
            return new JSONArray(response);
        } catch (Exception e) {
            Log.e("INFO", "Error parsing JSON. Printing stacktrace now");
            Log.printStackTrace(e);
            return null;
        }
    }

}
