package com.lakeel.altla.ghost.alpha.nearbysearch.di.component;

import com.lakeel.altla.ghost.alpha.nearbysearch.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.module.ApplicationModule;

import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class })
public interface ApplicationComponent {

    ActivityComponent activityComponent(ActivityModule module);

    Resources resources();
}
