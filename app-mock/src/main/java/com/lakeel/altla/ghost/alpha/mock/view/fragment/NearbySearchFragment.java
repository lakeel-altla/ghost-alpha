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
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.maps.urls.SearchUrlBuilder;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.FragmentHelper;
import com.lakeel.altla.ghost.alpha.mock.view.filter.Filter;
import com.lakeel.altla.ghost.alpha.mock.view.filter.Filterable;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
import com.lakeel.altla.ghost.alpha.mock.view.itemspace.ItemSpaceDecoration;
import com.lakeel.altla.ghost.alpha.mock.view.itemspace.ItemSpaceDecoration.Orientation;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.robertlevonyan.views.chip.Chip;
import com.wang.avi.AVLoadingIndicatorView;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;

public final class NearbySearchFragment extends Fragment {

    @BindView(R.id.layoutArea)
    View layoutArea;

    @BindView(R.id.layoutVirtualObject)
    View layoutVirtualObject;

    @BindView(R.id.circularProgressBar)
    CircularProgressBar circularProgressBar;

    @BindView(R.id.recyclerViewArea)
    RecyclerView recyclerViewArea;

    @BindView(R.id.recyclerViewVirtualObject)
    RecyclerView recyclerViewVirtualObject;

    @BindView(R.id.imageViewFilter)
    ImageView imageViewFilter;

    @BindView(R.id.expandableLayoutChips)
    ExpandableLayout expandableLayoutChips;

    @BindView(R.id.chipAltla)
    Chip chipAltla;

    @BindView(R.id.chipGoogle)
    Chip chipGoogle;

    @BindView(R.id.fabRefresh)
    FloatingActionButton fabRefresh;

    private static final Log LOG = LogFactory.getLog(NearbySearchFragment.class);

    private AreaAdapter areaAdapter;

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

        // filter
        imageViewFilter.setOnClickListener(v -> {
            filterClicked = !filterClicked;
            if (filterClicked) {
                expandableLayoutChips.expand();
            } else {
                expandableLayoutChips.collapse();
            }
        });

        // chips
        chipAltla.setOnSelectClickListener((v, selected) -> {
            altlaChipSelected = selected;
            filterVirtualObjects();
        });
        chipGoogle.setOnSelectClickListener((v, selected) -> {
            googleChipSelected = selected;
            filterVirtualObjects();
        });

        // Areas
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerViewArea.setLayoutManager(manager);
        recyclerViewArea.setHasFixedSize(true);
        recyclerViewArea.addItemDecoration(ItemSpaceDecoration.createDefaultDecoration(getContext(), Orientation.HORIZONTAL));

        areaAdapter = new AreaAdapter();
        // Animation adapter.
        AlphaAnimatorAdapter<AreaAdapter.ItemViewHolder> AreaAnimatorAdapter = new AlphaAnimatorAdapter<>(areaAdapter, recyclerViewArea);
        recyclerViewArea.setAdapter(AreaAnimatorAdapter);

        // VirtualObjects
        recyclerViewVirtualObject.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVirtualObject.setHasFixedSize(true);
        recyclerViewVirtualObject.addItemDecoration(new ItemSpaceDecoration(24, Orientation.VERTICAL));

        virtualObjectAdapter = new VirtualObjectAdapter();
        AlphaAnimatorAdapter<VirtualObjectAdapter.ItemViewHolder> animatorAdapter = new AlphaAnimatorAdapter<>(virtualObjectAdapter, recyclerViewVirtualObject);
        recyclerViewVirtualObject.setAdapter(animatorAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshItems();
    }

    @OnClick(R.id.fabRefresh)
    public void onClickRefresh() {
        fabRefresh.setClickable(false);

        refreshItems();
    }

