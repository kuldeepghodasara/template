package com.peoplethink.governmentjob.providers.soundcloud.api.object;

import java.util.Date;

public class CommentObject {
	private long id;
    private long trackid;
    private long userId;
	private Date createdAt;
    private int timeStamp;
	private String body;
	private String username;
	private String avatarUrl;

	public CommentObject(long id, long trackid, long userId, Date createdAt, int timeStamp, String body, String username, String avatarUrl) {
		super();
		this.id = id;
		this.trackid = trackid;
        this.userId = userId;
		this.createdAt = createdAt;
		this.timeStamp = timeStamp;
		this.body = body;
		this.username = username;
		this.avatarUrl = avatarUrl;
	}

	public int getTimeStamp() {
		return timeStamp;
	}

	public long getId() {
		return id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public long getUserId() {
		return userId;
	}

	public long getTrackid() {
		return trackid;
	}

	public String getBody() {
		return body;
	}

	public String getUsername() {
		return username;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

}
