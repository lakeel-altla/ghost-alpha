package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.app;

import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.BuildConfig;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.component.ApplicationComponent;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.component.DaggerApplicationComponent;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module.ApplicationModule;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

public final class MyApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        LogFactory.setDebug(BuildConfig.DEBUG);

        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public static ApplicationComponent getApplicationComponent(@NonNull Activity activity) {
        return ((MyApplication) activity.getApplication()).applicationComponent;
    }
}
