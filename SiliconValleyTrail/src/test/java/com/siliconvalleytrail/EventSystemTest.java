package com.siliconvalleytrail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * EventSystemTest.java — Tests for the event system and Event class.
 *
 * We test:
 * 1. That events are loaded (the event lists are not empty)
 * 2. That getRandomEvent() always returns something valid
 * 3. That applyOutcome() correctly changes the game state
 * 4. That weather events are correctly flagged as weather-dependent
 */
class EventSystemTest {

    private EventSystem eventSystem;
    private GameState   state;

    @BeforeEach
    void setUp() {
        eventSystem = new EventSystem();
        state       = new GameState();
    }

    @Test
    void testGeneralEventsAreLoaded() {
        assertFalse(eventSystem.getGeneralEvents().isEmpty(),
                "General events list should not be empty");
    }

    @Test
    void testWeatherEventsAreLoaded() {
        assertFalse(eventSystem.getWeatherEvents().isEmpty(),
                "Weather events list should not be empty");
    }

    @Test
    void testGetRandomEventAlwaysReturnsNonNull() {
        Weather weather = new Weather(65.0, 0);  // Clear sky, 65°F
        // Run it many times to make sure randomness never produces null
        for (int i = 0; i < 50; i++) {
            Event event = eventSystem.getRandomEvent(state, weather);
            assertNotNull(event, "getRandomEvent() must never return null");
        }
    }

    @Test
    void testEventsAlwaysHaveAtLeastOneChoice() {
        Weather weather = new Weather(65.0, 0);
        for (int i = 0; i < 20; i++) {
            Event event = eventSystem.getRandomEvent(state, weather);
            assertTrue(event.getNumberOfChoices() >= 1,
                    "Every event must have at least 1 choice");
        }
    }

    @Test
    void testApplyOutcomeChangesState() {
        // Build a simple event manually so the test doesn't depend on event ordering
        Event testEvent = new Event("Test Event", "A test event with predictable effects.");
        testEvent.addChoice(
                "Spend $2000 and lose 8 coffee",
                -2000, 0, -8, 0, 0,
                "Done."
        );

        int cashBefore   = state.getCash();
        int coffeeBefore = state.getCoffee();

        testEvent.applyOutcome(state, 0);  // choice index 0

        assertEquals(cashBefore   - 2000, state.getCash(),   "Cash should decrease by 2000");
        assertEquals(coffeeBefore - 8,    state.getCoffee(), "Coffee should decrease by 8");
    }

    @Test
    void testApplyOutcomeWithPositiveEffects() {
        Event testEvent = new Event("Good Event", "A lucky break.");
        testEvent.addChoice(
                "Gain morale and hype",
                0, 10, 0, 15, 0,
                "Great!"
        );

        int moraleBefore = state.getMorale();
        int hypeBefore   = state.getHype();

        testEvent.applyOutcome(state, 0);

        // Morale is already 100; +10 should clamp at MAX_MORALE (100)
        assertEquals(GameState.MAX_MORALE, state.getMorale(), "Morale should clamp at max");
        assertEquals(hypeBefore + 15,      state.getHype(),   "Hype should increase by 15");
    }

    @Test
    void testApplyOutcomeOutOfBoundsDoesNothing() {
        Event testEvent = new Event("Test", "Test");
        testEvent.addChoice("Only choice", 0, 0, 0, 0, 0, "Done");

        int cashBefore = state.getCash();
        testEvent.applyOutcome(state, 99);  // Invalid index — should do nothing
        assertEquals(cashBefore, state.getCash(), "Out-of-bounds choice should not change state");
    }

    @Test
    void testAllWeatherEventsAreMarkedAsWeatherDependent() {
        for (Event e : eventSystem.getWeatherEvents()) {
            assertTrue(e.isWeatherDependent(),
                    "Every event in weatherEvents must be marked weather-dependent");
        }
    }

    @Test
    void testGeneralEventsAreNotWeatherDependent() {
        for (Event e : eventSystem.getGeneralEvents()) {
            assertFalse(e.isWeatherDependent(),
                    "General events must not be weather-dependent");
        }
    }

    @Test
    void testEventMatchesCorrectWeatherCondition() {
        // Find any rainy weather event and verify it matches "rainy" but not "sunny"
        Event rainyEvent = null;
        for (Event e : eventSystem.getWeatherEvents()) {
            if (e.matchesWeather("rainy")) {
                rainyEvent = e;
                break;
            }
        }
        assertNotNull(rainyEvent, "There should be at least one rainy weather event");
        assertTrue(rainyEvent.matchesWeather("rainy"),   "Should match 'rainy'");
        assertFalse(rainyEvent.matchesWeather("sunny"),  "Should not match 'sunny'");
        assertFalse(rainyEvent.matchesWeather("foggy"),  "Should not match 'foggy'");
    }
}
