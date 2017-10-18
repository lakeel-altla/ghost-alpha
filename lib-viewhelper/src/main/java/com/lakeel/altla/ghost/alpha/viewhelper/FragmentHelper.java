package com.lakeel.altla.ghost.alpha.viewhelper;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Objects;

public final class FragmentHelper {

    private FragmentHelper() {
    }

    @NonNull
    public static Bundle getRequiredArguments(@NonNull Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        if (bundle == null) throw new IllegalStateException("The fragment has no arguments.");

        return bundle;
    }

    @NonNull
    public static <T extends View> T findViewById(@NonNull Fragment fragment, @IdRes int id) {
        View root = fragment.getView();
        Objects.requireNonNull(root);
        T view = root.findViewById(id);
        Objects.requireNonNull(view);
        return view;
    }
}
