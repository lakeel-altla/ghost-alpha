package com.lakeel.altla.ghost.alpha.nearbysearch.di.module;

import com.lakeel.altla.ghost.alpha.google.place.web.PlaceWebApi;
import com.lakeel.altla.ghost.alpha.nearbysearch.BuildConfig;
import com.lakeel.altla.ghost.alpha.nearbysearch.R;
import com.lakeel.altla.ghost.alpha.nearbysearch.app.MyApplication;
import com.lakeel.altla.ghost.alpha.nearbysearch.helper.ObjectColorSource;

import android.support.annotation.NonNull;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class ApplicationModule {

    private MyApplication application;

    public ApplicationModule(@NonNull MyApplication application) {
        this.application = application;
    }

    @Named(Names.GOOGLE_API_KEY)
    @Singleton
    @Provides
    String provideGoogleApiKey() {
        return application.getString(com.lakeel.altla.ghost.alpha.res.R.string.google_api_key);
    }

    @Singleton
    @Provides
    OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return builder.build();
    }

    @Singleton
    @Provides
    PlaceWebApi providePlaceWebApi(@Named(Names.GOOGLE_API_KEY) String key, OkHttpClient httpClient) {
        return new PlaceWebApi(key, httpClient);
    }

    @Singleton
    @Provides
    ObjectColorSource provideObjectColorSource() {
        int[] colors = application.getResources().getIntArray(R.array.letter_tile_colors);
        return new ObjectColorSource(colors);
    }
}
