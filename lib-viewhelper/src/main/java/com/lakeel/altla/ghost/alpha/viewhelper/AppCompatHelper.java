package com.lakeel.altla.ghost.alpha.viewhelper;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

public final class AppCompatHelper {

    private AppCompatHelper() {
    }

    @NonNull
    public static ActionBar getRequiredSupportActionBar(@NonNull Fragment fragment) {
        return getRequiredSupportActionBar(getAppCompatActivity(fragment));
    }

    @NonNull
    public static ActionBar getRequiredSupportActionBar(@NonNull AppCompatActivity activity) {
        return getRequiredSupportActionBar(activity.getDelegate());
    }

    @NonNull
    public static ActionBar getRequiredSupportActionBar(@NonNull AppCompatDelegate delegate) {
        ActionBar actionBar = delegate.getSupportActionBar();
        if (actionBar == null) {
            throw new IllegalStateException("No action bar is supported.");
        }
        return actionBar;
    }

    @NonNull
    private static AppCompatActivity getAppCompatActivity(@NonNull Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        if (activity instanceof AppCompatActivity) {
            return (AppCompatActivity) activity;
        } else {
            throw new IllegalStateException("'fragment.activity' isn't an instance of the AppCompatActivity class.");
        }
    }
}

