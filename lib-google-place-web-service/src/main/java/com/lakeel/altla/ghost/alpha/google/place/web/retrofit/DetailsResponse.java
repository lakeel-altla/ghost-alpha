package com.lakeel.altla.ghost.alpha.google.place.web.retrofit;


import com.lakeel.altla.ghost.alpha.google.place.web.Place;

public final class DetailsResponse {

    public Status status;

    public Place result;

    public enum Status {
        // Indicates an unknown value that is defined on Place API, but undefined on this app.
        UNDEFINED,
        OK,
        UNKNOWN_ERROR,
        ZERO_RESULTS,
        OVER_QUERY_LIMIT,
        REQUEST_DENIED,
        INVALID_REQUEST,
        NOT_FOUND
    }
}
