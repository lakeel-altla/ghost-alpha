package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper.getRequiredSupportActionBar;
import static com.lakeel.altla.ghost.alpha.viewhelper.BundleHelper.getRequiredString;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.findViewById;
import static com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper.getRequiredArguments;
import static com.lakeel.altla.ghost.alpha.viewhelper.ToastHelper.showShortToast;

public class MyObjectViewFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(MyObjectViewFragment.class);

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    private static final String ARG_KEY = "key";

    @Inject
    VirtualObjectApi virtualObjectApi;

    @Inject
    RichLinkLoader richLinkLoader;

    @Inject
    RichLinkImageLoader richLinkImageLoader;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String key;

    private FragmentContext fragmentContext;

    private TextView textViewUri;

    private MapView mapView;

    private ImageView imageViewRichLinkImage;

    private TextView textViewRichLinkTitle;

    @Nullable
    private VirtualObject object;

    @Nullable
    private GoogleMap googleMap;

    @Nullable
    private Marker marker;

    @NonNull
    public static MyObjectViewFragment newInstance(@NonNull String key) {
        MyObjectViewFragment fragment = new MyObjectViewFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_KEY, key);
        fragment.setArguments(arguments);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        key = getRequiredString(getRequiredArguments(this), ARG_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_object_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textViewUri = findViewById(this, R.id.text_view_uri);
        mapView = findViewById(this, R.id.map_view);
        imageViewRichLinkImage = findViewById(this, R.id.image_view_rich_link_image);
        textViewRichLinkTitle = findViewById(this, R.id.text_view_rich_link_title);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(false);

            updateMapView();
        });

        virtualObjectApi.findUserObject(CurrentUser.getInstance().getRequiredUserId(), key, object -> {
            this.object = object;
            getActivity().invalidateOptionsMenu();

            if (object == null) {
                LOG.e("No virtual object exists: key = %s", key);
            } else {
                textViewUri.setText(object.getRequiredUriString());
                updateMapView();

                Disposable disposable = Single
                        .<RichLink>create(e -> {
                            RichLink richLink = richLinkLoader.load(object.getRequiredUriString());
                            e.onSuccess(richLink);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(richLink -> {
                            richLinkImageLoader.load(richLink, imageViewRichLinkImage);
                            textViewRichLinkTitle.setText(richLink.getTitleOrUri());
                        }, e -> {
                            LOG.e("Failed to load a rich link.", e);
                            showShortToast(getContext(), R.string.toast_failed_to_load_rich_link);
                        });
                compositeDisposable.add(disposable);
            }
        }, e -> {
            LOG.e("Failed to find a virtual object.", e);
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_object_view, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean enabled = (object != null);
        menu.findItem(R.id.action_edit).setEnabled(enabled);
        menu.findItem(R.id.action_delete).setEnabled(enabled);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                if (object != null) {
                    fragmentContext.showMyObjectEditFragment(key);
                }
                return true;
            case R.id.action_delete:
                if (object != null) {
                    virtualObjectApi.deleteUserObjects(
                            CurrentUser.getInstance().getRequiredUserId(), object.getKey(),
                            aVoid -> {
                                fragmentContext.back();
                                showShortToast(getContext(), R.string.toast_deleted);
                            }, e -> {
                                LOG.e("Failed to delete a virtual object.", e);
                            });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        getActivity().setTitle(R.string.title_my_object_view);
        getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);
        getRequiredSupportActionBar(this).setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void updateMapView() {
        if (googleMap != null && object != null) {

            if (marker != null) {
                marker.remove();
                marker = null;
            }

            LatLng location = new LatLng(object.getRequiredGeoPoint().getLatitude(),
                                         object.getRequiredGeoPoint().getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM_LEVEL);

            googleMap.moveCamera(cameraUpdate);
            marker = googleMap.addMarker(new MarkerOptions().position(location));
        }
    }

    public interface FragmentContext {

        void showMyObjectEditFragment(@NonNull String key);

        void back();
    }
}
