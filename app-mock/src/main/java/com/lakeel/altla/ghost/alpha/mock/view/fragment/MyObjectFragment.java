package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.FragmentHelper;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public final class MyObjectFragment extends Fragment implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    @BindView(R.id.imageViewObject)
    ImageView imageViewObject;

    @BindView(R.id.textViewObjectName)
    TextView textViewObjectName;

    @BindView(R.id.textViewUrl)
    TextView textViewUrl;

    private static final Log LOG = LogFactory.getLog(MyObjectFragment.class);

    private static final int RC_LOCATION_PERMISSION = 1;

    private static final String BUNDLE_KEY_OBJECT_ID = "objectId";

    private TextDrawableImageLoader imageLoader;

    private GoogleMap map;

    public static MyObjectFragment newInstance(@NonNull Context context, @NonNull String objectId) {
        MyObjectFragment fragment = new MyObjectFragment();

        fragment.setSharedElementEnterTransition(TransitionInflater.from(context).inflateTransition(R.transition.move));

        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_OBJECT_ID, objectId);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_object, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        imageViewObject.setTransitionName(getString(R.string.transition_imageView));
        textViewObjectName.setTransitionName(getString(R.string.transition_textView));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        String objectId = FragmentHelper.getBundleString(this, BUNDLE_KEY_OBJECT_ID);
        VirtualObject object = getVirtualObject(objectId);

        textViewObjectName.setText(object.objectName);

        imageLoader = new TextDrawableImageLoader(imageViewObject, object.url, object.objectName);
        imageLoader.loadImage();

        textViewUrl.setText(object.url);

        // Map
        FragmentManager fragmentManager = getChildFragmentManager();

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.map, supportMapFragment).commit();

        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        imageLoader.cancel();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_my_object, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_remove:
                Snackbar.make(Objects.requireNonNull(getView()), R.string.snackbar_removed, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.snackbar_action_undo, v -> {
                        }).show();
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERMISSION)
    private void requestLocationPermission() {
        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        } else {
            EasyPermissions
                    .requestPermissions(
                            this,
                            getString(R.string.rationale_location),
                            RC_LOCATION_PERMISSION,
                            Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private VirtualObject getVirtualObject(@NonNull String objectId) {
        ArrayMap<String, VirtualObject> objectMap = new ArrayMap<>();

        {
            VirtualObject object = new VirtualObject("objectA", "Instagram投稿の投稿者: Keisuke Sunada さん 日時: 2017 5月 5 12:39午後 UTC", "https://www.instagram.com/p/BTtgNJMgYpY/?hl=ja&taken-by=sunada.chan", new VirtualObject.Location(35.6671141, 139.7401657));
            objectMap.put(object.objectId, object);
        }

        {
            VirtualObject object = new VirtualObject("objectB", "インビスハライコ", "https://tabelog.com/tokyo/A1307/A130701/13110227/", new VirtualObject.Location(35.667113, 139.740165));
            objectMap.put(object.objectId, object);
        }

        return objectMap.get(objectId);
    }

    private static final class VirtualObject {

        public String objectId;

        public String objectName;

        public String url;

        public Location location;

        public VirtualObject(@NonNull String objectId,
                             @NonNull String objectName,
                             @NonNull String url,
                             @NonNull Location location) {
            this.objectId = objectId;
            this.objectName = objectName;
            this.url = url;
            this.location = location;
        }

        public static final class Location {

            private final double latitude;

            private final double longitude;

            public Location(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }
        }
    }
}
