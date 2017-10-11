package com.lakeel.altla.ghost.alpha.virtualobject.di.module;

import com.lakeel.altla.ghost.alpha.api.virtualobject.VirtualObjectApi;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkParser;
import com.lakeel.altla.ghost.alpha.virtualobject.R;
import com.lakeel.altla.ghost.alpha.virtualobject.app.MyApplication;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.LinkLetterTileFactory;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.ObjectColorSource;
import com.lakeel.altla.ghost.alpha.virtualobject.helper.UriColorFactory;

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

    @Singleton
    @Provides
    VirtualObjectApi provideVirtualObjectApi() {
        return new VirtualObjectApi();
    }

    @Singleton
    @Provides
    RichLinkParser provideRichLinkParser() {
        return new RichLinkParser.Builder().build();
    }

    @Singleton
    @Provides
    LinkLetterTileFactory provideLinkLetterTileFactory(Resources resources) {
        int[] colors = resources.getIntArray(R.array.letter_tile_colors);
        ObjectColorSource objectColorSource = new ObjectColorSource(colors);
        UriColorFactory uriColorFactory = new UriColorFactory(objectColorSource);
        return new LinkLetterTileFactory(uriColorFactory);
    }
}
