package com.lakeel.altla.ghost.alpha.nearbysearch.place.model;

import java.util.List;

public final class OpeningHours {

    public boolean openNow;

    /**
     * Used only for a Place Details request.
     */
    public List<Period> periods;

    /**
     * Used only for a Place Details request.
     */
    public List<String> weekdayText;

}
