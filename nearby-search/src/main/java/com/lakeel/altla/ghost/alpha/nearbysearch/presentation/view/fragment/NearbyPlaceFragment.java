package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment;

import com.google.gson.Gson;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.Place;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.PlaceWebApi;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module.Names;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.helper.BundleHelper;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.helper.FragmentHelper;

import org.jdeferred.DeferredManager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class NearbyPlaceFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(NearbyPlaceFragment.class);

    @Inject
    PlaceWebApi placeWebApi;

    @Inject
    DeferredManager deferredManager;

    @Named(Names.PLACE_WEB_API_GSON)
    @Inject
    Gson gson;

    @BindView(R.id.text_view_details_json)
    TextView textViewDetailsJson;

    private String placeId;

    private String name;

    private FragmentContext fragmentContext;

    @NonNull
    public static NearbyPlaceFragment newInstance(@NonNull String placeId, @NonNull String name) {
        NearbyPlaceFragment fragment = new NearbyPlaceFragment();
        Arguments arguments = new Arguments().setPlaceId(placeId)
                                             .setName(name);
        fragment.setArguments(arguments.bundle);
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
        Arguments arguments = new Arguments(FragmentHelper.getRequiredArguments(this));
        placeId = arguments.getRequiredPlaceId();
        name = arguments.getRequiredName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_place, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(name);
        fragmentContext.setDisplayHomeAsUpEnabled(true);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        deferredManager.when(() -> {
            Place place = placeWebApi.details(placeId, null);
            if (place == null) {
                LOG.w("No details: placeId = %s", placeId);
            } else {
                getActivity().runOnUiThread(() -> {
                    setPlace(place);
                });
            }
        });
    }

    private void setPlace(@NonNull Place place) {
        textViewDetailsJson.setText(gson.toJson(place));
    }

    public interface FragmentContext {

        void setTitle(@Nullable CharSequence name);

        void setDisplayHomeAsUpEnabled(boolean enabled);

        void setHomeAsUpIndicator(@DrawableRes int resId);
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
