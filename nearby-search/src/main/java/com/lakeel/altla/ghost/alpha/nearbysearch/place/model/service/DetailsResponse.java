package com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service;

import com.lakeel.altla.ghost.alpha.nearbysearch.place.model.Place;

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
