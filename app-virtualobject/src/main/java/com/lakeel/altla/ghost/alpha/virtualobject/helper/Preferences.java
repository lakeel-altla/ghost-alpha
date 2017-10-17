package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import com.google.android.gms.location.LocationRequest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public final class Preferences {

    public static final String KEY_GOOGLE_MAP_VISIBLE = "google_map_visible";

    public static final String KEY_SEARCH_RADIUS = "search_radius";

    public static final String KEY_LOCATION_UPDATES_INTERVAL = "location_update_interval";

    public static final String KEY_LOCATION_UPDATES_DISTANCE = "location_update_distance";

    public static final String KEY_MANUAL_LOCATION_UPDATES_ENABLED = "manual_location_updates_enabled";

    public static final String KEY_LOCATION_REQUEST_PRIORITY = "location_request_priority";

    private static final int[] LOCATION_REQUEST_PRIORITIES = {
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationRequest.PRIORITY_LOW_POWER,
            LocationRequest.PRIORITY_NO_POWER
    };

    private SharedPreferences preferences;

    public Preferences(@NonNull Fragment fragment) {
        this(fragment.getContext());
    }

    public Preferences(@NonNull Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isGoogleMapVisible() {
        return preferences.getBoolean(KEY_GOOGLE_MAP_VISIBLE, false);
    }

    public int getSearchRadius() {
        return Integer.parseInt(preferences.getString(KEY_SEARCH_RADIUS, null));
    }

    public int getLocationUpdatesInterval() {
        return Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATES_INTERVAL, null));
    }

    public int getLocationUpdatesDistance() {
        return Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATES_DISTANCE, null));
    }

    public boolean isManualLocationUpdatesEnabled() {
        return preferences.getBoolean(KEY_MANUAL_LOCATION_UPDATES_ENABLED, false);
    }

    public int getLocationRequestPriority() {
        int index = Integer.parseInt(preferences.getString(KEY_LOCATION_REQUEST_PRIORITY, null));
        return LOCATION_REQUEST_PRIORITIES[index];
    }
}
