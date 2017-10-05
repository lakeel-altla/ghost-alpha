package com.lakeel.altla.ghost.alpha.locationpicker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public final class LocationPickerActivity extends AppCompatActivity
        implements OnMapReadyCallback,
                   GoogleMap.OnMapClickListener,
                   GoogleMap.OnMyLocationButtonClickListener {

    public static final String KEY_LATITUDE = "latitude";

    public static final String KEY_LONGITUDE = "longitude";

    private static final Log LOG = LogFactory.getLog(LocationPickerActivity.class);

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private MapView mapView;

    private GoogleMap googleMap;

    private Marker marker;

    private LatLng location;

    @Nullable
    public static LatLng getLatLng(@NonNull Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        } else {
            double latitude = extras.getDouble(KEY_LATITUDE);
            double longitude = extras.getDouble(KEY_LONGITUDE);
            return new LatLng(latitude, longitude);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        } else {
            LOG.w("ActionBar is null.");
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(KEY_LATITUDE) && extras.containsKey(KEY_LONGITUDE)) {
                double latitude = extras.getDouble(KEY_LATITUDE);
                double longitude = extras.getDouble(KEY_LONGITUDE);
                location = new LatLng(latitude, longitude);
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_picker, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_select);
        item.setEnabled(location != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_select) {
            if (location == null) {
                throw new IllegalStateException("The location is not selected.");
            } else {
                Intent intent = new Intent();
                intent.putExtra(KEY_LATITUDE, location.latitude);
                intent.putExtra(KEY_LONGITUDE, location.longitude);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        updateLocation(location, true);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMyLocationButtonClickListener(this);

        if (checkLocationPermission()) {
            // Enable the location layer.
            googleMap.setMyLocationEnabled(true);
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        updateLocation(latLng, false);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (checkLocationPermission()) {
            fusedLocationProviderClient
                    .getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            updateLocation(new LatLng(location.getLatitude(), location.getLongitude()), true);
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            LOG.e("Failed to get the last location.", e);
                        }
                    });
        } else {
            requestLocationPermission();
        }
        return false;
    }

    private void updateLocation(@Nullable LatLng location, boolean adjustZoomLevel) {
        this.location = location;
        invalidateOptionsMenu();

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

    private boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION);
    }

    private void requestLocationPermission() {
        EasyPermissions.requestPermissions(this,
                                           getString(R.string.rationale_location),
                                           REQUEST_LOCATION_PERMISSION,
                                           ACCESS_FINE_LOCATION);
    }

    public static final class Builder {

        private final Context context;

        private Double latitude;

        private Double longitude;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        public Builder setLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            return this;
        }

        @NonNull
        public Intent build() {
            Intent intent = new Intent(context, LocationPickerActivity.class);

            if (latitude != null && longitude != null) {
                intent.putExtra(KEY_LATITUDE, latitude);
                intent.putExtra(KEY_LONGITUDE, longitude);
            }

            return intent;
        }
    }
}
