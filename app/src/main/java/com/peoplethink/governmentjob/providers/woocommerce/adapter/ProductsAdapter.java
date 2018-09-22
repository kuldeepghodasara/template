package com.peoplethink.governmentjob.providers.woocommerce.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.CartAssistant;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.PriceFormat;
import com.peoplethink.governmentjob.providers.woocommerce.model.products.Product;
import com.peoplethink.governmentjob.providers.woocommerce.ui.ProductActivity;
import com.peoplethink.governmentjob.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductsAdapter extends InfiniteRecyclerViewAdapter {
    private Context mContext;
    private List<Product> productList;
    private float itemWidth;

    private View sliderView;
    private View headerView;

    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_SLIDER = 1;
    private final static int TYPE_HEADER = 2;

    private class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productPriceRegular, saleLabel;
        ImageView productImage;
        ImageView overflow;

        View view;

        ProductViewHolder(View view) {
            super(view);

            this.view = view;
            productName = view.findViewById(R.id.productName);
            productPrice = view.findViewById(R.id.productPrice);
            productPriceRegular = view.findViewById(R.id.productPriceRegular);
            productImage = view.findViewById(R.id.productImage);
            overflow = view.findViewById(R.id.overflow);
            saleLabel = view.findViewById(R.id.sale_label);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View view) {
            super(view);
        }
    }

    public ProductsAdapter(Context mContext, List<Product> productList, LoadMoreListener listener) {
        super(mContext, listener);
        this.mContext = mContext;
        this.productList = productList;
    }

    @Override
    protected int getViewType(int position) {
        if (headerView != null && position == 0) return TYPE_HEADER;
        if (sliderView != null &&
                (headerView != null && position == 1 || headerView == null && position == 0))
            return TYPE_SLIDER;
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_wc_product_card, parent, false);
            return new ProductViewHolder(itemView);
        } else if (viewType == TYPE_HEADER){
            RecyclerView.ViewHolder holder = new HeaderViewHolder(headerView);
            requestFullSpan(holder);
            return holder;
        } else if (viewType == TYPE_SLIDER){
            RecyclerView.ViewHolder holder = new HeaderViewHolder(sliderView);
            requestFullSpan(holder);
            return holder;
        }
        return null;
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProductViewHolder) {
            ProductViewHolder holder = (ProductViewHolder) viewHolder;

            final Product product = productList.get(position - ((headerView == null) ? 0 : 1) - ((sliderView == null) ? 0 : 1));

            final String name = product.getName();
            final String image = product.getImages().get(0).getSrc();

            holder.productName.setText(name);

            if (product.getOnSale()) {
                holder.productPriceRegular.setVisibility(View.VISIBLE);
                holder.saleLabel.setVisibility(View.VISIBLE);
                holder.productPriceRegular.setText(PriceFormat.formatPrice(product.getRegularPrice()));
                holder.productPriceRegular.setPaintFlags(holder.productPriceRegular.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.productPrice.setText(PriceFormat.formatPrice(product.getSalePrice()));
            } else {
                holder.productPriceRegular.setVisibility(View.GONE);
                holder.saleLabel.setVisibility(View.GONE);
                holder.productPrice.setText(PriceFormat.formatPrice(product.getPrice()));
            }

            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CartAssistant(mContext, view, product).addProductToCart(null);

                }
            });
            Picasso.with(mContext).load(image).into(holder.productImage);
            holder.productImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ProductActivity.class);
                    intent.putExtra(ProductActivity.PRODUCT, product);
                    mContext.startActivity(intent);
                }
            });

            if (itemWidth > 0) {
                holder.view.getLayoutParams().width = (int) itemWidth;
            }
        } else if (viewHolder instanceof HeaderViewHolder) {
            //Layout is already loaded
            requestFullSpan(viewHolder);
        }
    }

    public void setItemWidth(float width){
        this.itemWidth = width;
    }

    /**
     * Ensures that HeaderViewHolder still receives a full span, without refreshing the list
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
        return productList.size() + ((headerView == null) ? 0 : 1) + ((sliderView == null) ? 0 : 1);
    }

    public void setSlider(View sliderView) {
        this.sliderView = sliderView;
        notifyDataSetChanged();
    }

    public void setHeader(View headerView) {
        this.headerView = headerView;
        notifyDataSetChanged();
    }

}
