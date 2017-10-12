package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObject;
import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObjectApi;
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkParser;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.Names;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.PatternHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.RichLinkImageLoader;

import org.jdeferred.DeferredManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

public final class ObjectEditFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(ObjectEditFragment.class);

    private static final int REQUEST_CODE_LOCATION_PICKER = 100;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    private static final String ARG_URI_STRING = "uriString";

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;

    @Named(Names.GOOGLE_API_KEY)
    @Inject
    String googleApiKey;

    @Inject
    DeferredManager deferredManager;

    @Inject
    VirtualObjectApi virtualObjectApi;

    @Inject
    RichLinkParser richLinkParser;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private FragmentContext fragmentContext;

    private TextInputLayout textInputLayoutUri;

    private TextInputEditText textInputEditTextUri;

    private MapView mapView;

    private Button buttonLoadRichLink;

    private ImageView imageViewPhoto;

    private TextView textViewName;

    @Nullable
    private GoogleMap googleMap;

    @Nullable
    private Marker marker;

    @Nullable
    private LatLng location;

    private transient boolean saving;

    @NonNull
    public static ObjectEditFragment newInstance() {
        return new ObjectEditFragment();
    }

    @NonNull
    public static ObjectEditFragment newInstance(@NonNull String uriString) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_URI_STRING, uriString);
        ObjectEditFragment fragment = new ObjectEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentContext = (FragmentContext) context;
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        textInputLayoutUri = getView().findViewById(R.id.text_input_layout_uri);
        textInputEditTextUri = getView().findViewById(R.id.text_input_edit_text_uri);
        mapView = getView().findViewById(R.id.map_view);
        imageViewPhoto = getView().findViewById(R.id.image_view_photo);
        textViewName = getView().findViewById(R.id.text_view_name);
        buttonLoadRichLink = getView().findViewById(R.id.button_load_rich_link);

        textInputEditTextUri.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateUri();
                getActivity().invalidateOptionsMenu();
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(false);

            if (location == null) {
                if (fragmentContext.checkLocationPermission()) {
                    fusedLocationProviderClient
                            .getLastLocation()
                            .addOnSuccessListener(getActivity(), location -> {
                                updateLocation(new LatLng(location.getLatitude(), location.getLongitude()), true);
                            })
                            .addOnFailureListener(getActivity(), e -> {
                                LOG.e("Failed to get the last location.", e);
                            });
                } else {
                    fragmentContext.requestLocationPermission();
                }
            } else {
                updateLocation(location, true);
            }
        });

        buttonLoadRichLink.setOnClickListener(v -> {
            loadRichLink();
        });

        Button buttonPickLocation = getView().findViewById(R.id.button_pick_location);
        buttonPickLocation.setOnClickListener(v -> {
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

        Bundle bundle = getArguments();
        if (bundle != null) {
            String uriString = bundle.getString(ARG_URI_STRING);
            if (uriString != null) {
                textInputEditTextUri.setText(uriString);
                loadRichLink();
            }
        }

        validateUri();

        saving = false;
    }

    private void loadRichLink() {
        imageViewPhoto.setImageDrawable(null);
        textViewName.setText(null);

        Editable editable = textInputEditTextUri.getText();
        String text = editable.toString();
        String uriString = PatternHelper.parseUriString(text);
        if (uriString != null) {
            deferredManager
                    .when(() -> {
                        try {
                            return richLinkParser.parse(uriString);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .done(richLink -> {
                        richLinkImageLoader.load(richLink, imageViewPhoto);
                        textViewName.setText(richLink.getTitleOrUri());
                    })
                    .fail(e -> {
                        LOG.e("Failed to load a rich link.", e);
                        Toast.makeText(getContext(), R.string.toast_failed_to_load_rich_link, Toast.LENGTH_SHORT)
                             .show();
                    });
        }
    }

    private void validateUri() {
        Editable editable = textInputEditTextUri.getText();
        String text = editable.toString();
        String uriString = PatternHelper.parseUriString(text);
        String error = (uriString != null) ? null : getString(R.string.input_error_uri);
        textInputLayoutUri.setError(error);
        buttonLoadRichLink.setEnabled(error == null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.object_edit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean uriStringSpecified = false;
        Editable editable = textInputEditTextUri.getText();
        if (editable != null) {
            String text = editable.toString();
            String uriString = PatternHelper.parseUriString(text);
            uriStringSpecified = (uriString != null);
        }

        boolean saveEnabled = !saving && uriStringSpecified;

        menu.findItem(R.id.action_save).setEnabled(saveEnabled);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        getActivity().setTitle(R.string.title_object_edit);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);
        AppCompatHelper.getRequiredSupportActionBar(this).setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (location == null) throw new IllegalStateException("'location' is null.");

                saving = true;
                getActivity().invalidateOptionsMenu();

                VirtualObject virtualObject = new VirtualObject();
                virtualObject.setUserId(CurrentUser.getInstance().getRequiredUserId());
                virtualObject.setUriString(textInputEditTextUri.getEditableText().toString());
                virtualObject.setGeoPoint(new GeoPoint(location.latitude, location.longitude));

                virtualObjectApi.saveUserObject(virtualObject, aVoid -> {
                    LOG.v("Saved an object: key = %s", virtualObject.getKey());
                    Toast.makeText(getContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show();
                    AppCompatHelper.back(this);
                }, e -> {
                    LOG.e("Failed to save an object.", e);
                    Toast.makeText(getContext(), R.string.toast_save_error, Toast.LENGTH_SHORT).show();
                    AppCompatHelper.back(this);
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                updateLocation(LocationPickerActivity.getLocation(data), true);
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    private void updateLocation(@Nullable LatLng location, boolean adjustZoomLevel) {
        this.location = location;
        getActivity().invalidateOptionsMenu();

        if (marker != null) {
            marker.remove();
            marker = null;
        }

        if (location != null && googleMap != null) {

            CameraUpdate cameraUpdate;
            if (adjustZoomLevel) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM_LEVEL);
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLng(location);
            }

            googleMap.moveCamera(cameraUpdate);
            marker = googleMap.addMarker(new MarkerOptions().position(location));
        }
    }

    public interface FragmentContext {

        boolean checkLocationPermission();

        void requestLocationPermission();
    }
}
