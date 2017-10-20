package com.lakeel.altla.ghost.alpha.mock.lib.animation.helper;

import android.support.annotation.IntRange;

import org.parceler.Parcel;

@Parcel
public final class RevealAnimationSettings {

    public int centerX;

    public int centerY;

    public int width;

    public int height;

    @SuppressWarnings("WeakerAccess")
    public RevealAnimationSettings() {
        // Needed to parceler.
    }

    public RevealAnimationSettings(@IntRange(from = 0) int centerX,
                                   @IntRange(from = 0) int centerY,
                                   @IntRange(from = 0) int width,
                                   @IntRange(from = 0) int height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }
}