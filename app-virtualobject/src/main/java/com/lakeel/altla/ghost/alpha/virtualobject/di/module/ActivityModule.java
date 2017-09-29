package com.lakeel.altla.ghost.alpha.virtualobject.di.module;

import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScope;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import android.app.Activity;
import android.content.ContentResolver;
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
    ContentResolver provideContentResolver() {
        return activity.getContentResolver();
    }

    @ActivityScope
    @Provides
    DeferredManager provideDeferredManager() {
        return new AndroidDeferredManager();
    }
}
