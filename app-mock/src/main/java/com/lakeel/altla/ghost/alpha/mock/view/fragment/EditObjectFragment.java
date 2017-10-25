package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.text.Editable;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.ContextHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.android.view.TextContextMenuEditText;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;

public final class EditObjectFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.textInputLayout)
    TextInputLayout textInputLayout;

    @BindView(R.id.editTextUrl)
    TextContextMenuEditText editTextUrl;

    @BindView(R.id.imageViewPhoto)
    ImageView imageViewPhoto;

    @BindView(R.id.textViewNoImage)
    TextView textViewNoImage;

    @BindView(R.id.imageViewLinkThumbnail)
    ImageView imageViewLinkThumbnail;

    @BindView(R.id.textViewLinkTitle)
    TextView textViewLinkTitle;

    @BindView(R.id.textViewObjectManager)
    TextView textViewObjectManager;

    @BindView(R.id.buttonShowPreview)
    Button buttonShowPreview;

    @BindView(R.id.mapView)
    View mapView;

    @BindView(R.id.buttonLocationPicker)
    Button buttonLocationPicker;

    private static final Log LOG = LogFactory.getLog(EditObjectFragment.class);

    private static final String BUNDLE_URI = "uri";

    private static final String BUNDLE_OBJECT_ID = "objectId";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int REQUEST_CODE_LOCATION_PICKER = 2;

    private GoogleMap map;

    private LatLng location;

    public static EditObjectFragment newInstanceWithUri(@NonNull String uri) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_URI, uri);

        EditObjectFragment fragment = new EditObjectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EditObjectFragment newInstanceWithObjectId(@NonNull String objectId) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_OBJECT_ID, objectId);

        EditObjectFragment fragment = new EditObjectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EditObjectFragment newInstance() {
        return new EditObjectFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_object, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            String objectId = getArguments().getString(BUNDLE_OBJECT_ID);
            if (objectId != null) {
                VirtualObject object = getVirtualObject(objectId);
                editTextUrl.setText(object.url);
                showPreview(object.url);
                location = new LatLng(object.location.latitude, object.location.longitude);
            }

            String uri = getArguments().getString(BUNDLE_URI);
            if (uri != null) {
                editTextUrl.setText(uri);
                showPreview(uri);
            }
        }

        // url
        editTextUrl.addTextListener(new TextContextMenuEditText.EmptyTextCallback() {
            @Override
            public void afterTextChanged(@NonNull Editable editable) {
                initPreview();
                validateUrl();
            }

            @Override
            public void onPaste(@Nullable Editable editable) {
                if (editable == null) return;
                validateUrl();
            }
        });

        editTextUrl.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == ACTION_DOWN || event.getAction() == KEYCODE_ENTER) {
                ContextHelper
                        .getInputMethodManager(getContext())
                        .hideSoftInputFromWindow(editTextUrl.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
            return false;
        });

        buttonShowPreview.setOnClickListener(v -> {
            String url = editTextUrl.getText().toString();
            if (!url.isEmpty() && textInputLayout.getError() == null) {
                showPreview(url);
            }
        });

        // location
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.mapView, supportMapFragment)
                .commit();

        supportMapFragment.getMapAsync(googleMap -> {
            map = googleMap;

            if (checkLocationPermission()) {
                setLocation();
            } else {
                requestLocationPermission();
            }
        });

        buttonLocationPicker.setOnClickListener(v -> {
            LocationPickerActivity.Builder builder = new LocationPickerActivity.Builder(getContext())
                    .setMyLocationEnabled(true)
                    .setBuildingsEnabled(false)
                    .setIndoorEnabled(true)
                    .setTrafficEnabled(false)
                    .setMapToolbarEnabled(false)
                    .setZoomControlsEnabled(true)
                    .setMyLocationButtonEnabled(true)
                    .setCompassEnabled(true)
                    .setIndoorLevelPickerEnabled(true)
                    .setAllGesturesEnabled(true);

            if (location != null) {
                builder.setLocation(location.latitude, location.longitude);
            }

            Intent intent = builder.build();
            startActivityForResult(intent, REQUEST_CODE_LOCATION_PICKER);
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save_object, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_save:
                super.onOptionsItemSelected(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                location = LocationPickerActivity.getLocation(data);
                if (location == null) {
                    LOG.w("LocationPickerActivity returns null as a location.");
                } else {
                    setLocation();
                }
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        setLocation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private void requestLocationPermission() {
        EasyPermissions
                .requestPermissions(
                        this,
                        getString(R.string.rationale_location),
                        REQUEST_LOCATION_PERMISSION,
                        Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void validateUrl() {
        String url = editTextUrl.getText().toString();

        if (url.isEmpty()) {
            textInputLayout.setError(getString(R.string.error_required));
            return;
        }

        if (Patterns.WEB_URL.matcher(url).matches()) {
            textInputLayout.setError(null);
        } else {
            textInputLayout.setError(getString(R.string.error_incorrect_url));
        }
    }

    private void initPreview() {
        textViewLinkTitle.setText(null);
        imageViewPhoto.setImageDrawable(null);
        imageViewLinkThumbnail.setImageDrawable(null);
        textViewNoImage.setVisibility(View.INVISIBLE);
    }

    private void showPreview(@NonNull String url) {
        initPreview();

        RichLinkLoader loader = new RichLinkLoader.Builder().build();

        DeferredManager dm = new AndroidDeferredManager();
        dm.when(() -> loader.load(url))
                .done(richLink -> {
                    if (richLink.getTitle() != null) {
                        textViewLinkTitle.setText(richLink.getTitle());
                        imageViewLinkThumbnail.setImageDrawable(new InitialDrawableBuilder(richLink.getTitle()).build());
                    }

                    TextDrawableImageLoader photoImageLoader = new TextDrawableImageLoader(imageViewPhoto, richLink.getUri(), richLink.getTitle());
                    photoImageLoader.loadImage();
                })
                .fail(e -> {
                    textViewLinkTitle.setText(url);
                    textViewNoImage.setVisibility(View.VISIBLE);
                });
    }

    private boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void setLocation() {
        if (checkLocationPermission()) {
            map.clear();
            map.setMyLocationEnabled(true);

            if (location == null) {
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());
                client.getLastLocation().addOnSuccessListener(result -> {
                    if (result != null) {
                        location = new LatLng(result.getLatitude(), result.getLongitude());
                        showLocation();
                    }
                });
            } else {
                showLocation();
            }
        }
    }

    private void showLocation() {
        map.addMarker(new MarkerOptions()
                .position(location));

        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(14);
        map.moveCamera(cameraUpdate);
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    private VirtualObject getVirtualObject(@NonNull String objectId) {
        ArrayMap<String, VirtualObject> objectMap = new ArrayMap<>();

        {
            VirtualObject object = new VirtualObject("objectA", "Instagram投稿の投稿者: Keisuke Sunada さん 日時: 2017 5月 5 12:39午後 UTC", "https://www.instagram.com/p/BTtgNJMgYpY/?hl=ja&taken-by=sunada.chan", new VirtualObject.Location(35.66811, 139.7401657));
            objectMap.put(object.objectId, object);
        }

        {
            VirtualObject object = new VirtualObject("objectB", "インビスハライコ", "https://tabelog.com/tokyo/A1307/A130701/13110227/", new VirtualObject.Location(35.666468, 139.73775));
            objectMap.put(object.objectId, object);
        }

        return objectMap.get(objectId);
    }

    private static final class VirtualObject {

        String objectId;

        String objectName;

        String url;

        VirtualObject.Location location;

        VirtualObject(@NonNull String objectId,
                      @NonNull String objectName,
                      @NonNull String url,
                      @NonNull VirtualObject.Location location) {
            this.objectId = objectId;
            this.objectName = objectName;
            this.url = url;
            this.location = location;
        }

        static final class Location {

            private final double latitude;

            private final double longitude;

            Location(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }
        }
    }
}
