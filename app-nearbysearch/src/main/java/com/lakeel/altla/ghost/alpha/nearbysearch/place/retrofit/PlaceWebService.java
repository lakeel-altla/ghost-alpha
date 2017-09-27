package com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceWebService {

    @GET("place/nearbysearch/json")
    Call<SearchResponse> nearbySearch(@NonNull @Query("key") String key,
                                      @NonNull @Query("location") String location,
                                      @Query("radius") int radius,
                                      @Nullable @Query("language") String language);

    @GET("place/nearbysearch/json")
    Call<SearchResponse> nearbySearchByPageToken(@NonNull @Query("key") String key,
                                                 @NonNull @Query("pagetoken") String pageToken);

    @GET("place/details/json")
    Call<DetailsResponse> details(@NonNull @Query("key") String key,
                                  @NonNull @Query("placeid") String placeId,
                                  @Nullable @Query("language") String language);
}
