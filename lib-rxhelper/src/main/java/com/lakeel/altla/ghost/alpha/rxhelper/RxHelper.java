package com.lakeel.altla.ghost.alpha.rxhelper;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import io.reactivex.disposables.Disposable;

public final class RxHelper {

    private RxHelper() {
    }

    public static void disposeOnStop(@NonNull final Fragment fragment, @NonNull final Disposable disposable) {
        final FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
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
