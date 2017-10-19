package com.lakeel.altla.ghost.alpha.mock.view.imageloader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;

public final class TextDrawableImageLoader {

    @NonNull
    private final ImageView imageView;

    @Nullable
    private final String imageUri;

    @Nullable
    private final String failedText;

    public TextDrawableImageLoader(@NonNull ImageView imageView, @Nullable String imageUri) {
        this(imageView, imageUri, null);
    }

    public TextDrawableImageLoader(@NonNull ImageView imageView, @Nullable String imageUri, @Nullable String failedText) {
        this.imageView = imageView;
        this.imageUri = imageUri;
        this.failedText = failedText;
    }

    public void loadImage() {
        if (imageUri == null) {
            if (failedText != null && !failedText.isEmpty()) {
                InitialDrawableBuilder builder = new InitialDrawableBuilder(failedText);
                imageView.setImageDrawable(builder.build());
            }
        } else {
            Picasso.with(imageView.getContext())
                    .load(imageUri)
                    .into(imageView, new Callback.EmptyCallback() {
                        @Override
                        public void onError() {
                            if (failedText != null && !failedText.isEmpty()) {
                                InitialDrawableBuilder builder = new InitialDrawableBuilder(failedText);
                                imageView.setImageDrawable(builder.build());
                            }
                        }
                    });
        }
    }
}
