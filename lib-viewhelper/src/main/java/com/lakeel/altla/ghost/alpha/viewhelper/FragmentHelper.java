package com.lakeel.altla.ghost.alpha.viewhelper;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import java.util.Objects;

public final class FragmentHelper {

    private FragmentHelper() {
    }

    @NonNull
    public static Context getRequiredContext(@NonNull Fragment fragment) {
        Context context = fragment.getContext();
        Objects.requireNonNull(context);
        return context;
    }

    @NonNull
    public static FragmentActivity getRequiredActivity(@NonNull Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        Objects.requireNonNull(activity);
        return activity;
    }

    @NonNull
    public static Bundle getRequiredArguments(@NonNull Fragment fragment) {
        Bundle arguments = fragment.getArguments();
        Objects.requireNonNull(arguments, "The fragment has no arguments.");
        return arguments;
    }

    @NonNull
    public static <T extends View> T findViewById(@NonNull Fragment fragment, @IdRes int id) {
        View root = fragment.getView();
        Objects.requireNonNull(root, "The fragment has no root view.");
        T view = root.findViewById(id);
        Objects.requireNonNull(view);
        return view;
    }
}
