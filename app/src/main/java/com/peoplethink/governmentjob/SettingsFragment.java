package com.peoplethink.governmentjob;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.text.Html;
import android.widget.Toast;

import com.peoplethink.governmentjob.billing.BillingProcessor;
import com.peoplethink.governmentjob.billing.TransactionDetails;
import com.peoplethink.governmentjob.util.Log;

/**
 * This fragmnt is used to show a settings page to the user
 */

public class SettingsFragment extends PreferenceFragment implements
		BillingProcessor.IBillingHandler {
	
	//You can change this setting if you would like to disable rate-my-app
	boolean HIDE_RATE_MY_APP = false;

	private BillingProcessor bp;
	private Preference preferencepurchase;
	
	private AlertDialog dialog;
	
    private static String PRODUCT_ID_BOUGHT = "item_1_bought";
	public static String SHOW_DIALOG = "show_dialog";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_settings);

		// open play store page
		Preference preferencerate = findPreference("rate");
		preferencerate
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Uri uri = Uri.parse("market://details?id="
								+ getActivity().getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(getActivity(),
									"Could not open Play Store",
									Toast.LENGTH_SHORT).show();
							return true;
						}
						return true;
					}
				});

		// open about dialog
		Preference preferenceabout = findPreference("about");
		preferenceabout
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder ab = null;
						ab = new AlertDialog.Builder(getActivity());
						ab.setMessage(Html.fromHtml(getResources().getString(
								R.string.about_text)));
						ab.setPositiveButton(
								getResources().getString(R.string.ok), null);
						ab.setTitle(getResources().getString(
								R.string.about_header));
						ab.show();
						return true;
					}
				});

		if (Config.HIDE_DRAWER || !Config.DRAWER_OPEN_START) {
			PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
			Preference preferencedraweropen = findPreference("menuOpenOnStart");
			preferenceScreen.removePreference(preferencedraweropen);
		}
		
		// purchase
		preferencepurchase = findPreference("purchase");
		String license = getResources().getString(R.string.google_play_license);
		if (null != license && !license.equals("")){
			bp = new BillingProcessor(getActivity(),
				license, this);
			bp.loadOwnedPurchasesFromGoogle();
		
			preferencepurchase
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						bp.purchase(getActivity(), PRODUCT_ID());
						return true;
					}
				});
		
			if (getIsPurchased(getActivity())){
				preferencepurchase.setIcon(R.drawable.ic_action_action_done);
			}
		} else {
			PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
			PreferenceCategory billing = (PreferenceCategory) findPreference("billing");
			preferenceScreen.removePreference(billing);
		}
		
		String[] extra = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
		if (null != extra && extra.length != 0 && extra[0].equals(SHOW_DIALOG)){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Add the buttons
			builder.setPositiveButton(R.string.settings_purchase, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   bp.purchase(getActivity(), PRODUCT_ID());
			           }
			       });
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });
			builder.setTitle(getResources().getString(R.string.dialog_purchase_title));
			builder.setMessage(getResources().getString(R.string.dialog_purchase));

			// Create the AlertDialog
			dialog = builder.create();
			dialog.show();
		}
		
		if (HIDE_RATE_MY_APP){
			PreferenceCategory other = (PreferenceCategory) findPreference("other");
			Preference preference = findPreference("rate");
			other.removePreference(preference);
		}

	}

	@Override
	public void onBillingInitialized() {
		/*
		 * Called when BillingProcessor was initialized and it's ready to
		 * purchase
		 */
	}

	@Override
	public void onProductPurchased(String productId, TransactionDetails details) {
		if (productId.equals(PRODUCT_ID())){
			setIsPurchased(true, getActivity());
			preferencepurchase.setIcon(R.drawable.ic_action_action_done);
			Toast.makeText(getActivity(), getResources().getString(R.string.settings_purchase_success), Toast.LENGTH_LONG).show();
		}
		Log.v("INFO", "Purchase purchased");
	}

	@Override
	public void onBillingError(int errorCode, Throwable error) {
		Toast.makeText(getActivity(), getResources().getString(R.string.settings_purchase_fail), Toast.LENGTH_LONG).show();
		Log.v("INFO", "Error");
	}

	@Override
	public void onPurchaseHistoryRestored() {
		if (bp.isPurchased(PRODUCT_ID())){
            	setIsPurchased(true, getActivity());
            	Log.v("INFO", "Purchase actually restored");
            	preferencepurchase.setIcon(R.drawable.ic_action_action_done);
            	if (dialog != null) dialog.cancel();
            	Toast.makeText(getActivity(), getResources().getString(R.string.settings_restore_purchase_success), Toast.LENGTH_LONG).show();
            }
		Log.v("INFO", "Purchase restored called");
	}
	
	public void setIsPurchased(boolean purchased, Context c){
    	SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(c);
    	
    	SharedPreferences.Editor editor= prefs.edit();
    	
    	editor.putBoolean(PRODUCT_ID_BOUGHT, purchased);
 	    editor.apply();
	}
	
	public static boolean getIsPurchased(Context c){
		SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(c);
        
        boolean prefson = prefs.getBoolean(PRODUCT_ID_BOUGHT, false);
        
        return prefson;
	}
	
	private String PRODUCT_ID(){
		return getResources().getString(R.string.product_id);
	}
	

	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        bp.handleActivityResult(requestCode, resultCode, intent);
    }
	
	
	@Override
	public void onDestroy() {
	   if (bp != null) 
	        bp.release();

	    super.onDestroy();
	}
}
