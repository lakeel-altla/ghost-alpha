package com.lakeel.altla.ghost.alpha.nearbysearch.helper;

import com.google.android.gms.location.LocationRequest;

import com.lakeel.altla.ghost.alpha.viewhelper.FragmentHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public final class Preferences {

    public static final String KEY_SEARCH_RADIUS = "search_radius";

    public static final String KEY_LOCATION_UPDATES_INTERVAL = "location_update_interval";

    public static final String KEY_LOCATION_REQUEST_PRIORITY = "location_request_priority";

    private static final int[] LOCATION_REQUEST_PRIORITIES = {
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationRequest.PRIORITY_LOW_POWER,
            LocationRequest.PRIORITY_NO_POWER
    };

    private SharedPreferences preferences;

    public Preferences(@NonNull Fragment fragment) {
        this(FragmentHelper.getRequiredContext(fragment));
    }

    public Preferences(@NonNull Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getSearchRadius() {
        return Integer.parseInt(preferences.getString(KEY_SEARCH_RADIUS, null));
    }

    public int getLocationUpdatesInterval() {
        return Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATES_INTERVAL, null));
    }

    public int getLocationRequestPriority() {
        int index = Integer.parseInt(preferences.getString(KEY_LOCATION_REQUEST_PRIORITY, null));
        return LOCATION_REQUEST_PRIORITIES[index];
    }
}
