package com.lakeel.altla.ghost.alpha.areaservice.di.module;

import com.lakeel.altla.ghost.alpha.areaservice.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.areaservice.helper.LinkLetterTileFactory;
import com.lakeel.altla.ghost.alpha.areaservice.helper.RichLinkImageLoader;

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
    RichLinkImageLoader provideRichLinkImageLoader(LinkLetterTileFactory linkLetterTileFactory) {
        return new RichLinkImageLoader(activity, linkLetterTileFactory);
    }
}
