package com.peoplethink.governmentjob.providers.woocommerce.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.Cart;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.CartAssistant;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.CartProduct;
import com.peoplethink.governmentjob.providers.woocommerce.checkout.PriceFormat;
import com.peoplethink.governmentjob.providers.woocommerce.model.products.Product;
import com.peoplethink.governmentjob.providers.woocommerce.ui.ProductActivity;
import com.squareup.picasso.Picasso;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder>{
    private Context mContext;
    private Cart cart;

    public static int MAX_QUANTITY = 15;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productPriceRegular, productQuantity, productDetails;
        ImageView productImage;
        TextView overflowDelete, overflowEdit;

        MyViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.productName);
            productPrice = view.findViewById(R.id.productPrice);
            productPriceRegular = view.findViewById(R.id.productPriceRegular);
            productQuantity = view.findViewById(R.id.productQuantity);
            productDetails = view.findViewById(R.id.productDetails);
            productImage = view.findViewById(R.id.productImage);
            overflowDelete = view.findViewById(R.id.overflowDelete);
            overflowEdit = view.findViewById(R.id.overflowEdit);
        }
    }

    public CartAdapter(Context mContext, Cart cart) {
        this.mContext = mContext;
        this.cart = cart;
    }

    @Override
    public CartAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_wc_cart_item, parent, false);
        return new CartAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CartAdapter.MyViewHolder holder, int position) {

        final CartProduct cartProduct = cart.getCartProducts().get(position);
        final Product product = cartProduct.getProduct();

        String name = product.getName();
        String image = product.getImages().get(0).getSrc();
        float price = CartAssistant.getPrice(product, cartProduct.getVariation());
        String details = (product.getCategories().size() > 0) ?
                product.getCategories().get(0).getName() :
                product.getDescription().replaceAll("<[^>]*>", "").trim();


        if (cartProduct.getVariation() != null) {
            details = CartAssistant.getVariationDescription(cartProduct.getVariation());
        }

        holder.productName.setText(name);
        holder.productPrice.setText(PriceFormat.formatPrice(price));
        holder.productDetails.setText(details);
        holder.productQuantity.setText(String.format(mContext.getString(R.string.quantity), cartProduct.getQuantity()).trim());
        holder.overflowDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cart.removeProductFromCart(product, cartProduct.getVariation()))
                    notifyItemRemoved(holder.getAdapterPosition());
                else
                    notifyDataSetChanged();
            }
        });
        holder.overflowEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityEditor(holder.getAdapterPosition());
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

        if (product.getOnSale()){
            holder.productPriceRegular.setVisibility(View.VISIBLE);
            holder.productPriceRegular.setText(PriceFormat.formatPrice(product.getRegularPrice()));
            holder.productPriceRegular.setPaintFlags(holder.productPriceRegular.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productPrice.setText(PriceFormat.formatPrice(product.getSalePrice()));
        } else {
            holder.productPrice.setText(PriceFormat.formatPrice(price));
        }
    }

    private void showQuantityEditor(int item){
        final CartProduct cartItem = cart.getCartProducts().get(item);
        RelativeLayout linearLayout = new RelativeLayout(mContext);
        final NumberPicker aNumberPicker = new NumberPicker(mContext);
        aNumberPicker.setMaxValue(cartItem.getProduct().getManageStock() ? cartItem.getProduct().getStockQuantity() : MAX_QUANTITY);
        aNumberPicker.setMinValue(1);
        aNumberPicker.setValue(cartItem.getQuantity());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(R.string.quantity_picker);
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                cart.setProductQuantity(cartItem, aNumberPicker.getValue());
                                notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return cart.getCartProducts().size();
    }
}
