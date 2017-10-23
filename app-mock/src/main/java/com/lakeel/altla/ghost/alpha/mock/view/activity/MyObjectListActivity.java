package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.squareup.picasso.Picasso;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;

public class MyObjectListActivity extends AppCompatActivity {

    @BindView(R.id.container)
    View container;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recyclerViewVirtualObject)
    RecyclerView recyclerViewVirtualObject;

    private static final String BUNDLE_REMOVED_OBJECT_ID = "removedObjectId";

    private static final int REQUEST_REMOVE_OBJECT = 1;

    private static final int REQUEST_SAVE_OBJECT = 2;

    public static Intent newIntent(@NonNull String objectId) {
        Intent intent = new Intent();
        intent.putExtra(BUNDLE_REMOVED_OBJECT_ID, objectId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_object_list);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_clear);
        toolbar.setNavigationOnClickListener(v -> finish());

        setTitle(R.string.title_my_objects);

        recyclerViewVirtualObject.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewVirtualObject.setHasFixedSize(true);

        Adapter adapter = new Adapter();
        AlphaAnimatorAdapter<Adapter.ViewHolder> animatorAdapter = new AlphaAnimatorAdapter<>(adapter, recyclerViewVirtualObject);
        recyclerViewVirtualObject.setAdapter(animatorAdapter);

        adapter.setItems(getVirtualObjectItems());
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_objects, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(getApplicationContext(), EditObjectActivity.class);
                startActivityForResult(intent, REQUEST_SAVE_OBJECT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REMOVE_OBJECT) {
            if (resultCode == RESULT_OK) {
                String removedObjectId = data.getStringExtra(BUNDLE_REMOVED_OBJECT_ID);
                Snackbar
                        .make(container, R.string.snackbar_removed, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.snackbar_action_undo, v -> {
                            // undo
                        }).show();
            }
        } else if (requestCode == REQUEST_SAVE_OBJECT) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(container, R.string.snackbar_saved, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<Item> items = new ArrayList<>();

        void setItems(@NonNull List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_object, parent, false);
            return new Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            holder.showItem(items.get(position));
        }

        @Override
        public void onViewRecycled(Adapter.ViewHolder holder) {
            // ViewHolder uses previous instance, initialize it here.
            Picasso.with(getApplicationContext()).cancelRequest(holder.imageViewLinkThumbnail);

            holder.imageViewLinkThumbnail.setImageDrawable(null);
            holder.textViewLinkTitle.setText(null);
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

            @BindView(R.id.imageViewLinkThumbnail)
            ImageView imageViewLinkThumbnail;

            @BindView(R.id.textViewLinkTitle)
            TextView textViewLinkTitle;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void showItem(@NonNull Item item) {
                DeferredManager dm = new AndroidDeferredManager();
                dm.when(() -> new RichLinkLoader.Builder().build().load(item.linkUri))
                        .done(richLink -> {
                            textViewLinkTitle.setText(richLink.getTitle());

                            if (richLink.getTitle() != null) {
                                Drawable drawable = new InitialDrawableBuilder(richLink.getTitle()).build();
                                imageViewLinkThumbnail.setImageDrawable(drawable);
                            }
                        })
                        .fail(result -> textViewLinkTitle.setText(item.linkUri));

                layoutItem.setOnClickListener(v -> {
                    Intent intent = MyObjectActivity.newIntent(getApplicationContext(), item.objectId);
                    startActivityForResult(intent, REQUEST_REMOVE_OBJECT);
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