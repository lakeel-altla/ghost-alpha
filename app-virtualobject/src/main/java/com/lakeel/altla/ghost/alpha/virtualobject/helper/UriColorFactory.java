package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class UriColorFactory {

    private final ObjectColorSource objectColorSource;

    public UriColorFactory(@NonNull ObjectColorSource objectColorSource) {
        this.objectColorSource = objectColorSource;
    }

    @ColorInt
    public int create(@NonNull String uriString) {
        return objectColorSource.get(uriString);
    }

    @ColorInt
    public int getDarkColor(@NonNull String uriString) {
        int baseColor = create(uriString);
        float r = Color.red(baseColor);
        float g = Color.green(baseColor);
        float b = Color.blue(baseColor);
        r *= 0.9;
        g *= 0.9;
        b *= 0.9;
        return Color.rgb((int) r, (int) g, (int) b);
    }
}
