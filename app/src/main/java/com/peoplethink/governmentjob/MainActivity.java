package com.peoplethink.governmentjob;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.peoplethink.governmentjob.drawer.MenuItemCallback;
import com.peoplethink.governmentjob.drawer.NavItem;
import com.peoplethink.governmentjob.drawer.SimpleMenu;
import com.peoplethink.governmentjob.drawer.TabAdapter;
import com.peoplethink.governmentjob.inherit.BackPressFragment;
import com.peoplethink.governmentjob.inherit.CollapseControllingFragment;
import com.peoplethink.governmentjob.inherit.ConfigurationChangeFragment;
import com.peoplethink.governmentjob.inherit.PermissionsFragment;
import com.peoplethink.governmentjob.providers.CustomIntent;
import com.peoplethink.governmentjob.providers.fav.ui.FavFragment;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;
import com.peoplethink.governmentjob.util.layout.CustomAppBarLayout;
import com.peoplethink.governmentjob.util.layout.DisableableViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2017
 */
public class MainActivity extends AppCompatActivity implements MenuItemCallback, ConfigParser.CallBack {

    private static final int PERMISSION_REQUESTCODE = 123;

    //Layout
    public Toolbar mToolbar;
    private TabLayout tabLayout;
    private DisableableViewPager viewPager;
    private NavigationView navigationView;
    public DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    //Adapters
    private TabAdapter adapter;
    private static SimpleMenu menu;

    //Keep track of the interstitials we show
    private int interstitialCount = -1;

    //Data to pass to a fragment
    public static String FRAGMENT_DATA = "transaction_data";
    public static String FRAGMENT_CLASS = "transation_target";

    //Permissions Queu
    List<NavItem> queueItem;
    int queueMenuItemId;

    //InstanceState (rotation)
    private Bundle savedInstanceState;
    private static final String STATE_MENU_INDEX = "MENUITEMINDEX";
    private static final String STATE_PAGER_INDEX = "VIEWPAGERPOSITION";
    private static final String STATE_ACTIONS = "ACTIONS";


