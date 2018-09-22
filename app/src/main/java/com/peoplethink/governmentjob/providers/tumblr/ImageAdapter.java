package com.peoplethink.governmentjob.providers.tumblr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.tumblr.ui.TumblrPagerActivity;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends InfiniteRecyclerViewAdapter {

	private ArrayList<TumblrItem> listData;
	private Context mContext;
	
	public ImageAdapter(Context context, ArrayList<TumblrItem> listData, LoadMoreListener listener) {
		super(context, listener);
		this.listData = listData;
		mContext = context;
	}

	@Override
	protected int getViewType(int position) {
		return 0;
	}

	@Override
	protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tumblr_row, parent, false);
		return new ItemViewHolder(itemView);
	}

	@Override
	protected void doBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		if (holder instanceof ItemViewHolder){
			Picasso.with(mContext)
					.load(listData.get(position).getUrl())
					.placeholder(R.drawable.placeholder)
					.fit().centerCrop()
					.into(((ItemViewHolder) holder).imageView);

			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					startImagePagerActivity(position);
				}
			});
		}
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ItemViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		private ItemViewHolder(View view){
			super(view);
			this.imageView = view.findViewById(R.id.image);
		}
	}

	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(mContext, TumblrPagerActivity.class);

		ArrayList<TumblrItem> underlying =  new ArrayList<TumblrItem>();
		for (int i = 0; i < getCount(); i++)
			underlying.add(listData.get(i));

		Bundle b = new Bundle();
		b.putParcelableArrayList(Constants.Extra.IMAGES, underlying);
		intent.putExtras(b);
		intent.putExtra(Constants.Extra.IMAGE_POSITION, position);
		mContext.startActivity(intent);
	}
}
