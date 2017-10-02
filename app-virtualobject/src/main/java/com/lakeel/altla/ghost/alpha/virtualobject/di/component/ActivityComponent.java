package com.lakeel.altla.ghost.alpha.virtualobject.di.component;

import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.virtualobject.view.activity.MainActivity;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.DebugSettingsFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.NearbyObjectListFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(NearbyObjectListFragment fragment);

    void inject(DebugSettingsFragment fragment);
}
