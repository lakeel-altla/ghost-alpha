package com.lakeel.altla.ghost.alpha.richlink;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RichLink {

    @NonNull
    public final String documentUri;

    @Nullable
    public String documentTitle;

    @Nullable
    public String documentDescription;

    @Nullable
    public String ogUri;

    @Nullable
    public String ogTitle;

    @Nullable
    public String ogType;

    @Nullable
    public String ogImageUri;

    @Nullable
    public String ogDescription;

    // for debug
    @Nullable
    public String html;

    public RichLink(String documentUri) {
        this.documentUri = documentUri;
    }

    @NonNull
    public String getUri() {
        String value = ogUri;
        if (value == null) {
            value = documentUri;
        }
        return value;
    }

    @Nullable
    public String getTitle() {
        String value = ogTitle;
        if (value == null) {
            value = documentTitle;
        }
        return value;
    }

    @Nullable
    public String getDescription() {
        String value = ogDescription;
        if (value == null) {
            value = documentDescription;
        }
        return value;
    }

    @NonNull
    public String getTitleOrUri() {
        String title = getTitle();
        if (title == null) {
            return getUri();
        } else {
            return title;
        }
    }
}
