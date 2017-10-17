package com.lakeel.altla.ghost.alpha.nearbysearch.di.component;

import com.lakeel.altla.ghost.alpha.nearbysearch.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.nearbysearch.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.activity.MainActivity;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment.NearbyPlaceListFragment;
import com.lakeel.altla.ghost.alpha.nearbysearch.view.fragment.PlaceFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(NearbyPlaceListFragment fragment);

    void inject(PlaceFragment fragment);
}
