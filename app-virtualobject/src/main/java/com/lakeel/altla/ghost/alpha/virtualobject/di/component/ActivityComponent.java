package com.lakeel.altla.ghost.alpha.virtualobject.di.component;

import com.lakeel.altla.ghost.alpha.virtualobject.di.ActivityScope;
import com.lakeel.altla.ghost.alpha.virtualobject.di.module.ActivityModule;
import com.lakeel.altla.ghost.alpha.virtualobject.view.activity.MainActivity;
import com.lakeel.altla.ghost.alpha.virtualobject.view.activity.ShareActivity;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectEditFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectListFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.MyObjectViewFragment;
import com.lakeel.altla.ghost.alpha.virtualobject.view.fragment.NearbyObjectListFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(ShareActivity activity);

    void inject(NearbyObjectListFragment fragment);

    void inject(MyObjectListFragment fragment);

    void inject(MyObjectViewFragment fragment);

    void inject(MyObjectEditFragment fragment);
}
