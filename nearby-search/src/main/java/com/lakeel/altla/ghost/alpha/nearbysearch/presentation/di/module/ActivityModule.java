package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.PlacePhotoApiUriFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.PlaceType;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.PlaceWebApi;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.Scope;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.DetailsResponse;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.DetailsResponseStatusHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.PlaceTypeHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.PlaceWebService;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.ScopeHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.SearchResponse;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service.SearchResponseStatusHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.helper.DebugPreferences;

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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public final class ActivityModule {

    private static final Log LOG = LogFactory.getLog(ActivityModule.class);

    private static final String BASE_URL_MAPS_API = "https://maps.googleapis.com/maps/api/";

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
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    @Named(Names.PLACE_WEB_API_GSON)
    @ActivityScope
    @Provides
    Gson providePlaceWebApiGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(PlaceType.class, PlaceTypeHandler.INSTANCE)
                .registerTypeAdapter(Scope.class, ScopeHandler.INSTANCE)
                .registerTypeAdapter(SearchResponse.Status.class, SearchResponseStatusHandler.INSTANCE)
                .registerTypeAdapter(DetailsResponse.Status.class, DetailsResponseStatusHandler.INSTANCE)
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
    }

    @ActivityScope
    @Provides
    PlaceWebApi providePlaceWebApi(@Named(Names.GOOGLE_API_KEY) String key,
                                   OkHttpClient httpClient,
                                   @Named(Names.PLACE_WEB_API_GSON) Gson gson) {

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL_MAPS_API)
                                                  .client(httpClient)
                                                  .addConverterFactory(GsonConverterFactory.create(gson))
                                                  .build();

        return new PlaceWebApi(key, retrofit.create(PlaceWebService.class));
    }

    @ActivityScope
    @Provides
    PlacePhotoApiUriFactory providePlacePhotoApiUriFactory(@Named(Names.GOOGLE_API_KEY) String key) {
        return new PlacePhotoApiUriFactory(key);
    }
}
