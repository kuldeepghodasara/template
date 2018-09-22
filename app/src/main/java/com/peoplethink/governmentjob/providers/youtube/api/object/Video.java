package com.peoplethink.governmentjob.providers.youtube.api.object;

import java.io.Serializable;

/**
 * Storing information about the video, and formatting the time
 */
@SuppressWarnings("serial")
public class Video implements Serializable {

	private String title;
	private String id;	
	private String updated;	
	private String description;	
	private String thumbUrl;
	private String image;
	private String channel;
	
	public Video(String title, String id, String updated, String description, String thumbUrl, String image, String channel) {
		super();
		this.title = title;
		this.id = id;
		this.updated = updated;
		this.description = description;
		this.thumbUrl = thumbUrl;
		this.image = image;
		this.channel = channel;
	}

	public String getTitle(){
		return title;
	}
	
	public String getId() {
		return id;
	}
    public String getUpdated() {
		return updated;
    }
    
    public String getDescription() {
    	return description;
    }

	public String getThumbUrl() {
		return thumbUrl;
	}
	
	public String getImage() {
		return image;
	}
	
	public String getChannel() {
		return channel;
	}

}