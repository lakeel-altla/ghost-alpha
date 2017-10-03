package com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.place.web.Photo;
import com.lakeel.altla.ghost.alpha.google.place.web.Place;
import com.lakeel.altla.ghost.alpha.google.place.web.PlaceWebApi;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.DebugPreferences;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.OnLocationUpdatesAvailableListener;
import com.squareup.picasso.Picasso;

import org.jdeferred.DeferredManager;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public final class NearbyPlaceListFragment extends Fragment implements OnLocationUpdatesAvailableListener {

    private static final Log LOG = LogFactory.getLog(NearbyPlaceListFragment.class);

    private static final int MILLIS_1000 = 1000;

    private static final int FASTEST_INTERVAL_SECONDS = 5;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    @Inject
    DebugPreferences debugPreferences;

    @Inject
    DeferredManager deferredManager;

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;

    @Inject
    PlaceWebApi placeWebApi;

    private FragmentContext fragmentContext;

    private RecyclerView recyclerView;

    private MapView mapView;

    private TextView textViewAccuracyValue;

    private LocationCallback locationCallback;

    private LocationRequest locationRequest;

    @Nullable
    private LatLng queryLocation;

    private boolean quering;

    private final List<Item> items = new ArrayList<>();

    @Nullable
    private GoogleMap googleMap;

    @Nullable
    private Marker marker;

    @NonNull
    public static NearbyPlaceListFragment newInstance() {
        return new NearbyPlaceListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentContext = (FragmentContext) context;
        fragmentContext.addOnLocationUpdatesAvailableListener(this);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext.removeOnLocationUpdatesAvailableListener(this);
        fragmentContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby_place_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        recyclerView = getView().findViewById(R.id.recycler_view);
        mapView = getView().findViewById(R.id.map_view);
        textViewAccuracyValue = getView().findViewById(R.id.text_view_accuracy_value);

        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(debugPreferences.isManualLocationUpdatesEnabled());

            if (queryLocation == null) {
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(queryLocation, DEFAULT_ZOOM_LEVEL));

                if (marker != null) {
                    marker.remove();
                }
                marker = googleMap.addMarker(new MarkerOptions().position(queryLocation));
            }

            googleMap.setOnMapClickListener(latLng -> {
                if (debugPreferences.isManualLocationUpdatesEnabled()) {
                    setMyLocation(latLng);
                }
            });

            googleMap.setOnMyLocationButtonClickListener(() -> {
                if (debugPreferences.isManualLocationUpdatesEnabled()) {
                    if (fragmentContext.checkLocationPermission()) {
                        fusedLocationProviderClient
                                .getLastLocation()
                                .addOnSuccessListener(getActivity(), location -> {
                                    if (location == null) {
                                        if (marker != null) {
                                            marker.remove();
                                            marker = null;
                                        }
                                    } else {
                                        setMyLocation(location);
                                    }
                                })
                                .addOnFailureListener(getActivity(), e -> {
                                    LOG.e("Failed to get the last location.", e);
                                });
                    } else {
                        fragmentContext.requestLocationPermission();
                    }
                }
                return false;
            });

            if (fragmentContext.checkLocationPermission()) {
                // Enable the location layer.
                googleMap.setMyLocationEnabled(true);
            } else {
                fragmentContext.requestLocationPermission();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nearby_place_list, menu);
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

        fragmentContext.setTitle(R.string.title_nearby_place_list);
        fragmentContext.setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);

        mapView.setVisibility(debugPreferences.isGoogleMapVisible() ? View.VISIBLE : View.GONE);

        textViewAccuracyValue.setText(R.string.value_not_available);
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

        if (fragmentContext.checkLocationPermission()) {
            initializeLocationRequest();
        } else {
            fragmentContext.requestLocationPermission();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
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
            case R.id.action_debug:
                fragmentContext.showDebugView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationUpdatesAvailable() {
        if (!debugPreferences.isManualLocationUpdatesEnabled()) {
            if (fragmentContext.checkLocationPermission()) {
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        setMyLocation(locationResult.getLastLocation());
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        if (locationAvailability.isLocationAvailable()) {
                            LOG.i("The location is available.");
                        } else {
                            LOG.w("The location is not available.");
                        }
                    }
                };
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                fragmentContext.requestLocationPermission();
            }
        }
    }

    private void initializeLocationRequest() {
        locationRequest = new LocationRequest();

        locationRequest.setPriority(debugPreferences.getLocationRequestPriority());
        locationRequest.setInterval(debugPreferences.getLocationUpdatesInterval() * MILLIS_1000);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_SECONDS * MILLIS_1000);

        fragmentContext.checkLocationSettings(locationRequest);
    }

    private void setMyLocation(@NonNull Location location) {
        if (location.hasAccuracy()) {
            textViewAccuracyValue.setText(String.valueOf(location.getAccuracy()));
        } else {
            textViewAccuracyValue.setText(R.string.value_not_available);
        }

        setMyLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void setMyLocation(@NonNull LatLng latLng) {
        LOG.i("The location is resolved: latitude,longitude = %f,%f", latLng.latitude, latLng.longitude);

        if (googleMap != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            googleMap.moveCamera(cameraUpdate);

            if (marker != null) {
                marker.remove();
            }
            marker = googleMap.addMarker(new MarkerOptions().position(latLng));
        }

        if (quering) {
            LOG.d("Skip a new query because the previous query is not finished.");
            return;
        }

        boolean shouldSearch = false;

        if (queryLocation == null) {
            queryLocation = latLng;
            shouldSearch = true;
        } else {
            float[] results = new float[1];
            Location.distanceBetween(queryLocation.latitude, queryLocation.longitude,
                                     latLng.latitude, latLng.longitude, results);
            float distance = results[0];

            LOG.v("The location is moved: distance = %f", distance);

            if (debugPreferences.getLocationUpdatesDistance() <= distance) {
                queryLocation = latLng;
                shouldSearch = true;
            }
        }

        if (shouldSearch) {
            quering = true;

            double latitude = queryLocation.latitude;
            double longitude = queryLocation.longitude;

            deferredManager.when(() -> {
                searchNearbyPlaces(latitude, longitude);
            }).fail(e -> {
                LOG.e("Failed to search nearby places.", e);
                getActivity().runOnUiThread(() -> {
                    quering = false;
                });
            });
        }
    }

    private void searchNearbyPlaces(double latitude, double longitude) {
        int radius = debugPreferences.getSearchRadius();

        LOG.v("Searching nearby places: latitude = %f, longitude = %f, radius = %d, language = %s",
              latitude, longitude, radius, null);

        List<Place> places = placeWebApi.searchPlaces(latitude, longitude, radius, null);

        LOG.v("Searched nearby places.");

        items.clear();

        for (Place place : places) {
            if (!place.permanentlyClosed) {
                items.add(Item.newInstance(place, latitude, longitude));
            }
        }

        Collections.sort(items, ItemComparator.INSTANCE);

        getActivity().runOnUiThread(() -> {
            recyclerView.getAdapter().notifyDataSetChanged();
            quering = false;
        });
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setDisplayHomeAsUpEnabled(boolean enabled);

        void invalidateOptionsMenu();

        boolean checkLocationPermission();

        void requestLocationPermission();

        void checkLocationSettings(LocationRequest locationRequest);

        void addOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener);

        void removeOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener);

        void showDebugView();

        void showNearbyPlaceView(@NonNull String placeId, @NonNull String name);
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        LayoutInflater inflater;

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            View itemView = inflater.inflate(R.layout.item_nearby_place, parent, false);
            itemView.setOnClickListener(v -> {
                int position = recyclerView.getChildAdapterPosition(v);
                Item item = items.get(position);
                String placeId = item.place.placeId;
                double latitude = item.place.geometry.location.lat;
                double longitude = item.place.geometry.location.lng;

                String uriString = "https://www.google.com/maps/search/?api=1&query=" +
                                   latitude + "," + longitude +
                                   "&query_place_id=" + placeId;
                Uri uri = Uri.parse(uriString);

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

//                String name = item.place.name;
//                fragmentContext.showNearbyPlaceView(placeId, name);
//                fragmentContext.invalidateOptionsMenu();
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            Item item = items.get(position);

            Picasso picasso = Picasso.with(getContext());
            picasso.setIndicatorsEnabled(true);

            if (item.place.photos != null && 0 < item.place.photos.size()) {
                Photo photo = item.place.photos.get(0);
                Uri uri = placeWebApi.createPhotoUri(photo.photoReference, photo.width, photo.height);
                LOG.v("Loading the photo: %s", uri);
                picasso.load(uri)
                       .into(holder.imageViewPhoto);
            } else {
                holder.imageViewPhoto.setImageDrawable(null);
            }

            holder.textViewName.setText(item.place.name);
            holder.textViewDistance.setText(String.format(getString(R.string.format_nearby_place_distance),
                                                          item.distance));

            picasso.load(item.place.icon).into(holder.imageViewIcon);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageViewPhoto;

            ImageView imageViewIcon;

            TextView textViewName;

            TextView textViewDistance;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewPhoto = itemView.findViewById(R.id.image_view_photo);
                imageViewIcon = itemView.findViewById(R.id.image_view_icon);
                textViewName = itemView.findViewById(R.id.text_view_name);
                textViewDistance = itemView.findViewById(R.id.text_view_distance);
            }
        }
    }

    private static final class Item {

        private static final float[] DISTANCE_RESULTS = new float[1];

        private Place place;

        private float distance;

        private Item(Place place, float distance) {
            this.place = place;
            this.distance = distance;
        }

        @NonNull
        static Item newInstance(Place place, double latitude, double longitude) {
            Location.distanceBetween(latitude, longitude, place.geometry.location.lat,
                                     place.geometry.location.lng,
                                     DISTANCE_RESULTS);
            return new Item(place, DISTANCE_RESULTS[0]);
        }
    }

    private static final class ItemComparator implements Comparator<Item> {

        static final ItemComparator INSTANCE = new ItemComparator();

        private ItemComparator() {
        }

        @Override
        public int compare(Item o1, Item o2) {
            if (o1.distance < o2.distance) {
                return -1;
            } else if (o2.distance < o1.distance) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
