package com.peoplethink.governmentjob.providers.facebook;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
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
import com.peoplethink.governmentjob.attachmentviewer.ui.VideoPlayerActivity;
import com.peoplethink.governmentjob.comments.CommentsActivity;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.WebHelper;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class FacebookAdapter extends InfiniteRecyclerViewAdapter {
	
	private Context context;
    private List<FacebookItem> objects;

    public FacebookAdapter(Context context, List<FacebookItem> objects, LoadMoreListener listener) {
        super(context, listener);
        this.objects = objects;
    	this.context = context;
    }

    @Override
    protected int getViewType(int position) {
        return 0;
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_fb_insta_row, parent, false);
        return new FacebookItemViewHolder(itemView);
    }


    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FacebookItemViewHolder){
            final FacebookItemViewHolder viewHolder = (FacebookItemViewHolder) holder;
            final FacebookItem post = objects.get(position);

            viewHolder.profilePicImg.setImageDrawable(null);
            Picasso.with(context).load(post.profilePhotoUrl).into(viewHolder.profilePicImg);

            String userNameView  = post.username.substring(0,1).toUpperCase(Locale.getDefault()) + post.username.substring(1).toLowerCase(Locale.getDefault());
            viewHolder.userNameView.setText(userNameView);

            viewHolder.dateView.setText(DateUtils.getRelativeDateTimeString(context,post.createdTime.getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));

            viewHolder.inlineImg.setImageDrawable(null);

            Picasso.with(context).load(post.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.inlineImg);

            viewHolder.inlineImg.setTag(position);
            if (post.type.equals("video")){
                viewHolder.inlineImgBtn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.inlineImgBtn.setVisibility(View.GONE);
            }

            if (post.type.equals("photo")){
                viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {

                        AttachmentActivity.startActivity(context, MediaAttachment.withImage(
                                objects.get((int) viewHolder.inlineImg.getTag()).imageUrl
                        ));

                    }
                });
            }
            else if (post.type.equals("video")) {
                View.OnClickListener videoListener = new View.OnClickListener() {
                    public void onClick(View arg0) {

                        VideoPlayerActivity.startActivity(context, objects.get((int) viewHolder.inlineImg.getTag()).videoUrl);
                    }
                };

                viewHolder.inlineImgBtn.setOnClickListener(videoListener);
                viewHolder.inlineImg.setOnClickListener(videoListener);
            }

            viewHolder.likesCountView.setText(Helper.formatValue(post.likesCount));

            viewHolder.contentView.setText(Html.fromHtml(post.caption.replace("\n", "<br>")));
            viewHolder.contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));

            viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);

                    // this is the text that will be shared
                    sendIntent.putExtra(Intent.EXTRA_TEXT,post.link);

                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, context.getResources()
                            .getString(R.string.share_header)));
                }
            });

            viewHolder.openBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    HolderActivity.startWebViewActivity(context, post.link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);
                }
            });

            viewHolder.commentsCountView.setText(Helper.formatValue(post.commentsCount));
            viewHolder.commentsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    // Start NewActivity.class
                    Intent commentIntent = new Intent(context, CommentsActivity.class);
                    commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, post.commentsArray.toString());
                    commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.FACEBOOK);
                    context.startActivity(commentIntent);
                }
            });
        }
    }

    @Override
    protected int getCount() {
        return objects.size();
    }

    private class FacebookItemViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicImg;

        TextView userNameView;
        TextView dateView;
        ImageView inlineImg;
        FloatingActionButton inlineImgBtn;
        TextView likesCountView;
        TextView commentsCountView;
        TextView contentView;

        ImageView shareBtn;
        ImageView openBtn;
        ImageView commentsBtn;

        FacebookItemViewHolder(View view){
            super(view);

            profilePicImg = view.findViewById(R.id.profile_image);
            userNameView = view.findViewById(R.id.name);
            dateView = view.findViewById(R.id.date);
            inlineImg = view.findViewById(R.id.photo);
            inlineImgBtn = view.findViewById(R.id.playbutton);
            likesCountView = view.findViewById(R.id.like_count);
            commentsCountView = view.findViewById(R.id.comments_count);
            contentView = view.findViewById(R.id.message);
            shareBtn = view.findViewById(R.id.share);
            openBtn = view.findViewById(R.id.open);
            commentsBtn = view.findViewById(R.id.comments);

        }
    }
}
