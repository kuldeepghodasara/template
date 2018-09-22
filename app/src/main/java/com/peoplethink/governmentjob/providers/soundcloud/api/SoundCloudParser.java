package com.peoplethink.governmentjob.providers.soundcloud.api;

import com.peoplethink.governmentjob.providers.soundcloud.api.object.CommentObject;
import com.peoplethink.governmentjob.providers.soundcloud.api.object.TrackObject;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SoundCloudParser {
	
	public static final String TAG = SoundCloudParser.class.getSimpleName();

	public static TrackObject parsingTrackObject(JSONObject mJsonObject, SoundCloudClient api){
		if(mJsonObject!=null){
			try {
				long id = mJsonObject.getLong("id");
				String createdAt = mJsonObject.getString("created_at");
				Date date = null;
				try {
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss Z");
					date = format.parse(createdAt);
				} catch (ParseException e) {
					Log.printStackTrace(e);
				}

				long userId = mJsonObject.getLong("user_id");
				long duration = mJsonObject.getLong("duration");
				String sharing = mJsonObject.getString("sharing");
				String tagList = mJsonObject.getString("tag_list");
				String genre = mJsonObject.getString("genre");
				String title = mJsonObject.getString("title");
				String description= mJsonObject.getString("description");

				JSONObject mJsUser = mJsonObject.getJSONObject("user");
				String username = mJsUser.getString("username");
				String avartar = mJsUser.getString("avatar_url");
				String permalinkUrl= mJsonObject.getString("permalink_url");
				String artworkUrl = mJsonObject.getString("artwork_url");
				String waveformUrl = mJsonObject.getString("waveform_url");
				long playCount = mJsonObject.getLong("playback_count");
				long favoritingsCount = mJsonObject.getLong("favoritings_count");
				long commentCount = mJsonObject.getLong("comment_count");
				boolean streamable = mJsonObject.getBoolean("streamable");
				String streamUrl = String.format(SoundCloudClient.FORMAT_STREAM, id, api.getClientId());

				TrackObject mTrackObject = new TrackObject(id, date, userId, duration, sharing,
						tagList, genre, title, description, username, avartar, permalinkUrl, artworkUrl, waveformUrl,
						playCount, favoritingsCount, commentCount, streamUrl);
				mTrackObject.setStreamAble(streamable);

				return mTrackObject;

			}
			catch (JSONException e) {
				Log.printStackTrace(e);
			}
		}
		return null;
	}

	public static ArrayList<TrackObject> parsingListTrackObject(JSONArray rawTracks, SoundCloudClient api) {
		try {

				int size = rawTracks.length();
				ArrayList<TrackObject> listTrackObjects = new ArrayList<TrackObject>();

				if(size>0){
					for(int i=0;i<size;i++){
						TrackObject mTrackObject = parsingTrackObject(rawTracks.getJSONObject(i), api);
						if(mTrackObject!=null){
							listTrackObjects.add(mTrackObject);
						}
					}
				}

				return listTrackObjects;

			}
			catch (Exception e) {
				Log.printStackTrace(e);
			}
		return null;
	}
	
	private static CommentObject parsingCommentObject(JSONObject object) {
		try {
			long id = object.getLong("id");
			String createdAt = object.getString("created_at");
			Date date = null;
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss Z");
				date = format.parse(createdAt);
			} catch (ParseException e) {
				Log.printStackTrace(e);
			}

			long userId = object.getLong("user_id");
			long trackId = object.getLong("track_id");
			int timestamp = object.getInt("timestamp");
			String body = object.getString("body");

			JSONObject user = object.getJSONObject("user");
			String username = user.getString("username");
			String avatarUrl = user.getString("avatar_url");

			CommentObject mCommentObject = new CommentObject(id, trackId, userId, date, timestamp, body, username, avatarUrl);

			return mCommentObject;
		} catch (JSONException e) {
			Log.printStackTrace(e);
		}
		return null;
	}
	
	public static ArrayList<CommentObject> parsingListCommentObject(JSONArray rawComments) {
		try {
			int size = rawComments.length();
			if(size>0){
				ArrayList<CommentObject> listCommentObjects = new ArrayList<CommentObject>();
				for(int i=0;i<size;i++){
					CommentObject mTrackObject = parsingCommentObject(rawComments.getJSONObject(i));
					if(mTrackObject!=null){
						listCommentObjects.add(mTrackObject);
					}
				}
				return listCommentObjects;
			}

		}
		catch (Exception e) {
			Log.printStackTrace(e);
		}
		return null;
	}
}
