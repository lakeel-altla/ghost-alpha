package firestore.cloud.sample.altla.lakeel.com.lib.text.drawable;

import android.support.annotation.NonNull;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

public final class DrawableConfiguration {

    @NonNull
    public TextDrawable.IConfigBuilder configBuilder;

    @NonNull
    public ColorGenerator colorGenerator = ColorGenerator.DEFAULT;

    public DrawableConfiguration(@NonNull TextDrawable.IConfigBuilder configBuilder) {
        this.configBuilder = configBuilder;
    }
}