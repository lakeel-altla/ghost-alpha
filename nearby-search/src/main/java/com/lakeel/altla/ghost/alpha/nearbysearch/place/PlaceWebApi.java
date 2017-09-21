package com.lakeel.altla.ghost.alpha.nearbysearch.place;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.DetailsResponse;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.PlaceWebService;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit.SearchResponse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public final class PlaceWebApi {

    private static final Log LOG = LogFactory.getLog(PlaceWebApi.class);

    private String key;

    private PlaceWebService service;

    public PlaceWebApi(@NonNull String key, @NonNull PlaceWebService service) {
        this.key = key;
        this.service = service;
    }

    @NonNull
    public List<Place> nearbySearch(double latitude, double longitude, int radius, @Nullable String language) {

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
                        nearbySearchByPageToken(searchResponse.nextPageToken, results);
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

    private void nearbySearchByPageToken(@NonNull String pageToken, @NonNull List<Place> outResults) {

        Call<SearchResponse> call = service.nearbySearchByPageToken(key, pageToken);

        try {
            Response<SearchResponse> response = call.execute();
            SearchResponse searchResponse = response.body();
            if (searchResponse == null) throw new PlaceWebApiException("A response body is null.");

            switch (searchResponse.status) {
                case OK:
                    outResults.addAll(searchResponse.results);
                    if (searchResponse.nextPageToken != null) {
                        nearbySearchByPageToken(searchResponse.nextPageToken, outResults);
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
    public Place details(@NonNull String placeId, @Nullable String language) {

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