    @Override
    public void configLoaded(boolean facedException) {
        if (facedException || menu.getFirstMenuItem() == null) {
            if (Helper.isOnlineShowDialog(MainActivity.this))
                Toast.makeText(this, R.string.invalid_configuration, Toast.LENGTH_LONG).show();
        } else {
            if (savedInstanceState == null) {
                menuItemClicked(menu.getFirstMenuItem(), 0, false);
            } else {
                ArrayList<NavItem> actions = (ArrayList<NavItem>) savedInstanceState.getSerializable(STATE_ACTIONS);
                int menuItemId = savedInstanceState.getInt(STATE_MENU_INDEX);
                int viewPagerPosition = savedInstanceState.getInt(STATE_PAGER_INDEX);

                menuItemClicked(actions, menuItemId, false);
                viewPager.setCurrentItem(viewPagerPosition);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        //Load the appropriate layout
        if (useTabletMenu()) {
            setContentView(R.layout.activity_main_tablet);
            Helper.setStatusBarColor(MainActivity.this,
                    ContextCompat.getColor(this, R.color.myPrimaryDarkColor));
        } else {
            setContentView(R.layout.activity_main);
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (!useTabletMenu())
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        else {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        //Drawer
        if (!useTabletMenu()) {
            drawer = findViewById(R.id.drawer);
            toggle = new ActionBarDrawerToggle(
                    this, drawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        //Layouts
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);

        //Check if we should open a fragment based on the arguments we have
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(FRAGMENT_CLASS)) {
            try {
                Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getExtras().getSerializable(FRAGMENT_CLASS);
                if (fragmentClass != null) {
                    String[] extra = getIntent().getExtras().getStringArray(FRAGMENT_DATA);

                    HolderActivity.startActivity(this, fragmentClass, extra);
                    finish();
                    //Optionally, we can also point intents to holderactivity directly instead of MainAc.
                }
            } catch (Exception e) {
                //If we come across any errors, just continue and open the default fragment
                Log.printStackTrace(e);
            }
        }

        //Menu items
        navigationView = findViewById(R.id.nav_view);

        menu = new SimpleMenu(navigationView.getMenu(), this);
        if (Config.USE_HARDCODED_CONFIG) {
            Config.configureMenu(menu, this);
        } else if (!Config.CONFIG_URL.isEmpty() && Config.CONFIG_URL.contains("http"))
            new ConfigParser(Config.CONFIG_URL, menu, this, this).execute();
        else
            new ConfigParser("config.json", menu, this, this).execute();

        tabLayout.setupWithViewPager(viewPager);

        if (!useTabletMenu()) {
            drawer.setStatusBarBackgroundColor(
                    ContextCompat.getColor(this, R.color.myPrimaryDarkColor));
        }

        applyDrawerLocks();

        Helper.admobLoader(this, findViewById(R.id.adView));
        Helper.updateAndroidSecurityProvider(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                onTabBecomesActive(position);
            }
        });

    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUESTCODE:
                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                    }
                }
                if (allGranted) {
                    //Retry to open the menu item
                    menuItemClicked(queueItem, queueMenuItemId, false);
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void menuItemClicked(List<NavItem> actions, int menuItemIndex, boolean requiresPurchase) {
        // Checking the drawer should be open on start
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean openOnStart = Config.DRAWER_OPEN_START || prefs.getBoolean("menuOpenOnStart", false);
        if (drawer != null) {
            boolean firstClick = (savedInstanceState == null && adapter == null);
            if (openOnStart && !useTabletMenu() && firstClick) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                //Close the drawer
                drawer.closeDrawer(GravityCompat.START);
            }
        }

        //Check if the user is allowed to open item
        if (requiresPurchase && !isPurchased()) return; //isPurchased will handle this.
        if (!checkPermissionsHandleIfNeeded(actions, menuItemIndex))
            return; //checkPermissions will handle.

        if (isCustomIntent(actions)) return;

        //Uncheck all other items, check the current item
        for (MenuItem menuItem : menu.getMenuItems()) {
            if (menuItem.getItemId() == menuItemIndex) {
                menuItem.setChecked(true);
            } else
                menuItem.setChecked(false);
        }

        //Load the new tab
        boolean isRtl = ViewCompat.getLayoutDirection(tabLayout) == ViewCompat.LAYOUT_DIRECTION_RTL;
        adapter = new TabAdapter(getSupportFragmentManager(), actions, this, isRtl);
        viewPager.setAdapter(adapter);

        //Show or hide the tab bar depending on if we need it
        if (actions.size() == 1) {
            tabLayout.setVisibility(View.GONE);
            viewPager.setPagingEnabled(false);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setPagingEnabled(true);
        }
        ((CustomAppBarLayout) mToolbar.getParent()).setExpanded(true, true);

        //Show in interstitial
        showInterstitial();

        onTabBecomesActive(0);
    }

    private void onTabBecomesActive(int position) {
        Fragment fragment = adapter.getItem(position);
        //If fragment does not support collapse, or if OS does not support collapse, disable collapsing toolbar
        if ((fragment instanceof CollapseControllingFragment
                && !((CollapseControllingFragment) fragment).supportsCollapse())
                ||
                (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT))
            lockAppBar();
        else
            unlockAppBar();

        if (position != 0)
            showInterstitial();
    }

    /**
     * Show an interstitial ad
     */
    private void showInterstitial() {
        //if (fromPager) return;
        if (getResources().getString(R.string.admob_interstitial_id).length() == 0) return;
        if (SettingsFragment.getIsPurchased(this)) return;

        if (interstitialCount == (Config.INTERSTITIAL_INTERVAL - 1)) {
            final InterstitialAd mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
            AdRequest adRequestInter = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(adRequestInter);

            interstitialCount = 0;
        } else {
            interstitialCount++;
        }

    }

    /**
     * Checks if the item is/contains a custom intent, and if that the case it will handle it.
     *
     * @param items List of NavigationItems
     * @return True if the item is a custom intent, in that case
     */
    private boolean isCustomIntent(List<NavItem> items) {
        NavItem customIntentItem = null;
        for (NavItem item : items) {
            if (CustomIntent.class.isAssignableFrom(item.getFragment())) {
                customIntentItem = item;
            }
        }

        if (customIntentItem == null) return false;
        if (items.size() > 1)
            Log.e("INFO", "Custom Intent Item must be only child of menu item! Ignorning all other tabs");

        CustomIntent.performIntent(MainActivity.this, customIntentItem.getData());
        return true;
    }

    /**
     * If the item can be opened because it either has been purchased or does not require a purchase to show.
     *
     * @return true if the app is purchased. False if the app hasn't been purchased, or if iaps are disabled
     */
    private boolean isPurchased() {
        String license = getResources().getString(R.string.google_play_license);
        // if item does not require purchase, or app has purchased, or license is null/empty (app has no in app purchases)
        if (!SettingsFragment.getIsPurchased(this) && !license.equals("")) {
            String[] extra = new String[]{SettingsFragment.SHOW_DIALOG};
            HolderActivity.startActivity(this, SettingsFragment.class, extra);

            return false;
        }

        return true;
    }

    /**
     * Checks if the item can be opened because it has sufficient permissions.
     *
     * @param tabs The tabs to check
     * @return true if the item is safe to open
     */
    private boolean checkPermissionsHandleIfNeeded(List<NavItem> tabs, int menuItemId) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return true;

        List<String> allPermissions = new ArrayList<>();
        for (NavItem tab : tabs) {
            if (PermissionsFragment.class.isAssignableFrom(tab.getFragment())) {
                try {
                    for (String permission : ((PermissionsFragment) tab.getFragment().newInstance()).requiredPermissions()) {
                        if (!allPermissions.contains(permission))
                            allPermissions.add(permission);
                    }
                } catch (Exception e) {
                    //Don't really care
                }
            }
        }

        if (allPermissions.size() > 1) {
            boolean allGranted = true;
            for (String permission : allPermissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }

            if (!allGranted) {
                //TODO An explaination before asking
                requestPermissions(allPermissions.toArray(new String[0]), PERMISSION_REQUESTCODE);
                queueItem = tabs;
                queueMenuItemId = menuItemId;
                return false;
            }

            return true;
        }

        return true;
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
            case R.id.settings:
                HolderActivity.startActivity(this, SettingsFragment.class, null);
                return true;
            case R.id.favorites:
                HolderActivity.startActivity(this, FavFragment.class, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        Fragment activeFragment = null;
        if (adapter != null)
            activeFragment = adapter.getCurrentFragment();

        if (doubleBackToExitPressedOnce) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Like All Government Jobs 2018 app? Rate us 5 Stars.")
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
                    MainActivity.this.finish();
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
        }, 1000);



        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (activeFragment instanceof BackPressFragment) {
            boolean handled = ((BackPressFragment) activeFragment).handleBackPress();



        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null)
            for (Fragment frag : fragments)
                if (frag != null)
                    frag.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!(adapter.getCurrentFragment() instanceof ConfigurationChangeFragment))
            this.recreate();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (adapter == null) return;

        int menuItemIndex = 0;
        for (MenuItem menuItem : menu.getMenuItems()) {
            if (menuItem.isChecked()) {
                menuItemIndex = menuItem.getItemId();
                break;
            }
        }

        outState.putSerializable(STATE_ACTIONS, ((ArrayList<NavItem>) adapter.getActions()));
        outState.putInt(STATE_MENU_INDEX, menuItemIndex);
        outState.putInt(STATE_PAGER_INDEX, viewPager.getCurrentItem());
    }

    //Check if we should adjust our layouts for tablets
    public boolean useTabletMenu() {
        return (getResources().getBoolean(R.bool.isWideTablet) && Config.TABLET_LAYOUT);
    }

    //Apply the appropiate locks to the drawer
    public void applyDrawerLocks() {
        if (drawer == null) {
            if (Config.HIDE_DRAWER)
                navigationView.setVisibility(View.GONE);
            return;
        }

        if (Config.HIDE_DRAWER) {
            toggle.setDrawerIndicatorEnabled(false);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    private void lockAppBar() {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(0);
    }

    private void unlockAppBar() {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
    }

}