package com.siliconvalleytrail;

import java.io.Serializable;

/**
 * GameState.java — Holds ALL the data for a single playthrough.
 *
 * WHY THIS EXISTS:
 * In your TicTacToe project you stored game state (the board, current player)
 * as static variables directly in the game class. That works for small programs,
 * but it makes saving/loading nearly impossible and is hard to test.
 *
 * Here, ALL mutable game data lives in ONE object. This means:
 *   - To save the game, we just write this object's fields to a file.
 *   - To test the game logic, we create a fresh GameState in each test.
 *   - The Game class never needs to know HOW data is stored, just that it IS stored here.
 *
 * This pattern is called a "data model" or "state object".
 *
 * SERIALIZABLE:
 * Implementing Serializable is like stamping the class "this object can be
 * written to a file." The SaveManager uses it. The serialVersionUID is just
 * a version number Java uses to confirm the saved file matches this class.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================================
    // GAME CONSTANTS — values that never change during play
    // Using static final means: belongs to the class (not an instance),
    // and cannot be changed. Like a constant in math.
    // =====================================================================
    public static final int STARTING_CASH    = 50_000;  // Underscores are legal in Java — makes large numbers readable
    public static final int STARTING_MORALE  = 100;
    public static final int STARTING_COFFEE  = 50;
    public static final int STARTING_HYPE    = 50;
    public static final int MAX_MORALE       = 100;
    public static final int MAX_HYPE         = 100;
    public static final int MAX_BUGS         = 20;      // Hitting this = game over

    // =====================================================================
    // INSTANCE VARIABLES — the actual game data (changes during play)
    // Private: nothing outside this class can directly change these.
    //          They can only be changed through the methods below.
    //          This protects us from accidental bad states (e.g., cash = -999999).
    // =====================================================================
    private int cash;
    private int morale;
    private int coffee;
    private int hype;
    private int bugs;
    private int currentLocationIndex;  // Which city are we in? (0 = San Jose, 10 = SF)
    private int day;
    private int daysWithoutCoffee;     // Used for the "2 days no coffee = game over" rule
    private boolean gameWon;

    // =====================================================================
    // CONSTRUCTOR — sets up a brand-new game with starting values
    // =====================================================================
    public GameState() {
        this.cash                = STARTING_CASH;
        this.morale              = STARTING_MORALE;
        this.coffee              = STARTING_COFFEE;
        this.hype                = STARTING_HYPE;
        this.bugs                = 0;
        this.currentLocationIndex = 0;
        this.day                 = 1;
        this.daysWithoutCoffee   = 0;
        this.gameWon             = false;
    }

    // =====================================================================
    // CORE METHOD: applyChanges
    //
    // WHY THIS DESIGN:
    // Instead of having a separate method for every possible change
    // (addCash, subtractMorale, addCoffee...), we use ONE method that
    // takes deltas (changes). Positive = gain, negative = loss.
    //
    // Example: applyChanges(-2000, 0, -8, 0, 0)
    //   means: lose $2000, morale unchanged, lose 8 coffee, hype unchanged, bugs unchanged.
    //
    // The clamp() calls ensure values can never go below 0 or above their max.
    // Without clamping, morale could reach -50 or 150, which makes no sense.
    // =====================================================================
    public void applyChanges(int cashDelta, int moraleDelta, int coffeeDelta,
                             int hypeDelta, int bugsDelta) {
        this.cash   = Math.max(0, this.cash + cashDelta);
        this.morale = clamp(this.morale + moraleDelta, 0, MAX_MORALE);
        this.coffee = Math.max(0, this.coffee + coffeeDelta);
        this.hype   = clamp(this.hype + hypeDelta, 0, MAX_HYPE);
        this.bugs   = Math.max(0, this.bugs + bugsDelta);
    }

    /**
     * Called at the end of each game day (in Game.java's main loop).
     * Advances the day counter and checks the coffee supply.
     */
    public void advanceDay() {
        day++;
        // Track consecutive days with no coffee
        if (coffee == 0) {
            daysWithoutCoffee++;
        } else {
            daysWithoutCoffee = 0;  // Reset streak as soon as coffee is restocked
        }
    }

    /** Move to the next city on the map. */
    public void advanceLocation() {
        currentLocationIndex++;
    }

    // =====================================================================
    // WIN / LOSE CHECKS
    // =====================================================================

    /**
     * Returns a human-readable reason if the game is over, or null if still going.
     *
     * WHY RETURN A STRING (not boolean):
     * Returning the reason lets us display exactly why the player lost,
     * rather than just "you lost." It's more useful than a plain true/false.
     */
    public String getGameOverReason() {
        if (gameWon) return null;  // Won = not over in a bad way
        if (cash <= 0)
            return "You ran out of funding! The startup is dead.";
        if (morale <= 0)
            return "Team morale collapsed! Everyone quit and moved to FAANG.";
        if (bugs >= MAX_BUGS)
            return "Your product became unusable with " + bugs + " bugs! Users fled to competitors.";
        if (daysWithoutCoffee >= 2)
            return "Without coffee for 2 days straight, the team stopped functioning entirely.";
        return null;
    }

    /** Convenience method — true if the game should end right now. */
    public boolean isGameOver() {
        return getGameOverReason() != null;
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    /**
     * Clamps a value so it stays within [min, max].
     * Example: clamp(150, 0, 100) returns 100.
     * Example: clamp(-5, 0, 100)  returns 0.
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // =====================================================================
    // GETTERS — the only way for outside classes to READ these values
    // =====================================================================
    public int getCash()                 { return cash; }
    public int getMorale()               { return morale; }
    public int getCoffee()               { return coffee; }
    public int getHype()                 { return hype; }
    public int getBugs()                 { return bugs; }
    public int getCurrentLocationIndex() { return currentLocationIndex; }
    public int getDay()                  { return day; }
    public int getDaysWithoutCoffee()    { return daysWithoutCoffee; }
    public boolean isGameWon()           { return gameWon; }

    // =====================================================================
    // SETTERS — used ONLY by SaveManager when loading a saved game.
    //           The rest of the game uses applyChanges() instead.
    // =====================================================================
    public void setCash(int cash)                         { this.cash = Math.max(0, cash); }
    public void setMorale(int morale)                     { this.morale = clamp(morale, 0, MAX_MORALE); }
    public void setCoffee(int coffee)                     { this.coffee = Math.max(0, coffee); }
    public void setHype(int hype)                         { this.hype = clamp(hype, 0, MAX_HYPE); }
    public void setBugs(int bugs)                         { this.bugs = Math.max(0, bugs); }
    public void setCurrentLocationIndex(int index)        { this.currentLocationIndex = index; }
    public void setDay(int day)                           { this.day = day; }
    public void setDaysWithoutCoffee(int days)            { this.daysWithoutCoffee = days; }
    public void setGameWon(boolean gameWon)               { this.gameWon = gameWon; }
}
