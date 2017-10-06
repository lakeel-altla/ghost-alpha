package com.lakeel.altla.ghost.alpha.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class CurrentUser {

    private static final CurrentUser INSTANCE = new CurrentUser();

    private CurrentUser() {
    }

    @NonNull
    public static CurrentUser getInstance() {
        return INSTANCE;
    }

    @Nullable
    public String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        } else {
            return user.getUid();
        }
    }

    @NonNull
    public String getRequiredUserId() {
        String userId = getUserId();
        if (userId == null) {
            throw new IllegalStateException("A current user is not authorized.");
        } else {
            return userId;
        }
    }
}
