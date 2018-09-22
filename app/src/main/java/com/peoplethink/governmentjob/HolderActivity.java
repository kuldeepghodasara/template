package com.peoplethink.governmentjob;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebViewFragment;
import android.widget.Toast;

import com.peoplethink.governmentjob.inherit.BackPressFragment;
import com.peoplethink.governmentjob.inherit.PermissionsFragment;
import com.peoplethink.governmentjob.providers.CustomIntent;
import com.peoplethink.governmentjob.providers.fav.ui.FavFragment;
import com.peoplethink.governmentjob.providers.web.WebviewFragment;
import com.peoplethink.governmentjob.util.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HolderActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    private Class<? extends Fragment> queueItem;
    private String[] queueItemData;

    /**
     * Show the Holder Activity
     * @param mContext from which the new activity is stated.
     * @param fragment fragment to show
     * @param data data/arguments/parameters to pass to the fragment
     */
    public static void startActivity(Context mContext, Class<? extends Fragment> fragment, String[] data){
        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, data);
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, fragment);

        Intent intent = new Intent(mContext, HolderActivity.class);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    public static void startWebViewActivity(Context context, String url, boolean openExternal, boolean hideNavigation, String withData, int intentFlags){
        if (openExternal && withData == null){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, new String[]{url});
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, WebViewFragment.class);
        bundle.putBoolean(WebviewFragment.HIDE_NAVIGATION, hideNavigation);
        bundle.putString(WebviewFragment.LOAD_DATA, withData);

        Intent intent = new Intent(context, HolderActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(intentFlags);
        context.startActivity(intent);
    }

    public static void startWebViewActivity(Context context, String url, boolean openExternal, boolean hideNavigation, String withData){
        startWebViewActivity(context, url, openExternal, hideNavigation, withData, 0);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holder);
        mToolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getExtras().getSerializable(MainActivity.FRAGMENT_CLASS);
        String[] args = getIntent().getExtras().getStringArray(MainActivity.FRAGMENT_DATA);

        if (CustomIntent.class.isAssignableFrom(fragmentClass)) {
            CustomIntent.performIntent(HolderActivity.this, args);
        } else if (getIntent().hasExtra(WebviewFragment.HIDE_NAVIGATION) || getIntent().hasExtra(WebviewFragment.LOAD_DATA)) {
            openWebFragment(args, getIntent().getExtras().getBoolean(WebviewFragment.HIDE_NAVIGATION), getIntent().getExtras().getString(WebviewFragment.LOAD_DATA));
        } else {
            openFragment(fragmentClass, args);
        }

        Helper.admobLoader(this, findViewById(R.id.adView));
    }

    public void openFragment(Class<? extends Fragment> fragment, String[] data){
        if(!checkPermissionsHandleIfNeeded(fragment, data))
            return;

        try {
            Fragment frag = fragment.newInstance();

            // adding the data
            Bundle bundle = new Bundle();
            bundle.putStringArray(MainActivity.FRAGMENT_DATA, data);
            frag.setArguments(bundle);

            //Changing the fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, frag)
                    .commit();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void openWebFragment(String[] params, boolean hideNavigation, String data){
        Fragment fragment;
        fragment = new WebviewFragment();

        // adding the data
        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, params);
        bundle.putBoolean(WebviewFragment.HIDE_NAVIGATION, hideNavigation);
        if (data != null)
            bundle.putString(WebviewFragment.LOAD_DATA, data);
        fragment.setArguments(bundle);

        //Changing the fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment)
                .commit();

        //Setting the title
        if (data == null)
            setTitle(getResources().getString(R.string.webview_title));
        else
            setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.settings:
                openFragment(SettingsFragment.class, new String[0]);
                return true;
            case R.id.favorites:
                openFragment(FavFragment.class, new String[0]);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
    	Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);

        if (doubleBackToExitPressedOnce) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Like All Government Jobs 2018 app? Rate us 5 Stars")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id="
                                        + getPackageName())));
                    }

                    dialog.dismiss();
                    finish();
                }
            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setNeutralButton("Exit", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            HolderActivity.this.finish();
                            finish();
                        }
                    });


            AlertDialog alert = builder.create();
            alert.show();
            return;


        }

        this.doubleBackToExitPressedOnce = true;

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1700);



        if (fragment instanceof BackPressFragment) {
        	boolean handled = ((BackPressFragment) fragment).handleBackPress();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HolderActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }

    /**
     * Checks if the item can be opened because it has sufficient permissions.
     * @param fragment The item to check
     * @return true if the item is safe to open
     */
    private boolean checkPermissionsHandleIfNeeded(Class<? extends Fragment> fragment, String[] data){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return true;

        List<String> allPermissions = new ArrayList<>();
            if (PermissionsFragment.class.isAssignableFrom(fragment)) {
                try {
                    allPermissions.addAll(Arrays.asList(((PermissionsFragment) fragment.newInstance()).requiredPermissions()));
                } catch (Exception e) {
                    //Don't really care
                }
            }

        if (allPermissions.size() > 1) {
            boolean allGranted = true;
            for (String permission : allPermissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }

            if (!allGranted) {
                //TODO An explanation before asking
                requestPermissions(allPermissions.toArray(new String[0]), 1);
                queueItem = fragment;
                queueItemData = data;
                return false;
            }

            return true;
        }

        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                boolean foundfalse = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        foundfalse = true;
                    }
                }
                if (!foundfalse){
                    //Retry to open the menu item
                    //(we can assume the item is 'purchased' otherwise a permission check would have not occured)
                    openFragment(queueItem, queueItemData);
                } else {
                    // Permission Denied
                    Toast.makeText(HolderActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /** Implement if methods depend on this (like iaps?) don't work
  	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    List<Fragment> fragments = getSupportFragmentManager().getFragments();
    if (fragments != null)
    for (Fragment frag : fragments)
    if (frag != null)
    frag.onActivityResult(requestCode, resultCode, data);
    }
     */
}