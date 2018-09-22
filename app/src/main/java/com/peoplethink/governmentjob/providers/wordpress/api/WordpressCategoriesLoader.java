package com.peoplethink.governmentjob.providers.wordpress.api;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.apg.mobile.roundtextview.BadgeView;
import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.wordpress.CategoryItem;
import com.peoplethink.governmentjob.providers.wordpress.ui.WordpressFragment;
import com.peoplethink.governmentjob.util.Helper;

import java.util.ArrayList;

/**
 * Simply loads data from an url (gotten from a provider) and loads it into a list.
 * Various attributes of this list and the way to load are defined in a WordpressGetTaskInfo.
 */
public class WordpressCategoriesLoader implements WordpressGetTaskInfo.ListListener{

    private WordpressGetTaskInfo mInfo;
    private ArrayList<CategoryItem> categoryItems;

    public WordpressCategoriesLoader(WordpressGetTaskInfo info) {
        this.mInfo = info;
    }

    public void load(){
        WordpressCategoriesTask categoriesTask = new WordpressCategoriesTask(mInfo, new WordpressCategoriesTask.WordpressCategoriesCallback() {
            @Override
            public void categoriesLoaded(ArrayList<CategoryItem> result) {
                categoryItems = result;
                if (mInfo.adapter != null && mInfo.adapter.getCount() > 0)
                    createSlider(result);
                else
                    mInfo.setListener(WordpressCategoriesLoader.this);
            }

            @Override
            public void categoriesFailed() {

            }
        });
        if (Config.WP_CHIPS)
            categoriesTask.execute();
    }

    public void createSlider(ArrayList<CategoryItem> categoryItems){
        LayoutInflater layoutInflater = LayoutInflater.from(mInfo.context);
        final HorizontalScrollView sliderView = (HorizontalScrollView) layoutInflater.inflate(R.layout.listview_slider, null);
        for (final CategoryItem item : categoryItems){
            FrameLayout categoryChip = (FrameLayout) layoutInflater.inflate(R.layout.listview_slider_chip, null);

            BadgeView categoryChipText = categoryChip.findViewById(R.id.category_chip);
            categoryChipText.setBadgeMainText(item.getName());
            categoryChipText.setBadgeSubText(Helper.formatValue(item.getPostCount()));
            categoryChipText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HolderActivity.startActivity(mInfo.context, WordpressFragment.class, new String[]{mInfo.baseurl,item.getId()});
                }
            });
            ((LinearLayout) sliderView.findViewById(R.id.slider_content)).addView(categoryChip);
        }

        mInfo.adapter.setSlider(sliderView);

        //Animate the appearance
        sliderView.setAlpha(0);
        sliderView.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public void completedWithPosts() {
        createSlider(categoryItems);
    }
}
