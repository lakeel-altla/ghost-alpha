package com.lakeel.altla.ghost.alpha.nearbysearch.app;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.BuildConfig;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.component.ApplicationComponent;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.component.DaggerApplicationComponent;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.module.ApplicationModule;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import io.reactivex.plugins.RxJavaPlugins;

public final class MyApplication extends MultiDexApplication {

    private static final Log LOG = LogFactory.getLog(MyApplication.class);

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        LogFactory.setDebug(BuildConfig.DEBUG);

        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        // see: https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        RxJavaPlugins.setErrorHandler(e -> {
            LOG.e("An unhandled error.", e);
        });
    }

    public static ApplicationComponent getApplicationComponent(@NonNull Activity activity) {
        return ((MyApplication) activity.getApplication()).applicationComponent;
    }
}
