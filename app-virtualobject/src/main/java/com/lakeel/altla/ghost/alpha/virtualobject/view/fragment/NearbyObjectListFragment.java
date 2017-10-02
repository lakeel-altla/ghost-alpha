package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.OnLocationUpdatesAvailableListener;
import com.squareup.picasso.Picasso;

import org.jdeferred.DeferredManager;

import android.content.Context;
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
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public final class NearbyObjectListFragment extends Fragment implements OnLocationUpdatesAvailableListener {

    private static final Log LOG = LogFactory.getLog(NearbyObjectListFragment.class);

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int MILLIS_1000 = 1000;

    private static final int FASTEST_INTERVAL_SECONDS = 5;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    @Inject
    DeferredManager deferredManager;

    RecyclerView recyclerView;

    MapView mapView;

    private FragmentContext fragmentContext;

    private final List<Item> items = new ArrayList<>();

    @Nullable
    private LatLng queryLocation;

    private boolean quering;

    @Nullable
    private GoogleMap googleMap;

    public static NearbyObjectListFragment newInstance() {
        return new NearbyObjectListFragment();
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
        fragmentContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby_object_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view != null) {
            recyclerView = view.findViewById(R.id.recycler_view);
            mapView = view.findViewById(R.id.map_view);
        }

        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setZoomControlsEnabled(true);
//            googleMap.getUiSettings().setMyLocationButtonEnabled(debugPreferences.isManualLocationUpdatesEnabled());

            if (queryLocation == null) {
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(queryLocation, DEFAULT_ZOOM_LEVEL));

//                if (marker != null) {
//                    marker.remove();
//                }
//                marker = googleMap.addMarker(new MarkerOptions().position(queryLocation));
            }

            googleMap.setOnMapClickListener(latLng -> {
//                if (debugPreferences.isManualLocationUpdatesEnabled()) {
//                    setMyLocation(latLng);
//                }
            });

            googleMap.setOnMyLocationButtonClickListener(() -> {
//                if (debugPreferences.isManualLocationUpdatesEnabled()) {
//                    if (fragmentContext.checkLocationPermission()) {
//                        fusedLocationProviderClient
//                                .getLastLocation()
//                                .addOnSuccessListener(getActivity(), location -> {
//                                    if (location == null) {
//                                        if (marker != null) {
//                                            marker.remove();
//                                            marker = null;
//                                        }
//                                    } else {
//                                        setMyLocation(location);
//                                    }
//                                })
//                                .addOnFailureListener(getActivity(), e -> {
//                                    LOG.e("Failed to get the last location.", e);
//                                });
//                    } else {
//                        fragmentContext.requestLocationPermission();
//                    }
//                }
                return false;
            });

//            if (fragmentContext.checkLocationPermission()) {
//                // Enable the location layer.
//                googleMap.setMyLocationEnabled(true);
//            } else {
//                fragmentContext.requestLocationPermission();
//            }
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

        fragmentContext.setTitle(R.string.title_nearby_object_list);
        fragmentContext.setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);

//        mapView.setVisibility(debugPreferences.isGoogleMapVisible() ? View.VISIBLE : View.GONE);
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

        if (checkLocationPermission()) {
            // TODO
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

//        if (locationCallback != null) {
//            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//            locationCallback = null;
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
            case R.id.action_debug:
//                fragmentContext.showDebugView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onLocationUpdatesAvailable() {
//        if (!debugPreferences.isManualLocationUpdatesEnabled()) {
//            if (fragmentContext.checkLocationPermission()) {
//                locationCallback = new LocationCallback() {
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        setMyLocation(locationResult.getLastLocation());
//                    }
//
//                    @Override
//                    public void onLocationAvailability(LocationAvailability locationAvailability) {
//                        if (locationAvailability.isLocationAvailable()) {
//                            LOG.i("The location is available.");
//                        } else {
//                            LOG.w("The location is not available.");
//                        }
//                    }
//                };
//                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//            } else {
//                fragmentContext.requestLocationPermission();
//            }
//        }
    }

    boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(getContext(), ACCESS_FINE_LOCATION);
    }

    private void requestLocationPermission() {
        EasyPermissions.requestPermissions(this,
                                           getString(R.string.rationale_location),
                                           REQUEST_LOCATION_PERMISSION,
                                           ACCESS_FINE_LOCATION);
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setDisplayHomeAsUpEnabled(boolean enabled);

        void invalidateOptionsMenu();

        void checkLocationSettings(LocationRequest locationRequest);

        void addOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener);

        void removeOnLocationUpdatesAvailableListener(OnLocationUpdatesAvailableListener listener);
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
//                String placeId = item.place.placeId;
//                String name = item.place.name;
//                fragmentContext.showNearbyPlaceView(placeId, name);
                fragmentContext.invalidateOptionsMenu();
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            Item item = items.get(position);

            Picasso picasso = Picasso.with(getContext());
            picasso.setIndicatorsEnabled(true);

//            if (item.place.photos != null && 0 < item.place.photos.size()) {
//                Photo photo = item.place.photos.get(0);
//                Uri uri = placeWebApi.createPhotoUri(photo.photoReference, photo.width, photo.height);
//                LOG.v("Loading the photo: %s", uri);
//                picasso.load(uri)
//                       .into(holder.imageViewPhoto);
//            } else {
//                holder.imageViewPhoto.setImageDrawable(null);
//            }
//
//            holder.textViewName.setText(item.place.name);
//            holder.textViewDistance.setText(String.format(getString(R.string.format_nearby_place_distance),
//                                                          item.distance));
//
//            picasso.load(item.place.icon).into(holder.imageViewIcon);
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

//        private Place place;

        private float distance;

        private Item(/*Place place, */float distance) {
//            this.place = place;
            this.distance = distance;
        }

        //        @NonNull
        static Item newInstance(/*Place place, */double latitude, double longitude) {
//            Location.distanceBetween(latitude, longitude, place.geometry.location.lat,
//                                     place.geometry.location.lng,
//                                     DISTANCE_RESULTS);
//            return new Item(place, DISTANCE_RESULTS[0]);
            return null;
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
