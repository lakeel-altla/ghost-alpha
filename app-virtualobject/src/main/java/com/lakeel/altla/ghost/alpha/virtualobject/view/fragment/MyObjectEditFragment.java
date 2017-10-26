package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

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
import com.lakeel.altla.ghost.alpha.google.maps.MapViewLifecycle;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.richlink.RichLink;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.PatternHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.RichLinkImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.lakeel.altla.ghost.alpha.rxhelper.RxHelper.disposeOnStop;
import static com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper.getRequiredSupportActionBar;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.findViewById;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.getRequiredActivity;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.getRequiredContext;
import static com.lakeel.altla.ghost.alpha.viewhelper.ToastHelper.showShortToast;

public final class MyObjectEditFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(MyObjectEditFragment.class);

    private static final int REQUEST_CODE_LOCATION_PICKER = 100;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    private static final String ARG_KEY = "key";

    private static final String ARG_URI_STRING = "uriString";

    @Inject
    VirtualObjectApi virtualObjectApi;

    @Inject
    RichLinkLoader richLinkLoader;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private FragmentContext fragmentContext;

    @Nullable
    private String key;

    @Nullable
    private String uriString;

    @Nullable
    private VirtualObject object;

    @Nullable
    private LatLng location;

    private TextInputLayout textInputLayoutUri;

    private TextInputEditText textInputEditTextUri;

    private Button buttonLoadRichLink;

    private ImageView imageViewRichLinkImage;

    private TextView textViewRichLinkTitle;

    @Nullable
    private GoogleMap googleMap;

    @Nullable
    private Marker marker;

    private transient boolean saving;

    @NonNull
    public static MyObjectEditFragment newInstance() {
        return new MyObjectEditFragment();
    }

    @NonNull
    public static MyObjectEditFragment newInstanceWithUriString(@NonNull String uriString) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_URI_STRING, uriString);
        MyObjectEditFragment fragment = new MyObjectEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    public static MyObjectEditFragment newInstanceWithKey(@NonNull String key) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);
        MyObjectEditFragment fragment = new MyObjectEditFragment();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            key = bundle.getString(ARG_KEY);
            uriString = bundle.getString(ARG_URI_STRING);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_object_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textInputLayoutUri = findViewById(this, R.id.text_input_layout_uri);
        textInputEditTextUri = findViewById(this, R.id.text_input_edit_text_uri);
        imageViewRichLinkImage = findViewById(this, R.id.image_view_rich_link_image);
        textViewRichLinkTitle = findViewById(this, R.id.text_view_rich_link_title);
        buttonLoadRichLink = findViewById(this, R.id.button_load_rich_link);

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
                getRequiredActivity(MyObjectEditFragment.this).invalidateOptionsMenu();
            }
        });

        MapView mapView = findViewById(this, R.id.map_view);
        MapViewLifecycle.manage(this, mapView);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(false);

            if (location == null) {
                Location lastLocation = fragmentContext.getLastLocation();
                if (lastLocation == null) {
                    location = null;
                    fragmentContext.checkLocationSettings();
                } else {
                    location = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }

            updateLocation();
        });

        buttonLoadRichLink.setOnClickListener(v -> loadRichLink());

        Button buttonPickLocation = findViewById(this, R.id.button_pick_location);
        buttonPickLocation.setOnClickListener(v -> {
            LocationPickerActivity.Builder builder = new LocationPickerActivity.Builder(getRequiredContext(this))
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

        if (key == null) {
            // This is a view to create a new object.
            if (uriString != null) {
                textInputEditTextUri.setText(uriString);
                loadRichLink();
            }
            validateUri();
        } else {
            // This is a view to edit an existing object.
            virtualObjectApi.findUserObject(CurrentUser.getInstance().getRequiredUserId(), key, object -> {
                this.object = object;
                getRequiredActivity(this).invalidateOptionsMenu();

                if (object == null) {
                    LOG.e("No virtual object exists: key = %s", key);
                } else {
                    textInputEditTextUri.setText(object.getRequiredUriString());
                    location = new LatLng(object.getRequiredGeoPoint().getLatitude(),
                                          object.getRequiredGeoPoint().getLongitude());
                    updateLocation();
                }

                validateUri();
                loadRichLink();
            }, e -> {
                LOG.e("Failed to find a virtual object.", e);
            });
        }

        saving = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_object_edit, menu);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (location == null) throw new IllegalStateException("'location' is null.");

                saving = true;
                getRequiredActivity(this).invalidateOptionsMenu();

                VirtualObject savedObject;
                if (object == null) {
                    savedObject = new VirtualObject();
                    savedObject.setUserId(CurrentUser.getInstance().getRequiredUserId());
                } else {
                    savedObject = object;
                }

                savedObject.setUriString(textInputEditTextUri.getEditableText().toString());
                savedObject.setGeoPoint(new GeoPoint(location.latitude, location.longitude));

                virtualObjectApi.saveUserObject(savedObject, aVoid -> {
                    LOG.v("Saved an object: key = %s", savedObject.getKey());
                    showShortToast(getRequiredContext(this), R.string.toast_saved);
                    fragmentContext.back();
                }, e -> {
                    LOG.e("Failed to save an object.", e);
                    showShortToast(getRequiredContext(this), R.string.toast_save_error);
                    fragmentContext.back();
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getRequiredActivity(this).setTitle(R.string.title_my_object_edit);
        getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);
        getRequiredSupportActionBar(this).setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        setHasOptionsMenu(true);
        getRequiredActivity(this).invalidateOptionsMenu();

        fragmentContext.startLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        fragmentContext.stopLocationUpdates();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                location = LocationPickerActivity.getLocation(data);
                updateLocation();
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    private void loadRichLink() {
        imageViewRichLinkImage.setImageDrawable(null);
        textViewRichLinkTitle.setText(null);

        Editable editable = textInputEditTextUri.getText();
        String text = editable.toString();
        String uriString = PatternHelper.parseUriString(text);
        if (uriString != null) {
            disposeOnStop(this, Single
                    .<RichLink>create(e -> {
                        RichLink richLink = richLinkLoader.load(uriString);
                        e.onSuccess(richLink);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(richLink -> {
                        richLinkImageLoader.load(richLink, imageViewRichLinkImage);
                        textViewRichLinkTitle.setText(richLink.getTitleOrUri());
                    }, e -> {
                        LOG.e("Failed to load a rich link.", e);
                        showShortToast(getRequiredContext(this), R.string.toast_failed_to_load_rich_link);
                    })
            );
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

    private void updateLocation() {
        getRequiredActivity(this).invalidateOptionsMenu();

        if (marker != null) {
            marker.remove();
            marker = null;
        }

        if (location != null && googleMap != null) {

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM_LEVEL);
            googleMap.moveCamera(cameraUpdate);
            marker = googleMap.addMarker(new MarkerOptions().position(location));
        }
    }

    public interface FragmentContext {

        void checkLocationSettings();

        void startLocationUpdates();

        void stopLocationUpdates();

        Location getLastLocation();

        void back();
    }
}
