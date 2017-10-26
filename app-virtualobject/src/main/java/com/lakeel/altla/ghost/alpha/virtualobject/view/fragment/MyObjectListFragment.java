package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObject;
import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObjectApi;
import com.lakeel.altla.ghost.alpha.auth.CurrentUser;
import com.lakeel.altla.ghost.alpha.richlink.RichLink;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.RichLinkImageLoader;

import android.content.Context;
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
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.lakeel.altla.ghost.alpha.rxhelper.RxHelper.disposeOnStop;
import static com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper.getRequiredSupportActionBar;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.findViewById;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.getRequiredActivity;

public class MyObjectListFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(MyObjectListFragment.class);

    @Inject
    VirtualObjectApi virtualObjectApi;

    @Inject
    RichLinkLoader richLinkLoader;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private FragmentContext fragmentContext;

    private final List<Item> items = new ArrayList<>();

    private RecyclerView recyclerView;

    @NonNull
    public static MyObjectListFragment newInstance() {
        return new MyObjectListFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_object_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = findViewById(this, R.id.recycler_view);
        recyclerView.setAdapter(new Adapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_object_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_object:
                fragmentContext.showMyObjectEditFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getRequiredActivity(this).setTitle(R.string.title_my_object_list);
        getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);
        getRequiredSupportActionBar(this).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        items.clear();
        recyclerView.getAdapter().notifyDataSetChanged();

        virtualObjectApi.findUserObjects(
                CurrentUser.getInstance().getRequiredUserId(),
                objects -> {
                    LOG.v("Found user objects: count = %d", objects.size());
                    for (int i = 0; i < objects.size(); i++) {
                        VirtualObject object = objects.get(i);

                        LOG.v("[%d] (%f, %f) %s",
                              i,
                              object.getRequiredGeoPoint().getLatitude(),
                              object.getRequiredGeoPoint().getLongitude(),
                              object.getRequiredUriString());

                        Item item = new Item(object);
                        items.add(item);
                        recyclerView.getAdapter().notifyItemInserted(i);

                        final int index = i;

                        disposeOnStop(this, Completable
                                .create(e -> {
                                    item.loadRichLink();
                                    e.onComplete();
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    recyclerView.getAdapter().notifyItemChanged(index);
                                }, e -> {
                                    LOG.e(String.format("Failed to load the rich link: uri = %s",
                                                        object.getRequiredUriString()), e);
                                })
                        );
                    }
                },
                e -> {
                    LOG.e("Failed to find user objects.", e);
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        items.clear();
    }

    public interface FragmentContext {

        void showMyObjectViewFragment(@NonNull String key);

        void showMyObjectEditFragment();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        LayoutInflater inflater;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            View itemView = inflater.inflate(R.layout.item_my_object, parent, false);
            itemView.setOnClickListener(v -> {
                int position = recyclerView.getChildAdapterPosition(v);
                Item item = items.get(position);
                fragmentContext.showMyObjectViewFragment(item.object.getKey());
            });

            return new ViewHolder(itemView);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.imageViewRichLinkImage.setImageDrawable(null);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = items.get(position);

            if (item.richLink != null) {
                richLinkImageLoader.load(item.richLink, holder.imageViewRichLinkImage);
                holder.textViewRichLinkTitle.setText(item.richLink.getTitleOrUri());
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageViewRichLinkImage;

            ImageView imageViewIcon;

            TextView textViewRichLinkTitle;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageViewRichLinkImage = itemView.findViewById(R.id.image_view_rich_link_image);
                imageViewIcon = itemView.findViewById(R.id.image_view_icon);
                textViewRichLinkTitle = itemView.findViewById(R.id.text_view_rich_link_title);
            }
        }
    }

    private final class Item {

        final VirtualObject object;

        @Nullable
        RichLink richLink;

        Item(@NonNull VirtualObject object) {
            this.object = object;
        }

        void loadRichLink() throws IOException {
            String uriString = object.getRequiredUriString();
            richLink = richLinkLoader.load(uriString);
        }
    }
}
