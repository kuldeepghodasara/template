package com.peoplethink.governmentjob.providers.youtube.api.object;

import java.util.ArrayList;

/**
 * Storing information about the video, and formatting the time
 */
public class ReturnItem {
    private ArrayList<Video> list;
    private String pagetoken;
    
    public ReturnItem(ArrayList<Video> list, String pagetoken) {
        super();
        this.list = list;
        this.pagetoken = pagetoken;
    }

    public String getPageToken() {
        return this.pagetoken;
    }
    
    public ArrayList<Video> getList() {
        return this.list;
    }
}