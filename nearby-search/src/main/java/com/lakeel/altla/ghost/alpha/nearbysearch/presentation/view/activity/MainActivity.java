package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.activity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.app.MyApplication;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.component.ActivityComponent;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.helper.OnLocationUpdatesAvailableListener;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment.DebugSettingsFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment.NearbyPlaceFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment.NearbyPlaceListFragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.widget.Toast.LENGTH_SHORT;

public final class MainActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   NearbyPlaceListFragment.FragmentContext,
                   NearbyPlaceFragment.FragmentContext,
                   DebugSettingsFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(MainActivity.class);

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int REQUEST_CHECK_SETTINGS = 2;

    //    @Inject
    private SettingsClient settingsClient;

    private List<OnLocationUpdatesAvailableListener> onLocationUpdatesAvailableListeners = new ArrayList<>();

    private ActivityComponent activityComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setDisplayHomeAsUpEnabled(true);

        settingsClient = LocationServices.getSettingsClient(this);

        if (savedInstanceState == null) {
            replaceFragment(NearbyPlaceListFragment.newInstance());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                        Toast.makeText(this, R.string.toast_permission_required, LENGTH_SHORT).show();
                    }
                    finish();
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    LOG.i("Upgraded location settings.");
                    break;
                case Activity.RESULT_CANCELED:
                    LOG.i("Cancelled to upgrade location settings.");
                    Toast.makeText(this, R.string.toast_enable_location, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void setDisplayHomeAsUpEnabled(boolean enabled) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enabled);
        } else {
            LOG.w("ActionBar is null.");
        }
    }

    @Override
    public void setHomeAsUpIndicator(@DrawableRes int resId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(resId);
        } else {
            LOG.w("ActionBar is null.");
        }
    }

    @Override
    public boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
    }

    @Override
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] { ACCESS_FINE_LOCATION }, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void checkLocationSettings(LocationRequest locationRequest) {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        settingsClient.checkLocationSettings(settingsRequest)
                      .addOnSuccessListener(this, locationSettingsResponse -> {
                          for (OnLocationUpdatesAvailableListener listener : onLocationUpdatesAvailableListeners) {
                              listener.onLocationUpdatesAvailable();
                          }
                      })
                      .addOnFailureListener(this, e -> {
                          int statusCode = ((ApiException) e).getStatusCode();
                          switch (statusCode) {
                              case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                  LOG.i("Upgrading location settings.");
                                  try {
                                      ResolvableApiException rae = (ResolvableApiException) e;
                                      rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                                  } catch (IntentSender.SendIntentException sie) {
                                      LOG.e("Failed to upgrade location settings.", sie);
                                  }
                                  break;
                              case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                  LOG.e("Location settings are inadequate.");
                                  Toast.makeText(this, getString(R.string.toast_location_settings_inadequate),
                                                 Toast.LENGTH_LONG).show();
                                  break;
                              default:
                                  LOG.e("An unknown error occured.", e);
                                  break;
                          }
                      });
    }

    @Override
    public void addOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener) {
        onLocationUpdatesAvailableListeners.add(listener);
    }

    @Override
    public void removeOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener) {
        onLocationUpdatesAvailableListeners.remove(listener);
    }

    @Override
    public void backView() {
        if (0 < getSupportFragmentManager().getBackStackEntryCount()) {
            getSupportFragmentManager().popBackStack();
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    public void showDebugView() {
        replaceFragmentAndAddToBackStack(DebugSettingsFragment.newInstance());
    }

    @Override
    public void showNearbyPlaceView(@NonNull String placeId, @NonNull String name) {
        replaceFragmentAndAddToBackStack(NearbyPlaceFragment.newInstance(placeId, name));
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }

    private void replaceFragmentAndAddToBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .addToBackStack(fragment.getClass().getName())
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }
}
