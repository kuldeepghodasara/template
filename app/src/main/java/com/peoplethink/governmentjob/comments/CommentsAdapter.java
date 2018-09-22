package com.peoplethink.governmentjob.comments;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsAdapter extends ArrayAdapter<Comment> {
	
	private Context context;
	private int type;
	
    public CommentsAdapter(Context context, List<Comment> objects, int type) {
        super(context, 0, objects);
        this.context = context;
        this.type = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Comment comment = getItem(position);
        CommentViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_comments_row, parent, false);
            viewHolder = new CommentViewHolder();
            viewHolder.ivProfilePhoto = convertView.findViewById(R.id.ivProfilePhoto);
            viewHolder.tvUsername = convertView.findViewById(R.id.tvUsername);
            viewHolder.tvComment = convertView.findViewById(R.id.tvComment);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (CommentViewHolder)convertView.getTag();
        }

        viewHolder.ivProfilePhoto.setImageDrawable(null);
        if (comment.profileUrl != null){
        	viewHolder.ivProfilePhoto.setVisibility(View.VISIBLE);

            Picasso.with(context).load(comment.profileUrl).into(viewHolder.ivProfilePhoto);
        } else {
        	viewHolder.ivProfilePhoto.setVisibility(View.GONE);
        }

        viewHolder.tvUsername.setText(comment.username);

        viewHolder.tvComment.setText(Html.fromHtml(comment.text.replaceAll("<img.+?>", "")));
        
        if (type == CommentsActivity.WORDPRESS_JETPACK || type == CommentsActivity.WORDPRESS_JSON || type == CommentsActivity.WORDPRESS_REST){
    		LinearLayout lineView = convertView.findViewById(R.id.lineView);
        	
        	lineView.removeAllViews();
        	for (int i = 0; i < comment.linesCount; i++) {
        		View line = View.inflate(context, R.layout.activity_comment_sub, null);
        		lineView.addView(line);
        	}
        }

        return convertView;
    }
    
    public class CommentViewHolder {
        ImageView ivProfilePhoto;
        TextView tvUsername;
        TextView tvComment;
    }

}
