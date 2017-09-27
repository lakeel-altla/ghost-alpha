package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.component;

import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module.ApplicationModule;

import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class })
public interface ApplicationComponent {

    ActivityComponent activityComponent(ActivityModule module);

    Resources resources();
}
