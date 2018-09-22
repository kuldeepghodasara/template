package com.peoplethink.governmentjob.providers.soundcloud.api;

import com.peoplethink.governmentjob.providers.soundcloud.api.object.CommentObject;
import com.peoplethink.governmentjob.providers.soundcloud.api.object.TrackObject;
import com.peoplethink.governmentjob.util.Helper;

import java.util.ArrayList;

public class SoundCloudClient {

	private String clientId;
	private String mPrefixClientId;

    //Constants
    public static final String BASEURL = "http://api.soundcloud.com/";
    public static final String USER="users";
    public static final String TRACKS="tracks";
    public static final String COMMENTS="comments";
    public static final String PLAYLISTS="playlists";

    public static final String JSON_PREFIX=".json";

    public static final String FORMAT_CLIENT_ID="?client_id=%1$s";
    public static final String FORMAT_OFFSET="&offset=%1$s&limit=%2$s";
    public static final String FORMAT_FILTER_QUERY="&q=%1$s";
    public static final String FORMAT_STREAM = "https://api.soundcloud.com/tracks/%1$s/stream?client_id=%2$s";

    public SoundCloudClient(String clientId) {
		this.clientId = clientId;
		this.mPrefixClientId = String.format(FORMAT_CLIENT_ID, clientId);
	}
	
	public ArrayList<TrackObject> getListTrackObjectsByQuery(String query, int offset, int limit){
		StringBuilder builder = new StringBuilder();
		builder.append(BASEURL);
		builder.append(TRACKS);
		builder.append(JSON_PREFIX);
		builder.append(mPrefixClientId);
		builder.append(String.format(FORMAT_FILTER_QUERY, query));
		builder.append(String.format(FORMAT_OFFSET, String.valueOf(offset), String.valueOf(limit)));

		String url = builder.toString();

        return SoundCloudParser.parsingListTrackObject(Helper.getJSONArrayFromUrl(url), this);
	}
	
	public ArrayList<TrackObject> getListTrackObjectsOfUser(long userId, int offset, int limit){
		StringBuilder builder = new StringBuilder();
		builder.append(BASEURL);
		builder.append(USER+"/");
		builder.append(String.valueOf(userId)+"/");
		builder.append(TRACKS);
		builder.append(JSON_PREFIX);
		builder.append(mPrefixClientId);
		builder.append(String.format(FORMAT_OFFSET, String.valueOf(offset), String.valueOf(limit)));

		String url = builder.toString();

        return SoundCloudParser.parsingListTrackObject(Helper.getJSONArrayFromUrl(url), this);
    }

    public ArrayList<TrackObject> getListTrackObjectsOfPlaylist(long playListID, int offset, int limit){
        StringBuilder builder = new StringBuilder();
        builder.append(BASEURL);
        builder.append(PLAYLISTS+"/");
        builder.append(String.valueOf(playListID)+"/");
        builder.append(TRACKS);
        builder.append(JSON_PREFIX);
        builder.append(mPrefixClientId);
        builder.append(String.format(FORMAT_OFFSET, String.valueOf(offset), String.valueOf(limit)));

        String url = builder.toString();

        return SoundCloudParser.parsingListTrackObject(Helper.getJSONArrayFromUrl(url), this);
    }
	
	public ArrayList<CommentObject> getListCommentObject(long trackId){
		StringBuilder builder = new StringBuilder();
		builder.append(BASEURL);
		builder.append(TRACKS+"/");
		builder.append(String.valueOf(trackId)+"/");
		builder.append(COMMENTS);
		builder.append(JSON_PREFIX);
		builder.append(mPrefixClientId);

        String url = builder.toString();

        return SoundCloudParser.parsingListCommentObject(Helper.getJSONArrayFromUrl(url));

    }

    /**
	public TrackObject getTrackObject(long id){
		StringBuilder builder = new StringBuilder();
		builder.append(URL_API);
		builder.append(METHOD_TRACKS);
		builder.append("/");
		builder.append(String.valueOf(id));
		builder.append(JSON_PREFIX);
		builder.append(mPrefixClientId);
		String url = builder.toString();
		builder=null;

        return SoundCloudJsonParsingUtils.parsingTrackObject(Helper.getJSONObjectFromUrl(url));

     }
     **/

	public String getClientId(){
		return clientId;
	}

	
	
}
