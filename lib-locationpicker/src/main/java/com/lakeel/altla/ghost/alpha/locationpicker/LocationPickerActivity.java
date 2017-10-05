package com.lakeel.altla.ghost.alpha.locationpicker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
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

    private static final Log LOG = LogFactory.getLog(LocationPickerActivity.class);

    private static final String KEY_LATITUDE = "latitude";

    private static final String KEY_LONGITUDE = "longitude";

    private static final String KEY_DEFAULT_ZOOM = "defaultZoom";

    private static final String KEY_MY_LOCATION_ENABLED = "myLocationEnabled";

    private static final String KEY_BUILDINGS_ENABLED = "buildingsEnabled";

    private static final String KEY_INDOOR_ENABLED = "indoorEnabled";

    private static final String KEY_TRAFFIC_ENABLED = "trafficEnabled";

    private static final String KEY_MAP_TOOLBAR_ENABLED = "mapToolbarEnabled";

    private static final String KEY_ZOOM_CONTROLS_ENABLED = "zoomControlsEnabled";

    private static final String KEY_MY_LOCATION_BUTTON_ENABLED = "myLocationButtonEnabled";

    private static final String KEY_COMPASS_ENABLED = "compassEnabled";

    private static final String KEY_INDOOR_LEVEL_PICKER_ENABLED = "indoorLevelPickerEnabled";

    private static final String KEY_ROTATE_GESTURES_ENABLED = "rotateGesturesEnabled";

    private static final String KEY_SCROLL_GESTURES_ENABLED = "scrollGesturesEnabled";

    private static final String KEY_TILT_GESTURES_ENABLED = "tiltGesturesEnabled";

    private static final String KEY_ZOOM_GESTURES_ENABLED = "zoomGesturesEnabled";

    private static final String KEY_ALL_GESTURES_ENABLED = "allGesturesEnabled";

    private static final float DEFAULT_ZOOM = 17;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private MapView mapView;

    private GoogleMap googleMap;

    private Marker marker;

    private LatLng location;

    private float defaultZoom;

    private boolean myLocationEnabled;

    private boolean buildingsEnabled;

    private boolean indoorEnabled;

    private boolean trafficEnabled;

    private boolean maxZoomPreference;

    private boolean mapToolbarEnabled;

    private boolean zoomControlsEnabled;

    private boolean myLocationButtonEnabled;

    private boolean compassEnabled;

    private boolean indoorLevelPickerEnabled;

    private boolean rotateGesturesEnabled;

    private boolean scrollGesturesEnabled;

    private boolean tiltGesturesEnabled;

    private boolean zoomGesturesEnabled;

    private boolean allGesturesEnabled;

    @Nullable
    public static LatLng getLocation(@NonNull Intent intent) {
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
            defaultZoom = extras.getFloat(KEY_DEFAULT_ZOOM);

            myLocationEnabled = extras.getBoolean(KEY_MY_LOCATION_ENABLED);
            buildingsEnabled = extras.getBoolean(KEY_BUILDINGS_ENABLED);
            indoorEnabled = extras.getBoolean(KEY_INDOOR_ENABLED);
            trafficEnabled = extras.getBoolean(KEY_TRAFFIC_ENABLED);

            mapToolbarEnabled = extras.getBoolean(KEY_MAP_TOOLBAR_ENABLED);
            zoomControlsEnabled = extras.getBoolean(KEY_ZOOM_CONTROLS_ENABLED);
            myLocationButtonEnabled = extras.getBoolean(KEY_MY_LOCATION_BUTTON_ENABLED);
            compassEnabled = extras.getBoolean(KEY_COMPASS_ENABLED);
            indoorLevelPickerEnabled = extras.getBoolean(KEY_INDOOR_LEVEL_PICKER_ENABLED);
            rotateGesturesEnabled = extras.getBoolean(KEY_ROTATE_GESTURES_ENABLED);
            scrollGesturesEnabled = extras.getBoolean(KEY_SCROLL_GESTURES_ENABLED);
            tiltGesturesEnabled = extras.getBoolean(KEY_TILT_GESTURES_ENABLED);
            zoomGesturesEnabled = extras.getBoolean(KEY_ZOOM_GESTURES_ENABLED);
            allGesturesEnabled = extras.getBoolean(KEY_ALL_GESTURES_ENABLED);
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

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(mapToolbarEnabled);
        uiSettings.setZoomControlsEnabled(zoomControlsEnabled);
        uiSettings.setMyLocationButtonEnabled(myLocationButtonEnabled);
        uiSettings.setCompassEnabled(compassEnabled);
        uiSettings.setIndoorLevelPickerEnabled(indoorLevelPickerEnabled);
        uiSettings.setAllGesturesEnabled(allGesturesEnabled);
        uiSettings.setRotateGesturesEnabled(rotateGesturesEnabled);
        uiSettings.setScrollGesturesEnabled(scrollGesturesEnabled);
        uiSettings.setTiltGesturesEnabled(tiltGesturesEnabled);
        uiSettings.setZoomGesturesEnabled(zoomGesturesEnabled);

        updateLocation(location, true);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMyLocationButtonClickListener(this);

        googleMap.setBuildingsEnabled(buildingsEnabled);
        googleMap.setIndoorEnabled(indoorEnabled);
        googleMap.setTrafficEnabled(trafficEnabled);

        if (myLocationEnabled) {
            if (checkLocationPermission()) {
                // Enable the location layer.
                googleMap.setMyLocationEnabled(true);
            } else {
                requestLocationPermission();
            }
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
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, defaultZoom);
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

        private Float defaultZoom = DEFAULT_ZOOM;

        private boolean myLocationEnabled;

        private boolean buildingsEnabled;

        private boolean indoorEnabled;

        private boolean trafficEnabled;

        private boolean mapToolbarEnabled;

        private boolean zoomControlsEnabled;

        private boolean myLocationButtonEnabled;

        private boolean compassEnabled;

        private boolean indoorLevelPickerEnabled;

        private boolean rotateGesturesEnabled;

        private boolean scrollGesturesEnabled;

        private boolean tiltGesturesEnabled;

        private boolean zoomGesturesEnabled;

        private boolean allGesturesEnabled;

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
        public Builder setDefaultZoom(float defaultZoom) {
            this.defaultZoom = defaultZoom;
            return this;
        }

        @NonNull
        public Builder setMyLocationEnabled(boolean myLocationEnabled) {
            this.myLocationEnabled = myLocationEnabled;
            return this;
        }

        @NonNull
        public Builder setBuildingsEnabled(boolean buildingsEnabled) {
            this.buildingsEnabled = buildingsEnabled;
            return this;
        }

        @NonNull
        public Builder setIndoorEnabled(boolean indoorEnabled) {
            this.indoorEnabled = indoorEnabled;
            return this;
        }

        @NonNull
        public Builder setTrafficEnabled(boolean trafficEnabled) {
            this.trafficEnabled = trafficEnabled;
            return this;
        }

        @NonNull
        public Builder setMapToolbarEnabled(boolean mapToolbarEnabled) {
            this.mapToolbarEnabled = mapToolbarEnabled;
            return this;
        }

        @NonNull
        public Builder setZoomControlsEnabled(boolean zoomControlsEnabled) {
            this.zoomControlsEnabled = zoomControlsEnabled;
            return this;
        }

        @NonNull
        public Builder setMyLocationButtonEnabled(boolean myLocationButtonEnabled) {
            this.myLocationButtonEnabled = myLocationButtonEnabled;
            return this;
        }

        @NonNull
        public Builder setCompassEnabled(boolean compassEnabled) {
            this.compassEnabled = compassEnabled;
            return this;
        }

        @NonNull
        public Builder setIndoorLevelPickerEnabled(boolean indoorLevelPickerEnabled) {
            this.indoorLevelPickerEnabled = indoorLevelPickerEnabled;
            return this;
        }

        @NonNull
        public Builder setRotateGesturesEnabled(boolean rotateGesturesEnabled) {
            this.rotateGesturesEnabled = rotateGesturesEnabled;
            return this;
        }

        @NonNull
        public Builder setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
            this.scrollGesturesEnabled = scrollGesturesEnabled;
            return this;
        }

        @NonNull
        public Builder setTiltGesturesEnabled(boolean tiltGesturesEnabled) {
            this.tiltGesturesEnabled = tiltGesturesEnabled;
            return this;
        }

        @NonNull
        public Builder setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
            this.zoomGesturesEnabled = zoomGesturesEnabled;
            return this;
        }

        @NonNull
        public Builder setAllGesturesEnabled(boolean allGesturesEnabled) {
            this.allGesturesEnabled = allGesturesEnabled;
            rotateGesturesEnabled = allGesturesEnabled;
            scrollGesturesEnabled = allGesturesEnabled;
            tiltGesturesEnabled = allGesturesEnabled;
            zoomGesturesEnabled = allGesturesEnabled;
            return this;
        }

        @NonNull
        public Intent build() {
            Intent intent = new Intent(context, LocationPickerActivity.class);

            if (latitude != null && longitude != null) {
                intent.putExtra(KEY_LATITUDE, latitude);
                intent.putExtra(KEY_LONGITUDE, longitude);
            }
            intent.putExtra(KEY_DEFAULT_ZOOM, defaultZoom);

            intent.putExtra(KEY_MY_LOCATION_ENABLED, myLocationEnabled);
            intent.putExtra(KEY_BUILDINGS_ENABLED, buildingsEnabled);
            intent.putExtra(KEY_INDOOR_ENABLED, indoorEnabled);
            intent.putExtra(KEY_TRAFFIC_ENABLED, trafficEnabled);

            intent.putExtra(KEY_MAP_TOOLBAR_ENABLED, mapToolbarEnabled);
            intent.putExtra(KEY_ZOOM_CONTROLS_ENABLED, zoomControlsEnabled);
            intent.putExtra(KEY_MY_LOCATION_BUTTON_ENABLED, myLocationButtonEnabled);
            intent.putExtra(KEY_COMPASS_ENABLED, compassEnabled);
            intent.putExtra(KEY_INDOOR_LEVEL_PICKER_ENABLED, indoorLevelPickerEnabled);
            intent.putExtra(KEY_ROTATE_GESTURES_ENABLED, rotateGesturesEnabled);
            intent.putExtra(KEY_SCROLL_GESTURES_ENABLED, scrollGesturesEnabled);
            intent.putExtra(KEY_TILT_GESTURES_ENABLED, tiltGesturesEnabled);
            intent.putExtra(KEY_ZOOM_GESTURES_ENABLED, zoomGesturesEnabled);
            intent.putExtra(KEY_ALL_GESTURES_ENABLED, allGesturesEnabled);

            return intent;
        }
    }
}
