package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.maps.MapViewLifecycle;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.ContextHelper;
import com.lakeel.altla.ghost.alpha.mock.helper.FragmentHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.android.view.TextContextMenuEditText;
import com.lakeel.altla.ghost.alpha.mock.lib.animation.helper.RevealAnimationHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.animation.helper.RevealAnimationSettings;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.wang.avi.AVLoadingIndicatorView;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;
import org.parceler.Parcels;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;

public final class AddObjectFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.textInputLayout)
    TextInputLayout textInputLayout;

    @BindView(R.id.editTextUrl)
    TextContextMenuEditText editTextUrl;

    @BindView(R.id.imageViewPhoto)
    ImageView imageViewPhoto;

    @BindView(R.id.textViewNoImage)
    TextView textViewNoImage;

    @BindView(R.id.indicatorView)
    AVLoadingIndicatorView indicatorView;

    @BindView(R.id.imageViewLinkThumbnail)
    ImageView imageViewLinkThumbnail;

    @BindView(R.id.textViewLinkTitle)
    TextView textViewLinkTitle;

    @BindView(R.id.textViewObjectManager)
    TextView textViewObjectManager;

    @BindView(R.id.buttonShowPreview)
    Button buttonShowPreview;

    @BindView(R.id.mapView)
    MapView mapView;

    @BindView(R.id.buttonLocationPicker)
    Button buttonLocationPicker;

    private static final Log LOG = LogFactory.getLog(AddObjectFragment.class);

    private static final String BUNDLE_ANIMATION_SETTINGS = "animationSettings";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int REQUEST_CODE_LOCATION_PICKER = 2;

    private GoogleMap map;

    private LatLng location;

    public static AddObjectFragment newInstance(@NonNull RevealAnimationSettings animationSettings) {
        AddObjectFragment fragment = new AddObjectFragment();

        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_ANIMATION_SETTINGS, Parcels.wrap(animationSettings));

        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_object, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RevealAnimationSettings animationSettings = Parcels.unwrap(FragmentHelper.getArguments(this).getParcelable(BUNDLE_ANIMATION_SETTINGS));
            RevealAnimationHelper
                    .startCircularRevealAnimation(
                            getContext(),
                            view,
                            animationSettings,
                            ContextCompat.getColor(getContext(), R.color.colorPrimary),
                            ContextCompat.getColor(getContext(), R.color.white));
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkInputtedText();

        buttonShowPreview.setOnClickListener(v -> {
            String url = editTextUrl.getText().toString();
            if (!url.isEmpty() && textInputLayout.getError() == null) {
                showPreview(url);
            }
        });

        // Map
        MapViewLifecycle.manage(this, mapView);
        mapView.getMapAsync(googleMap -> {
            map = googleMap;

            if (checkLocationPermission()) {
                map.setMyLocationEnabled(true);

                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());
                client.getLastLocation().addOnSuccessListener(location1 -> {
                    if (location1 != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location1.getLatitude(), location1.getLongitude())));
                    }
                });
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
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.title_add_object));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_object, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getView() != null) {
                    getFragmentManager().popBackStack();
                }
                return true;
            case R.id.action_save:
                Snackbar.make(Objects.requireNonNull(getView()), R.string.snackbar_saved, Snackbar.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
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
                    CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(5);
                    map.moveCamera(cameraUpdate);
                    map.moveCamera(CameraUpdateFactory.newLatLng(location));
                }
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                }
            });
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private void requestLocationPermission() {
        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                }
            });
        } else {
            EasyPermissions
                    .requestPermissions(
                            this,
                            getString(R.string.rationale_location),
                            REQUEST_LOCATION_PERMISSION,
                            Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkInputtedText() {
        editTextUrl.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                initPreview();
                validateUrl();
            }
        });

        editTextUrl.addTextContextMenuListener(new TextContextMenuEditText.TextContextMenuListener() {

            @Override
            public void onCut() {
            }

            @Override
            public void onPaste(@Nullable Editable editable) {
                if (editable == null) return;
                validateUrl();
            }

            @Override
            public void onCopy() {
            }
        });

        editTextUrl.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == ACTION_DOWN || event.getAction() == KEYCODE_ENTER) {
                ContextHelper.getInputMethodManager(getContext()).hideSoftInputFromWindow(editTextUrl.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
            return false;
        });
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
            textInputLayout.setError(getString(R.string.error_invalid_url));
        }
    }

    private void showPreview(@NonNull String url) {
        initPreview();

        indicatorView.setVisibility(View.VISIBLE);
        indicatorView.show();

        RichLinkLoader loader = new RichLinkLoader.Builder().build();

        DeferredManager dm = new AndroidDeferredManager();
        dm.when(() -> loader.load(url))
                .done(richLink -> {
                    indicatorView.hide();
                    textViewLinkTitle.setText(richLink.getTitle());

                    TextDrawableImageLoader photoImageLoader = new TextDrawableImageLoader(imageViewPhoto, richLink.getUri(), richLink.getTitle());
                    photoImageLoader.loadImage();

                    TextDrawableImageLoader thumbnailImageLoader = new TextDrawableImageLoader(imageViewLinkThumbnail, richLink.getUri());
                    thumbnailImageLoader.loadImage();
                })
                .fail(e -> {
                    LOG.e("Failed to fetch rich link.", e);

                    textViewLinkTitle.setText(url);
                    textViewNoImage.setVisibility(View.VISIBLE);
                })
                .always((state, resolved, rejected) -> indicatorView.smoothToHide());
    }

    private void initPreview() {
        textViewLinkTitle.setText(null);
        imageViewPhoto.setImageDrawable(null);
        imageViewLinkThumbnail.setImageDrawable(null);
        textViewNoImage.setVisibility(View.INVISIBLE);
    }

    private boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }
}