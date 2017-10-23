package com.lakeel.altla.ghost.alpha.mock.view.imageloader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import firestore.cloud.sample.altla.lakeel.com.lib.text.drawable.InitialDrawableBuilder;

public final class TextDrawableImageLoader {

    @NonNull
    private final ImageView imageView;

    @Nullable
    private final String imageUri;

    @Nullable
    private final String failedText;

    private static final Log LOG = LogFactory.getLog(TextDrawableImageLoader.class);

    public TextDrawableImageLoader(@NonNull ImageView imageView, @Nullable String imageUri, @Nullable String failedText) {
        this.imageView = Objects.requireNonNull(imageView);
        this.imageUri = Objects.requireNonNull(imageUri);
        this.failedText = failedText;
    }

    public void loadImage() {
        Picasso.with(imageView.getContext())
                .load(imageUri)
                .into(imageView, new Callback.EmptyCallback() {

                    @Override
                    public void onError() {
                        LOG.d("Failed to download image from uri.");

                        if (failedText != null && !failedText.isEmpty()) {
                            imageView.setImageDrawable(new InitialDrawableBuilder(failedText).build());
                        }
                    }
                });
    }

    public void cancel() {
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
    }
}
