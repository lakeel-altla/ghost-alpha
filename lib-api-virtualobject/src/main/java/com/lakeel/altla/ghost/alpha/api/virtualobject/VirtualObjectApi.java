package com.lakeel.altla.ghost.alpha.api.virtualobject;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualObjectApi {

    private static final Log LOG = LogFactory.getLog(VirtualObjectApi.class);

    private static final String DATABASE_USER_GEO_FIRES = "userGeoFires";

    private static final String FIRESTORE_USER_OBJECTS = "userObjects";

    private static final double KILOMETERS_PER_METER = 1d / 1000d;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private final UserGeoFireFactory userGeoFireFactory;

    private final CollectionReference userObjectsReference;

    public VirtualObjectApi() {
        // TODO: Setup for the public data.
        userGeoFireFactory = new UserGeoFireFactory(database.getReference(DATABASE_USER_GEO_FIRES));
        userObjectsReference = firestore.collection(FIRESTORE_USER_OBJECTS);
    }

    public void saveUserObject(@NonNull VirtualObject object,
                               @Nullable OnSuccessListener<Void> onSuccessListener,
                               @Nullable OnFailureListener onFailureListener) {
        object.setUpdatedAt(null);

        GeoFire geoFire = userGeoFireFactory.create(object.getRequiredUserId());

        String key = object.getKey();

        GeoPoint geoPoint = object.getRequiredGeoPoint();
        GeoLocation geoLocation = new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude());

        geoFire.setLocation(key, geoLocation, (k, error) -> {
            if (error == null) {
                Task<Void> task = userObjectsReference.document(key).set(object);
                if (onSuccessListener != null) {
                    task.addOnSuccessListener(aVoid -> onSuccessListener.onSuccess(null));
                }
                if (onFailureListener != null) {
                    task.addOnFailureListener(onFailureListener::onFailure);
                }
            } else {
                Exception e = error.toException();
                LOG.e("Failed to set a location: key = " + key, e);
                if (onFailureListener != null) onFailureListener.onFailure(e);
            }
        });
    }

    public void findUserObject(@NonNull String userId,
                               @NonNull String key,
                               @Nullable OnSuccessListener<VirtualObject> onSuccessListener,
                               @Nullable OnFailureListener onFailureListener) {
        GeoFire geoFire = userGeoFireFactory.create(userId);
        geoFire.getLocation(key, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location == null) {
                    LOG.e("No location exists: key = " + key);
                } else {
                    userObjectsReference
                            .document(key)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (onSuccessListener != null) {
                                    if (snapshot.exists()) {
                                        onSuccessListener.onSuccess(snapshot.toObject(VirtualObject.class));
                                    } else {
                                        onSuccessListener.onSuccess(null);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (onFailureListener != null) onFailureListener.onFailure(e);
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LOG.e("Failed to get a location.", error.toException());
            }
        });
    }

    public void findUserObjects(@NonNull String userId,
                                @Nullable OnSuccessListener<List<VirtualObject>> onSuccessListener,
                                @Nullable OnFailureListener onFailureListener) {
        userObjectsReference
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (onSuccessListener != null) {
                        List<VirtualObject> results = new ArrayList<>(snapshots.size());
                        for (DocumentSnapshot snapshot : snapshots) {
                            results.add(snapshot.toObject(VirtualObject.class));
                        }
                        onSuccessListener.onSuccess(results);
                    }
                })
                .addOnFailureListener(e -> {
                    if (onFailureListener != null) onFailureListener.onFailure(e);
                });
    }

    @NonNull
    public ObjectQuery queryUserObjects(@NonNull String userId, GeoPoint center, int radius) {
        GeoFire geoFire = userGeoFireFactory.create(userId);

        GeoLocation geoLocation = new GeoLocation(center.getLatitude(), center.getLongitude());
        GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation, ((double) radius) * KILOMETERS_PER_METER);

        return new ObjectQuery(geoQuery);
    }

    public void deleteUserObjects(@NonNull String userId, @NonNull String key,
                                  @Nullable OnSuccessListener<Void> onSuccessListener,
                                  @Nullable OnFailureListener onFailureListener) {

        GeoFire geoFire = userGeoFireFactory.create(userId);
        geoFire.removeLocation(key, (k, error) -> {
            if (error == null) {
                userObjectsReference.document(key)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (onFailureListener != null) onFailureListener.onFailure(e);
                                    });
            } else {
                Exception e = error.toException();
                LOG.e("Failed to delete a location: key = " + key, e);
                if (onFailureListener != null) onFailureListener.onFailure(e);
            }
        });
    }

    public interface OnSuccessListener<T> {

        void onSuccess(T result);
    }

    public interface OnFailureListener {

        void onFailure(Exception e);
    }

    public interface ObjectQueryEventListener {

        void onObjectEntered(VirtualObject object);

        void onObjectExited(String key);

        void onObjectQueryReady();

        void onObjectQueryError(Exception e);
    }

    public class ObjectQuery {

        private final GeoQuery geoQuery;

        private final Map<ObjectQueryEventListener, GeoQueryEventListener> listenerMap = new HashMap<>();

        private ObjectQuery(@NonNull GeoQuery geoQuery) {
            this.geoQuery = geoQuery;
        }

        public void addObjectQueryEventListener(@NonNull ObjectQueryEventListener listener) {
            GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    userObjectsReference.document(key)
                                        .get()
                                        .addOnSuccessListener(snapshot -> {
                                            VirtualObject object = snapshot.toObject(VirtualObject.class);
                                            listener.onObjectEntered(object);
                                        })
                                        .addOnFailureListener(listener::onObjectQueryError);
                }

                @Override
                public void onKeyExited(String key) {
                    listener.onObjectExited(key);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    // Ignore.
                }

                @Override
                public void onGeoQueryReady() {
                    listener.onObjectQueryReady();
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    listener.onObjectQueryError(error.toException());
                }
            };
            listenerMap.put(listener, geoQueryEventListener);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        }

        public void removeObjectQueryEventListener(@NonNull ObjectQueryEventListener listener) {
            GeoQueryEventListener geoQueryEventListener = listenerMap.remove(listener);
            geoQuery.removeGeoQueryEventListener(geoQueryEventListener);
        }

        public void setCenter(@NonNull GeoPoint center) {
            geoQuery.setCenter(new GeoLocation(center.getLatitude(), center.getLongitude()));
        }

        public void setRadius(long radius) {
            geoQuery.setRadius(radius * KILOMETERS_PER_METER);
        }
    }

    private class UserGeoFireFactory {

        final DatabaseReference userGeoFiresReference;

        UserGeoFireFactory(DatabaseReference userGeoFiresReference) {
            this.userGeoFiresReference = userGeoFiresReference;
        }

        @NonNull
        GeoFire create(@NonNull String userId) {
            return new GeoFire(userGeoFiresReference.child(userId));
        }
    }
}
