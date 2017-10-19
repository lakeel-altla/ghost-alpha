package com.lakeel.altla.ghost.alpha.areaservice.view.fragment;

import com.google.android.gms.maps.model.LatLng;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.areaservice.R;
import com.lakeel.altla.ghost.alpha.areaservice.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.areaservice.helper.Preferences;
import com.lakeel.altla.ghost.alpha.areaservice.helper.RichLinkImageLoader;
import com.lakeel.altla.ghost.alpha.richlink.RichLink;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
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

import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.findViewById;

public final class NearbyAreaListFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(NearbyAreaListFragment.class);

    private static final float[] TEMP_DISTANCE_RESULTS = new float[1];

    @Inject
    RichLinkLoader richLinkLoader;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private FragmentContext fragmentContext;

    private Preferences preferences;

    private final List<Item> items = new ArrayList<>();

    private RecyclerView recyclerView;

    public static NearbyAreaListFragment newInstance() {
        return new NearbyAreaListFragment();
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
        return inflater.inflate(R.layout.fragment_nearby_area_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        preferences = new Preferences(this);

        recyclerView = findViewById(this, R.id.recycler_view);
        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nearby_object_list, menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().setTitle(R.string.title_nearby_area_list);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_object:
                // TODO
                return true;
            case R.id.action_settings:
                // TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface FragmentContext {

        void checkLocationSettings();

        void startLocationUpdates();

        void stopLocationUpdates();

        Location getLastLocation();
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
//                Uri uri = Uri.load(uriString);
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
//            richLink = richLinkParser.load(uriString);
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
