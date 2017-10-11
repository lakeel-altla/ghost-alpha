package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class ObjectColorSource {

    private final int[] colors;

    public ObjectColorSource(@NonNull int[] colors) {
        this.colors = colors;
    }

    @ColorInt
    public int get(@NonNull Object value) {
        int index = Math.abs(value.hashCode()) % colors.length;
        return colors[index];
    }
}
