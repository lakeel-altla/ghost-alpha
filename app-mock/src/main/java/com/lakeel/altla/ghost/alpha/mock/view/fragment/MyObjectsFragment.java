package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.FragmentHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.animation.helper.RevealAnimationSettings;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
import com.lakeel.altla.ghost.alpha.mock.view.itemspace.ItemSpaceDecoration;
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
import butterknife.OnClick;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;

public final class MyObjectsFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(MyObjectsFragment.class);

    @BindView(R.id.recyclerViewVirtualObject)
    RecyclerView recyclerViewVirtualObject;

    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;

    public static MyObjectsFragment newInstance() {
        return new MyObjectsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_objects, container, false);

        setHasOptionsMenu(true);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        recyclerViewVirtualObject.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVirtualObject.setHasFixedSize(true);
        recyclerViewVirtualObject.addItemDecoration(new ItemSpaceDecoration(24, ItemSpaceDecoration.Orientation.VERTICAL));

        Adapter adapter = new Adapter();
        AlphaAnimatorAdapter<Adapter.ViewHolder> animatorAdapter = new AlphaAnimatorAdapter<>(adapter, recyclerViewVirtualObject);
        recyclerViewVirtualObject.setAdapter(animatorAdapter);

        adapter.setItems(getVirtualObjectItems());
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.title_my_objects));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fabAdd)
    public void onClickAdd() {
        int centerX = (int) (fabAdd.getX() + fabAdd.getWidth() / 2);
        int centerY = (int) (fabAdd.getY() + fabAdd.getHeight() / 2);

        View view = Objects.requireNonNull(getView());
        int width = view.getWidth();
        int height = view.getHeight();

        FragmentHelper.showFragment(getFragmentManager(), AddObjectFragment.newInstance(new RevealAnimationSettings(centerX, centerY, width, height)));
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<Item> items = new ArrayList<>();

        void setItems(@NonNull List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_object, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.showItem(items.get(position));
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

        void sort() {
            Collections.sort(items, DateComparator.INSTANCE);
        }

        final class ViewHolder extends RecyclerView.ViewHolder {

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

            ViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
            }

            void showItem(@NonNull Item item) {
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

                textViewObjectManager.setText(getString(R.string.textView_altla));

                layoutItem.setOnClickListener(v -> {
                    MyObjectFragment target = MyObjectFragment.newInstance(getContext(), item.objectId);

                    ArrayMap<View, String> sharedElements = new ArrayMap<>();
                    sharedElements.put(imageViewPhoto, getString(R.string.transition_imageView));
                    sharedElements.put(textViewLinkTitle, getString(R.string.transition_textView));

                    FragmentHelper.showFragmentWithAnimation(getFragmentManager(), target, sharedElements);
                });
            }
        }
    }

    private static final class Item {

        @NonNull
        final String objectId;

        @NonNull
        final String linkUri;

        private final long createdAt;

        Item(@NonNull String objectId, @NonNull String linkUri, long createdAt) {
            this.objectId = objectId;
            this.linkUri = linkUri;
            this.createdAt = createdAt;
        }
    }

    private static class DateComparator implements Comparator<Item> {

        private static final DateComparator INSTANCE = new DateComparator();

        @Override
        public int compare(Item o1, Item o2) {
            return Long.compare(o1.createdAt, o2.createdAt);
        }
    }

    // Mock data
    @NonNull
    private List<Item> getVirtualObjectItems() {
        List<Item> items = new ArrayList<>();

        {
            // Instagram
            Item item = new Item("objectA", "https://www.instagram.com/p/BTtgNJMgYpY/?hl=ja&taken-by=sunada.chan", System.currentTimeMillis());
            items.add(item);
        }

        {
            // インビスハライコ
            Item item = new Item("objectB", "https://tabelog.com/tokyo/A1307/A130701/13110227/", System.currentTimeMillis());
            items.add(item);
        }

        return items;
    }
}
