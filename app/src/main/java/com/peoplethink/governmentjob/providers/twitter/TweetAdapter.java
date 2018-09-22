package com.peoplethink.governmentjob.providers.twitter;

/**
 * This class is used to create an adapter of the tweets, and fill the listview
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.attachmentviewer.model.MediaAttachment;
import com.peoplethink.governmentjob.attachmentviewer.ui.AttachmentActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.WebHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class TweetAdapter extends InfiniteRecyclerViewAdapter {

    private Context context;
    private ArrayList<Tweet> tweets;

    public TweetAdapter(Context context, ArrayList<Tweet> tweets, LoadMoreListener listener) {
        super(context, listener);
        this.context = context;
        this.tweets = tweets;
    }

    @Override
    protected int getViewType(int position) {
        return 0;
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tweets_row, parent, false);
        return new TweetHolder(itemView);
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof TweetHolder) {
            TweetHolder holder = (TweetHolder) viewHolder;
            final Tweet tweet = tweets.get(position);

            holder.name.setText(tweet.getname());
            holder.username.setText("@" + tweet.getusername());
            holder.date.setText(tweet.getData());
            holder.message.setText(Html.fromHtml(tweet.getmessage()));
            holder.message.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
            holder.retweetCount.setText(Helper.formatValue(tweet.getRetweetCount()));

            Picasso.with(context).load(tweet.geturlProfileImage()).into(holder.imagem);

            if (tweet.getImageUrl() != null) {
                holder.photo.setVisibility(View.VISIBLE);
                Picasso.with(context).load(tweet.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.photo);

                holder.photo.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {

                        AttachmentActivity.startActivity(context, MediaAttachment.withImage(
                                tweet.getImageUrl()
                        ));
                    }
                });
            } else {
                holder.photo.setImageDrawable(null);
                holder.photo.setVisibility(View.GONE);
            }

            holder.itemView.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String link = ("http://twitter.com/" + tweet.getusername() + "/status/" + tweet.getTweetId());
                    // this is the text that will be shared
                    sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, tweet.getusername()
                            + context.getResources().getString(R.string.tweet_share_header));

                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, context.getResources()
                            .getString(R.string.share_header)));
                }
            });

            holder.itemView.findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    String link = ("http://twitter.com/" + tweet.getusername() + "/status/" + tweet.getTweetId());
                    HolderActivity.startWebViewActivity(context, link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);

                }
            });
        }

    }


    @Override
    protected int getCount() {
        return tweets.size();
    }

    private class TweetHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView username;
        ImageView imagem;
        ImageView photo;
        TextView message;
        TextView retweetCount;
        TextView date;

        TweetHolder(View view) {
            super(view);

            name = view.findViewById(R.id.name);
            username = view.findViewById(R.id.username);
            imagem = view.findViewById(R.id.profile_image);
            photo = view.findViewById(R.id.photo);
            message = view.findViewById(R.id.message);
            retweetCount = view.findViewById(R.id.retweet_count);
            date = view.findViewById(R.id.date);

        }
    }
}

