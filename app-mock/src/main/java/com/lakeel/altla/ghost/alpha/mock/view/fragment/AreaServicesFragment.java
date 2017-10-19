package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
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
import com.lakeel.altla.ghost.alpha.mock.view.itemspace.ItemSpaceDecoration;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;

public final class AreaServicesFragment extends Fragment {

    @BindView(R.id.imageViewArea)
    ImageView imageViewArea;

    @BindView(R.id.textViewAreaName)
    TextView textViewAreaName;

    @BindView(R.id.recyclerViewAreaServices)
    RecyclerView recyclerViewAreaServices;

    private static final Log LOG = LogFactory.getLog(AreaServicesFragment.class);

    private static final String BUNDLE_KEY_Area_ID = "AreaId";

    @NonNull
    public static AreaServicesFragment newInstance(@NonNull String AreaId, @NonNull Context context) {
        AreaServicesFragment fragment = new AreaServicesFragment();
        fragment.setSharedElementEnterTransition(TransitionInflater.from(context).inflateTransition(R.transition.move));

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_Area_ID, AreaId);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area_services, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        imageViewArea.setTransitionName(getString(R.string.transition_imageView));
        textViewAreaName.setTransitionName(getString(R.string.transition_textView));

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

        recyclerViewAreaServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAreaServices.setHasFixedSize(true);
        recyclerViewAreaServices.addItemDecoration(new ItemSpaceDecoration(24, ItemSpaceDecoration.Orientation.VERTICAL));

        String areaId = FragmentHelper.getBundleString(this, BUNDLE_KEY_Area_ID);
        Area area = new Area();

        textViewAreaName.setText(area.name);

        if (area.photo == null) {
            InitialDrawableBuilder builder = new InitialDrawableBuilder(area.name);
            imageViewArea.setImageDrawable(builder.build());
        } else {
            imageViewArea.setImageBitmap(area.photo);
        }

//        Adapter adapter = new Adapter();
//        adapter.setAreaServices(getAreaServices(areaId));
//
//        recyclerViewAreaServices.setAdapter(adapter);
//        recyclerViewAreaServices.getAdapter().notifyDataSetChanged();
    }

    private class Area {

        public String name = "めとろ";

        public Bitmap photo = BitmapFactory.decodeResource(getResources(), R.drawable.tokyo_metro);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class Adapter extends RecyclerView.Adapter<Adapter.ItemViewHolder> {

        private final List<AreaServiceItem> items = new ArrayList<>();

        void setAreaServices(@NonNull List<AreaServiceItem> AreaServiceItems) {
            items.clear();
            items.addAll(AreaServiceItems);
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_virtual_object, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.showItem(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.layoutItem)
            View layoutItem;

            @BindView(R.id.imageViewPhoto)
            ImageView imageViewPhoto;

            @BindView(R.id.textViewNoImage)
            TextView textViewNoImage;

            @BindView(R.id.imageViewLinkThumbnail)
            ImageView imageViewLinkThumbnail;

            @BindView(R.id.textViewLinkTitle)
            TextView textViewLinkTitle;

            @BindView(R.id.indicatorView)
            AVLoadingIndicatorView indicatorView;

            @BindView(R.id.textViewObjectManager)
            TextView textViewObjectManager;

            ItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void showItem(AreaServiceItem AreaServiceItem) {
                // The text value is included in the OGP data.
                // ViewHolder uses previous instance, initialize it here.
                textViewNoImage.setVisibility(View.INVISIBLE);
                textViewLinkTitle.setText(null);
                imageViewLinkThumbnail.setImageDrawable(null);

                indicatorView.setVisibility(View.VISIBLE);
                indicatorView.show();

                DeferredManager dm = new AndroidDeferredManager();
                RichLinkLoader loader = new RichLinkLoader.Builder().build();
                dm.when(() -> loader.load(AreaServiceItem.linkUri))
                        .done(richLink -> {
                            indicatorView.hide();
                            textViewLinkTitle.setText(richLink.getTitle());

                            // photo / thumbnail
                            if (richLink.getUri() == null) {
                                InitialDrawableBuilder builder = new InitialDrawableBuilder(richLink.getTitle());
                                imageViewPhoto.setImageDrawable(builder.build());

                                imageViewLinkThumbnail.setImageDrawable(builder.build());
                            } else {
                                Picasso.with(getContext())
                                        .load(richLink.getUri())
                                        .into(imageViewPhoto, new Callback.EmptyCallback() {
                                            @Override
                                            public void onError() {
                                                InitialDrawableBuilder builder = new InitialDrawableBuilder(richLink.getTitle());
                                                imageViewPhoto.setImageDrawable(builder.build());
                                            }
                                        });

                                Picasso.with(getContext())
                                        .load(richLink.getUri())
                                        .into(imageViewLinkThumbnail, new Callback.EmptyCallback() {
                                            @Override
                                            public void onError() {
                                                InitialDrawableBuilder builder = new InitialDrawableBuilder(richLink.getTitle());
                                                imageViewLinkThumbnail.setImageDrawable(builder.build());
                                            }
                                        });
                            }
                        })
                        .fail(e -> {
                            LOG.e("Failed to fetch rich link.", e);

                            // photo
                            textViewNoImage.setVisibility(View.VISIBLE);

                            textViewLinkTitle.setText(AreaServiceItem.linkUri);
                        })
                        .always((state, resolved, rejected) -> indicatorView.smoothToHide());

                textViewObjectManager.setText(AreaServiceItem.managerName);

                layoutItem.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AreaServiceItem.linkUri));
                    startActivity(intent);
                });
            }
        }
    }

    private final class AreaServiceItem {

        @NonNull
        final String linkUri;

        @NonNull
        final String managerName;

        AreaServiceItem(@NonNull String linkUri, @NonNull String managerName) {
            this.linkUri = linkUri;
            this.managerName = managerName;
        }
    }

    //
    // Mock data
    //
