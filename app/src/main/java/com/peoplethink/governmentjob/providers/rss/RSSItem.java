package com.peoplethink.governmentjob.providers.rss;

import android.text.Html;

import java.io.Serializable;

/**
 * This class is used to remember rss item data
 */

public class RSSItem implements Serializable {

    private String title = null;
    private String description = null;
    private String rowdescription = null;
    private String link = null;
    private String pubdate = null;
    private String thumburl = null;
    private String audiourl = null;
    private String videourl = null;

    void setTitle(String value) {
        title = value;
    }

    void setDescription(String value) {
        description = value;
        rowdescription = stripHtml(value).toString();
    }

    void setLink(String value) {
        link = value;
    }

    void setPubdate(String value) {
        pubdate = value;
    }

    void setThumburl(String value) {
        thumburl = value;
    }

    void setVideourl(String value) {
        videourl = value;
    }

    void setAudiourl(String value) {
        audiourl = value;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRowDescription() {
        return rowdescription;
    }

    public String getLink() {
        return link;
    }

    public String getPubdate() {
        return pubdate;
    }

    public String getAudiourl() {
        return audiourl;
    }

    public String getVideourl() {
        return videourl;
    }

    public String getThumburl() {
        return thumburl;
    }

    public CharSequence stripHtml(String s) {
        return Html.fromHtml(s).toString().replace('\n', (char) 32)
                .replace((char) 160, (char) 32).replace((char) 65532, (char) 32).trim();
    }

}
