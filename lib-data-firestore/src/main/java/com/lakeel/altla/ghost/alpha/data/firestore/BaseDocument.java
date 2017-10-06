package com.lakeel.altla.ghost.alpha.data.firestore;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class BaseDocument {

    private String key;

    private String userId;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    protected BaseDocument() {
    }

    @NonNull
    public String getKey() {
        if (key == null) {
            key = UUID.randomUUID().toString();
        }
        return key;
    }

    public void setKey(@Nullable String key) {
        this.key = key;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    @Exclude
    @NonNull
    public String getRequiredUserId() {
        if (userId == null) throw new IllegalStateException("'userId' is null.");
        return userId;
    }

    public void setUserId(@Nullable String userId) {
        this.userId = userId;
    }

    @Nullable
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@Nullable Date createdAt) {
        this.createdAt = createdAt;
    }

    @Nullable
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@Nullable Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseDocument that = (BaseDocument) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
