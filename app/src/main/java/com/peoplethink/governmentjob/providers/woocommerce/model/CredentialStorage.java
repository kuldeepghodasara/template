package com.peoplethink.governmentjob.providers.woocommerce.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.peoplethink.governmentjob.providers.woocommerce.model.users.User;

public class CredentialStorage {

    private static final String KEY = "WOO_CREDENTIALS";
    private static final String MAIL = "WOO_CREDENTIALS_MAIL";
    private static final String PASS = "WOO_CREDENTIALS_PASS";
    private static final String ID = "WOO_CREDENTIALS_ID";
    private static final String NAME = "WOO_CREDENTIALS_NAME";

    private static SharedPreferences sharedPreferences;

    public static void saveCredentials(Context context,
                                       String email,
                                       String password,
                                       int id,
                                       String name){
        SharedPreferences sharedPref= getSharedPreferences(context);
        SharedPreferences.Editor editor =sharedPref.edit();

        // Save your string in SharedPref
        editor.putString(MAIL, email);
        editor.putString(PASS, password);
        editor.putInt(ID, id);
        editor.putString(NAME, name);
        editor.apply();
    }

    public static void clearCredentials(Context context){
        SharedPreferences sharedPref= getSharedPreferences(context);
        SharedPreferences.Editor editor =sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    public static boolean credentialsAvailable(Context context){
        SharedPreferences sharedPref= getSharedPreferences(context);
        return sharedPref.contains(MAIL) && sharedPref.contains(PASS);
    }

    public static String getEmail(Context context){
        SharedPreferences sharedPref= getSharedPreferences(context);
        return sharedPref.getString(MAIL, null);
    }

    public static String getPassword(Context context){
        SharedPreferences sharedPref= getSharedPreferences(context);
        return sharedPref.getString(PASS, null);
    }

    public static String getName(Context context){
        SharedPreferences sharedPref= getSharedPreferences(context);
        return sharedPref.getString(NAME, null);
    }

    public static Integer getId(Context context){
        SharedPreferences sharedPref= getSharedPreferences(context);
        return sharedPref.getInt(ID, 0);
    }

    private static SharedPreferences getSharedPreferences(Context context){
        if (sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

}
