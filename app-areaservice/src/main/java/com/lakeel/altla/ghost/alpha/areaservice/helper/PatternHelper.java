package com.lakeel.altla.ghost.alpha.areaservice.helper;

import android.support.annotation.NonNull;
import android.util.Patterns;

import java.util.regex.Matcher;

public final class PatternHelper {

    private PatternHelper() {
    }

    public static String parseUriString(@NonNull String value) {
        Matcher matcher = Patterns.WEB_URL.matcher(value);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }
}
