package com.peoplethink.governmentjob.providers.facebook;

import org.json.JSONArray;

import java.util.Date;


public class FacebookItem {
    public String id;
    public String type;
    public String username;
    public String profilePhotoUrl;
    public String captionUsername;
    public String caption;
    public String imageUrl;
    public String videoUrl;
    public String link;
    public Date createdTime;
    public int likesCount;
    public int commentsCount;
    public JSONArray commentsArray;
}
