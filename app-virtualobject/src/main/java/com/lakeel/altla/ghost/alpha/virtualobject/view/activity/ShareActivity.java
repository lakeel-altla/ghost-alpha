package com.lakeel.altla.ghost.alpha.virtualobject.view.activity;

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
import com.lakeel.altla.ghost.alpha.virtualobject.helper.PatternHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.ObjectEditFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ShareActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   EasyPermissions.PermissionCallbacks,
                   ObjectEditFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(ShareActivity.class);

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private ActivityComponent activityComponent;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

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
            replaceFragment(ObjectEditFragment.newInstance(uriString));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = MainActivity.createStartActivityIntent(this);
                startActivity(intent);
                finish();
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

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }
}
