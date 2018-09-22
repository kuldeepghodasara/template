package com.peoplethink.governmentjob.providers.radio;

import android.graphics.Bitmap;

import java.util.ArrayList;

import com.peoplethink.governmentjob.providers.radio.metadata.Metadata;

public class StaticEventDistributor {

    private static ArrayList<EventListener> listeners;

    public static void registerAsListener(EventListener listener){
        if (listeners == null) listeners = new ArrayList<>();

        listeners.add(listener);
    }

    public static void unregisterAsListener(EventListener listener){
        listeners.remove(listener);
    }

    public static void onEvent(String status){
        if (listeners == null) return;

        for (EventListener listener : listeners){
            listener.onEvent(status);
        }
    }

    public static void onAudioSessionId(Integer id){
        if (listeners == null) return;

        for (EventListener listener : listeners){
            listener.onAudioSessionId(id);
        }
    }

    public static void onMetaDataReceived(Metadata meta, Bitmap image){
        if (listeners == null) return;

        for (EventListener listener : listeners){
            listener.onMetaDataReceived(meta, image);
        }
    }



    public static interface EventListener {
        public void onEvent(String status);
        public void onAudioSessionId(Integer i);
        public void onMetaDataReceived(Metadata meta, Bitmap image);
    }
}
