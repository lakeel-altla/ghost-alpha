package com.lakeel.altla.ghost.alpha.viewhelper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

public final class ToastHelper {

    private ToastHelper() {
    }

    public static void showShortToast(@NonNull Context context, @StringRes int resId) {
        makeShortToast(context, resId).show();
    }

    public static void showShortToast(@NonNull Context context, @NonNull CharSequence text) {
        makeShortToast(context, text).show();
    }

    public static void showLongToast(@NonNull Context context, @StringRes int resId) {
        makeLongToast(context, resId).show();
    }

    public static void showLongToast(@NonNull Context context, @NonNull CharSequence text) {
        makeLongToast(context, text).show();
    }

    @NonNull
    public static Toast makeShortToast(@NonNull Context context, @StringRes int resId) {
        return Toast.makeText(context, resId, Toast.LENGTH_SHORT);
    }

    @NonNull
    public static Toast makeShortToast(@NonNull Context context, @NonNull CharSequence text) {
        return Toast.makeText(context, text, Toast.LENGTH_SHORT);
    }

    @NonNull
    public static Toast makeLongToast(@NonNull Context context, @StringRes int resId) {
        return Toast.makeText(context, resId, Toast.LENGTH_LONG);
    }

    @NonNull
    public static Toast makeLongToast(@NonNull Context context, @NonNull CharSequence text) {
        return Toast.makeText(context, text, Toast.LENGTH_LONG);
    }
}
