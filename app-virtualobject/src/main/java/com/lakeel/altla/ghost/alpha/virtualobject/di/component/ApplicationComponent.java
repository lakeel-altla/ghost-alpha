package com.lakeel.altla.ghost.alpha.virtualobject.di.component;


import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class })
public interface ApplicationComponent {

    ActivityComponent activityComponent(ActivityModule module);
}
