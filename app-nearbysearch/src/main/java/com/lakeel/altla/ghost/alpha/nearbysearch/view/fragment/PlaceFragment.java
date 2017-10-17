package com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.place.web.Place;
import com.lakeel.altla.ghost.alpha.google.place.web.PlaceWebApi;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.BundleHelper;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.FragmentHelper;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class PlaceFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(PlaceFragment.class);

    @Inject
    PlaceWebApi placeWebApi;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String placeId;

    private String name;

    private TextView textViewDetailsJson;

    @NonNull
    public static PlaceFragment newInstance(@NonNull String placeId, @NonNull String name) {
        PlaceFragment fragment = new PlaceFragment();
        Arguments arguments = new Arguments().setPlaceId(placeId)
                                             .setName(name);
        fragment.setArguments(arguments.bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Arguments arguments = new Arguments(FragmentHelper.getRequiredArguments(this));
        placeId = arguments.getRequiredPlaceId();
        name = arguments.getRequiredName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        textViewDetailsJson = getView().findViewById(R.id.text_view_details_json);
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().setTitle(name);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);
        AppCompatHelper.getRequiredSupportActionBar(this).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        Disposable disposable = Single
                .<Place>create(e -> {
                    Place place = placeWebApi.getPlace(placeId, null);
                    e.onSuccess(place);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(place -> {
                    if (place == null) {
                        LOG.w("No place exists: placeId = %s", placeId);
                    } else {
                        textViewDetailsJson.setText(placeWebApi.getGson().toJson(place));
                    }
                }, e -> {
                    LOG.e("Failed to get a place.", e);
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    private static final class Arguments {

        static final String PLACE_ID = "placeId";

        static final String NAME = "name";

        Bundle bundle;

        Arguments() {
            this(new Bundle());
        }

        Arguments(@NonNull Bundle bundle) {
            this.bundle = bundle;
        }

        @NonNull
        String getRequiredPlaceId() {
            return BundleHelper.getRequiredString(bundle, PLACE_ID);
        }

        @NonNull
        Arguments setPlaceId(@NonNull String placeId) {
            bundle.putString(PLACE_ID, placeId);
            return this;
        }

        @NonNull
        String getRequiredName() {
            return BundleHelper.getRequiredString(bundle, NAME);
        }

        @NonNull
        Arguments setName(@NonNull String name) {
            bundle.putString(NAME, name);
            return this;
        }
    }
}
