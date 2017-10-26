package com.lakeel.altla.ghost.alpha.rxhelper;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.Objects;

import io.reactivex.disposables.Disposable;

public final class RxHelper {

    private RxHelper() {
    }

    public static void disposeOnStop(@NonNull final Fragment fragment, @NonNull final Disposable disposable) {
        FragmentActivity activity = fragment.getActivity();
        Objects.requireNonNull(activity);
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentStopped(FragmentManager fm, Fragment f) {
                if (f == fragment) {
                    disposable.dispose();
                    fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                }
            }
        }, false);
    }
}
