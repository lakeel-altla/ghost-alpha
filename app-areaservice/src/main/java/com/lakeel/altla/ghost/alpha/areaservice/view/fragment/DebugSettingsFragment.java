package com.lakeel.altla.ghost.alpha.areaservice.view.fragment;

import com.lakeel.altla.ghost.alpha.areaservice.R;
import com.lakeel.altla.ghost.alpha.areaservice.di.ActivityScopeContext;
import com.lakeel.altla.ghost.alpha.areaservice.helper.DebugPreferences;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatHelper;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import static java.lang.String.format;

public final class DebugSettingsFragment extends Fragment {

    private DebugPreferences debugPreferences;

    @NonNull
    public static DebugSettingsFragment newInstance() {
        return new DebugSettingsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null) throw new IllegalStateException("The root view could not be found.");

        debugPreferences = new DebugPreferences(this);

        getActivity().setTitle(R.string.title_debug_settings);
        AppCompatHelper.getRequiredSupportActionBar(this).setDisplayHomeAsUpEnabled(true);
        AppCompatHelper.getRequiredSupportActionBar(this).setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        Switch switchGoogleMapVisible = getView().findViewById(R.id.switch_google_map_visible);
        switchGoogleMapVisible.setChecked(debugPreferences.isGoogleMapVisible());
        switchGoogleMapVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            debugPreferences.setGoogleMapVisible(isChecked);
        });

        TextView textViewSearchRadiusValue = getView().findViewById(R.id.text_view_search_radius_value);
        int searchRadius = debugPreferences.getSearchRadius();
        textViewSearchRadiusValue.setText(formatTextViewSearchRadiusValue(searchRadius));

        DiscreteSeekBar seekBarSearchRadius = getView().findViewById(R.id.seek_bar_search_radius);
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

        TextView textViewLocationUpdatesIntervalValue = getView().findViewById(
                R.id.text_view_location_updates_interval_value);
        int locationUpdateInterval = debugPreferences.getLocationUpdatesInterval();

        DiscreteSeekBar seekBarLocationUpdatesInterval = getView().findViewById(
                R.id.seek_bar_location_updates_interval);
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

        Switch switchManualLocationUpdatesEnabled = getView().findViewById(R.id.switch_manual_location_updates_enabled);
        switchManualLocationUpdatesEnabled.setChecked(debugPreferences.isManualLocationUpdatesEnabled());
        switchManualLocationUpdatesEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            debugPreferences.setManualLocationUpdatesEnabled(isChecked);
        });

        Spinner spinnerLocationRequestPriority = getView().findViewById(R.id.spinner_location_request_priority);
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

    abstract class OnProgressChangeListener implements DiscreteSeekBar.OnProgressChangeListener {

        @Override
        public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        }
    }
}
