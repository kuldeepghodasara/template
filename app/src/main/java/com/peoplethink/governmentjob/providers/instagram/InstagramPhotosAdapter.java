package com.peoplethink.governmentjob.providers.instagram;

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

public class InstagramPhotosAdapter  extends InfiniteRecyclerViewAdapter{
	
	private Context context;
    private List<InstagramPhoto> objects;

    public InstagramPhotosAdapter(Context context, List<InstagramPhoto> objects, InfiniteRecyclerViewAdapter.LoadMoreListener listener) {
        super(context, listener);
    	this.context = context;
        this.objects = objects;
    }

    @Override
    protected int getViewType(int position) {
        return 0;
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_fb_insta_row, parent, false);
        return new InstagramPhotoViewHolder(itemView);
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InstagramPhotoViewHolder){
            final InstagramPhoto photo = objects.get(position);
            InstagramPhotoViewHolder viewHolder = (InstagramPhotoViewHolder) holder;

            viewHolder.profileImg.setImageDrawable(null);
            Picasso.with(context).load(photo.profilePhotoUrl).into(viewHolder.profileImg);

            String username  = photo.username.substring(0,1).toUpperCase(Locale.getDefault()) +
                    photo.username.substring(1).toLowerCase(Locale.getDefault());
            viewHolder.userNameView.setText(username);

            viewHolder.dateView.setText(
                    DateUtils.getRelativeDateTimeString(context,photo.createdTime.getTime(),
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL));

            viewHolder.inlineImg.setImageDrawable(null);
            Picasso.with(context).load(photo.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.inlineImg);

            if (photo.type.equals("video")){
                viewHolder.inlineImgBtn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.inlineImgBtn.setVisibility(View.GONE);
            }

            if (photo.type.equals("image")){
                viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        AttachmentActivity.startActivity(context, MediaAttachment.withImage(
                                photo.imageUrl
                        ));
                    }
                });
            }
            else if (photo.type.equals("video")){
                View.OnClickListener videoListener = new View.OnClickListener() {
                    public void onClick(View arg0) {
                        VideoPlayerActivity.startActivity(context, photo.videoUrl);
                    }
                };

                viewHolder.inlineImgBtn.setOnClickListener(videoListener);
                viewHolder.inlineImg.setOnClickListener(videoListener);
            }

            viewHolder.likesCountView.setText(Helper.formatValue(photo.likesCount));

            if (photo.caption != null){
                viewHolder.descriptionView.setText(Html.fromHtml(photo.caption));
                viewHolder.descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
                viewHolder.descriptionView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.descriptionView.setVisibility(View.GONE);
            }

            viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);

                    // this is the text that will be shared
                    sendIntent.putExtra(Intent.EXTRA_TEXT,photo.link);

                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, context.getResources()
                            .getString(R.string.share_header)));
                }
            });

            viewHolder.openBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    HolderActivity.startWebViewActivity(context, photo.link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);

                }
            });

            // Set comments
            viewHolder.commentsCountView.setText(Helper.formatValue(photo.commentsCount));

            viewHolder.commentsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    // Start NewActivity.class
                    Intent commentIntent = new Intent(context, CommentsActivity.class);
                    commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.INSTAGRAM);
                    commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, photo.commentsJson);
                    commentIntent.putExtra(CommentsActivity.DATA_ID, photo.id);
                    context.startActivity(commentIntent);
                }
            });
        }
    }

    @Override
    protected int getCount() {
        return objects.size();
    }

    private class InstagramPhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImg;
        ImageView inlineImg;
        FloatingActionButton inlineImgBtn;

        TextView userNameView;
        TextView dateView;
        TextView likesCountView;
        TextView commentsCountView;
        TextView descriptionView;
        
        ImageView shareBtn;
        ImageView openBtn;
        ImageView commentsBtn;

        InstagramPhotoViewHolder(View view){
            super(view);

            this.profileImg = view.findViewById(R.id.profile_image);
            this.userNameView = view.findViewById(R.id.name);
            this.dateView = view.findViewById(R.id.date);
            this.inlineImg = view.findViewById(R.id.photo);
            this.inlineImgBtn = view.findViewById(R.id.playbutton);
            this.likesCountView = view.findViewById(R.id.like_count);
            this.descriptionView = view.findViewById(R.id.message);
            this.descriptionView = view.findViewById(R.id.message);
            this.shareBtn = view.findViewById(R.id.share);
            this.openBtn = view.findViewById(R.id.open);
            this.commentsBtn = view.findViewById(R.id.comments);
            this.commentsCountView = view.findViewById(R.id.comments_count);

        }
    }
}
