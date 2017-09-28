package com.lakeel.altla.ghost.alpha.geohash;

public final class GeoHash {

    private static final int MIN_PRECISION = 1;

    private static final int MAX_PRECISION = 22;

    private static final int BITS_PER_BASE32_CHAR = 5;

    private static final char[] BASE32_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm',
            'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'
    };

    private static final String BASE32_CHARS_STRING = "0123456789bcdefghjkmnpqrstuvwxyz";

    private static final int[] BIT_MASKS = { 16, 8, 4, 2, 1 };

    private static final double MIN_LATITUDE = -90;

    private static final double MAX_LATITUDE = 90;

    private static final double MIN_LONGITUDE = -180;

    private static final double MAX_LONGITUDE = 180;

    public static String encode(double latitude, double longitude, int precision) {
        if (latitude < MIN_LATITUDE || MAX_LATITUDE < latitude) {
            throw new IllegalArgumentException("'latitude' is out of range: latitude = " + latitude);
        }
        if (longitude < MIN_LONGITUDE || MAX_LONGITUDE < longitude) {
            throw new IllegalArgumentException("'longitude' is out of range: longitude = " + longitude);
        }
        if (precision < MIN_PRECISION || MAX_PRECISION < precision) {
            throw new IllegalArgumentException("'precision' is out of range: precision = " + precision);
        }

        double[] latitudeRange = { MIN_LATITUDE, MAX_LATITUDE };
        double[] longitudeRange = { MIN_LONGITUDE, MAX_LONGITUDE };

        boolean evenBit = true;
        char[] chars = new char[precision];

        for (int i = 0; i < precision; i++) {

            int bits = 0;

            for (int j = 0; j < BITS_PER_BASE32_CHAR; j++) {

                double value = evenBit ? longitude : latitude;
                double[] range = evenBit ? longitudeRange : latitudeRange;
                double mid = (range[0] + range[1]) / 2;

                if (mid <= value) {
                    bits = (bits << 1) + 1;
                    range[0] = mid;
                } else {
                    bits = (bits << 1);
                    range[1] = mid;
                }

                evenBit = !evenBit;
            }
            chars[i] = BASE32_CHARS[bits];
        }

        return new String(chars);
    }

    public static LatLng decode(String hash) {
        if (hash == null) throw new IllegalArgumentException("'hash' is null.");

        double[] latitudeRange = { MIN_LATITUDE, MAX_LATITUDE };
        double[] longitudeRange = { MIN_LONGITUDE, MAX_LONGITUDE };

        boolean evenBit = true;

        for (int i = 0; i < hash.length(); i++) {

            char c = hash.charAt(i);
            int bits = BASE32_CHARS_STRING.indexOf(c);

            for (int j = 0; j < BITS_PER_BASE32_CHAR; j++) {

                int mask = BIT_MASKS[j];
                boolean bitTrue = (bits & mask) != 0;
                double[] range = evenBit ? longitudeRange : latitudeRange;
                double mid = (range[0] + range[1]) / 2;

                if (bitTrue) {
                    range[0] = mid;
                } else {
                    range[1] = mid;
                }

                evenBit = !evenBit;
            }
        }

        double latitude = (latitudeRange[0] + latitudeRange[1]) * 0.5d;
        double longitude = (longitudeRange[0] + longitudeRange[1]) * 0.5d;

        return new LatLng(latitude, longitude);
    }

    public static final class LatLng {

        public final double latitude;

        public final double longitude;

        private LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
