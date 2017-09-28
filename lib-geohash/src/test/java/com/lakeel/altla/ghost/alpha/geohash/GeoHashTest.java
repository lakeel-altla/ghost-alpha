package com.lakeel.altla.ghost.alpha.geohash;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class GeoHashTest {

    @Test
    public void encode() {
        String actual = GeoHash.encode(20, 31, 12);
        assertEquals("sew1c2vs2q5r", actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithLatitudeLessThanMin() {
        GeoHash.encode(-100, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithLatitudeGreaterThanMax() {
        GeoHash.encode(100, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithLongitudeLessThanMin() {
        GeoHash.encode(0, -200, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithLongitudeGreaterThanMax() {
        GeoHash.encode(0, 200, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithPrecisionLessThanMin() {
        GeoHash.encode(0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithPrecisionGreaterThanMax() {
        GeoHash.encode(0, 200, 23);
    }

    @Test
    public void decode() {
        double delta = 0.00001d;
        GeoHash.LatLng actual = GeoHash.decode("sew1c2vs2q5r");
        assertEquals(20, actual.latitude, delta);
        assertEquals(31, actual.longitude, delta);
    }
}
