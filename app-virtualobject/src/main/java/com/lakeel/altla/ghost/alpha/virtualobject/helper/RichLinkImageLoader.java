package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import com.lakeel.altla.ghost.alpha.richlink.RichLink;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

public class RichLinkImageLoader {

    private final Picasso picasso;

    private final LinkLetterTileFactory linkLetterTileFactory;

    public RichLinkImageLoader(@NonNull Context context, @NonNull LinkLetterTileFactory linkLetterTileFactory) {
        picasso = Picasso.with(context);
        picasso.setIndicatorsEnabled(true);

        this.linkLetterTileFactory = linkLetterTileFactory;
    }

    public void load(@NonNull RichLink richLink, @NonNull ImageView imageView) {
        if (richLink.ogImageUri != null) {
            Uri ogImageUri = Uri.parse(richLink.ogImageUri);
            picasso.load(ogImageUri).into(imageView);
        } else {
            Drawable drawable = linkLetterTileFactory.create(richLink.getUri(), richLink.getTitleOrUri());
            imageView.setImageDrawable(drawable);
        }
    }
}
