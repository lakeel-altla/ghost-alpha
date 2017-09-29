package com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.DebugPreferences;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.String.format;

public final class DebugSettingsFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(DebugSettingsFragment.class);

    @Inject
    DebugPreferences debugPreferences;

    @BindView(R.id.switch_google_map_visible)
    Switch switchGoogleMapVisible;

    @BindView(R.id.text_view_search_radius_value)
    TextView textViewSearchRadiusValue;

    @BindView(R.id.seek_bar_search_radius)
    DiscreteSeekBar seekBarSearchRadius;

    @BindView(R.id.text_view_location_updates_interval_value)
    TextView textViewLocationUpdatesIntervalValue;

    @BindView(R.id.seek_bar_location_updates_interval)
    DiscreteSeekBar seekBarLocationUpdatesInterval;

    @BindView(R.id.text_view_location_updates_distance_value)
    TextView textViewLocationUpdatesDistanceValue;

    @BindView(R.id.seek_bar_location_updates_distance)
    DiscreteSeekBar seekBarLocationUpdatesDistance;

    @BindView(R.id.switch_manual_location_updates_enabled)
    Switch switchManualLocationUpdatesEnabled;

    @BindView(R.id.spinner_location_request_priority)
    Spinner spinnerLocationRequestPriority;

    private FragmentContext fragmentContext;

    @NonNull
    public static DebugSettingsFragment newInstance() {
        return new DebugSettingsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view == null) return;

        ButterKnife.bind(this, view);

        fragmentContext.setTitle(R.string.title_debug);
        fragmentContext.setDisplayHomeAsUpEnabled(true);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        switchGoogleMapVisible.setChecked(debugPreferences.isGoogleMapVisible());
        switchGoogleMapVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            debugPreferences.setGoogleMapVisible(isChecked);
        });

        int searchRadius = debugPreferences.getSearchRadius();
        textViewSearchRadiusValue.setText(formatTextViewSearchRadiusValue(searchRadius));
        seekBarSearchRadius.setMin(DebugPreferences.RANGE_SEARCH_RADIUS.min);
        seekBarSearchRadius.setMax(DebugPreferences.RANGE_SEARCH_RADIUS.max);
        seekBarSearchRadius.setProgress(searchRadius);
        seekBarSearchRadius.setOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                debugPreferences.setSearchRadius(value);
                textViewSearchRadiusValue.setText(formatTextViewSearchRadiusValue(value));
            }
        });

        int locationUpdateInterval = debugPreferences.getLocationUpdatesInterval();
        textViewLocationUpdatesIntervalValue.setText(formatTextViewLocationUpdatesInterval(locationUpdateInterval));
        seekBarLocationUpdatesInterval.setMin(DebugPreferences.RANGE_LOCATION_UPDATES_INTERVAL.min);
        seekBarLocationUpdatesInterval.setMax(DebugPreferences.RANGE_LOCATION_UPDATES_INTERVAL.max);
        seekBarLocationUpdatesInterval.setProgress(locationUpdateInterval);
        seekBarLocationUpdatesInterval.setOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                debugPreferences.setLocationUpdatesInterval(value);
                textViewLocationUpdatesIntervalValue.setText(formatTextViewLocationUpdatesInterval(value));
            }
        });

        // TODO
        int locationUpdateDistance = debugPreferences.getLocationUpdatesDistance();
        textViewLocationUpdatesDistanceValue.setText(formatTextViewLocationUpdatesDistance(locationUpdateDistance));
        seekBarLocationUpdatesDistance.setMin(DebugPreferences.RANGE_LOCATION_UPDATES_DISTANCE.min);
        seekBarLocationUpdatesDistance.setMax(DebugPreferences.RANGE_LOCATION_UPDATES_DISTANCE.max);
        seekBarLocationUpdatesDistance.setProgress(locationUpdateDistance);
        seekBarLocationUpdatesDistance.setOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                debugPreferences.setLocationUpdatesDistance(value);
                textViewLocationUpdatesDistanceValue.setText(formatTextViewLocationUpdatesDistance(value));
            }
        });

        switchManualLocationUpdatesEnabled.setChecked(debugPreferences.isManualLocationUpdatesEnabled());
        switchManualLocationUpdatesEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            debugPreferences.setManualLocationUpdatesEnabled(isChecked);
        });

        spinnerLocationRequestPriority.setSelection(debugPreferences.getLocationRequestPriorityIndex());
        spinnerLocationRequestPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                debugPreferences.setLocationRequestPriority(
                        DebugPreferences.getLocationRequestPriorityByIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @NonNull
    private String formatTextViewSearchRadiusValue(int value) {
        return format(getString(R.string.format_text_view_search_radius_value), value);
    }

    @NonNull
    private String formatTextViewLocationUpdatesInterval(int value) {
        return format(getString(R.string.format_text_view_location_updates_interval_value), value);
    }

    @NonNull
    private String formatTextViewLocationUpdatesDistance(int value) {
        return format(getString(R.string.format_text_view_location_updates_distance_value), value);
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setDisplayHomeAsUpEnabled(boolean enabled);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void backView();
    }

    abstract class OnProgressChangeListener implements DiscreteSeekBar.OnProgressChangeListener {

        @Override
        public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        }
    }
}
