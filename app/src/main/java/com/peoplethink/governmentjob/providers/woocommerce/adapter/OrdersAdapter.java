package com.peoplethink.governmentjob.providers.woocommerce.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.PriceFormat;
import com.peoplethink.governmentjob.providers.woocommerce.model.orders.Order;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;

import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends InfiniteRecyclerViewAdapter {
    private Context mContext;
    private List<Order> ordersList;
    private float itemWidth;

    private View headerView;

    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_HEADER = 1;

    private class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDescription, orderDate, orderTotal, orderStatus;

        View view;

        OrderViewHolder(View view) {
            super(view);

            this.view = view;
            orderDescription = view.findViewById(R.id.orderDescription);
            orderDate = view.findViewById(R.id.orderDate);
            orderTotal = view.findViewById(R.id.orderTotal);
            orderStatus = view.findViewById(R.id.orderStatus);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View view) {
            super(view);
        }
    }

    public OrdersAdapter(Context mContext, List<Order> ordersList, LoadMoreListener listener) {
        super(mContext, listener);
        this.mContext = mContext;
        this.ordersList = ordersList;
    }

    @Override
    protected int getViewType(int position) {
        if (headerView != null && position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_wc_order, parent, false);
            return new OrderViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            RecyclerView.ViewHolder holder = new HeaderViewHolder(headerView);
            requestFullSpan(holder);
            return holder;
        }
        return null;
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof OrderViewHolder) {
            OrderViewHolder holder = (OrderViewHolder) viewHolder;

            final Order order = ordersList.get(position - ((headerView == null) ? 0 : 1));

            final String description = (order.getLineItems().size() == 1) ?
                    String.format(mContext.getString(R.string.order_item), order.getId(), order.getLineItems().get(0).getName()) :
                    String.format(mContext.getString(R.string.order_items), order.getId(), order.getLineItems().size());

            final String total = PriceFormat.formatPrice(order.getTotal());
            final String status = order.getStatus();
            final String date = DateUtils.getRelativeDateTimeString(mContext,
                    order.getDateCreated().getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();

            holder.orderDescription.setText(description);
            holder.orderTotal.setText(total);
            holder.orderDate.setText(date);
            holder.orderStatus.setText(status);

            if (itemWidth > 0) {
                holder.view.getLayoutParams().width = (int) itemWidth;
            }
        } else if (viewHolder instanceof HeaderViewHolder) {
            //Layout is already loaded
            requestFullSpan(viewHolder);
        }
    }

    public void setItemWidth(float width) {
        this.itemWidth = width;
    }

    /**
     * Ensures that HeaderViewHolder still receives a full span, without refreshing the list
     *
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof HeaderViewHolder)
            requestFullSpan(holder);
    }

    @Override
    public int getCount() {
        return ordersList.size() + ((headerView == null) ? 0 : 1);
    }

    public void setHeader(View headerView) {
        this.headerView = headerView;
        notifyDataSetChanged();
    }

}

