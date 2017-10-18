package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObject;
import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObjectApi;
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.locationpicker.LocationPickerActivity;
import com.lakeel.altla.ghost.alpha.richlink.RichLink;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.Preferences;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.RichLinkImageLoader;
import com.lakeel.altla.ghost.alpha.virtualobject.view.activity.SettingsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class NearbyObjectListFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(NearbyObjectListFragment.class);

    private static final int MILLIS_1000 = 1000;

    private static final int FASTEST_INTERVAL_SECONDS = 5;

    private static final float[] TEMP_DISTANCE_RESULTS = new float[1];

    private static final int REQUEST_CODE_LOCATION_PICKER = 100;

    @Inject
    VirtualObjectApi virtualObjectApi;

    @Inject
    RichLinkLoader richLinkLoader;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FragmentContext fragmentContext;

    private Preferences preferences;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest locationRequest;

    private final List<Item> items = new ArrayList<>();

    private RecyclerView recyclerView;

    @Nullable
    private LatLng location;

    @Nullable
    private VirtualObjectApi.ObjectQuery objectQuery;

    @Nullable
    private VirtualObjectApi.ObjectQueryEventListener objectQueryEventListener;

    public static NearbyObjectListFragment newInstance() {
        return new NearbyObjectListFragment();
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
        return inflater.inflate(R.layout.fragment_nearby_object_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        preferences = new Preferences(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        recyclerView = getView().findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton floatingActionButton = getView().findViewById(R.id.fab);
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
                Location lastLocation = fragmentContext.getLastLocation();
                if (lastLocation == null) {
                    LOG.w("The last location could not be resolved.");
                    location = null;
                    LOG.w("Trying to check location settings.");
                    fragmentContext.checkLocationSettings();
                } else {
                    location = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    searchObjects();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nearby_object_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list_my_objects:
                fragmentContext.showMyObjectListFragment();
                return true;
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

        getActivity().setTitle(R.string.title_nearby_object_list);
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
        fragmentContext.startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentContext.stopLocationUpdates();

        if (objectQuery != null) {
            if (objectQueryEventListener != null) {
                objectQuery.removeObjectQueryEventListener(objectQueryEventListener);
                objectQueryEventListener = null;
            }
            objectQuery = null;
            items.clear();
        }
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
                    searchObjects();
                }
            } else {
                LOG.d("Picking a location is cancelled.");
            }
        }
    }

    private void searchObjects() {
        if (location == null) return;

        if (objectQuery == null) {
            int radius = preferences.getSearchRadius();
            GeoPoint center = new GeoPoint(location.latitude, location.longitude);
            objectQuery = virtualObjectApi.queryUserObjects(CurrentUser.getInstance().getRequiredUserId(),
                                                            center, radius);
            objectQueryEventListener = new VirtualObjectApi.ObjectQueryEventListener() {
                @Override
                public void onObjectEntered(VirtualObject object) {
                    LOG.v("onObjectEntered: key = %s", object.getKey());

                    Item item = new Item(object);
                    Disposable disposable = Completable
                            .create(e -> {
                                item.updateDistance(location);
                                item.loadRichLink();
                                e.onComplete();
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                items.add(item);
                                Collections.sort(items, ItemComparator.INSTANCE);
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }, e -> {
                                LOG.w("Failed to load the rich link: " + object.getRequiredUriString(), e);
                            });
                    compositeDisposable.add(disposable);
                }

                @Override
                public void onObjectExited(String key) {
                    LOG.v("onObjectExited: key = %s", key);
                    int index = -1;
                    for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        if (key.equals(item.object.getKey())) {
                            index = i;
                            break;
                        }
                    }
                    if (0 <= index) {
                        items.remove(index);
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                }

                @Override
                public void onObjectQueryReady() {
                    LOG.v("onObjectQueryReady");
                }

                @Override
                public void onObjectQueryError(Exception e) {
                    LOG.e("Failed to query user objects.", e);
                }
            };
            objectQuery.addObjectQueryEventListener(objectQueryEventListener);
        } else {
            for (Item item : items) {
                item.updateDistance(location);
            }
            recyclerView.getAdapter().notifyDataSetChanged();

            GeoPoint center = new GeoPoint(location.latitude, location.longitude);
            objectQuery.setCenter(center);
        }
    }

    public interface FragmentContext {

        void checkLocationSettings();

        void startLocationUpdates();

        void stopLocationUpdates();

        Location getLastLocation();

        void showMyObjectListFragment();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        LayoutInflater inflater;

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            View itemView = inflater.inflate(R.layout.item_nearby_object, parent, false);
            itemView.setOnClickListener(v -> {
                int position = recyclerView.getChildAdapterPosition(v);
                Item item = items.get(position);

                String uriString = item.object.getRequiredUriString();
                Uri uri = Uri.parse(uriString);

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.imageViewRichLinkImage.setImageDrawable(null);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            Item item = items.get(position);

            richLinkImageLoader.load(item.richLink, holder.imageViewRichLinkImage);
            holder.textViewRichLinkTitle.setText(item.richLink.getTitleOrUri());
            holder.textViewDistance.setText(String.format(getString(R.string.format_nearby_object_distance),
                                                          item.distance));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageViewRichLinkImage;

            ImageView imageViewIcon;

            TextView textViewRichLinkTitle;

            TextView textViewDistance;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageViewRichLinkImage = itemView.findViewById(R.id.image_view_rich_link_image);
                imageViewIcon = itemView.findViewById(R.id.image_view_icon);
                textViewRichLinkTitle = itemView.findViewById(R.id.text_view_rich_link_title);
                textViewDistance = itemView.findViewById(R.id.text_view_distance);
            }
        }
    }

    private final class Item {

        final VirtualObject object;

        float distance;

        RichLink richLink;

        Item(@NonNull VirtualObject object) {
            this.object = object;
        }

        void updateDistance(@NonNull LatLng location) {
            Location.distanceBetween(location.latitude, location.longitude,
                                     object.getRequiredGeoPoint().getLatitude(),
                                     object.getRequiredGeoPoint().getLongitude(),
                                     TEMP_DISTANCE_RESULTS);
            distance = TEMP_DISTANCE_RESULTS[0];
        }

        void loadRichLink() throws IOException {
            String uriString = object.getRequiredUriString();
            richLink = richLinkLoader.load(uriString);
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
