package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import com.google.android.gms.location.LocationRequest;

import android.content.SharedPreferences;

public final class DebugPreferences {

    public static final IntRange RANGE_SEARCH_RADIUS = new IntRange(10, 100);

    public static final IntRange RANGE_LOCATION_UPDATES_INTERVAL = new IntRange(5, 60);

    public static final IntRange RANGE_LOCATION_UPDATES_DISTANCE = new IntRange(1, 10);

    private static final int[] LOCATION_REQUEST_PRIORITIES = {
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationRequest.PRIORITY_LOW_POWER,
            LocationRequest.PRIORITY_NO_POWER
    };

    private static final String KEY_GOOGLE_MAP_VISIBLE = "googleMapVisible";

    private static final String KEY_SEARCH_RADIUS = "searchRadius";

    private static final String KEY_LOCATION_UPDATES_INTERVAL = "locationUpdatesInterval";

    private static final String KEY_LOCATION_UPDATES_DISTANCE = "locationUpdatesDistance";

    private static final String KEY_MANUAL_LOCATION_UPDATES_ENABLED = "manualLocationUpdatesEnabled";

    private static final String KEY_LOCATION_REQUEST_PRIORITY = "locationRequestPriority";

    private SharedPreferences preferences;

    public DebugPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isGoogleMapVisible() {
        return preferences.getBoolean(KEY_GOOGLE_MAP_VISIBLE, false);
    }

    public void setGoogleMapVisible(boolean visible) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_GOOGLE_MAP_VISIBLE, visible);
        editor.apply();
    }

    public int getSearchRadius() {
        return preferences.getInt(KEY_SEARCH_RADIUS, RANGE_SEARCH_RADIUS.min);
    }

    public void setSearchRadius(int value) {
        if (!RANGE_SEARCH_RADIUS.contains(value)) {
            throw new IllegalArgumentException(
                    "'value' is out of range: value = " + value + ", range = " + RANGE_SEARCH_RADIUS);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_SEARCH_RADIUS, value);
        editor.apply();
    }

    public int getLocationUpdatesInterval() {
        return preferences.getInt(KEY_LOCATION_UPDATES_INTERVAL, RANGE_LOCATION_UPDATES_INTERVAL.min);
    }

    public void setLocationUpdatesInterval(int value) {
        if (!RANGE_LOCATION_UPDATES_INTERVAL.contains(value)) {
            throw new IllegalArgumentException(
                    "'value' is out of range: value = " + value + ", range = " + RANGE_LOCATION_UPDATES_INTERVAL);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_LOCATION_UPDATES_INTERVAL, value);
        editor.apply();
    }

    public int getLocationUpdatesDistance() {
        return preferences.getInt(KEY_LOCATION_UPDATES_DISTANCE, RANGE_LOCATION_UPDATES_DISTANCE.min);
    }

    public void setLocationUpdatesDistance(int value) {
        if (!RANGE_LOCATION_UPDATES_DISTANCE.contains(value)) {
            throw new IllegalArgumentException(
                    "'value' is out of range: value = " + value + ", range = " + RANGE_LOCATION_UPDATES_DISTANCE);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_LOCATION_UPDATES_DISTANCE, value);
        editor.apply();
    }

    public boolean isManualLocationUpdatesEnabled() {
        return preferences.getBoolean(KEY_MANUAL_LOCATION_UPDATES_ENABLED, false);
    }

    public void setManualLocationUpdatesEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_MANUAL_LOCATION_UPDATES_ENABLED, enabled);
        editor.apply();
    }

    public int getLocationRequestPriority() {
        return preferences.getInt(KEY_LOCATION_REQUEST_PRIORITY, LOCATION_REQUEST_PRIORITIES[0]);
    }

    public int getLocationRequestPriorityIndex() {
        return getLocationRequestPriorityIndex(getLocationRequestPriority());
    }

    public void setLocationRequestPriority(int values) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_LOCATION_REQUEST_PRIORITY, values);
        editor.apply();
    }

    public static int getLocationRequestPriorityByIndex(int index) {
        return LOCATION_REQUEST_PRIORITIES[index];
    }

    public static int getLocationRequestPriorityIndex(int priority) {
        for (int i = 0; i < LOCATION_REQUEST_PRIORITIES.length; i++) {
            if (LOCATION_REQUEST_PRIORITIES[i] == priority) {
                return i;
            }
        }
        throw new IllegalArgumentException("'priority' is out of range: priority = " + priority);
    }
}
