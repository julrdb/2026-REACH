package com.siliconvalleytrail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WeatherServiceTest.java — Tests for the weather API and Weather class.
 *
 * KEY TESTING PHILOSOPHY HERE:
 * We can't reliably test "does the API return the right weather?" because the
 * weather changes and we'd need a live internet connection. Instead, we test:
 *
 * 1. The JSON parsing logic (parseWeatherJson) with a known JSON string
 * 2. The fallback (getMockWeather) returns valid data
 * 3. The Weather class calculates penalties correctly for various conditions
 *
 * This is called "testing the seams" — we isolate the parts we control.
 */
class WeatherServiceTest {

    private WeatherService service;

    @BeforeEach
    void setUp() {
        service = new WeatherService();
    }

    // =====================================================================
    // WeatherService: JSON Parsing
    // =====================================================================

    @Test
    void testParseValidJson() {
        // This is what the actual Open-Meteo API returns
        String json = "{\"latitude\":37.34,\"longitude\":-121.89,"
                + "\"current_weather\":{\"temperature\":68.5,"
                + "\"windspeed\":10.2,\"weathercode\":2}}";

        Weather w = service.parseWeatherJson(json);

        assertNotNull(w);
        assertEquals(68.5, w.getTemperature(), 0.01, "Temperature should parse correctly");
        assertEquals(2,    w.getWeatherCode(),        "Weather code should parse correctly");
    }

    @Test
    void testParseJsonWithRainyCode() {
        String json = "{\"current_weather\":{\"temperature\":55.0,\"weathercode\":63}}";
        Weather w = service.parseWeatherJson(json);
        assertEquals(55.0,   w.getTemperature(), 0.01);
        assertEquals("rainy", w.getCondition());
    }

    @Test
    void testParseInvalidJsonFallsBackGracefully() {
        // If JSON is garbage, we should get mock data — NOT a crash
        Weather w = service.parseWeatherJson("this is not json at all {{{}");
        assertNotNull(w, "Should return mock weather instead of crashing");
    }

    @Test
    void testParseEmptyStringFallsBackGracefully() {
        Weather w = service.parseWeatherJson("");
        assertNotNull(w, "Empty JSON should fall back to mock weather");
    }

    // =====================================================================
    // WeatherService: Mock Fallback
    // =====================================================================

    @Test
    void testMockWeatherIsAlwaysValid() {
        // Run it 20 times to check randomness never breaks it
        for (int i = 0; i < 20; i++) {
            Weather w = service.getMockWeather();
            assertNotNull(w);
            assertTrue(w.getTemperature() > 0,   "Temperature should be positive");
            assertNotNull(w.getCondition(),        "Condition must not be null");
            assertNotNull(w.getDescription(),      "Description must not be null");
        }
    }

    // =====================================================================
    // Weather: Condition Labels (from WMO codes)
    // =====================================================================

    @Test
    void testClearSkyIsSunny() {
        assertEquals("sunny", new Weather(70.0, 0).getCondition());
    }

    @Test
    void testOvercastIsCloudy() {
        assertEquals("cloudy", new Weather(65.0, 3).getCondition());
    }

    @Test
    void testFogCode() {
        assertEquals("foggy", new Weather(58.0, 45).getCondition());
        assertEquals("foggy", new Weather(58.0, 48).getCondition());
    }

    @Test
    void testRainCodes() {
        assertEquals("rainy", new Weather(57.0, 61).getCondition());
        assertEquals("rainy", new Weather(55.0, 63).getCondition());
        assertEquals("rainy", new Weather(54.0, 65).getCondition());
    }

    @Test
    void testShoweryCode() {
        assertEquals("showery", new Weather(60.0, 80).getCondition());
        assertEquals("showery", new Weather(60.0, 81).getCondition());
    }

    @Test
    void testThunderstormCode() {
        assertEquals("stormy", new Weather(62.0, 95).getCondition());
    }

    // =====================================================================
    // Weather: Gameplay Penalty Calculations
    // =====================================================================

    @Test
    void testRainyWeatherHasMoralePenalty() {
        Weather rainy = new Weather(58.0, 63);  // Moderate rain
        assertTrue(rainy.getMoralePenalty() > 0, "Rain should reduce morale");
    }

    @Test
    void testStormyWeatherHasHighMoralePenalty() {
        Weather stormy = new Weather(60.0, 95);
        assertTrue(stormy.getMoralePenalty() > 0);
    }

    @Test
    void testClearMildWeatherHasNoPenalty() {
        Weather nice = new Weather(72.0, 1);  // Mild and clear
        assertEquals(0, nice.getMoralePenalty(), "Nice weather should not penalize morale");
        assertEquals(0, nice.getCoffeePenalty(), "Nice weather should not increase coffee use");
        assertEquals(0, nice.getBugPenalty(),    "Nice weather should not cause bugs");
    }

    @Test
    void testHeatWaveHasMoralePenalty() {
        Weather hot = new Weather(95.0, 0);  // Sunny and extremely hot
        assertTrue(hot.getMoralePenalty() > 0, "Extreme heat should penalize morale");
    }

    @Test
    void testColdWeatherIncreasesCoffeeUse() {
        Weather cold = new Weather(45.0, 2);  // Cold and cloudy
        assertTrue(cold.getCoffeePenalty() > 0, "Cold weather should increase coffee consumption");
    }

    @Test
    void testStormyWeatherIncreasesBugs() {
        Weather stormy = new Weather(60.0, 95);
        assertTrue(stormy.getBugPenalty() > 0, "Storms should distract the team and cause bugs");
    }

    // =====================================================================
    // Weather: Emoji Display
    // =====================================================================

    @Test
    void testEmojiIsNotNull() {
        // Verify every known condition has an emoji (no switch fallthrough returning null)
        String[] codes_to_test = new String[]{ "sunny", "cloudy", "foggy", "rainy", "showery", "stormy" };
        // We test by creating weathers with matching codes
        int[] weatherCodes = { 0, 3, 45, 63, 81, 95 };
        for (int code : weatherCodes) {
            Weather w = new Weather(65.0, code);
            assertNotNull(w.getEmoji(), "Every weather type should have an emoji");
        }
    }
}
