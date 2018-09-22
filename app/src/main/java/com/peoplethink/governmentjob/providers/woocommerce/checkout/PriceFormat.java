package com.peoplethink.governmentjob.providers.woocommerce.checkout;

import com.peoplethink.governmentjob.providers.woocommerce.model.RestAPI;

import java.text.DecimalFormat;

/**
 * Created by Anh Pham on 20/06/2016.
 */

public class PriceFormat {

    public PriceFormat() {
    }

    private static String priceWithDecimal(Float price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###.00");
        return formatter.format(price);
    }

    private static String priceWithoutDecimal(Float price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###.##");
        return formatter.format(price) + ",-";
    }

    public static String formatDecimal(Float price) {
        String toShow = priceWithoutDecimal(price);
        String priceString;
        if (toShow.indexOf(".") > 0) {
            return priceWithDecimal(price);
        } else {
            return priceWithoutDecimal(price);
        }
    }

    public static String formatPrice(Float price){
        return String.format(RestAPI.getCurrencyFormat(), formatDecimal(price));
    }

}