    private void refreshItems() {
        layoutArea.setVisibility(View.GONE);
        layoutVirtualObject.setVisibility(View.GONE);

        areaAdapter.clearAll();
        virtualObjectAdapter.clearAll();

        ((CircularProgressDrawable) circularProgressBar.getIndeterminateDrawable()).start();

        DeferredManager dm = new AndroidDeferredManager();
        dm.when(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }).done(Void -> {
            circularProgressBar.progressiveStop();

            // Areas data.
            List<AreaItem> AreaItems = getAreas();
            if (0 < AreaItems.size()) layoutArea.setVisibility(View.VISIBLE);
            areaAdapter.setItems(AreaItems);
            areaAdapter.notifyDataSetChanged();

            // Virtual objects data.
            List<VirtualObjectItem> virtualObjectItems = getVirtualObjectItems();
            if (0 < virtualObjectItems.size()) layoutVirtualObject.setVisibility(View.VISIBLE);
            virtualObjectAdapter.setItems(virtualObjectItems);
            virtualObjectAdapter.sort();
            virtualObjectAdapter.notifyDataSetChanged();
        }).always((state, resolved, rejected) -> {
            circularProgressBar.progressiveStop();
            fabRefresh.setClickable(true);
        });
    }

    private void filterVirtualObjects() {
        List<VirtualObjectItem.Type> types = new ArrayList<>(2);

        if (altlaChipSelected) types.add(VirtualObjectItem.Type.ALTLA);
        if (googleChipSelected) types.add(VirtualObjectItem.Type.GOOGLE);

        if (types.isEmpty()) {
            imageViewFilter.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_filter));
        } else {
            imageViewFilter.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_enabled));
        }

        Filter<VirtualObjectItem, List<VirtualObjectItem.Type>> filter = virtualObjectAdapter.getFilter();
        filter.setCondition(types);
        filter.execute();
    }

    final class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.ItemViewHolder> {

        private final List<AreaItem> items = new ArrayList<>();

        void setItems(@NonNull List<AreaItem> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        void clearAll() {
            int size = items.size();
            items.clear();
            notifyItemRangeRemoved(0, size);
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_area, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder viewHolder, int position) {
            viewHolder.showItem(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.layoutItem)
            View layoutItem;

            @BindView(R.id.imageViewArea)
            ImageView imageViewArea;

            @BindView(R.id.textViewAreaName)
            TextView textViewAreaName;

            ItemViewHolder(@NonNull View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
            }

            void showItem(@NonNull AreaItem item) {
                textViewAreaName.setText(item.name);

                if (item.photo == null) {
                    InitialDrawableBuilder builder = new InitialDrawableBuilder(item.name);
                    imageViewArea.setImageDrawable(builder.build());
                } else {
                    imageViewArea.setImageBitmap(item.photo);
                }

                layoutItem.setOnClickListener(v -> {
                    AreaServicesFragment target = AreaServicesFragment.newInstance(item.AreaId, getContext());

                    ArrayMap<View, String> sharedElements = new ArrayMap<>();
                    sharedElements.put(imageViewArea, getString(R.string.transition_imageView));
                    sharedElements.put(textViewAreaName, getString(R.string.transition_textView));

                    FragmentHelper.showFragmentWithAnimation(getFragmentManager(), target, sharedElements);
                });
            }
        }
    }

    final class VirtualObjectAdapter extends RecyclerView.Adapter<VirtualObjectAdapter.ItemViewHolder> implements Filterable<VirtualObjectItem, List<VirtualObjectItem.Type>> {

        class ItemFilter extends Filter<VirtualObjectItem, List<VirtualObjectItem.Type>> {

            ItemFilter(@NonNull List<VirtualObjectItem> originalItems) {
                super(originalItems);
            }

            @Override
            protected List<VirtualObjectItem> execute(@NonNull List<VirtualObjectItem> items, @NonNull List<VirtualObjectItem.Type> types) {
                if (types.isEmpty()) return Collections.unmodifiableList(items);

                // TODO: Rx
                List<VirtualObjectItem> results = new ArrayList<>();
                for (VirtualObjectItem item : items) {
                    for (VirtualObjectItem.Type type : types) {
                        if (item.type == type) {
                            results.add(item);
                        }
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(@NonNull List<VirtualObjectItem> results) {
                items.clear();
                items.addAll(results);
                notifyDataSetChanged();
            }
        }

        private final List<VirtualObjectItem> items = new ArrayList<>();

        private ItemFilter filter;

        void setItems(@NonNull List<VirtualObjectItem> items) {
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
        public VirtualObjectAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewTypeValue) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_virtual_object, parent, false));
        }

        @Override
        public void onBindViewHolder(VirtualObjectAdapter.ItemViewHolder viewHolder, int position) {
            viewHolder.showItem(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public Filter<VirtualObjectItem, List<VirtualObjectItem.Type>> getFilter() {
            return filter;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

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

            @BindView(R.id.textViewObjectManager)
            TextView textViewObjectManager;

            ItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void showItem(@NonNull VirtualObjectItem item) {
                // These values are included in the OGP data.
                // ViewHolder uses previous instance, initialize it here.
                textViewLinkTitle.setText(null);
                imageViewLinkThumbnail.setImageDrawable(null);
                textViewNoImage.setVisibility(View.INVISIBLE);

                textViewObjectManager.setText(item.type.getValue());
                layoutItem.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.linkUri));
                    startActivity(intent);
                });

                if (item.type == VirtualObjectItem.Type.ALTLA) {
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
                } else if (item.type == VirtualObjectItem.Type.GOOGLE) {
                    VirtualObjectItem.GoogleObjectItem googleObjectItem = item.googleObjectItem;
                    if (googleObjectItem == null) {
                        throw new NullPointerException("'googleObject' is null.");
                    }

                    textViewLinkTitle.setText(googleObjectItem.placeName);
                    textViewObjectManager.setText(item.type.getValue());

                    // photo
                    if (googleObjectItem.photo != null) {
                        imageViewPhoto.setImageBitmap(googleObjectItem.photo);
                    } else {
                        InitialDrawableBuilder builder = new InitialDrawableBuilder(googleObjectItem.placeName);
                        imageViewPhoto.setImageDrawable(builder.build());
                    }

                    // icon
                    TextDrawableImageLoader imageLoader = new TextDrawableImageLoader(imageViewLinkThumbnail, googleObjectItem.iconUri);
                    imageLoader.loadImage();
                } else {
                    LOG.w("Unknown object type:" + item.type);
                }
            }
        }
    }

    private static final class AreaItem {

        @NonNull
        String AreaId;

        @NonNull
        String name;

        Bitmap photo;

        AreaItem(@NonNull String AreaId, @NonNull String name) {
            this.AreaId = AreaId;
            this.name = name;
        }
    }

    private static final class VirtualObjectItem {

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

        static class GoogleObjectItem {

            @NonNull
            final String placeName;

            @NonNull
            final String iconUri;

            Bitmap photo;

            private GoogleObjectItem(@NonNull String placeName, @NonNull String iconUri) {
                this.placeName = placeName;
                this.iconUri = iconUri;
            }
        }

        @NonNull
        final String linkUri;

        @NonNull
        final VirtualObjectItem.Type type;

        final long distance;

        @Nullable
        GoogleObjectItem googleObjectItem;

        VirtualObjectItem(@NonNull String linkUri, @NonNull VirtualObjectItem.Type type, long distance) {
            this.linkUri = linkUri;
            this.type = type;
            this.distance = distance;
        }
    }

    private static class DistanceComparator implements Comparator<VirtualObjectItem> {

        private static final DistanceComparator INSTANCE = new DistanceComparator();

        @Override
        public int compare(VirtualObjectItem o1, VirtualObjectItem o2) {
            return Long.compare(o1.distance, o2.distance);
        }
    }

    // Mock data
    @NonNull
    private List<AreaItem> getAreas() {
        List<AreaItem> Areas = new ArrayList<>();

        AreaItem item1 = new AreaItem("bbb", "港区市役所");
        item1.photo = BitmapFactory.decodeResource(getResources(), R.drawable.minatoku);

        AreaItem item2 = new AreaItem("aaa", "六本木一丁目駅");
        item2.photo = BitmapFactory.decodeResource(getResources(), R.drawable.tokyo_metro);

        AreaItem item3 = new AreaItem("ccc", "溜池山王駅");
        item3.photo = BitmapFactory.decodeResource(getResources(), R.drawable.tokyo_metro);

        Areas.add(item1);
        Areas.add(item2);
        Areas.add(item3);

        return Areas;
    }

    // Mock data
    @NonNull
    private List<VirtualObjectItem> getVirtualObjectItems() {
        List<VirtualObjectItem> items = new ArrayList<>();

        {
            // APA Hotel
            SearchUrlBuilder builder = new SearchUrlBuilder(35.6664178, 139.737642);
            builder.setPlaceId("ChIJBdgnZ4OLGGARwMiNPCqXt3Y");
            String linkUri = builder.build();

            VirtualObjectItem item = new VirtualObjectItem(linkUri, VirtualObjectItem.Type.GOOGLE, 50);

            VirtualObjectItem.GoogleObjectItem googleObjectItem = new VirtualObjectItem.GoogleObjectItem("アパホテル <六本木一丁目駅前> ", "https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png");
            googleObjectItem.photo = BitmapFactory.decodeResource(getResources(), R.drawable.apa_hotel);

            item.googleObjectItem = googleObjectItem;

            items.add(item);
        }

        {
            // ステーキてっぺい×六本木バフ
            SearchUrlBuilder builder = new SearchUrlBuilder(35.6666111, 139.7379585);
            builder.setPlaceId("ChIJb-6UvYSLGGAR3TqGoJjRz4M");
            String linkUri = builder.build();

            VirtualObjectItem item = new VirtualObjectItem(linkUri, VirtualObjectItem.Type.GOOGLE, 10);

            VirtualObjectItem.GoogleObjectItem googleObjectItem = new VirtualObjectItem.GoogleObjectItem("ステーキてっぺい×六本木バフ", "https://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png");
            googleObjectItem.photo = BitmapFactory.decodeResource(getResources(), R.drawable.steak_teppei);

            item.googleObjectItem = googleObjectItem;

            items.add(item);
        }

        {
            // インビスハライコ
            VirtualObjectItem item = new VirtualObjectItem("https://tabelog.com/tokyo/A1307/A130701/13110227/", VirtualObjectItem.Type.ALTLA, 70);
            items.add(item);
        }

        {
            // Instagram
            VirtualObjectItem item = new VirtualObjectItem("https://www.instagram.com/p/BTtgNJMgYpY/?hl=ja&taken-by=sunada.chan", VirtualObjectItem.Type.ALTLA, 20);
            items.add(item);
        }

        {
            // まぐろたけボーノ 白川
            SearchUrlBuilder builder = new SearchUrlBuilder(35.6669041, 139.7384696);
            builder.setPlaceId("ChIJoftg6oSLGGAR6XfGSaY4Ads");
            String linkUri = builder.build();

            VirtualObjectItem item = new VirtualObjectItem(linkUri, VirtualObjectItem.Type.GOOGLE, 40);

            VirtualObjectItem.GoogleObjectItem googleObjectItem = new VirtualObjectItem.GoogleObjectItem("まぐろだけボーノ 白川", "https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png");
            googleObjectItem.photo = BitmapFactory.decodeResource(getResources(), R.drawable.maguro_takebono);

            item.googleObjectItem = googleObjectItem;

            items.add(item);
        }

        return items;
    }
}