package com.lakeel.altla.ghost.alpha.areaservice.di.component;


import com.lakeel.altla.ghost.alpha.areaservice.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.areaservice.di.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class })
public interface ApplicationComponent {

    ActivityComponent activityComponent(ActivityModule module);
}
