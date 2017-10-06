package com.lakeel.altla.ghost.alpha.data.firestore;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class OnSuccessQueryAdapter<T> implements OnSuccessListener<QuerySnapshot> {

    private final DocumentSnapshotConverter<T> converter;

    private final OnSuccessListener<List<T>> onSuccessListener;

    public OnSuccessQueryAdapter(@NonNull Class<T> clazz, @NonNull OnSuccessListener<List<T>> onSuccessListener) {
        this(new DefaultDocumentSnapshotConverter<>(clazz), onSuccessListener);
    }

    public OnSuccessQueryAdapter(@NonNull DocumentSnapshotConverter<T> converter,
                                 @NonNull OnSuccessListener<List<T>> onSuccessListener) {
        this.converter = converter;
        this.onSuccessListener = onSuccessListener;
    }

    @Override
    public void onSuccess(QuerySnapshot snapshots) {
        List<T> results = new ArrayList<>(snapshots.size());
        for (DocumentSnapshot snapshot : snapshots) {
            results.add(converter.convert(snapshot));
        }
        onSuccessListener.onSuccess(results);
    }
}
