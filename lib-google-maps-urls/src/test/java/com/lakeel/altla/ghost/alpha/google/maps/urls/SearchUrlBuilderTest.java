package com.lakeel.altla.ghost.alpha.google.maps.urls;

import org.junit.Assert;
import org.junit.Test;

public class SearchUrlBuilderTest {

    @Test
    public void buildWithKeyword() {
        String expected = "https://www.google.com/maps/search/?api=1&query=ab%26cd+ef";
        String actual = new SearchUrlBuilder("ab&cd ef").build();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildWithLatitudeLongitude() {
        String expected = "https://www.google.com/maps/search/?api=1&query=34.1742634,-86.616382";
        String actual = new SearchUrlBuilder(34.1742634, -86.616382).build();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildWithLatitudeLongitudePlaceId() {
        String expected =
                "https://www.google.com/maps/search/?api=1&query=34.1742634,-86.616382&query_place_id=ChIJu3PgzXbBiYgRnn_bNlEcEVw";
        String actual =
                new SearchUrlBuilder(34.1742634, -86.616382).setPlaceId("ChIJu3PgzXbBiYgRnn_bNlEcEVw").build();
        Assert.assertEquals(expected, actual);
    }
}
