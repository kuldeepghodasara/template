package com.peoplethink.governmentjob.providers.woocommerce.checkout;

import android.content.Context;

import com.peoplethink.governmentjob.providers.woocommerce.model.products.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class to manage your shopping cart
 */
public class Cart implements Serializable {
    private Context context;
    private static ArrayList<CartProduct> productsInCart;
    private CartListener callback;

    private String CACHE_FILE = "cart";

    private Cart(Context context){
        this.context = context;

        //Restore cart products
        if (productsInCart == null) {
            ArrayList<CartProduct> storedCart = retrieveCart();
            if (storedCart != null)
                productsInCart = storedCart;
            else
                productsInCart = new ArrayList<>();
        }
    }

    public static Cart getInstance(Context context){
        return new Cart(context);
    }

    /**
     * Add product with variation to the cart, or increase quantity by 1 if it already is in the cart
     * @param product Product to add
     * @param variation Variation
     * @return true if product has been added, false if not (out of stock)
     */
     boolean addProductToCart(Product product, Product variation){
        //If product is already in the cart, update quantity
        for (CartProduct productInCart : productsInCart){
            if (productInCart.getProduct().getId().equals(product.getId()) &&
                    (productInCart.getVariation() == null ||
                            productInCart.getVariation().getId().equals(variation.getId()))){
                if (productIsInStock(product, productInCart.getQuantity() + 1, variation)) {
                    productInCart.updateQuantity(1);
                    return true;
                } else {
                    return false;
                }
            }
        }

        //If the product is not yet in the cart, add it
        if (productIsInStock(product, 1, variation)) {
            addProductToCart(new CartProduct(product, variation));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a new product to the cart
     * @param product Product to add
     */
    private void addProductToCart(CartProduct product){
        productsInCart.add(product);
        saveCart();
    }

    /**
     * Reduce quantity of product in cart by 1 and remove if 0.
     * @param product product to remove
     * @param variation variation of the product (0 if none)
     * @return true if the product is removed, false if only the quantity has been decreased
     */
    public boolean removeProductFromCart(Product product, Product variation){
        for (Iterator<CartProduct> iter = productsInCart.listIterator(); iter.hasNext(); ){
            CartProduct productInCart = iter.next();
            if (productInCart.getProduct().getId().equals(product.getId()) &&
                    (productInCart.getVariation() == null ||
                            productInCart.getVariation().getId().equals(variation.getId()))){
                boolean removedFromCart;
                if (productInCart.getQuantity() > 1) {
                    productInCart.updateQuantity(-1);
                    removedFromCart = false;
                } else {
                    iter.remove();
                    removedFromCart = true;
                }
                saveCart();
                return removedFromCart;
            }
        }

        return false;
    }

    /**
     * updateProductQuantity
     * @param product Product
     * @param quantity Quantity to update
     * @return false if failed, true if success
     */
    public boolean setProductQuantity(CartProduct product, int quantity){
        if (!productsInCart.contains(product)) return false;
        if (!productIsInStock(product.getProduct(), quantity, product.getVariation())) return false;

        product.setQuantity(quantity);
        saveCart();

        return true;
    }

    public void clearCart(){
        productsInCart.clear();
        saveCart();
    }

    public ArrayList<CartProduct> getCartProducts(){
        return productsInCart;
    }


    /**
     * Check if a product is in stock for a certain quantity
     * @param product Product
     * @param quantity Quantity
     * @param variation Varation
     * @return True if in stock
     */
    private boolean productIsInStock(Product product, int quantity, Product variation){

        if (!product.getInStock()) return false;
        return !(product.getManageStock() && product.getStockQuantity() > quantity);

    }

    private void saveCart(){
        if (callback != null)
            callback.onCartUpdated();

        try {
            // Save the JSONObject
            ObjectOutput out = null;

            out = new ObjectOutputStream(new FileOutputStream(new File(context.getCacheDir(),"")+ CACHE_FILE));

            out.writeObject(productsInCart);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<CartProduct> retrieveCart(){
        try {
            // Load in an object
            ObjectInputStream in = null;
            File cacheFile = new File(new File(context.getCacheDir(),"")+ CACHE_FILE);
            in = new ObjectInputStream(new FileInputStream(cacheFile));
            ArrayList<CartProduct> productsList = (ArrayList<CartProduct>) in.readObject();
            in.close();

            return productsList;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setCartListener(CartListener callback){
        this.callback = callback;
    }

    public interface CartListener{
        void onCartUpdated();
    }


}
