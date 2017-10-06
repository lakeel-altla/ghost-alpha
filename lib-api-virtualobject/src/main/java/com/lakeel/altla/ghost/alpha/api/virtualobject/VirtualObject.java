package com.lakeel.altla.ghost.alpha.api.virtualobject;

import com.google.firebase.firestore.GeoPoint;

import com.lakeel.altla.ghost.alpha.data.firestore.BaseDocument;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class VirtualObject extends BaseDocument {

    private String uriString;

    private GeoPoint geoPoint;

    @Nullable
    public String getUriString() {
        return uriString;
    }

    @NonNull
    public String getRequiredUriString() {
        if (uriString == null) throw new IllegalStateException("'uriString' is null.");
        return uriString;
    }

    public void setUriString(@Nullable String uriString) {
        this.uriString = uriString;
    }

    @Nullable
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    @NonNull
    public GeoPoint getRequiredGeoPoint() {
        if (geoPoint == null) throw new IllegalStateException("'geoPoint' is null.");
        return geoPoint;
    }

    public void setGeoPoint(@Nullable GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
