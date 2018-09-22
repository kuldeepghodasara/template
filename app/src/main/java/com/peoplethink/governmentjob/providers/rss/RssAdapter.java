package com.peoplethink.governmentjob.providers.rss;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.rss.ui.RssDetailActivity;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class RssAdapter extends InfiniteRecyclerViewAdapter {

    private List<RSSItem> objects;
    private Context context;

    public RssAdapter(Context context,
                       List<RSSItem> list) {
        super(context, null);
        this.context = context;
        this.objects = list;
    }

    @Override
    protected int getViewType(int position) {
        return objects.size();
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_rss_row, parent, false);
        return new RssViewHolder(itemView);
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof RssViewHolder){
            final RssViewHolder holder = (RssViewHolder) viewHolder;

            holder.listTitle.setText(objects.get(position).getTitle());
            holder.listPubdate.setText(objects.get(position).getPubdate());

            String html = objects.get(position).getRowDescription();

            holder.listDescription.setText(html);

            holder.listThumb.setImageDrawable(null);

            String thumburl = objects.get(position).getThumburl();
            if (thumburl != null && !thumburl.equals("")) {
                //setting the image
                final ImageView imageView = holder.listThumb; // The view Picasso is loading an image into
                final Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                    /* Save the bitmap or do something with it here */

                        if (10 > bitmap.getWidth() || 10 > bitmap.getHeight()) {
                            // handle scaling
                            holder.listThumb.setVisibility(View.GONE);
                        } else {
                            holder.listThumb.setVisibility(View.VISIBLE);
                            holder.listThumb.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };

                imageView.setTag(target);

                Picasso.with(context)
                        .load(objects.get(position).getThumburl())
                        .into(target);
            } else {
                holder.listThumb.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context,
                            RssDetailActivity.class);
                    Bundle bundle = new Bundle();
                    intent.putExtra(RssDetailActivity.EXTRA_RSSITEM, objects.get(position));

                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    protected int getCount() {
        return objects.size();
    }

    private class RssViewHolder extends RecyclerView.ViewHolder {
        TextView listTitle;
        TextView listPubdate;
        TextView listDescription;
        ImageView listThumb;

        RssViewHolder(View view){
            super(view);
            this.listTitle = view.findViewById(R.id.listtitle);
            this.listPubdate = view.findViewById(R.id.listpubdate);
            this.listDescription = view.findViewById(R.id.shortdescription);
            this.listThumb = view.findViewById(R.id.listthumb);
        }
    }
}
