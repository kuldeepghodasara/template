package com.peoplethink.governmentjob.providers.wordpress.api.providers;

import com.peoplethink.governmentjob.providers.wordpress.CategoryItem;
import com.peoplethink.governmentjob.providers.wordpress.PostItem;
import com.peoplethink.governmentjob.providers.wordpress.api.WordpressGetTaskInfo;

import java.util.ArrayList;

/**
 * This is an interface for Wordpress API Providers.
 */
public interface WordpressProvider {

    String getRecentPosts(WordpressGetTaskInfo info);

    String getTagPosts(WordpressGetTaskInfo info, String tag);

    String getCategoryPosts(WordpressGetTaskInfo info, String category);

    String getSearchPosts(WordpressGetTaskInfo info, String query);

    ArrayList<CategoryItem> getCategories(WordpressGetTaskInfo info);

    ArrayList<PostItem> parsePostsFromUrl(WordpressGetTaskInfo info, String url);

}
