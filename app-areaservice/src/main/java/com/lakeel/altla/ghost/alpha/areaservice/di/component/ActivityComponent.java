package com.lakeel.altla.ghost.alpha.areaservice.di.component;

import com.lakeel.altla.ghost.alpha.areaservice.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.areaservice.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.areaservice.view.activity.MainActivity;
import com.lakeel.altla.ghost.alpha.areaservice.view.fragment.NearbyAreaListFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(NearbyAreaListFragment fragment);
}
