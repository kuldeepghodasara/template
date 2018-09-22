package com.peoplethink.governmentjob.providers.overview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.drawer.NavItem;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */

public class CategoryAdapter extends InfiniteRecyclerViewAdapter {
    private static final int TEXT_TYPE = 0;
    private static final int IMAGE_TYPE = 1;

    private List<NavItem> data;
    private Context context;
    private OnOverViewClick callback;

    private int number;

    public CategoryAdapter(List<NavItem> data, Context context, OnOverViewClick click) {
        super(context, null);
        this.data = data;
        this.context = context;
        this.callback = click;
    }

    @Override
    protected int getViewType(int position) {
        if (position >= 0 && position < data.size()){
            if (data.get(position).categoryImageUrl != null && !data.get(position).categoryImageUrl.isEmpty())
                return IMAGE_TYPE;
            else
                return TEXT_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == TEXT_TYPE)
            return new TextViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_overview_card_text, parent, false));
        else if (viewType == IMAGE_TYPE)
            return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_overview_card_image, parent, false));

        return null;
    }

    @Override
    protected void doBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.onOverViewSelected(data.get(holder.getAdapterPosition()));
            }
        });

        if (holder instanceof TextViewHolder) {

            ((TextViewHolder) holder).title.setText(data.get(position).getText(context));
            ((TextViewHolder) holder).background.setBackgroundResource(randomGradientResource());

        }  else if (holder instanceof ImageViewHolder) {

            Picasso.with(context)
                    .load(data.get(position).categoryImageUrl)
                    .placeholder(R.color.black_more_translucent)
                    .into(((ImageViewHolder) holder).image);
            ((ImageViewHolder) holder).title.setText(data.get(position).getText(context));
        }
    }

    @Override
    protected int getCount() {
        return data.size();
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView image;

        public View itemView;

        private ImageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
        }
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public View background;

        public View itemView;

        private TextViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            background =  itemView.findViewById(R.id.background);
            title = itemView.findViewById(R.id.title);
        }
    }

    private int randomGradientResource(){
        number += 1;
        if (number == 6) number = 1;

        return Helper.getGradient(number);
    }

    public interface OnOverViewClick{
        void onOverViewSelected(NavItem item);
    }


}