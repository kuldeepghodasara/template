package com.peoplethink.governmentjob.providers.soundcloud.player.player;

import com.peoplethink.governmentjob.providers.soundcloud.api.object.TrackObject;

/**
 * Listener used to catch events performed on the play playlist.
 */
public interface CheerleaderPlaylistListener {

    /**
     * Called when a tracks has been added to the player playlist.
     *
     * @param track track added.
     */
    void onTrackAdded(TrackObject track);


    /**
     * Called when a tracks has been removed from the player playlist.
     *
     * @param track   track removed.
     * @param isEmpty true if the playlist is empty after deletion.
     */
    void onTrackRemoved(TrackObject track, boolean isEmpty);
}