//    @NonNull
//    public Area getArea(@NonNull String AreaId) {
//        ArrayMap<String, Area> AreaMap = new ArrayMap<>();
//
//        Area Area1 = new Area("aaa", "六本木一丁目駅");
//        Area1.photo = BitmapFactory.decodeResource(getResources(), R.drawable.tokyo_metro);
//
//        Area Area2 = new Area("bbb", "港区市役所");
//        Area2.photo = BitmapFactory.decodeResource(getResources(), R.drawable.minatoku);
//
//        Area Area3 = new Area("ccc", "溜池山王駅");
//        Area3.photo = BitmapFactory.decodeResource(getResources(), R.drawable.tokyo_metro);
//
//        AreaMap.put(Area1.AreaId, Area1);
//        AreaMap.put(Area2.AreaId, Area2);
//        AreaMap.put(Area3.AreaId, Area3);
//
//        return AreaMap.get(AreaId);
//    }

    //
    // Mock data
    //
    @NonNull
    public List<AreaServiceItem> getAreaServices(@NonNull String AreaId) {
        ArrayMap<String, List<AreaServiceItem>> AreaServicesMap = new ArrayMap<>();

        List<AreaServiceItem> city = new ArrayList<>();
        city.add(new AreaServiceItem("https://www.facebook.com/city.minato.akasakashinzentaishi/", "Altla"));
        city.add(new AreaServiceItem("https://twitter.com/minato_city", "Altla"));

        List<AreaServiceItem> metro = new ArrayList<>();
        metro.add(new AreaServiceItem("http://www.tokyometro.jp/station/roppongi-itchome/yardmap/index.html", "Altla"));

        List<AreaServiceItem> metoro2 = new ArrayList<>();
        metoro2.add(new AreaServiceItem("http://www.tokyometro.jp/station/tameike-sanno/yardmap/index.html", "Altla"));

        AreaServicesMap.put("aaa", metro);
        AreaServicesMap.put("bbb", city);
        AreaServicesMap.put("ccc", metoro2);

        return AreaServicesMap.get(AreaId);
    }
}
