package com.peoplethink.governmentjob.providers.youtube;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.youtube.api.object.Video;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Setting our custom listview rows with the retrieved videos
 */
public class VideosAdapter extends InfiniteRecyclerViewAdapter {

    private List<Video> videos;
    private Context mContext;
    private AdapterView.OnItemClickListener clickListener;

    //Post types
    private final static int VIDEO = 0;
    private final static int HIGHLIGHT_VIDEO = 1;

    public VideosAdapter(Context context, List<Video> videos, LoadMoreListener listener, AdapterView.OnItemClickListener clickListener) {
        super(context, listener);
        this.mContext = context;
        this.videos = videos;
        this.clickListener = clickListener;
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    protected int getViewType(int position) {
        if (position == 0 || Config.YT_ROW_IMMERSIVE) {
            return HIGHLIGHT_VIDEO;
        } else {
            return VIDEO;
        }
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIDEO) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_youtube_row, parent, false);
            return new VideoViewHolder(itemView);
        } else if (viewType == HIGHLIGHT_VIDEO) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_highlight, parent, false);
            RecyclerView.ViewHolder holder = new HighlightViewHolder(itemView);
            requestFullSpan(holder);
            return holder;
        }
        return null;
    }

    @Override
    protected void doBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Video video = videos.get(position);
        if (holder instanceof HighlightViewHolder) {
            HighlightViewHolder highlightViewHolder = (HighlightViewHolder) holder;

            Picasso.with(mContext).load(video.getImage()).placeholder(R.color.gray).into(highlightViewHolder.thumb);
            highlightViewHolder.title.setText(video.getTitle());
            highlightViewHolder.date.setText(video.getUpdated());

        } else if (holder instanceof VideoViewHolder) {

            VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
            videoViewHolder.thumb.setImageDrawable(null);
            videoViewHolder.title.setText(video.getTitle());
            videoViewHolder.date.setText(video.getUpdated());

            //setting the image
            Picasso.with(mContext).load(video.getThumbUrl()).placeholder(R.color.gray).into(videoViewHolder.thumb);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(null, holder.itemView, position, 0);
            }
        });
    }

    private static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        ImageView thumb;

        VideoViewHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.userVideoTitleTextView);
            this.date = view.findViewById(R.id.userVideoDateTextView);
            this.thumb = view.findViewById(R.id.userVideoThumbImageView);

        }
    }

    private static class HighlightViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        ImageView thumb;

        HighlightViewHolder(View view) {
            super(view);
            this.date = view.findViewById(R.id.textViewDate);
            this.title = view.findViewById(R.id.textViewHighlight);
            this.thumb = view.findViewById(R.id.imageViewHighlight);
        }
    }

}