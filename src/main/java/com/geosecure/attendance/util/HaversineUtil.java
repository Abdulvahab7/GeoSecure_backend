package com.geosecure.attendance.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Single source of truth for the geofence distance calculation. Replaces
 * the old sp_mark_attendance stored procedure removed in Phase 1 - this
 * logic now lives ONLY here in the Java service layer.
 */
public final class HaversineUtil {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private HaversineUtil() {
    }

    /** Great-circle distance between two lat/lng points, in metres. */
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    public static BigDecimal distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double d = distanceMeters(lat1.doubleValue(), lon1.doubleValue(), lat2.doubleValue(), lon2.doubleValue());
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean withinRadius(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
        return distanceMeters(lat1, lon1, lat2, lon2) <= radiusMeters;
    }
}
