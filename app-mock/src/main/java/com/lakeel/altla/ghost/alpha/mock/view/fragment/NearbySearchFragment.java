package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.maps.urls.SearchUrlBuilder;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.view.activity.OthersActivity;
import com.lakeel.altla.ghost.alpha.mock.view.filter.Filter;
import com.lakeel.altla.ghost.alpha.mock.view.filter.Filterable;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
import com.lakeel.altla.ghost.alpha.mock.view.itemspace.ItemSpaceDecoration;
import com.lakeel.altla.ghost.alpha.mock.view.itemspace.ItemSpaceDecoration.Orientation;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;

public final class NearbySearchFragment extends Fragment {

    @BindView(R.id.layoutVirtualObject)
    View layoutVirtualObject;

    @BindView(R.id.circularProgressBar)
    CircularProgressBar circularProgressBar;

    @BindView(R.id.recyclerViewVirtualObject)
    RecyclerView recyclerViewVirtualObject;

    @BindView(R.id.fabSearch)
    FloatingActionButton fabSearch;

    private static final Log LOG = LogFactory.getLog(NearbySearchFragment.class);

    private VirtualObjectAdapter virtualObjectAdapter;

    private boolean filterClicked;

    private boolean altlaChipSelected;

    private boolean googleChipSelected;

    @NonNull
    public static NearbySearchFragment newInstance() {
        return new NearbySearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_search, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }

        fabSearch.setOnClickListener(v -> {
            fabSearch.setClickable(false);
            refreshItems();
        });

        // VirtualObjects
        recyclerViewVirtualObject.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVirtualObject.addItemDecoration(new ItemSpaceDecoration(24, Orientation.VERTICAL));

