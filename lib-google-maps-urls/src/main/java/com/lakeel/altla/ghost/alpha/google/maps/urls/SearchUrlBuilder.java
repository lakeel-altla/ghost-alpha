package com.lakeel.altla.ghost.alpha.google.maps.urls;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SearchUrlBuilder {

    @NonNull
    private final String query;

    private String queryPlaceId;

    public SearchUrlBuilder(@NonNull String keyword) {
        query = encode(keyword);
    }

    public SearchUrlBuilder(double latitude, double longitude) {
        query = latitude + "," + longitude;
    }

    @NonNull
    public SearchUrlBuilder setPlaceId(@Nullable String placeId) {
        this.queryPlaceId = encode(placeId);
        return this;
    }

    @NonNull
    public String build() {
        StringBuilder builder = new StringBuilder("https://www.google.com/maps/search/?api=1&query=").append(query);

        if (queryPlaceId != null) {
            builder.append("&query_place_id=").append(queryPlaceId);
        }

        return builder.toString();
    }

    @NonNull
    private String encode(@NonNull String source) {
        try {
            return URLEncoder.encode(source, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException(e);
        }
    }
}
