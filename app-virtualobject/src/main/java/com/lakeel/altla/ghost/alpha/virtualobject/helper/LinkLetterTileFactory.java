package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import com.amulyakhare.textdrawable.TextDrawable;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public final class LinkLetterTileFactory {

    private final UriColorFactory uriColorFactory;

    public LinkLetterTileFactory(@NonNull UriColorFactory uriColorFactory) {
        this.uriColorFactory = uriColorFactory;
    }

    @NonNull
    public Drawable create(@NonNull String uri, @NonNull String title) {
        String letter = title.substring(0, 1);
        int color = uriColorFactory.create(uri);
        return TextDrawable.builder().buildRect(letter, color);
    }
}
