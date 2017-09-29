package com.lakeel.altla.ghost.alpha.nearbysearch.di.module;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.lakeel.altla.ghost.alpha.nearbysearch.BuildConfig;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.DebugPreferences;
import com.lakeel.altla.ghost.alpha.google.place.web.PlaceWebApi;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

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
    DebugPreferences provideMyPreferences(Activity activity) {
        return new DebugPreferences(activity.getPreferences(Context.MODE_PRIVATE));
    }

    @ActivityScope
    @Provides
    DeferredManager provideDeferredManager() {
        return new AndroidDeferredManager();
    }

    @ActivityScope
    @Provides
    FusedLocationProviderClient provideFusedLocationProviderClient(Activity activity) {
        return LocationServices.getFusedLocationProviderClient(activity);
    }

    @ActivityScope
    @Provides
    OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return builder.build();
    }

    @ActivityScope
    @Provides
    PlaceWebApi providePlaceWebApi(@Named(Names.GOOGLE_API_KEY) String key, OkHttpClient httpClient) {
        return new PlaceWebApi(key, httpClient);
    }
}
