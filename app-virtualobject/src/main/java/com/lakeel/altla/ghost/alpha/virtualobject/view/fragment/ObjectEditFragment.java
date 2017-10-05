package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.Names;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;
import javax.inject.Named;

public final class ObjectEditFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(ObjectEditFragment.class);

    private static final int REQUEST_CODE_LOCATION_PICKER = 100;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;

    @Named(Names.GOOGLE_API_KEY)
    @Inject
    String googleApiKey;

    private FragmentContext fragmentContext;

    private TextInputEditText textInputEditTextUri;

    private MapView mapView;

    @Nullable
    private GoogleMap googleMap;

    @Nullable
    private Marker marker;

    @Nullable
    private LatLng location;

    @NonNull
    public static ObjectEditFragment newInstance() {
        return new ObjectEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentContext = (FragmentContext) context;
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        textInputEditTextUri = getView().findViewById(R.id.text_input_edit_text_uri);
        mapView = getView().findViewById(R.id.map_view);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(false);

            if (location == null) {
                if (fragmentContext.checkLocationPermission()) {
                    fusedLocationProviderClient
                            .getLastLocation()
                            .addOnSuccessListener(getActivity(), location -> {
                                updateLocation(new LatLng(location.getLatitude(), location.getLongitude()), true);
                            })
                            .addOnFailureListener(getActivity(), e -> {
                                LOG.e("Failed to get the last location.", e);
                            });
                } else {
                    fragmentContext.requestLocationPermission();
                }
            } else {
                updateLocation(location, true);
            }

            googleMap.setOnMapClickListener(latLng -> {
                LocationPickerActivity.Builder builder = new LocationPickerActivity.Builder(getContext())
                        .setMyLocationEnabled(true)
                        .setBuildingsEnabled(false)
                        .setIndoorEnabled(true)
                        .setTrafficEnabled(false)
                        .setMapToolbarEnabled(false)
                        .setZoomControlsEnabled(true)
                        .setMyLocationButtonEnabled(true)
                        .setCompassEnabled(true)
                        .setIndoorLevelPickerEnabled(true)
                        .setAllGesturesEnabled(true);

                if (location != null) {
                    builder.setLocation(location.latitude, location.longitude);
                }

                Intent intent = builder.build();
                startActivityForResult(intent, REQUEST_CODE_LOCATION_PICKER);
            });
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.object_edit, menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        fragmentContext.setTitle(R.string.title_object_edit);
        fragmentContext.setDisplayHomeAsUpEnabled(false);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        setHasOptionsMenu(true);
        fragmentContext.invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (fragmentContext.checkLocationPermission()) {
//            initializeLocationRequest();
        } else {
            fragmentContext.requestLocationPermission();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // TODO: save
                fragmentContext.backView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                updateLocation(LocationPickerActivity.getLocation(data), true);
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    private void updateLocation(@Nullable LatLng location, boolean adjustZoomLevel) {
        this.location = location;
        fragmentContext.invalidateOptionsMenu();

        if (marker != null) {
            marker.remove();
            marker = null;
        }

        if (location != null && googleMap != null) {

            CameraUpdate cameraUpdate;
            if (adjustZoomLevel) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM_LEVEL);
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLng(location);
            }

            googleMap.moveCamera(cameraUpdate);
            marker = googleMap.addMarker(new MarkerOptions().position(location));
        }
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setDisplayHomeAsUpEnabled(boolean enabled);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void invalidateOptionsMenu();

        boolean checkLocationPermission();

        void requestLocationPermission();

        void backView();
    }
}
