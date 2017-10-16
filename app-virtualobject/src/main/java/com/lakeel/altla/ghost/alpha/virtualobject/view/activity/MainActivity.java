package com.lakeel.altla.ghost.alpha.virtualobject.view.activity;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.auth.FirebaseAuth;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.location.LocationSettingsChecker;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.app.MyApplication;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.di.component.ActivityComponent;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.OnLocationUpdatesAvailableListener;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.DebugSettingsFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectEditFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectListFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectViewFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.NearbyObjectListFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
                   LocationSettingsChecker.LocationSettingsCallbacks,
                   NearbyObjectListFragment.FragmentContext,
                   MyObjectListFragment.FragmentContext,
                   MyObjectViewFragment.FragmentContext,
                   MyObjectEditFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(MainActivity.class);

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int REQUEST_CHECK_SETTINGS = 2;

    private List<OnLocationUpdatesAvailableListener> onLocationUpdatesAvailableListeners = new ArrayList<>();

    private ActivityComponent activityComponent;

    private FirebaseAuth firebaseAuth;

    private boolean locationPermissionRequested;

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
    public boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION);
    }

    @Override
    public void requestLocationPermission() {
        if (locationPermissionRequested) return;

        locationPermissionRequested = true;
        EasyPermissions.requestPermissions(this,
                                           getString(R.string.rationale_location),
                                           REQUEST_LOCATION_PERMISSION,
                                           ACCESS_FINE_LOCATION);
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
    public void checkLocationSettings(LocationRequest locationRequest) {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
        LocationSettingsChecker.checkLocationSettings(this, settingsRequest, REQUEST_CHECK_SETTINGS, this);
    }

    @Override
    public void onLocationSettingsSatisfied() {
        for (OnLocationUpdatesAvailableListener listener : onLocationUpdatesAvailableListeners) {
            listener.onLocationUpdatesAvailable();
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
    public void addOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener) {
        onLocationUpdatesAvailableListeners.add(listener);
    }

    @Override
    public void removeOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener) {
        onLocationUpdatesAvailableListeners.remove(listener);
    }

    @Override
    public void showMyObjectListFragment() {
        replaceFragmentAndAddToBackStack(MyObjectListFragment.newInstance());
    }

    @Override
    public void showMyObjectViewFragment(@NonNull String key) {
        replaceFragmentAndAddToBackStack(MyObjectViewFragment.newInstance(key));
    }

    @Override
    public void showMyObjectEditFragment() {
        replaceFragmentAndAddToBackStack(MyObjectEditFragment.newInstance());
    }

    @Override
    public void showMyObjectEditFragment(@NonNull String key) {
        replaceFragmentAndAddToBackStack(MyObjectEditFragment.newInstanceWithKey(key));
    }

    @Override
    public void showDebugSettingsFragment() {
        replaceFragmentAndAddToBackStack(DebugSettingsFragment.newInstance());
    }

    @Override
    public void back() {
        AppCompatHelper.back(this);
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
