package com.lakeel.altla.ghost.alpha.areaservice.view.fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.areaservice.R;
import com.lakeel.altla.ghost.alpha.areaservice.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.areaservice.helper.DebugPreferences;
import com.lakeel.altla.ghost.alpha.areaservice.helper.OnLocationUpdatesAvailableListener;
import com.lakeel.altla.ghost.alpha.areaservice.helper.RichLinkImageLoader;
import com.lakeel.altla.ghost.alpha.richlink.RichLink;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkParser;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public final class NearbyAreaListFragment extends Fragment implements OnLocationUpdatesAvailableListener {

    private static final Log LOG = LogFactory.getLog(NearbyAreaListFragment.class);

    private static final int MILLIS_1000 = 1000;

    private static final int FASTEST_INTERVAL_SECONDS = 5;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    private static final float[] TEMP_DISTANCE_RESULTS = new float[1];

    @Inject
    RichLinkParser richLinkParser;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FragmentContext fragmentContext;

    private DebugPreferences debugPreferences;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationCallback locationCallback;

    private LocationRequest locationRequest;

    private final List<Item> items = new ArrayList<>();

    private RecyclerView recyclerView;

    private MapView mapView;

    private TextView textViewAccuracyValue;

    @Nullable
    private LatLng location;

    @Nullable
    private GoogleMap googleMap;

    @Nullable
    private Marker marker;

    public static NearbyAreaListFragment newInstance() {
        return new NearbyAreaListFragment();
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
        return inflater.inflate(R.layout.fragment_nearby_area_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        debugPreferences = new DebugPreferences(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        recyclerView = getView().findViewById(R.id.recycler_view);
        textViewAccuracyValue = getView().findViewById(R.id.text_view_accuracy_value);
        mapView = getView().findViewById(R.id.map_view);

        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(debugPreferences.isManualLocationUpdatesEnabled());

            if (location == null) {
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM_LEVEL));

                if (marker != null) {
                    marker.remove();
                }
                marker = googleMap.addMarker(new MarkerOptions().position(location));
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
        inflater.inflate(R.menu.nearby_object_list, menu);
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

        getActivity().setTitle(R.string.title_nearby_area_list);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);

        mapView.setVisibility(debugPreferences.isGoogleMapVisible() ? View.VISIBLE : View.GONE);

        textViewAccuracyValue.setText(R.string.value_not_available);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        compositeDisposable.clear();
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

//        if (objectQuery != null) {
//            if (objectQueryEventListener != null) {
//                objectQuery.removeObjectQueryEventListener(objectQueryEventListener);
//                objectQueryEventListener = null;
//            }
//            objectQuery = null;
//            items.clear();
//        }
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
            case R.id.action_add_object:
                // TODO
//                fragmentContext.showObjectEditView();
                return true;
            case R.id.action_debug:
                fragmentContext.showDebugSettingsView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //    @Override
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

    private void setMyLocation(@NonNull LatLng location) {
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));

            if (marker != null) {
                marker.remove();
            }
            marker = googleMap.addMarker(new MarkerOptions().position(location));
        }

        boolean locationChanged = false;

        if (this.location == null) {
            this.location = location;
            locationChanged = true;
        } else if (this.location.latitude != location.latitude || this.location.longitude != location.longitude) {
            this.location = location;
            locationChanged = true;
        }

//        if (objectQuery == null) {
//            int radius = debugPreferences.getSearchRadius();
//            GeoPoint center = new GeoPoint(location.latitude, location.longitude);
//            objectQuery = virtualObjectApi.queryUserObjects(CurrentUser.getInstance().getRequiredUserId(),
//                                                            center, radius);
//            objectQueryEventListener = new VirtualObjectApi.ObjectQueryEventListener() {
//                @Override
//                public void onObjectEntered(VirtualObject object) {
//                    LOG.v("onObjectEntered: key = %s", object.getKey());
//
//                    Item item = new Item(object);
//
//                    deferredManager
//                            .when(() -> {
//                                item.updateDistance(location);
//                                try {
//                                    item.loadRichLink();
//                                } catch (IOException e) {
//                                    // Ignore.
//                                }
//                            })
//                            .done(result -> {
//                                items.add(item);
//                                Collections.sort(items, ItemComparator.INSTANCE);
//                                recyclerView.getAdapter().notifyDataSetChanged();
//                            })
//                            .fail(e -> {
//                                LOG.w("Failed to parse the rich link: " + object.getRequiredUriString(), e);
//                            });
//                }
//
//                @Override
//                public void onObjectExited(String key) {
//                    LOG.v("onObjectExited: key = %s", key);
//                    int index = -1;
//                    for (int i = 0; i < items.size(); i++) {
//                        Item item = items.get(i);
//                        if (key.equals(item.object.getKey())) {
//                            index = i;
//                            break;
//                        }
//                    }
//                    if (0 <= index) {
//                        items.remove(index);
//                        recyclerView.getAdapter().notifyDataSetChanged();
//                    }
//                }
//
//                @Override
//                public void onObjectQueryReady() {
//                    LOG.v("onObjectQueryReady");
//                }
//
//                @Override
//                public void onObjectQueryError(Exception e) {
//                    LOG.e("Failed to query user objects.", e);
//                }
//            };
//            objectQuery.addObjectQueryEventListener(objectQueryEventListener);
//        } else {
//            if (locationChanged) {
//                for (Item item : items) {
//                    item.updateDistance(location);
//                }
//                recyclerView.getAdapter().notifyDataSetChanged();
//
//                GeoPoint center = new GeoPoint(location.latitude, location.longitude);
//                objectQuery.setCenter(center);
//            }
//        }
    }

    public interface FragmentContext {

        boolean checkLocationPermission();

        void requestLocationPermission();

        void checkLocationSettings(LocationRequest locationRequest);

        void addOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener);

        void removeOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener);
//
//        void showObjectEditView();

        void showDebugSettingsView();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        LayoutInflater inflater;

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            View itemView = inflater.inflate(R.layout.item_nearby_area, parent, false);
            itemView.setOnClickListener(v -> {
                int position = recyclerView.getChildAdapterPosition(v);
                Item item = items.get(position);

//                String uriString = item.object.getRequiredUriString();
//                Uri uri = Uri.parse(uriString);
//
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.imageViewPhoto.setImageDrawable(null);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            Item item = items.get(position);

            richLinkImageLoader.load(item.richLink, holder.imageViewPhoto);
            holder.textViewName.setText(item.richLink.getTitleOrUri());
            holder.textViewDistance.setText(String.format(getString(R.string.format_nearby_object_distance),
                                                          item.distance));
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

    private final class Item {

//        final VirtualObject object;

        float distance;

        RichLink richLink;

        Item(/*@NonNull VirtualObject object*/) {
//            this.object = object;
        }

        void updateDistance(@NonNull LatLng location) {
//            Location.distanceBetween(location.latitude, location.longitude,
//                                     object.getRequiredGeoPoint().getLatitude(),
//                                     object.getRequiredGeoPoint().getLongitude(),
//                                     TEMP_DISTANCE_RESULTS);
            distance = TEMP_DISTANCE_RESULTS[0];
        }

        void loadRichLink() throws IOException {
//            String uriString = object.getRequiredUriString();
//            richLink = richLinkParser.parse(uriString);
        }
    }

    private static final class ItemComparator implements Comparator<Item> {

        static final ItemComparator INSTANCE = new ItemComparator();

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