        virtualObjectAdapter = new VirtualObjectAdapter();
        AlphaAnimatorAdapter<VirtualObjectAdapter.ViewHolder> animatorAdapter = new AlphaAnimatorAdapter<>(virtualObjectAdapter, recyclerViewVirtualObject);
        recyclerViewVirtualObject.setAdapter(animatorAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nearby_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_other:
                Intent intent = new Intent(getContext(), OthersActivity.class);
                startActivity(intent);
                break;
            case R.id.action_filter:
                // TODO:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshItems() {
        layoutVirtualObject.setVisibility(View.GONE);

        virtualObjectAdapter.clearAll();

        circularProgressBar.setVisibility(View.VISIBLE);
        ((CircularProgressDrawable) circularProgressBar.getIndeterminateDrawable()).start();

        DeferredManager dm = new AndroidDeferredManager();
        dm.when(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }).done(Void -> {
            hideProgressBar();

            // Virtual objects data.
            List<Item> items = getVirtualObjectItems();
            if (0 < items.size()) layoutVirtualObject.setVisibility(View.VISIBLE);
            virtualObjectAdapter.setItems(items);
            virtualObjectAdapter.sort();
            virtualObjectAdapter.notifyDataSetChanged();
        }).always((state, resolved, rejected) -> {
            hideProgressBar();
            fabSearch.setClickable(true);
        });
    }

    private void filterVirtualObjects() {
        List<Item.Type> types = new ArrayList<>(2);

        if (altlaChipSelected) types.add(Item.Type.ALTLA);
        if (googleChipSelected) types.add(Item.Type.GOOGLE);

        Filter<Item, List<Item.Type>> filter = virtualObjectAdapter.getFilter();
        filter.setCondition(types);
        filter.execute();
    }

    private void hideProgressBar() {
        circularProgressBar.setVisibility(View.INVISIBLE);
        circularProgressBar.progressiveStop();
    }

    final class VirtualObjectAdapter extends RecyclerView.Adapter<VirtualObjectAdapter.ViewHolder> implements Filterable<Item, List<Item.Type>> {

        class ItemFilter extends Filter<Item, List<Item.Type>> {

            ItemFilter(@NonNull List<Item> originalItems) {
                super(originalItems);
            }

            @Override
            protected List<Item> execute(@NonNull List<Item> items, @NonNull List<Item.Type> types) {
                if (types.isEmpty()) return Collections.unmodifiableList(items);

                List<Item> filteredItems = new ArrayList<>();

                for (Item item : items) {
                    for (Item.Type type : types) {
                        if (item.type == type) {
                            filteredItems.add(item);
                        }
                    }
                }

                return filteredItems;
            }

            @Override
            protected void publishResults(@NonNull List<Item> results) {
                items.clear();
                items.addAll(results);
                notifyDataSetChanged();
            }
        }

        private final List<Item> items = new ArrayList<>();

        private ItemFilter filter;

        void setItems(@NonNull List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
            filter = new ItemFilter(items);
        }

        void clearAll() {
            int size = items.size();
            items.clear();
            notifyItemRangeRemoved(0, size);
        }

        void sort() {
            Collections.sort(items, DistanceComparator.INSTANCE);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewTypeValue) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_virtual_object, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.showItem(items.get(position));
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            // ViewHolder uses previous instance, initialize it here.

            Picasso.with(getContext()).cancelRequest(holder.imageViewPhoto);
            Picasso.with(getContext()).cancelRequest(holder.imageViewLinkThumbnail);

            holder.imageViewPhoto.setImageDrawable(null);
            holder.imageViewLinkThumbnail.setImageDrawable(null);
            holder.textViewLinkTitle.setText(null);
            holder.textViewNoImage.setVisibility(View.INVISIBLE);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public Filter<Item, List<Item.Type>> getFilter() {
            return filter;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.layoutItem)
            View layoutItem;

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

            @BindView(R.id.textViewDistance)
            TextView textViewDistance;

            @BindView(R.id.textViewObjectManager)
            TextView textViewObjectManager;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void showItem(@NonNull Item item) {
                if (item.type == Item.Type.ALTLA) {
                    indicatorView.setVisibility(View.VISIBLE);
                    indicatorView.show();

                    // Fetch rich link.
                    DeferredManager dm = new AndroidDeferredManager();
                    RichLinkLoader loader = new RichLinkLoader.Builder().build();
                    dm.when(() -> loader.load(item.linkUri))
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

                                textViewNoImage.setVisibility(View.VISIBLE);
                                textViewLinkTitle.setText(item.linkUri);
                            })
                            .always((state, resolved, rejected) -> indicatorView.smoothToHide());
                } else if (item.type == Item.Type.GOOGLE) {
                    Item.GoogleObject googleObject = item.googleObject;
                    Objects.requireNonNull(googleObject);

                    textViewLinkTitle.setText(googleObject.placeName);
                    textViewObjectManager.setText(item.type.getValue());

                    // photo
                    if (googleObject.photo != null) {
                        imageViewPhoto.setImageBitmap(googleObject.photo);
                    } else {
                        InitialDrawableBuilder builder = new InitialDrawableBuilder(googleObject.placeName);
                        imageViewPhoto.setImageDrawable(builder.build());
                    }

                    // icon
                    TextDrawableImageLoader imageLoader = new TextDrawableImageLoader(imageViewLinkThumbnail, googleObject.iconUri);
                    imageLoader.loadImage();
                } else {
                    LOG.w("Unknown object type:" + item.type);
                }

                textViewDistance.setText(getString(R.string.textView_format_distance, item.distance));
                textViewObjectManager.setText(item.type.getValue());

                layoutItem.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.linkUri));
                    startActivity(intent);
                });
            }
        }
    }

    private static final class Item {

        enum Type {
            ALTLA("altla"), GOOGLE("google.com");

            private final String value;

            Type(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }

        static class GoogleObject {

            @NonNull
            final String placeName;

            @NonNull
            final String iconUri;

            Bitmap photo;

            private GoogleObject(@NonNull String placeName, @NonNull String iconUri) {
                this.placeName = placeName;
                this.iconUri = iconUri;
            }
        }

        @NonNull
        final String linkUri;

        @NonNull
        final Item.Type type;

        final long distance;

        @Nullable
        GoogleObject googleObject;

        Item(@NonNull String linkUri, @NonNull Item.Type type, long distance) {
            this.linkUri = linkUri;
            this.type = type;
            this.distance = distance;
        }
    }

    private static class DistanceComparator implements Comparator<Item> {

        private static final DistanceComparator INSTANCE = new DistanceComparator();

        @Override
        public int compare(Item o1, Item o2) {
            return Long.compare(o1.distance, o2.distance);
        }
    }

    // Mock data
    @NonNull
    private List<Item> getVirtualObjectItems() {
        List<Item> items = new ArrayList<>();

        {
            // APA Hotel
            SearchUrlBuilder builder = new SearchUrlBuilder(35.6664178, 139.737642);
            builder.setPlaceId("ChIJBdgnZ4OLGGARwMiNPCqXt3Y");
            String linkUri = builder.build();

            Item item = new Item(linkUri, Item.Type.GOOGLE, 50);

            Item.GoogleObject googleObject = new Item.GoogleObject("アパホテル <六本木一丁目駅前> ", "https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png");
            googleObject.photo = BitmapFactory.decodeResource(getResources(), R.drawable.apa_hotel);

            item.googleObject = googleObject;

            items.add(item);
        }

        {
            // ステーキてっぺい×六本木バフ
            SearchUrlBuilder builder = new SearchUrlBuilder(35.6666111, 139.7379585);
            builder.setPlaceId("ChIJb-6UvYSLGGAR3TqGoJjRz4M");
            String linkUri = builder.build();

            Item item = new Item(linkUri, Item.Type.GOOGLE, 10);

            Item.GoogleObject googleObject = new Item.GoogleObject("ステーキてっぺい×六本木バフ", "https://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png");
            googleObject.photo = BitmapFactory.decodeResource(getResources(), R.drawable.steak_teppei);

            item.googleObject = googleObject;

            items.add(item);
        }

        {
            // インビスハライコ
            Item item = new Item("https://tabelog.com/tokyo/A1307/A130701/13110227/", Item.Type.ALTLA, 70);
            items.add(item);
        }

        {
            // Instagram
            Item item = new Item("https://www.instagram.com/p/BTtgNJMgYpY/?hl=ja&taken-by=sunada.chan", Item.Type.ALTLA, 20);
            items.add(item);
        }

        {
            // まぐろたけボーノ 白川
            SearchUrlBuilder builder = new SearchUrlBuilder(35.6669041, 139.7384696);
            builder.setPlaceId("ChIJoftg6oSLGGAR6XfGSaY4Ads");
            String linkUri = builder.build();

            Item item = new Item(linkUri, Item.Type.GOOGLE, 40);

            Item.GoogleObject googleObject = new Item.GoogleObject("まぐろだけボーノ 白川", "https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png");
            googleObject.photo = BitmapFactory.decodeResource(getResources(), R.drawable.maguro_takebono);

            item.googleObject = googleObject;

            items.add(item);
        }

        return items;
    }
}