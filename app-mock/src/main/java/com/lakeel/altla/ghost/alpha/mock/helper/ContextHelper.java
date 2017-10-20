package com.lakeel.altla.ghost.alpha.mock.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputMethodManager;

public final class ContextHelper {

    private ContextHelper() {
    }

    public static InputMethodManager getInputMethodManager(@NonNull Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
}
