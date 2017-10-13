package com.lakeel.altla.ghost.alpha.nearbysearch.di.module;

import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScope;

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
}
