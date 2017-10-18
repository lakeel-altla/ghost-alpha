package com.lakeel.altla.ghost.alpha.nearbysearch.view.activity;

import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.Preferences;
import com.lakeel.altla.ghost.alpha.viewhelper.AppCompatPreferenceActivity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener onPreferenceChangeListener = (preference, value) -> {
        String string = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(string);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            preference.setSummary(string);
        }

        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.preference);
        bindPreferenceSummaryToValue(findPreference(Preferences.KEY_SEARCH_RADIUS));
        bindPreferenceSummaryToValue(findPreference(Preferences.KEY_LOCATION_UPDATES_INTERVAL));
        bindPreferenceSummaryToValue(findPreference(Preferences.KEY_LOCATION_REQUEST_PRIORITY));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        onPreferenceChangeListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                                 .getString(preference.getKey(), ""));
    }
}
