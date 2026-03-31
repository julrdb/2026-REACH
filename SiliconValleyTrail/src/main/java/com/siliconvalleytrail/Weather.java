package com.siliconvalleytrail;

/**
 * Weather.java — Stores the current weather and calculates its gameplay effects.
 *
 * WHY THIS EXISTS:
 * We could just use raw numbers (an int for temperature, an int for a code)
 * everywhere. But that would mean scattering weather-effect calculations all over
 * the codebase. Instead, this class is the single place that knows:
 *   "if it's raining, what does that mean for morale, coffee, and bugs?"
 *
 * This is called the "single responsibility principle" — one class, one job.
 *
 * The weather code comes directly from the Open-Meteo API (WMO standard codes).
 * See: https://open-meteo.com/en/docs#weathervariables
 */
public class Weather {

    private double temperature;  // Fahrenheit
    private int    weatherCode;  // WMO code from Open-Meteo API
    private String condition;    // Derived label: "sunny", "rainy", "foggy", etc.
    private String description;  // Human-readable: "Moderate rain", "Clear sky", etc.

    public Weather(double temperature, int weatherCode) {
        this.temperature = temperature;
        this.weatherCode = weatherCode;
        this.condition   = conditionFromCode(weatherCode);
        this.description = descriptionFromCode(weatherCode);
    }

    // =====================================================================
    // GAMEPLAY EFFECTS
    // These methods answer: "how much does today's weather hurt/help the team?"
    // Returning 0 means no effect. Returning 5 means lose 5 of that resource.
    // =====================================================================

    /** Bad weather kills morale. Extreme heat is also rough. */
    public int getMoralePenalty() {
        if (condition.equals("stormy") || condition.equals("rainy"))  return 8;
        if (condition.equals("showery"))                               return 5;
        if (condition.equals("foggy"))                                 return 3;
        if (temperature > 92)                                          return 10; // Heat wave
        return 0;
    }

    /** Cold and rainy days make everyone drink more coffee. */
    public int getCoffeePenalty() {
        if (temperature < 50)                                          return 3;
        if (condition.equals("rainy") || condition.equals("stormy"))   return 2;
        return 0;
    }

    /** Distracted teams write more bugs on bad weather days. */
    public int getBugPenalty() {
        if (condition.equals("stormy"))                                return 2;
        if (condition.equals("rainy"))                                 return 1;
        if (temperature > 95)                                          return 1; // Heat-induced bugs
        return 0;
    }

    // =====================================================================
    // DISPLAY HELPERS
    // =====================================================================

    /** Returns an emoji matching the weather for the status display. */
    public String getEmoji() {
        switch (condition) {
            case "sunny":   return temperature > 88 ? "☀️ " : "🌤 ";
            case "cloudy":  return "☁️ ";
            case "foggy":   return "🌫 ";
            case "rainy":   return "🌧 ";
            case "showery": return "🌦 ";
            case "stormy":  return "⛈ ";
            default:        return "🌤 ";
        }
    }

    // =====================================================================
    // PRIVATE HELPERS — convert API weather codes to readable strings
    // WMO code reference: 0=clear, 1-3=cloudy, 45/48=fog, 61-65=rain, 95=storm
    // =====================================================================

    private String conditionFromCode(int code) {
        if (code == 0)                           return "sunny";
        if (code <= 3)                           return "cloudy";
        if (code == 45 || code == 48)            return "foggy";
        if (code >= 51 && code <= 67)            return "rainy";
        if (code >= 80 && code <= 82)            return "showery";
        if (code == 95 || code == 96 || code == 99) return "stormy";
        return "cloudy";
    }

    private String descriptionFromCode(int code) {
        if (code == 0)  return "Clear sky";
        if (code == 1)  return "Mainly clear";
        if (code == 2)  return "Partly cloudy";
        if (code == 3)  return "Overcast";
        if (code == 45 || code == 48) return "Foggy";
        if (code == 51) return "Light drizzle";
        if (code == 53) return "Moderate drizzle";
        if (code == 61) return "Light rain";
        if (code == 63) return "Moderate rain";
        if (code == 65) return "Heavy rain";
        if (code == 80) return "Slight showers";
        if (code == 81) return "Moderate showers";
        if (code == 82) return "Heavy showers";
        if (code == 95) return "Thunderstorm";
        return "Cloudy";
    }

    // Getters
    public double getTemperature() { return temperature; }
    public int    getWeatherCode() { return weatherCode; }
    public String getCondition()   { return condition; }
    public String getDescription() { return description; }
}
