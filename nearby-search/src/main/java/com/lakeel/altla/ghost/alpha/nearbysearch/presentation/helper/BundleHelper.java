package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.helper;

import android.os.Bundle;
import android.support.annotation.NonNull;

import static java.lang.String.format;

public final class BundleHelper {

    private BundleHelper() {
    }

    @NonNull
    public static String getRequiredString(@NonNull Bundle bundle, @NonNull String key) {
        String value = bundle.getString(key);
        if (value == null) throw new IllegalStateException(format("The key '%s' is required.", key));

        return value;
    }
}
