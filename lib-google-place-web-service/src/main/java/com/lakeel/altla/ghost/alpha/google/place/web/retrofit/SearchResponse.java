package com.lakeel.altla.ghost.alpha.google.place.web.retrofit;

import com.lakeel.altla.ghost.alpha.google.place.web.Place;

import java.util.List;

/**
 * @see <a href="https://developers.google.com/places/web-service/search#PlaceSearchResults">
 * Search Results - Google Place API</a>
 */
public final class SearchResponse {

    public Status status;

    public String nextPageToken;

    public List<Place> results;

    public enum Status {
        // Indicates an unknown value that is defined on Place API, but undefined on this app.
        UNDEFINED,
        OK,
        ZERO_RESULTS,
        OVER_QUERY_LIMIT,
        REQUEST_DENIED,
        INVALID_REQUEST
    }
}
