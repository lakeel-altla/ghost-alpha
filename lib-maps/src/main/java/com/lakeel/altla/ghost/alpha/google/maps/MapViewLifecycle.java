package com.lakeel.altla.ghost.alpha.google.maps;

import com.google.android.gms.maps.MapView;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.Objects;

public final class MapViewLifecycle {

    private MapViewLifecycle() {
    }

    public static void manage(@NonNull final Fragment fragment, @NonNull final MapView mapView) {

        final ComponentCallbacks componentCallbacks = new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(Configuration newConfig) {
            }

            @Override
            public void onLowMemory() {
                mapView.onLowMemory();
            }
        };
        Context context = fragment.getContext();
        Objects.requireNonNull(context);
        context.registerComponentCallbacks(componentCallbacks);

        FragmentActivity activity = fragment.getActivity();
        Objects.requireNonNull(activity);
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                if (f == fragment) {
                    mapView.onCreate(savedInstanceState);
                }
            }

            @Override
            public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                if (f == fragment) {
                    mapView.onDestroy();
                    fragment.getContext().unregisterComponentCallbacks(componentCallbacks);
                    fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                }
            }

            @Override
            public void onFragmentStarted(FragmentManager fm, Fragment f) {
                if (f == fragment) {
                    mapView.onStart();
                }
            }

            @Override
            public void onFragmentStopped(FragmentManager fm, Fragment f) {
                if (f == fragment) {
                    mapView.onStop();
                }
            }

            @Override
            public void onFragmentResumed(FragmentManager fm, Fragment f) {
                if (f == fragment) {
                    mapView.onResume();
                }
            }

            @Override
            public void onFragmentPaused(FragmentManager fm, Fragment f) {
                if (f == fragment) {
                    mapView.onPause();
                }
            }

            @Override
            public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
                if (f == fragment) {
                    mapView.onSaveInstanceState(outState);
                }
            }

        }, false);
    }
}
