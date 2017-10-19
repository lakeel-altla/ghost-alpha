package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.Manifest;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.List;

import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

// TODO: LocationFragment (Library)
// TODO: stepper の再検討 (先に fragment を生成している)

public final class SelectPlaceFragment extends Fragment implements EasyPermissions.PermissionCallbacks, OnMapReadyCallback, Step {

    private static final Log LOG = LogFactory.getLog(SelectPlaceFragment.class);

    private static final int RC_LOCATION_PERMISSION = 1;

    private final LocationCallback locationCallback = new LocationCallback();

    private GoogleMap map;

    private FusedLocationProviderClient client;

    public static SelectPlaceFragment newInstance() {
        return new SelectPlaceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_place, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fragmentManager = getChildFragmentManager();

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.map, supportMapFragment).commit();

        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        requestLocationUpdates();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        } else {
            requestLocationPermission();
        }

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                LOG.d("onMarkerDragStart");
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                LOG.d("onMarkerDrag");
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                LOG.d("latlng:" + latLng);
            }
        });
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
    }

    @Override
    public void onError(@NonNull VerificationError error) {
    }

    @AfterPermissionGranted(RC_LOCATION_PERMISSION)
    private void requestLocationPermission() {
        if (checkLocationPermission()) {
            requestLocationUpdates();
        } else {
            EasyPermissions
                    .requestPermissions(
                            this,
                            "aaaaa",
                            RC_LOCATION_PERMISSION,
                            Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestLocationUpdates() {
        if (!checkLocationPermission()) {
            return;
        }
        client.requestLocationUpdates(LocationRequest.create(), locationCallback, Looper.myLooper())
                .addOnFailureListener(e -> setLocationOnMap(0, 0));
    }

    private void setLocationOnMap(double latitude, double longitude) {
        if (map == null) {
            return;
        }
        map.clear();

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(14);
        map.moveCamera(cameraUpdate);

        LatLng latLng = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private final class LocationCallback extends com.google.android.gms.location.LocationCallback {

        @Override
        public void onLocationResult(LocationResult result) {
            setLocationOnMap(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude());
            client.removeLocationUpdates(locationCallback);
        }

        @Override
        public void onLocationAvailability(LocationAvailability availability) {
            if (!availability.isLocationAvailable()) {
                // Set default location.
                setLocationOnMap(0, 0);
            }
        }
    }
}