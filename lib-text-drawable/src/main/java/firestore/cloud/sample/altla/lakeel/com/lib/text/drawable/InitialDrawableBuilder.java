package firestore.cloud.sample.altla.lakeel.com.lib.text.drawable;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Objects;

public final class InitialDrawableBuilder {

    @NonNull
    private final String text;

    @Nullable
    private final DrawableConfiguration configuration;

    public InitialDrawableBuilder(@NonNull String text) {
        this(text, null);
    }

    public InitialDrawableBuilder(@NonNull String text, @Nullable DrawableConfiguration configuration) {
        this.text = Objects.requireNonNull(text);
        if (this.text.length() < 0)
            throw new IllegalArgumentException("Length of argument 'text' must be greater than zero.");
        this.configuration = configuration;
    }

    @NonNull
    public Drawable build() {
        if (configuration == null) {
            return buildDefault();
        } else {
            TextDrawable.IBuilder builder = configuration.configBuilder.endConfig().rect();
            return builder.build(text.substring(0, 1), configuration.colorGenerator.getColor(text));
        }
    }

    private Drawable buildDefault() {
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .rect();

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(text);

        return builder.build(text.substring(0, 1), color);
    }
}
