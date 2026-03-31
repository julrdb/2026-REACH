package com.siliconvalleytrail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameStateTest.java — Unit tests for the GameState class.
 *
 * WHAT IS A UNIT TEST?
 * A unit test checks that one small "unit" of code (usually a single method)
 * does exactly what it's supposed to do. We feed it known inputs and assert
 * that the output is what we expect.
 *
 * JUnit 5 annotations:
 *   @Test       — marks a method as a test case to run
 *   @BeforeEach — runs before EVERY test (fresh setup each time)
 *
 * JUnit 5 assertions:
 *   assertEquals(expected, actual)   — fails if the two values differ
 *   assertTrue(condition)            — fails if condition is false
 *   assertFalse(condition)           — fails if condition is true
 *   assertNull(value)                — fails if value is not null
 *   assertNotNull(value)             — fails if value is null
 *
 * WHY TEST GAMESTATE SPECIFICALLY:
 * GameState is the heart of the game. If applyChanges() has a bug, resource
 * tracking breaks everywhere. If getGameOverReason() is wrong, the game never
 * ends (or ends too early). Testing this class gives us the most safety coverage
 * for the least amount of test code.
 */
class GameStateTest {

    private GameState state;

    // @BeforeEach creates a fresh GameState before each individual test.
    // Without this, one test's changes could bleed into the next test.
    @BeforeEach
    void setUp() {
        state = new GameState();
    }

    // =====================================================================
    // STARTING VALUES
    // =====================================================================

    @Test
    void testStartingValues() {
        assertEquals(GameState.STARTING_CASH,   state.getCash(),    "Starting cash is wrong");
        assertEquals(GameState.STARTING_MORALE, state.getMorale(),  "Starting morale is wrong");
        assertEquals(GameState.STARTING_COFFEE, state.getCoffee(),  "Starting coffee is wrong");
        assertEquals(GameState.STARTING_HYPE,   state.getHype(),    "Starting hype is wrong");
        assertEquals(0,  state.getBugs(),                           "Should start with 0 bugs");
        assertEquals(0,  state.getCurrentLocationIndex(),           "Should start at index 0");
        assertEquals(1,  state.getDay(),                            "Should start on day 1");
        assertEquals(0,  state.getDaysWithoutCoffee(),              "Should start with 0 no-coffee days");
        assertFalse(state.isGameWon(),                              "Game should not be won at start");
    }

    // =====================================================================
    // applyChanges() — the core resource update method
    // =====================================================================

    @Test
    void testApplyChangesPositive() {
        state.applyChanges(1000, 0, 5, 15, 0);  // Gain cash and coffee and hype
        assertEquals(51_000, state.getCash());
        assertEquals(55,     state.getCoffee());
        assertEquals(65,     state.getHype());
    }

    @Test
    void testApplyChangesNegative() {
        state.applyChanges(-1000, -20, -10, -15, 5);
        assertEquals(49_000, state.getCash());
        assertEquals(80,     state.getMorale());
        assertEquals(40,     state.getCoffee());
        assertEquals(35,     state.getHype());
        assertEquals(5,      state.getBugs());
    }

    @Test
    void testCashCannotGoBelowZero() {
        state.applyChanges(-999_999, 0, 0, 0, 0);  // Spend way more than we have
        assertEquals(0, state.getCash(), "Cash must never be negative");
    }

    @Test
    void testMoraleCannotGoBelowZero() {
        state.applyChanges(0, -999, 0, 0, 0);
        assertEquals(0, state.getMorale(), "Morale must never be negative");
    }

    @Test
    void testMoraleCannotExceedMax() {
        // Already at 100; adding more should clamp at MAX_MORALE
        state.applyChanges(0, 999, 0, 0, 0);
        assertEquals(GameState.MAX_MORALE, state.getMorale(), "Morale must not exceed 100");
    }

    @Test
    void testHypeCannotExceedMax() {
        state.applyChanges(0, 0, 0, 999, 0);
        assertEquals(GameState.MAX_HYPE, state.getHype(), "Hype must not exceed 100");
    }

    @Test
    void testHypeCannotGoBelowZero() {
        state.applyChanges(0, 0, 0, -999, 0);
        assertEquals(0, state.getHype(), "Hype must never be negative");
    }

    @Test
    void testBugsCannotGoBelowZero() {
        state.applyChanges(0, 0, 0, 0, -999);  // Can't have negative bugs
        assertEquals(0, state.getBugs(), "Bugs must never be negative");
    }

    // =====================================================================
    // WIN / LOSE CONDITIONS
    // =====================================================================

    @Test
    void testGameNotOverAtStart() {
        assertFalse(state.isGameOver());
        assertNull(state.getGameOverReason());
    }

    @Test
    void testGameOverWhenCashIsZero() {
        state.setCash(0);
        assertTrue(state.isGameOver(), "Game should be over when cash hits 0");
        assertNotNull(state.getGameOverReason());
    }

    @Test
    void testGameOverWhenMoraleIsZero() {
        state.setMorale(0);
        assertTrue(state.isGameOver(), "Game should be over when morale hits 0");
    }

    @Test
    void testGameOverWhenBugsReachMax() {
        state.setBugs(GameState.MAX_BUGS);
        assertTrue(state.isGameOver(), "Game should be over when bugs hit MAX_BUGS");
    }

    @Test
    void testOneDayWithoutCoffeeIsNotGameOver() {
        // Day 1 coffee = 0: counter becomes 1, should NOT be game over yet
        state.setCoffee(0);
        state.advanceDay();
        assertFalse(state.isGameOver(), "One day without coffee should not end the game");
        assertEquals(1, state.getDaysWithoutCoffee());
    }

    @Test
    void testTwoDaysWithoutCoffeeIsGameOver() {
        // Day 1 and day 2 both end with coffee = 0
        state.setCoffee(0);
        state.advanceDay();  // daysWithoutCoffee = 1
        state.advanceDay();  // daysWithoutCoffee = 2
        assertTrue(state.isGameOver(), "Two days without coffee must trigger game over");
    }

    @Test
    void testCoffeeCounterResetsWhenRestocked() {
        state.setCoffee(0);
        state.advanceDay();  // daysWithoutCoffee = 1
        state.setCoffee(20); // Restock!
        state.advanceDay();  // Counter should reset to 0
        assertEquals(0, state.getDaysWithoutCoffee(), "Counter should reset when coffee is available");
        assertFalse(state.isGameOver());
    }

    // =====================================================================
    // LOCATION AND DAY PROGRESSION
    // =====================================================================

    @Test
    void testAdvanceLocation() {
        assertEquals(0, state.getCurrentLocationIndex());
        state.advanceLocation();
        assertEquals(1, state.getCurrentLocationIndex());
        state.advanceLocation();
        assertEquals(2, state.getCurrentLocationIndex());
    }

    @Test
    void testAdvanceDay() {
        assertEquals(1, state.getDay());
        state.advanceDay();
        assertEquals(2, state.getDay());
    }

    @Test
    void testGameWonFlagCanBeSet() {
        assertFalse(state.isGameWon());
        state.setGameWon(true);
        assertTrue(state.isGameWon());
        // gameWon=true means getGameOverReason() returns null (it's not a loss)
        assertNull(state.getGameOverReason());
        assertFalse(state.isGameOver());
    }
}
