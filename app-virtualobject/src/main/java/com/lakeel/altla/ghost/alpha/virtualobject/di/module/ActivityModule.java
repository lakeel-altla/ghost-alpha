package com.lakeel.altla.ghost.alpha.virtualobject.di.module;

import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.LinkLetterTileFactory;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.RichLinkImageLoader;

import android.app.Activity;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public final class ActivityModule {

    private Activity activity;

    public ActivityModule(@NonNull Activity activity) {
        this.activity = activity;
    }

    @ActivityScope
    @Provides
    Activity provideActivity() {
        return activity;
    }

    @ActivityScope
    @Provides
    RichLinkImageLoader provideRichLinkImageLoader(Activity activity, LinkLetterTileFactory linkLetterTileFactory) {
        return new RichLinkImageLoader(activity, linkLetterTileFactory);
    }
}
