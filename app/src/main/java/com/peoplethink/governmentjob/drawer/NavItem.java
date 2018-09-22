package com.peoplethink.governmentjob.drawer;

import android.content.Context;
import android.support.v4.app.Fragment;

import java.io.Serializable;

public class NavItem implements Serializable {
	
    private String mText;
    private int mTextResource;
    private String[] mData;
    private Class<? extends Fragment> mFragment;

    public String categoryImageUrl;

    //Create a new item with a resource string, resource drawable, type, fragment and data
    public NavItem(int text, Class<? extends Fragment> fragment, String[] data) {
        this(null, fragment, data);
        mTextResource = text;
    }

    //Create a new item with a text string, resource drawable, type, fragment, data and purchase requirement
    public NavItem(String text, Class<? extends Fragment> fragment, String[] data) {
        mText = text;
        mFragment = fragment;
        mData = data;
    }

    public String getText(Context c) {
        if (mText != null) {
            return mText;
        } else {
            return c.getResources().getString(mTextResource);
        }
    }
    
    public Class<? extends Fragment> getFragment() {
    	return mFragment;
    }
    
    public String[] getData() {
    	return mData;
    }

    public void setCategoryImageUrl(String url){
        this.categoryImageUrl = url;
    }
}
