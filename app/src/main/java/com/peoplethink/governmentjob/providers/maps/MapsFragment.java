package com.peoplethink.governmentjob.providers.maps;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonGeometryCollection;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonMultiLineString;
import com.google.maps.android.data.geojson.GeoJsonMultiPoint;
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.MainActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.inherit.CollapseControllingFragment;
import com.peoplethink.governmentjob.inherit.PermissionsFragment;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements PermissionsFragment, CollapseControllingFragment {

	private LinearLayout ll;

	private MapView mMapView;
	private GoogleMap googleMap;
	private Activity mAct;

	private String[] data;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		ll = (LinearLayout) inflater.inflate(R.layout.fragment_maps, container,
				false);

		setHasOptionsMenu(true);

		mMapView = ll.findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		Helper.isOnlineShowDialog(mAct);

		data = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);

		MapsInitializer.initialize(mAct);

		mMapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				MapsFragment.this.googleMap = googleMap;
				if (ContextCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_FINE_LOCATION)
						== PackageManager.PERMISSION_GRANTED)
					MapsFragment.this.googleMap.setMyLocationEnabled(true);
				new LoadGeoData(mAct).execute(data);
			}
		});


	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	}

	private void focusMapOnLayer(final GeoJsonLayer layer) {
				List<LatLng> coordinates = new ArrayList<>();

				for (GeoJsonFeature feature : layer.getFeatures()) {

					if (feature.hasGeometry()) {
						Geometry geometry = feature.getGeometry();

						if (geometry.getGeometryType().equals("GeometryCollection")) {
							List<Geometry> geometries =
									((GeoJsonGeometryCollection)geometry).getGeometries();

							for (Geometry geom : geometries) {
								coordinates.addAll(getCoordinatesFromGeometry(geom));
							}
						}
						else {
							coordinates.addAll(getCoordinatesFromGeometry(geometry));
						}
					}
				}

				// Get the bounding box builder.
				LatLngBounds.Builder builder = LatLngBounds.builder();

				// Feed the coordinates to the builder.
				for (LatLng latLng : coordinates) {
					builder.include(latLng);
				}

				final LatLngBounds boundingBoxFromBuilder = builder.build();

				mAct.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundingBoxFromBuilder, 100), 500, null);
					}
				});

	}

	private List<LatLng> getCoordinatesFromGeometry(Geometry geometry) {

		List<LatLng> coordinates = new ArrayList<>();

		switch (geometry.getGeometryType()) {
			case "Point":
				coordinates.add(((GeoJsonPoint) geometry).getCoordinates());
				break;
			case "MultiPoint":
				List<GeoJsonPoint> points = ((GeoJsonMultiPoint) geometry).getPoints();
				for (GeoJsonPoint point : points) {
					coordinates.add(point.getCoordinates());
				}
				break;
			case "LineString":
				coordinates.addAll(((GeoJsonLineString) geometry).getCoordinates());
				break;
			case "MultiLineString":
				List<GeoJsonLineString> lines =
						((GeoJsonMultiLineString) geometry).getLineStrings();
				for (GeoJsonLineString line : lines) {
					coordinates.addAll(line.getCoordinates());
				}
				break;
			case "Polygon":
				List<? extends List<LatLng>> lists =
						((GeoJsonPolygon) geometry).getCoordinates();
				for (List<LatLng> list : lists) {
					coordinates.addAll(list);
				}
				break;
			case "MultiPolygon":
				List<GeoJsonPolygon> polygons =
						((GeoJsonMultiPolygon) geometry).getPolygons();
				for (GeoJsonPolygon polygon : polygons) {
					for (List<LatLng> list : polygon.getCoordinates()) {
						coordinates.addAll(list);
					}
				}
				break;
		}

		return coordinates;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public String[] requiredPermissions() {
		return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
	}

	@Override
	public boolean supportsCollapse() {
		return false;
	}

	private class LoadGeoData extends AsyncTask<String, String, GeoJsonLayer> {

		private ProgressDialog dialog;
		private Context context;

		public LoadGeoData(Context context) {
			this.context = context;
		}

		@Override
		protected void onPostExecute(final GeoJsonLayer layer) {
			super.onPostExecute(layer);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (layer == null){
				Helper.noConnection(mAct);
			} else {
				googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick(Marker marker) {
						//Loop the features that are markers for one with the same location
						for (GeoJsonFeature feature : layer.getFeatures()){
							if (feature.getGeometry().getGeometryType().equals("Point")) {
								GeoJsonPoint point = (GeoJsonPoint) feature.getGeometry();
								if (point.getCoordinates().equals(marker.getPosition()) && feature.hasProperty("url")){
									String url = feature.getProperty("url");
									HolderActivity.startWebViewActivity(mAct, url, Config.OPEN_EXPLICIT_EXTERNAL, false, null);
								}
							}
						}
					}
				});
			}
		}

		@Override
		protected GeoJsonLayer doInBackground(String... param) {
			JSONObject geoJsonData = null;
			if (param[0].startsWith("http")) {
				geoJsonData = Helper.getJSONObjectFromUrl(param[0]);
			} else {
				String json = Helper.loadJSONFromAsset(mAct, param[0]);
				try {
					geoJsonData = new JSONObject(json);
				} catch (JSONException e){
					Log.e("INFO", "Error parsing JSON. Printing stacktrace now");
					Log.printStackTrace(e);
				}
			}

			if (geoJsonData == null) return null;

			final GeoJsonLayer layer = new GeoJsonLayer(MapsFragment.this.googleMap, geoJsonData);
			mAct.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					layer.addLayerToMap();

					for (GeoJsonFeature feature : layer.getFeatures()) {
						if (feature.getPointStyle() != null) {
							GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
							pointStyle.setTitle(feature.getProperty("name"));
							if (feature.hasProperty("snippet"))
								pointStyle.setSnippet(feature.getProperty("snippet"));
							else if (feature.hasProperty("description"))
								pointStyle.setSnippet(feature.getProperty("description"));
							else if (feature.hasProperty("popupContent"))
								pointStyle.setSnippet(feature.getProperty("popupContent"));
							feature.setPointStyle(pointStyle);
						}
					}
				}
			});

			focusMapOnLayer(layer);

			return layer;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (data[0].startsWith("http")) {
				dialog = new ProgressDialog(context);
				dialog.setCancelable(true);
				dialog.setMessage(getResources().getString(R.string.loading));
				dialog.isIndeterminate();
				dialog.show();
			}
		}

	}
}