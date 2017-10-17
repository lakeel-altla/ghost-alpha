package com.lakeel.altla.ghost.alpha.virtualobject.app;

import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.virtualobject.BuildConfig;
import com.lakeel.altla.ghost.alpha.virtualobject.di.component.ApplicationComponent;
import com.lakeel.altla.ghost.alpha.virtualobject.di.component.DaggerApplicationComponent;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ApplicationModule;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

public final class MyApplication extends MultiDexApplication {

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
