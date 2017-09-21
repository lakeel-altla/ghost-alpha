package com.lakeel.altla.ghost.alpha.nearbysearch.place;

import java.util.List;

/**
 * @see <a href="https://developers.google.com/places/web-service/search#PlaceSearchResults">
 * Search Results - Google Place API</a>
 */
public final class Place {

    /**
     * Used only for a Place Details request.
     */
    public List<AddressComponent> addressComponents;

    public String formattedAddress;

    /**
     * Used only for a Place Details request.
     */
    public String formattedPhoneNumber;

    public Geometry geometry;

    public String icon;

    /**
     * Used only for a Place Details request.
     */
    public String internationalPhoneNumber;

    public String name;

    public OpeningHours openingHours;

    public boolean permanentlyClosed;

    /**
     * A Place Search will return at most one photo object.
     * Performing a Place Details request on the place may return up to ten photos.
     */
    public List<Photo> photos;

    public String placeId;

    public Scope scope;

    public List<AltId> altIds;

    public int priceLevel;

    public float rating;

    /**
     * Used only for a Place Details request.
     */
    public List<Review> reviews;

    public List<PlaceType> types;

    public String url;

    public int utcOffset;

    public String vicinity;

    public String website;
}
