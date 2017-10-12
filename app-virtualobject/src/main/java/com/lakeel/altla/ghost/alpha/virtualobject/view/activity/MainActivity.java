package com.lakeel.altla.ghost.alpha.virtualobject.view.activity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.auth.FirebaseAuth;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.app.MyApplication;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.di.component.ActivityComponent;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.OnLocationUpdatesAvailableListener;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.DebugSettingsFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.NearbyObjectListFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.ObjectEditFragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public final class MainActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   EasyPermissions.PermissionCallbacks,
                   NearbyObjectListFragment.FragmentContext,
                   ObjectEditFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(MainActivity.class);

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int REQUEST_CHECK_SETTINGS = 2;

    private SettingsClient settingsClient;

    private List<OnLocationUpdatesAvailableListener> onLocationUpdatesAvailableListeners = new ArrayList<>();

    private ActivityComponent activityComponent;

    private FirebaseAuth firebaseAuth;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);

        settingsClient = LocationServices.getSettingsClient(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInAnonymously()
                    .addOnSuccessListener(this, authResult -> {
                        LOG.i("Signed in anonymously: userId = %s", CurrentUser.getInstance().getRequiredUserId());
                    })
                    .addOnFailureListener(this, e -> {
                        LOG.e("Failed to sign-in anonymously.", e);
                    });

        if (savedInstanceState == null) {
            replaceFragment(NearbyObjectListFragment.newInstance());
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
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION);
    }

    @Override
    public void requestLocationPermission() {
        EasyPermissions.requestPermissions(this,
                                           getString(R.string.rationale_location),
                                           REQUEST_LOCATION_PERMISSION,
                                           ACCESS_FINE_LOCATION);
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
    public void showObjectEditView() {
        replaceFragmentAndAddToBackStack(ObjectEditFragment.newInstance());
    }

    @Override
    public void showDebugSettingsView() {
        replaceFragmentAndAddToBackStack(DebugSettingsFragment.newInstance());
    }

    @Override
    public void backView() {
        if (0 < getSupportFragmentManager().getBackStackEntryCount()) {
            getSupportFragmentManager().popBackStack();
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
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
