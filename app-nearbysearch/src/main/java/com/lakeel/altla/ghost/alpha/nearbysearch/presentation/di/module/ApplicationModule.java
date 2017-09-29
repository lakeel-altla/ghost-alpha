package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module;

import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.app.MyApplication;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private MyApplication application;

    public ApplicationModule(@NonNull MyApplication application) {
        this.application = application;
    }

    @Singleton
    @Provides
    Resources provideResources() {
        return application.getResources();
    }

    @Named(Names.GOOGLE_API_KEY)
    @Singleton
    @Provides
    String provideGoogleApiKey() {
        return application.getString(com.lakeel.altla.ghost.alpha.res.R.string.google_api_key);
    }
}
