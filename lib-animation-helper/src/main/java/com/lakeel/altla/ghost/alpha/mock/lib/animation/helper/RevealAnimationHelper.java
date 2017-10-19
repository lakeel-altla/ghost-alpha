package com.lakeel.altla.ghost.alpha.mock.lib.animation.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewAnimationUtils;

import java.util.Objects;

public final class RevealAnimationHelper {

    private RevealAnimationHelper() {
    }

    public static void startCircularRevealAnimation(@NonNull final Context context,
                                                    @NonNull final View view,
                                                    @NonNull final RevealAnimationSettings animationSettings,
                                                    @ColorInt final int startColor,
                                                    @ColorInt final int endColor) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(view);
        Objects.requireNonNull(animationSettings);

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int duration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

                // Simply use the diagonal of the view.
                float finalRadius = (float) Math.sqrt(animationSettings.width * animationSettings.width + animationSettings.height * animationSettings.height);

                Animator anim = ViewAnimationUtils.createCircularReveal(v, animationSettings.centerX, animationSettings.centerY, 0, finalRadius).setDuration(duration);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.start();

                startColorAnimation(view, startColor, endColor, duration);
            }
        });
    }

    public static void startCircularExitAnimation(@NonNull Context context,
                                                  @NonNull View view,
                                                  @NonNull RevealAnimationSettings animationSettings,
                                                  @ColorInt int startColor,
                                                  @ColorInt int endColor,
                                                  @NonNull final OnDismissedListener listener) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(view);
        Objects.requireNonNull(animationSettings);
        Objects.requireNonNull(listener);

        int duration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

        float initRadius = (float) Math.sqrt(animationSettings.width * animationSettings.width + animationSettings.height * animationSettings.height);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, animationSettings.centerX, animationSettings.centerY, initRadius, 0);
        anim.setDuration(duration);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onDismissed();
            }
        });
        anim.start();

        startColorAnimation(view, startColor, endColor, duration);
    }

    private static void startColorAnimation(@NonNull final View view,
                                            @ColorInt int startColor,
                                            @ColorInt int endColor,
                                            @IntRange(from = 0) int duration) {
        Objects.requireNonNull(view);

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(startColor, endColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        anim.setDuration(duration);
        anim.start();
    }
}

