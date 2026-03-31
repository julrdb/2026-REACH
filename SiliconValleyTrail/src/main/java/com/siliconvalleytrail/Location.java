package com.siliconvalleytrail;

/**
 * Location.java — Represents one stop on the Silicon Valley Trail.
 *
 * WHY THIS EXISTS:
 * We need to know each city's name, a flavor description, and its GPS coordinates
 * so we can pass the lat/lon to the weather API. Bundling these together in one
 * class is cleaner than having four parallel arrays (names[], descriptions[], lats[], lons[]).
 *
 * This is a simple "data class" — it has no behavior, just data and getters.
 * In your CS111B assignments you wrote similar classes (Team, Competition, Vehicle).
 *
 * The shortCode ("SJ", "SF") is used to draw the ASCII map.
 */
public class Location {

    private String name;
    private String description;
    private double latitude;   // GPS latitude  (e.g. 37.3382)
    private double longitude;  // GPS longitude (e.g. -121.8863)
    private String shortCode;  // 2-3 char label for the map display

    public Location(String name, String description,
                    double latitude, double longitude, String shortCode) {
        this.name        = name;
        this.description = description;
        this.latitude    = latitude;
        this.longitude   = longitude;
        this.shortCode   = shortCode;
    }

    // Getters
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }
    public String getShortCode()   { return shortCode; }
}
