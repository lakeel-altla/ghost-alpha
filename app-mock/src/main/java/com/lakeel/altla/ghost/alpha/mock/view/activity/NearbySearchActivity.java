package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.maps.urls.SearchUrlBuilder;
import com.lakeel.altla.ghost.alpha.lib.android.view.decoration.ItemSpaceDecoration;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
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

public final class NearbySearchActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.layoutEmpty)
    View layoutEmpty;

    @BindView(R.id.circularProgressBar)
    CircularProgressBar circularProgressBar;

    @BindView(R.id.textViewEmptyBody)
    TextView textViewEmptyBody;

    @BindView(R.id.recyclerViewVirtualObject)
    RecyclerView recyclerViewVirtualObject;

    @BindView(R.id.fabSearch)
    FloatingActionButton fabSearch;

    private static final Log LOG = LogFactory.getLog(NearbySearchActivity.class);

    private VirtualObjectAdapter virtualObjectAdapter;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_search);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setTitle(R.string.title_nearby_objects);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.textView_empty_body_nearby_objects));
        builder.setSpan(new ImageSpan(getApplicationContext(), R.drawable.ic_search_for_empty),
                10, 11, 0);
        textViewEmptyBody.setText(builder);

        fabSearch.setOnClickListener(v -> {
            layoutEmpty.setVisibility(View.INVISIBLE);
            fabSearch.setClickable(false);
            refreshItems();
        });

        recyclerViewVirtualObject.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewVirtualObject.addItemDecoration(new ItemSpaceDecoration(24, ItemSpaceDecoration.Orientation.VERTICAL));

        virtualObjectAdapter = new VirtualObjectAdapter();
        AlphaAnimatorAdapter<VirtualObjectAdapter.ViewHolder> animatorAdapter = new AlphaAnimatorAdapter<>(virtualObjectAdapter, recyclerViewVirtualObject);
        recyclerViewVirtualObject.setAdapter(animatorAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nearby_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_other:
                Intent intent = new Intent(getApplicationContext(), OptionsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_filter:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshItems() {
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
            virtualObjectAdapter.setItems(items);
            virtualObjectAdapter.sort();
            virtualObjectAdapter.notifyDataSetChanged();
        }).always((state, resolved, rejected) -> {
            hideProgressBar();
            fabSearch.setClickable(true);
        });
    }

    private void hideProgressBar() {
        circularProgressBar.setVisibility(View.INVISIBLE);
        circularProgressBar.progressiveStop();
    }

    final class VirtualObjectAdapter extends RecyclerView.Adapter<VirtualObjectAdapter.ViewHolder> {

        private final List<Item> items = new ArrayList<>();

        void setItems(@NonNull List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
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
        public VirtualObjectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTypeValue) {
            return new VirtualObjectAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_virtual_object, parent, false));
        }

        @Override
        public void onBindViewHolder(VirtualObjectAdapter.ViewHolder viewHolder, int position) {
            viewHolder.showItem(items.get(position));
        }

        @Override
        public void onViewRecycled(VirtualObjectAdapter.ViewHolder holder) {
            // ViewHolder uses previous instance, initialize it here.

            Picasso.with(getApplicationContext()).cancelRequest(holder.imageViewPhoto);
            Picasso.with(getApplicationContext()).cancelRequest(holder.imageViewLinkThumbnail);

            holder.imageViewPhoto.setImageDrawable(null);
            holder.imageViewLinkThumbnail.setImageDrawable(null);
            holder.textViewLinkTitle.setText(null);
            holder.textViewNoImage.setVisibility(View.INVISIBLE);
        }

        @Override
        public int getItemCount() {
            return items.size();
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

                                if (richLink.getTitle() != null) {
                                    textViewLinkTitle.setText(richLink.getTitle());
                                    imageViewLinkThumbnail.setImageDrawable(new InitialDrawableBuilder(richLink.getTitle()).build());
                                }

                                TextDrawableImageLoader photoImageLoader = new TextDrawableImageLoader(imageViewPhoto, richLink.getUri(), richLink.getTitle());
                                photoImageLoader.loadImage();
                            })
                            .fail(e -> {
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
                    Picasso.with(getApplicationContext()).load(googleObject.iconUri).into(imageViewLinkThumbnail);
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
        Item.GoogleObject googleObject;

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