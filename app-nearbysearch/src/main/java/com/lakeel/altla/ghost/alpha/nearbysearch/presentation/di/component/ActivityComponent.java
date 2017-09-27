package com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.component;

import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment.DebugSettingsFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.activity.MainActivity;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment.NearbyPlaceFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.presentation.view.fragment.NearbyPlaceListFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(DebugSettingsFragment fragment);

    void inject(NearbyPlaceListFragment fragment);

    void inject(NearbyPlaceFragment fragment);
}
