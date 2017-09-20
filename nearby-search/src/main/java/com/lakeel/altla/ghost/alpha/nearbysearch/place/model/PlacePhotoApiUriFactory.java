package com.lakeel.altla.ghost.alpha.nearbysearch.place.model;

import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public final class PlacePhotoApiUriFactory {

    private static final int MAX_SIZE = 1600;

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/photo";

    @NonNull
    private final String key;

    public PlacePhotoApiUriFactory(@NonNull String key) {
        this.key = key;
    }

    @NonNull
    public Uri create(@NonNull String photoReference,
                      @IntRange(to = MAX_SIZE) int maxWidth,
                      @IntRange(to = MAX_SIZE) int maxHeight) {

        if (maxWidth <= 0 && maxHeight <= 0) {
            throw new IllegalArgumentException("Either 'maxWidth' or 'maxHeight' must be greater than 0.");
        }

        StringBuilder builder = new StringBuilder(BASE_URL);
        builder.append("?key=").append(key)
               .append("&photoreference=").append(photoReference);

        if (0 < maxWidth) builder.append("&maxwidth=").append(maxWidth);
        if (0 < maxHeight) builder.append("&maxheight=").append(maxHeight);

        return Uri.parse(builder.toString());
    }
}
