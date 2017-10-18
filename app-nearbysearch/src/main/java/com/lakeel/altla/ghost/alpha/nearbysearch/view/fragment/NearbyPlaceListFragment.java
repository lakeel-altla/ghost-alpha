package com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.maps.urls.SearchUrlBuilder;
import com.lakeel.altla.ghost.alpha.google.place.web.Photo;
import com.lakeel.altla.ghost.alpha.google.place.web.Place;
import com.lakeel.altla.ghost.alpha.google.place.web.PlaceWebApi;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.Preferences;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.activity.SettingsActivity;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class NearbyPlaceListFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(NearbyPlaceListFragment.class);

    private static final int REQUEST_CODE_LOCATION_PICKER = 100;

    @Inject
    PlaceWebApi placeWebApi;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FragmentContext fragmentContext;

    private Preferences preferences;

    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    @Nullable
    private LatLng location;

    private boolean quering;

    private final List<Item> items = new ArrayList<>();

    @NonNull
    public static NearbyPlaceListFragment newInstance() {
        return new NearbyPlaceListFragment();
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
        return inflater.inflate(R.layout.fragment_nearby_place_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferences = new Preferences(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(GONE);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            if (preferences.isManualLocationUpdatesEnabled()) {
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
            } else {
                if (fragmentContext.checkLocationPermission()) {
                    fragmentContext.getLastLocation(task -> {
                        if (task.isSuccessful()) {
                            Location lastLocation = task.getResult();
                            if (lastLocation == null) {
                                location = null;
                                LOG.w("The last location could not be resolved.");
                            } else {
                                location = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                                searchPlaces();
                            }
                        } else {
                            LOG.e("Failed to get the last location.", task.getException());
                        }
                    });
                } else {
                    fragmentContext.requestLocationPermission();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nearby_place_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                getActivity().startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().setTitle(R.string.title_nearby_place_list);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fragmentContext.checkLocationPermission()) {
            fragmentContext.checkLocationSettings();
        } else {
            fragmentContext.requestLocationPermission();
        }

        fragmentContext.startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        fragmentContext.stopLocationUpdates();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                location = LocationPickerActivity.getLocation(data);
                searchPlaces();
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    private void searchPlaces() {
        if (location == null) return;
        if (quering) return;

        quering = true;

        double latitude = location.latitude;
        double longitude = location.longitude;
        int radius = preferences.getSearchRadius();

        items.clear();
        recyclerView.getAdapter().notifyDataSetChanged();

        progressBar.setVisibility(VISIBLE);

        Disposable disposable = Single
                .<List<Place>>create(e -> {
                    List<Place> places = placeWebApi.searchPlaces(latitude, longitude, radius, null);
                    e.onSuccess(places);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(places -> {
                    for (Place place : places) {
                        if (!place.permanentlyClosed) {
                            items.add(Item.newInstance(place, latitude, longitude));
                        }
                    }

                    Collections.sort(items, ItemComparator.INSTANCE);
                    recyclerView.getAdapter().notifyDataSetChanged();

                    progressBar.setVisibility(GONE);
                    quering = false;
                }, e -> {
                    LOG.e("Failed to search nearby places.", e);

                    progressBar.setVisibility(GONE);
                    quering = false;
                });
        compositeDisposable.add(disposable);
    }

    private <T extends View> T findViewById(@IdRes int id) {
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");
        return getView().findViewById(id);
    }

    public interface FragmentContext {

        boolean checkLocationPermission();

        void requestLocationPermission();

        void checkLocationSettings();

        void startLocationUpdates();

        void stopLocationUpdates();

        void getLastLocation(OnCompleteListener<Location> onCompleteListener);

        void showPlaceFragment(@NonNull String placeId, @NonNull String name);
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

                if (preferences.isPlaceDetailsViewEnabled()) {
                    String placeId = item.place.placeId;
                    String name = item.place.name;

                    fragmentContext.showPlaceFragment(placeId, name);
                    getActivity().invalidateOptionsMenu();
                } else {
                    String placeId = item.place.placeId;
                    double latitude = item.place.geometry.location.lat;
                    double longitude = item.place.geometry.location.lng;

                    String uriString = new SearchUrlBuilder(latitude, longitude).setPlaceId(placeId).build();
                    Uri uri = Uri.parse(uriString);

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
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
