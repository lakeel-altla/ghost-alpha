package com.lakeel.altla.ghost.alpha.nearbysearch.di.component;

import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment.DebugSettingsFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.activity.MainActivity;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment.NearbyPlaceFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment.NearbyPlaceListFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(DebugSettingsFragment fragment);

    void inject(NearbyPlaceListFragment fragment);

    void inject(NearbyPlaceFragment fragment);
}
