package com.lakeel.altla.ghost.alpha.data.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

public interface DocumentSnapshotConverter<T> {

    T convert(DocumentSnapshot snapshot);
}
