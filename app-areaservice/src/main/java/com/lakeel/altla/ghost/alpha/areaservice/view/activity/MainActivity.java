package com.lakeel.altla.ghost.alpha.areaservice.view.activity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.auth.FirebaseAuth;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.areaservice.R;
import com.lakeel.altla.ghost.alpha.areaservice.app.MyApplication;
import com.lakeel.altla.ghost.alpha.areaservice.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.areaservice.di.component.ActivityComponent;
import com.lakeel.altla.ghost.alpha.areaservice.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.areaservice.helper.Preferences;
import com.lakeel.altla.ghost.alpha.areaservice.view.fragment.NearbyAreaListFragment;
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.location.LocationSettingsChecker;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper.getRequiredSupportActionBar;
import static com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper.replaceFragment;

public class MainActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   EasyPermissions.PermissionCallbacks,
                   LocationSettingsChecker.LocationSettingsCallbacks,
                   NearbyAreaListFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(MainActivity.class);

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int REQUEST_CHECK_SETTINGS = 2;

    private static final int MILLIS_1000 = 1000;

    private static final int FASTEST_INTERVAL_SECONDS = 5;

    private ActivityComponent activityComponent;

    private Preferences preferences;

    private FirebaseAuth firebaseAuth;

    private boolean locationPermissionRequested;

    private LocationRequest locationRequest;

    private boolean locationUpdatesEnabled;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationCallback locationCallback;

    private Location lastLocation;

    @NonNull
    public static Intent createStartActivityIntent(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        preferences = new Preferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInAnonymously()
                    .addOnSuccessListener(this, authResult -> {
                        LOG.i("Signed in anonymously: userId = %s", CurrentUser.getInstance().getRequiredUserId());
                    })
                    .addOnFailureListener(this, e -> {
                        LOG.e("Failed to sign-in anonymously.", e);
                    });

        if (savedInstanceState == null) {
            replaceFragment(this, R.id.fragment_container, NearbyAreaListFragment.newInstance());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AppCompatHelper.back(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionRequested = false;
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        LOG.v("onPermissionsGranted(): %d, %s", requestCode, perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        LOG.e("onPermissionsDenied(): %d, %s", requestCode, perms);
        Toast.makeText(this, R.string.toast_permission_required, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void checkLocationSettings() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(preferences.getLocationRequestPriority());
        locationRequest.setInterval(preferences.getLocationUpdatesInterval() * MILLIS_1000);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_SECONDS * MILLIS_1000);

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
        LocationSettingsChecker.checkLocationSettings(this, locationSettingsRequest, REQUEST_CHECK_SETTINGS, this);
    }

    @Override
    public void startLocationUpdates() {
        locationUpdatesEnabled = true;

        if (checkLocationPermission()) {
            checkLocationSettings();
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void stopLocationUpdates() {
        locationUpdatesEnabled = false;

        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    @Override
    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationSettingsSatisfied() {
        if (locationUpdatesEnabled) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onLocationSettingsNeverFixed() {
        LOG.e("Location settings are not satisfied. However, we have no way to fix them.");
        Toast.makeText(this, getString(R.string.toast_location_settings_never_fixed), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    onLocationSettingsSatisfied();
                } else {
                    Toast.makeText(this, R.string.toast_enable_location, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    //    @Override
    public void back() {
        AppCompatHelper.back(this);
    }

    private boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION);
    }

    private void requestLocationPermission() {
        if (locationPermissionRequested) return;

        locationPermissionRequested = true;
        EasyPermissions.requestPermissions(this,
                                           getString(R.string.rationale_location),
                                           REQUEST_LOCATION_PERMISSION,
                                           ACCESS_FINE_LOCATION);
    }

    private void requestLocationUpdates() {
        if (checkLocationPermission()) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    lastLocation = locationResult.getLastLocation();
                    LOG.v("A location is updated: location = %s", lastLocation);
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                    if (locationAvailability.isLocationAvailable()) {
                        LOG.i("A location is available.");
                    } else {
                        LOG.w("A location is not available.");
                        lastLocation = null;
                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }
}
