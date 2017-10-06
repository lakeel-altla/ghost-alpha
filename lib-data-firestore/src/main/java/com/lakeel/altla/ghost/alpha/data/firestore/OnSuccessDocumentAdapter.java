package com.lakeel.altla.ghost.alpha.data.firestore;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import android.support.annotation.NonNull;

public class OnSuccessDocumentAdapter<T> implements OnSuccessListener<DocumentSnapshot> {

    private final DocumentSnapshotConverter<T> converter;

    private final OnSuccessListener<T> onSuccessListener;

    public OnSuccessDocumentAdapter(@NonNull Class<T> clazz, @NonNull OnSuccessListener<T> onSuccessListener) {
        this(new DefaultDocumentSnapshotConverter<>(clazz), onSuccessListener);
    }

    public OnSuccessDocumentAdapter(@NonNull DocumentSnapshotConverter<T> converter,
                                    @NonNull OnSuccessListener<T> onSuccessListener) {
        this.converter = converter;
        this.onSuccessListener = onSuccessListener;
    }

    @Override
    public void onSuccess(DocumentSnapshot snapshot) {
        onSuccessListener.onSuccess(converter.convert(snapshot));
    }
}
