package me.suszczewicz.util;

public class GPSUtil {

    public static boolean validateLatitude(String lat) {
        try {
            Float f = Float.parseFloat(lat);

            return f >= -90 && f <= 90;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static boolean validateLongitude(String longitude) {
        try {
            Float f = Float.parseFloat(longitude);

            return f >= -180 && f <= 180;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }
}
