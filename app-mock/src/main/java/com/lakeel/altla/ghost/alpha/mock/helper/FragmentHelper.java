package com.lakeel.altla.ghost.alpha.mock.helper;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.lakeel.altla.ghost.alpha.mock.R;

import java.util.Map;

public final class FragmentHelper {

    private FragmentHelper() {
    }

    @NonNull
    public static String getBundleString(@NonNull Fragment fragment, @NonNull String bundleKey) {
        Bundle bundle = getArguments(fragment);
        String value = bundle.getString(bundleKey);
        if (value == null) {
            throw new NullPointerException("The string value of the bundle is null.");
        }
        return value;
    }

    @NonNull
    public static int getBundleInt(@NonNull Fragment fragment, @NonNull String bundleKey) {
        Bundle bundle = getArguments(fragment);
        return bundle.getInt(bundleKey);
    }

    public static void showFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment target) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        showFragment(fragmentTransaction, target, target.getClass().getSimpleName());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showFragmentWithAnimation(@NonNull FragmentManager fragmentManager, @NonNull Fragment target, @NonNull ArrayMap<View, String> sharedElements) {
        String tag = target.getClass().getSimpleName();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (Map.Entry<View, String> entry : sharedElements.entrySet()) {
            View view = entry.getKey();
            String name = entry.getValue();

            view.setTransitionName(name);

            fragmentTransaction.addSharedElement(view, name);
        }

        showFragment(fragmentTransaction, target, tag);
    }

    @NonNull
    public static Bundle getArguments(@NonNull Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        if (null == bundle) throw new NullPointerException("The variable (bundle) is null.");
        return bundle;
    }

    private static void showFragment(@NonNull FragmentTransaction fragmentTransaction, @NonNull Fragment target, @NonNull String tag) {
        fragmentTransaction
                .replace(R.id.content, target, tag)
                .addToBackStack(tag)
                .commit();
    }
}