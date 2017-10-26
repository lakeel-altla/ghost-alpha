package com.lakeel.altla.ghost.alpha.viewhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Objects;

public final class IntentHelper {

    private IntentHelper() {
    }

    @NonNull
    public static Bundle getRequiredExtras(@NonNull Intent intent) {
        Bundle extras = intent.getExtras();
        Objects.requireNonNull(extras);
        return extras;
    }
}
