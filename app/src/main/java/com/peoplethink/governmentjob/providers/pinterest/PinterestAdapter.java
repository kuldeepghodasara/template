package com.peoplethink.governmentjob.providers.pinterest;

import android.content.Context;
import android.content.Intent;
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
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.peoplethink.governmentjob.util.WebHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

class PinterestAdapter extends InfiniteRecyclerViewAdapter {
	
	private Context context;
    private List<Pin> objects;

    PinterestAdapter(Context context, List<Pin> objects, LoadMoreListener listener) {
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_pinterest_row, parent, false);
        return new PinterestViewHolder(itemView);
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PinterestViewHolder){
            final Pin pin = objects.get(position);
            PinterestViewHolder viewHolder = (PinterestViewHolder) holder;

            viewHolder.profileImg.setImageDrawable(null);
            Picasso.with(context).load(pin.creatorImageUrl).into(viewHolder.profileImg);

            viewHolder.userNameView.setText(pin.creatorName);

            viewHolder.dateView.setText(DateUtils.getRelativeDateTimeString(context,pin.createdTime.getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));

            viewHolder.inlineImg.setImageDrawable(null);
            Picasso.with(context).load(pin.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.inlineImg);

            if (pin.type.equals("image")){
                viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {

                        AttachmentActivity.startActivity(context, MediaAttachment.withImage(
                                pin.imageUrl
                        ));

                    }
                });
            }
            else {
                viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        HolderActivity.startWebViewActivity(context, pin.link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);

                    }
                });
            }

            viewHolder.repinCountView.setText(Helper.formatValue(pin.repinCount));

            if (pin.caption != null){
                viewHolder.descriptionView.setText(Html.fromHtml(pin.caption));
                viewHolder.descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
            }

            viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);

                    // this is the text that will be shared
                    sendIntent.putExtra(Intent.EXTRA_TEXT,pin.link);

                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, context.getResources()
                            .getString(R.string.share_header)));
                }
            });

            viewHolder.openBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    HolderActivity.startWebViewActivity(context, pin.link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);
                }
            });

            // Set comments
            if (pin.commentsCount == 0) viewHolder.commentsView.setVisibility(View.GONE);
            else {
                viewHolder.commentsView.setVisibility(View.VISIBLE);
                viewHolder.commentsCountView.setText(Helper.formatValue(pin.commentsCount));
            }
        }
    }

    @Override
    protected int getCount() {
        return objects.size();
    }

    private class PinterestViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImg;
        ImageView inlineImg;
        
        TextView userNameView;
        TextView dateView;
        TextView repinCountView;
        TextView descriptionView;
        ImageView commentsView;
        TextView commentsCountView;
        
        ImageView shareBtn;
        ImageView openBtn;

        private PinterestViewHolder(View view){
            super(view);

            this.profileImg = view.findViewById(R.id.profile_image);
            this.userNameView = view.findViewById(R.id.name);
            this.dateView = view.findViewById(R.id.date);
            this.inlineImg = view.findViewById(R.id.photo);
            this.repinCountView = view.findViewById(R.id.like_count);
            this.descriptionView = view.findViewById(R.id.message);
            this.commentsView = view.findViewById(R.id.comments);
            this.commentsCountView = view.findViewById(R.id.comments_count);

            this.shareBtn = view.findViewById(R.id.share);
            this.openBtn = view.findViewById(R.id.open);

        }
    }
}
