package com.siliconvalleytrail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * WeatherService.java — Fetches real weather from the Open-Meteo API.
 *
 * WHY OPEN-METEO:
 * It's 100% free and requires NO API key — perfect for a take-home project
 * where you don't want to commit secrets to GitHub. Just pass lat/lon and get JSON.
 *
 * WHY TRY/CATCH:
 * Network calls can fail for many reasons (no wifi, API down, timeout).
 * If we let those failures crash the game, users would get a scary stack trace.
 * Instead, we "catch" any error and fall back to a mock weather object so the
 * game always continues. This is called "graceful degradation."
 *
 * This is also your first introduction to exception handling in Java —
 * a concept not yet covered in your CS111B course.
 */
public class WeatherService {

    private static final String API_BASE   = "https://api.open-meteo.com/v1/forecast";
    private static final int    TIMEOUT_MS = 5000;  // Give the API 5 seconds to respond

    /**
     * Main public method. Tries the live API; returns mock data if anything fails.
     *
     * @param latitude  GPS latitude of the current city
     * @param longitude GPS longitude of the current city
     * @return A Weather object (real or mocked)
     */
    public Weather getWeather(double latitude, double longitude) {
        try {
            // Build the URL with query parameters
            // temperature_unit=fahrenheit: ask the API to give us Fahrenheit directly
            String urlString = API_BASE
                    + "?latitude="          + latitude
                    + "&longitude="         + longitude
                    + "&current_weather=true"
                    + "&temperature_unit=fahrenheit";

            // Open a connection (like opening a browser tab to that URL)
            // URI.create().toURL() is the modern replacement for deprecated new URL(String)
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 200 = HTTP "OK". Anything else means the API returned an error.
            if (connection.getResponseCode() != 200) {
                return getMockWeather();
            }

            // Read the response line by line and combine into one string
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parseWeatherJson(response.toString());

        } catch (Exception e) {
            // Something went wrong (no internet, timeout, etc.) — use mock data
            return getMockWeather();
        }
    }

    /**
     * Parses the relevant fields from the Open-Meteo JSON response.
     *
     * WHY NO JSON LIBRARY:
     * Adding a full JSON library (like Gson or Jackson) would mean adding a
     * dependency to pom.xml and explaining it. The response is simple enough
     * that we can extract just the two values we need with String operations.
     *
     * Example JSON we receive:
     * {"latitude":37.34,"longitude":-121.89,"current_weather":{"temperature":68.5,"windspeed":10.2,"weathercode":3,...}}
     *
     * Package-private (no "public") so tests in the same package can call it directly.
     */
    Weather parseWeatherJson(String json) {
        try {
            double temperature = extractDouble(json, "temperature");
            int    weatherCode = (int) extractDouble(json, "weathercode");
            return new Weather(temperature, weatherCode);
        } catch (Exception e) {
            // If parsing fails for any reason, fall back gracefully
            return getMockWeather();
        }
    }

    /**
     * Finds a numeric value in JSON by its field name.
     *
     * Example: extractDouble("{\"temperature\":68.5,\"foo\":1}", "temperature")
     *          returns 68.5
     *
     * We walk character by character from the field name's position until
     * we've consumed all digits (and the decimal point if present).
     */
    private double extractDouble(String json, String fieldName) {
        String key   = "\"" + fieldName + "\":";
        int    start = json.indexOf(key) + key.length();

        // Skip any spaces after the colon
        while (start < json.length() && json.charAt(start) == ' ') {
            start++;
        }

        // Walk forward to find the end of the number
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (Character.isDigit(c) || c == '.' || c == '-') {
                end++;
            } else {
                break;
            }
        }

        return Double.parseDouble(json.substring(start, end));
    }

    /**
     * Returns a realistic mock weather object for the Bay Area.
     * Used when the API is unavailable (offline, timeout, etc.).
     * Random so the game has variety even in offline mode.
     */
    public Weather getMockWeather() {
        int[]    codes = { 0,    1,    2,    3,    45,   61,   63  };
        double[] temps = { 72.0, 68.0, 65.0, 62.0, 58.0, 57.0, 54.0 };
        int index = (int) (Math.random() * codes.length);
        return new Weather(temps[index], codes[index]);
    }
}
