package com.lakeel.altla.ghost.alpha.mock.view.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

public class FabBehavior extends FloatingActionButton.Behavior {

    public FabBehavior(Context context, AttributeSet attrs) {
        super();
    }

    private boolean animating = false;

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target,
                                       @ViewCompat.ScrollAxis int nestedScrollAxes, @ViewCompat.NestedScrollType int type) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child,
                               @NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        if (!animating) {
            if (dyConsumed > 0) {
                animateHide(child);
            } else {
                animateShow(child);
            }
        }
    }

    private void animateShow(FloatingActionButton child) {
        ViewCompat.animate(child).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                .setInterpolator(new FastOutSlowInInterpolator());
    }

    private void animateHide(FloatingActionButton child) {
        ViewCompat.animate(child).scaleX(0.0F).scaleY(0.0F).alpha(0.0F)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        animating = true;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        animating = false;
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        animating = false;
                    }
                });
    }
}
