package utils;


/**
 * Some geo-related utilities 
 * 
 * @author Martin Steiger 
 */ 
public class GeoUtils { 
 
    /**
     * WGS84 The flattening factor 1/f of the earth spheroid 
     */ 
    public static final double WGS84_EARTH_FLATTENING = 1.0 / 298.257223563; 
 
    /**
     * WGS84 The (transverse) major (equatorial) radius 
     */ 
    public static final double WGS84_EARTH_MAJOR = 6378137.0; 
 
    /**
     * WGS84 The polar semi-minor (conjugate) radius 
     */ 
    public static final double WGS84_EARTH_MINOR =  
            WGS84_EARTH_MAJOR * (1.0 - WGS84_EARTH_FLATTENING); 
 
    /**
     * The mean radius as defined by the International Union of Geodesy and 
     * Geophysics (IUGG) 
     */ 
    public static final double WGS84_MEAN_RADIUS =  
            (2 * WGS84_EARTH_MAJOR + WGS84_EARTH_MINOR) / 3.0; 
 
    /**
     * This uses the "haversine" formula to calculate the great-circle distance 
     * between two points � that is, the shortest distance over the earth's 
     * surface � giving an 'as-the-crow-flies' distance between the points 
     * 
     * @param lat1 latitude of point 1 
     * @param lon1 longitude of point 1 
     * @param lat2 latitude of point 2 
     * @param lon2 longitude of point 2 
     * @return distance in meters 
     */ 
    public static double computeDistance(double lat1, double lon1, double lat2, double lon2) { 
        double radius = 6371000; // 6371 kilometers == 3960 miles 
 
        double deltaLat = Math.toRadians(lat2 - lat1); 
        double deltaLon = Math.toRadians(lon2 - lon1); 
 
        // a is the square of half the chord length between the points 
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) 
                + Math.cos(Math.toRadians(lat1)) 
                * Math.cos(Math.toRadians(lat2)) * Math.sin(deltaLon / 2) 
                * Math.sin(deltaLon / 2); 
 
        // c is the angular distance in radians 
        double c = 2 * Math.asin(Math.min(1, Math.sqrt(a))); 
 
        return radius * c; 
    } 
}