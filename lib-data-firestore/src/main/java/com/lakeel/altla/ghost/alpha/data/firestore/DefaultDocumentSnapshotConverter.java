package com.lakeel.altla.ghost.alpha.data.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

import android.support.annotation.NonNull;

public class DefaultDocumentSnapshotConverter<T> implements DocumentSnapshotConverter<T> {

    private final Class<T> clazz;

    public DefaultDocumentSnapshotConverter(@NonNull Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convert(DocumentSnapshot snapshot) {
        return snapshot.toObject(clazz);
    }
}
