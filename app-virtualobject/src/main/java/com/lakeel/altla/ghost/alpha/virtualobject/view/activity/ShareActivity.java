package com.lakeel.altla.ghost.alpha.virtualobject.view.activity;

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
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.location.LocationSettingsChecker;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.app.MyApplication;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.di.component.ActivityComponent;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.PatternHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.Preferences;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectEditFragment;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper.getRequiredSupportActionBar;

public class ShareActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   EasyPermissions.PermissionCallbacks,
                   LocationSettingsChecker.LocationSettingsCallbacks,
                   MyObjectEditFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(ShareActivity.class);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

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

        Intent intent = getIntent();
        if (!Intent.ACTION_SEND.equals(intent.getAction())) {
            LOG.e("An invalid action: action = %s", intent.getAction());
            Toast.makeText(this, R.string.toast_invalid_intent_received, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String extraText = intent.getExtras().getString(Intent.EXTRA_TEXT);
        if (extraText == null) {
            LOG.e("'Intent.EXTRA_TEXT' is null.");
            Toast.makeText(this, R.string.toast_invalid_intent_received, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uriString = PatternHelper.parseUriString(extraText);
        if (uriString == null) {
            LOG.e("'Intent.EXTRA_TEXT' is not a URL string: %s", extraText);
            Toast.makeText(this, R.string.toast_invalid_intent_received, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (savedInstanceState == null) {
            replaceFragment(MyObjectEditFragment.newInstanceWithUriString(uriString));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
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
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        LOG.v("onPermissionsGranted(): %d, %s", requestCode, perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        LOG.v("onPermissionsDenied(): %d, %s", requestCode, perms);
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

    @Override
    public void back() {
        Intent intent = MainActivity.createStartActivityIntent(this);
        startActivity(intent);
        finish();
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

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }
}
