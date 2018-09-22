package com.peoplethink.governmentjob.providers.woocommerce.checkout;

import com.peoplethink.governmentjob.providers.woocommerce.model.products.Product;

import java.io.Serializable;

/**
 * Class to represent an object in the shopping carts
 */
public class CartProduct implements Serializable {

    private int quantity;
    private Product variation;
    private Product product;

    public CartProduct(Product product, Product variation){
        this.product = product;
        this.quantity = 1;
        this.variation = variation;
    }

    public CartProduct(Product product){
        this.product = product;
        this.quantity = 1;
        this.variation = null;
    }

    void updateQuantity(int quantity){
        this.quantity += quantity;
    }

    void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getVariation() {
        return variation;
    }

    public Product getProduct() {
        return product;
    }

}
