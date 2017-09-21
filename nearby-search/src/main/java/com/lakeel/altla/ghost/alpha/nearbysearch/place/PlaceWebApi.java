package com.lakeel.altla.ghost.alpha.nearbysearch.place;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.DetailsResponse;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.DetailsResponseStatusHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.PlaceTypeHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.PlaceWebService;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.ScopeHandler;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.SearchResponse;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.SearchResponseStatusHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PlaceWebApi {

    private static final Log LOG = LogFactory.getLog(PlaceWebApi.class);

    private static final String BASE_URL_MAPS_API = "https://maps.googleapis.com/maps/api/";

    private String key;

    private Gson gson;

    private PlaceWebService service;

    public PlaceWebApi(@NonNull String key, @NonNull OkHttpClient httpClient) {
        this.key = key;

        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(PlaceType.class, PlaceTypeHandler.INSTANCE)
                .registerTypeAdapter(Scope.class, ScopeHandler.INSTANCE)
                .registerTypeAdapter(SearchResponse.Status.class, SearchResponseStatusHandler.INSTANCE)
                .registerTypeAdapter(DetailsResponse.Status.class, DetailsResponseStatusHandler.INSTANCE)
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL_MAPS_API)
                                                  .client(httpClient)
                                                  .addConverterFactory(GsonConverterFactory.create(gson))
                                                  .build();

        this.service = retrofit.create(PlaceWebService.class);
    }

    @NonNull
    public Gson getGson() {
        return gson;
    }

    @NonNull
    public List<Place> searchPlaces(double latitude, double longitude, int radius, @Nullable String language) {

        String location = latitude + "," + longitude;
        if (language == null) language = Locale.getDefault().getLanguage();

        Call<SearchResponse> call = service.nearbySearch(key, location, radius, language);

        try {
            Response<SearchResponse> response = call.execute();
            SearchResponse searchResponse = response.body();
            if (searchResponse == null) throw new PlaceWebApiException("A response body is null.");

            switch (searchResponse.status) {
                case OK:
                    if (searchResponse.nextPageToken == null) {
                        return new ArrayList<>(searchResponse.results);
                    } else {
                        List<Place> results = new ArrayList<>();
                        results.addAll(searchResponse.results);
                        fetchNextPage(searchResponse.nextPageToken, results);
                        return results;
                    }
                case ZERO_RESULTS:
                    return Collections.emptyList();
                case INVALID_REQUEST:
                case REQUEST_DENIED:
                case OVER_QUERY_LIMIT:
                case UNDEFINED:
                default:
                    LOG.e("Received an error status: %s", searchResponse.status);
                    return Collections.emptyList();
            }
        } catch (IOException e) {
            throw new PlaceWebApiException(e);
        }
    }

    private void fetchNextPage(@NonNull String pageToken, @NonNull List<Place> outResults) {

        Call<SearchResponse> call = service.nearbySearchByPageToken(key, pageToken);

        try {
            Response<SearchResponse> response = call.execute();
            SearchResponse searchResponse = response.body();
            if (searchResponse == null) throw new PlaceWebApiException("A response body is null.");

            switch (searchResponse.status) {
                case OK:
                    outResults.addAll(searchResponse.results);
                    if (searchResponse.nextPageToken != null) {
                        fetchNextPage(searchResponse.nextPageToken, outResults);
                    }
                    break;
                case ZERO_RESULTS:
                    break;
                // Ignore INVALID_REQUEST because Place API with the query 'pagetoken' sometimes return it
                // even though a request is valid.
                case INVALID_REQUEST:
                case REQUEST_DENIED:
                case OVER_QUERY_LIMIT:
                case UNDEFINED:
                default:
                    LOG.e("Received an error status: %s", searchResponse.status);
                    break;
            }
        } catch (IOException e) {
            throw new PlaceWebApiException(e);
        }
    }

    @Nullable
    public Place getPlace(@NonNull String placeId, @Nullable String language) {

        if (language == null) language = Locale.getDefault().getLanguage();

        Call<DetailsResponse> call = service.details(key, placeId, language);

        try {
            Response<DetailsResponse> response = call.execute();
            DetailsResponse detailsResponse = response.body();
            if (detailsResponse == null) throw new PlaceWebApiException("A response body is null.");

            switch (detailsResponse.status) {
                case OK:
                case ZERO_RESULTS:
                    break;
                case UNKNOWN_ERROR:
                case INVALID_REQUEST:
                case REQUEST_DENIED:
                case OVER_QUERY_LIMIT:
                case NOT_FOUND:
                case UNDEFINED:
                default:
                    LOG.e("Received an error status: %s", detailsResponse.status);
                    break;
            }

            return detailsResponse.result;
        } catch (IOException e) {
            throw new PlaceWebApiException(e);
        }
    }

    public class PlaceWebApiException extends RuntimeException {

        public PlaceWebApiException(String message) {
            super(message);
        }

        public PlaceWebApiException(Throwable cause) {
            super(cause);
        }
    }

    public final class DetailsStatusException extends PlaceWebApiException {

        @NonNull
        public final DetailsResponse.Status status;

        private DetailsStatusException(@NonNull DetailsResponse.Status status) {
            super(status.name());
            this.status = status;
        }
    }
}
