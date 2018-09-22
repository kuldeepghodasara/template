package com.peoplethink.governmentjob.providers.fav.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.fav.FavDbAdapter;
import com.peoplethink.governmentjob.providers.rss.ui.RssDetailActivity;
import com.peoplethink.governmentjob.providers.woocommerce.ui.ProductActivity;
import com.peoplethink.governmentjob.providers.wordpress.ui.WordpressDetailActivity;
import com.peoplethink.governmentjob.providers.youtube.ui.YoutubeDetailActivity;

import java.io.Serializable;

/**
 *  This activity is used to display a list of favorites, and let the user 
 *  open them & delete them
 */

public class FavFragment extends ListFragment {
    @SuppressWarnings("unused")
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    @SuppressWarnings("unused")
	private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private FavDbAdapter mDbHelper;

    private LinearLayout ll;

	String menu;
	String noconnection;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ll = (LinearLayout) inflater.inflate(R.layout.fragment_fav, container, false);
        setHasOptionsMenu(true);
        
	    return ll;
    }
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	mDbHelper = new FavDbAdapter(getActivity());
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    @SuppressWarnings("deprecation")
	private void fillData() {
        Cursor favoritesCursor = mDbHelper.getFavorites();
        getActivity().startManagingCursor(favoritesCursor);

        String[] from = new String[]{FavDbAdapter.KEY_TITLE};

        int[] to = new int[]{R.id.text1};

        SimpleCursorAdapter favorites = 
            new SimpleCursorAdapter(getActivity(), R.layout.fragment_fav_row, favoritesCursor, from, to);
        setListAdapter(favorites);
    }
    
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 	    inflater.inflate(R.menu.favorite_menu, menu);
 	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {    
        case R.id.clear:
        	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.item_del_text))
                   .setPositiveButton(getResources().getString(R.string.item_del_confirmation), new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   //mDbHelper = new NotesDbAdapter(RssDetailActivity.this);
                    	   mDbHelper.emptyDatabase();
                    	   fillData();
                       }
                   })
                   .setCancelable(true);
            // Create the AlertDialog object
            builder.create();
            builder.show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteFav(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        openActivity(id);
    }

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    private void openActivity(Long mRowId) {
        FavDbAdapter mDbHelper = new FavDbAdapter(getActivity());
        mDbHelper.open();

            Cursor note = mDbHelper.getFavorite(mRowId);
            getActivity().startManagingCursor(note);
            String title = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_TITLE));
            Serializable object = FavDbAdapter.readSerializedObject(note.getBlob(note.getColumnIndexOrThrow(FavDbAdapter.KEY_OBJECT)));
            int provider = note.getInt(note.getColumnIndexOrThrow(FavDbAdapter.KEY_PROVIDER));

        if (FavDbAdapter.KEY_YOUTUBE == provider) {
            Intent intent = new Intent(getActivity(), YoutubeDetailActivity.class);
            intent.putExtra(YoutubeDetailActivity.EXTRA_VIDEO, object);
            startActivity(intent);
        } else if (FavDbAdapter.KEY_RSS == provider) {
            Intent intent = new Intent(getActivity(), RssDetailActivity.class);
            intent.putExtra(RssDetailActivity.EXTRA_RSSITEM, object);
            startActivity(intent);
        } else if (FavDbAdapter.KEY_WEB == provider) {
            HolderActivity.startWebViewActivity(getActivity(), (String) object, false, false, null);
        } else if (FavDbAdapter.KEY_WORDPRESS == provider) {
            Intent intent = new Intent(getActivity(), WordpressDetailActivity.class);
            intent.putExtra(WordpressDetailActivity.EXTRA_POSTITEM, object);
            startActivity(intent);
        } else if (FavDbAdapter.KEY_WOOCOMMERCE == provider) {
            Intent intent = new Intent(getActivity(), ProductActivity.class);
            intent.putExtra(ProductActivity.PRODUCT, object);
            startActivity(intent);
        }
    }
}
