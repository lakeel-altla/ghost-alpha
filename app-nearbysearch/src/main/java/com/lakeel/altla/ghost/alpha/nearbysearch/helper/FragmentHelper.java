package com.lakeel.altla.ghost.alpha.nearbysearch.helper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public final class FragmentHelper {

    private FragmentHelper() {
    }

    @NonNull
    public static Bundle getRequiredArguments(@NonNull Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        if (bundle == null) throw new IllegalStateException("The fragment has no arguments.");

        return bundle;
    }
}
