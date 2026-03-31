package com.siliconvalleytrail;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Game.java — The coordinator. Runs the main game loop and connects all other classes.
 *
 * THINK OF THIS CLASS LIKE A CONDUCTOR:
 * It doesn't do the hard work itself — it calls the right class at the right time:
 *   - Display: "print the menu"
 *   - WeatherService: "get me today's weather"
 *   - EventSystem: "pick a random event"
 *   - GameState: "apply these changes"
 *   - SaveManager: "save to disk"
 *
 * THE GAME LOOP (what happens each turn):
 *   1. Fetch live weather for the current city
 *   2. Display status + weather + map
 *   3. Player picks an action (travel / rest / work / market / supplies / save / quit)
 *   4. Apply weather effects at end of turn
 *   5. If player traveled: trigger a random event
 *   6. Check win/lose conditions
 *   7. Advance the day counter
 *   8. Repeat
 *
 * INPUT VALIDATION:
 * The readInt() helper re-prompts until the player enters a valid number.
 * This is safer than calling scanner.nextInt() directly, which throws an exception
 * on non-numeric input and also leaves a newline in the buffer causing read issues.
 */
public class Game {

    private GameState      state;
    private List<Location> locations;
    private EventSystem    eventSystem;
    private WeatherService weatherService;
    private Scanner        scanner;
    private boolean        running;

    public Game() {
        this.locations      = buildLocations();
        this.eventSystem    = new EventSystem();
        this.weatherService = new WeatherService();
        this.scanner        = new Scanner(System.in);
        this.running        = true;
    }

    // =====================================================================
    // STARTUP
    // =====================================================================

    /** Entry point — shows the main menu until the player quits. */
    public void start() {
        while (running) {
            Display.printMainMenu();
            int choice = readInt(1, 4);
            switch (choice) {
                case 1: startNewGame();  break;
                case 2: loadSavedGame(); break;
                case 3:
                    Display.printHowToPlay();
                    Display.pressEnterToContinue(scanner);
                    break;
                case 4:
                    System.out.println("Thanks for playing Silicon Valley Trail. Goodbye!");
                    running = false;
                    break;
            }
        }
        scanner.close();
    }

    private void startNewGame() {
        state = new GameState();
        Display.printIntro();
        Display.pressEnterToContinue(scanner);
        runGameLoop();
    }

    private void loadSavedGame() {
        if (!SaveManager.hasSaveFile()) {
            System.out.println("  No save file found.");
            Display.pressEnterToContinue(scanner);
            return;
        }
        state = SaveManager.loadGame();
        if (state == null) {
            System.out.println("  Failed to load save file. Starting fresh might be safer.");
            Display.pressEnterToContinue(scanner);
            return;
        }
        System.out.println("  Game loaded successfully!");
        Display.pressEnterToContinue(scanner);
        runGameLoop();
    }

    // =====================================================================
    // MAIN GAME LOOP
    // =====================================================================

    private void runGameLoop() {
        while (true) {
            Location currentLocation = locations.get(state.getCurrentLocationIndex());

            // --- Step 1: Fetch today's weather ---
            // This calls the Open-Meteo API (or falls back to mock if offline)
            Weather weather = weatherService.getWeather(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );

            // --- Step 2: Display current status ---
            Display.printDayHeader(state, currentLocation);
            Display.printWeather(weather);
            Display.printMap(locations, state.getCurrentLocationIndex());

            // --- Step 3: Player picks an action ---
            Display.printActionMenu();
            int action = readInt(1, 7);

            // Track whether this counts as a "real" turn (save/quit do not advance the day)
            boolean endOfTurn = true;

            switch (action) {
                case 1:  // Travel
                    doTravel();
                    // An event fires every time the team arrives at a new city
                    if (!state.isGameOver() && !state.isGameWon()) {
                        doEvent(weather);
                    }
                    break;
                case 2:  doRest();       break;
                case 3:  doWork();       break;
                case 4:  doMarketing();  break;
                case 5:  doSupplies();   break;
                case 6:  // Save — does NOT count as a turn
                    if (SaveManager.saveGame(state)) {
                        System.out.println("  Game saved!");
                    }
                    Display.pressEnterToContinue(scanner);
                    endOfTurn = false;
                    break;
                case 7:  // Quit to menu
                    System.out.println("  Returning to main menu...");
                    return;
            }

            if (!endOfTurn) continue;

            // --- Step 4: Apply weather effects at end of turn ---
            // (shown as feedback so the player knows what changed and why)
            int moralePenalty = weather.getMoralePenalty();
            int coffeePenalty = weather.getCoffeePenalty();
            int bugPenalty    = weather.getBugPenalty();
            if (moralePenalty > 0 || coffeePenalty > 0 || bugPenalty > 0) {
                state.applyChanges(0, -moralePenalty, -coffeePenalty, 0, bugPenalty);
                Display.printWeatherEffect(weather);
            }

            // --- Step 5: Check win condition ---
            if (state.isGameWon()) {
                Display.printWin(state);
                SaveManager.deleteSaveFile();
                Display.pressEnterToContinue(scanner);
                return;
            }

            // --- Step 6: Check lose conditions ---
            if (state.isGameOver()) {
                Display.printGameOver(state.getGameOverReason());
                SaveManager.deleteSaveFile();
                Display.pressEnterToContinue(scanner);
                return;
            }

            // --- Step 7: Advance the day (also updates daysWithoutCoffee) ---
            state.advanceDay();
        }
    }

    // =====================================================================
    // PLAYER ACTIONS
    // Each method applies the action's effects to state and prints feedback.
    // =====================================================================

    private void doTravel() {
        int currentIndex = state.getCurrentLocationIndex();
        int lastIndex    = locations.size() - 1;

        if (currentIndex >= lastIndex) {
            // Shouldn't happen normally, but just in case
            System.out.println("  You're already in San Francisco!");
            Display.pressEnterToContinue(scanner);
            return;
        }

        System.out.println();
        System.out.println("  Your team hits the road...");
        state.applyChanges(0, -5, -3, 0, 0);
        state.advanceLocation();

        Location arrived = locations.get(state.getCurrentLocationIndex());
        System.out.println("  Arrived at " + arrived.getName() + "!");
        Display.pressEnterToContinue(scanner);

        // Check if the team just reached San Francisco
        if (state.getCurrentLocationIndex() == lastIndex) {
            state.setGameWon(true);
        }
    }

    private void doRest() {
        System.out.println();
        System.out.println("  The team takes the day to rest...");
        state.applyChanges(0, 15, -5, 0, 0);
        System.out.println("  Morale restored!  (+15 morale, -5 coffee)");
        Display.pressEnterToContinue(scanner);
    }

    private void doWork() {
        System.out.println();
        if (state.getBugs() == 0) {
            System.out.println("  No bugs to fix — the codebase is clean!");
            state.applyChanges(0, 5, -3, 0, 0);
            System.out.println("  Team celebrates clean code.  (+5 morale, -3 coffee)");
        } else {
            System.out.println("  The team focuses on squashing bugs...");
            state.applyChanges(0, -5, -8, 0, -3);
            System.out.println("  Productive bug bash!  (-3 bugs, -8 coffee, -5 morale)");
        }
        Display.pressEnterToContinue(scanner);
    }

    private void doMarketing() {
        System.out.println();
        if (state.getCash() < 1500) {
            System.out.println("  Not enough cash for a marketing push. (Need $1,500)");
            Display.pressEnterToContinue(scanner);
            return;
        }
        System.out.println("  You launch a marketing campaign...");
        state.applyChanges(-1500, 5, 0, 20, 0);
        System.out.println("  Campaign live!  (+20 hype, +5 morale, -$1,500)");
        Display.pressEnterToContinue(scanner);
    }

    private void doSupplies() {
        System.out.println();
        if (state.getCash() < 500) {
            System.out.println("  Not enough cash for supplies. (Need $500)");
            Display.pressEnterToContinue(scanner);
            return;
        }
        System.out.println("  You restock coffee and snacks...");
        state.applyChanges(-500, 5, 15, 0, 0);
        System.out.println("  Supplies restocked!  (+15 coffee, +5 morale, -$500)");
        Display.pressEnterToContinue(scanner);
    }

    private void doEvent(Weather weather) {
        Event event = eventSystem.getRandomEvent(state, weather);
        Display.printEvent(event);

        // readInt returns 1-based; convert to 0-based index for the Event class
        int choice = readInt(1, event.getNumberOfChoices()) - 1;
        event.applyOutcome(state, choice);
        Display.printEventOutcome(event.getOutcomeMessage(choice));
        Display.pressEnterToContinue(scanner);
    }

    // =====================================================================
    // LOCATIONS — all 11 stops, San Jose to San Francisco
    // =====================================================================

    private List<Location> buildLocations() {
        List<Location> locs = new ArrayList<>();
        // Latitude and longitude from Google Maps — used for real weather API calls
        locs.add(new Location("San Jose",         "Your startup's humble garage HQ",          37.3382, -121.8863, "SJ" ));
        locs.add(new Location("Santa Clara",      "Home of tech giants",                      37.3541, -121.9552, "SC" ));
        locs.add(new Location("Sunnyvale",        "Where every block has a startup",           37.3688, -122.0363, "SN" ));
        locs.add(new Location("Mountain View",    "Google's backyard",                         37.3861, -122.0839, "MV" ));
        locs.add(new Location("Palo Alto",        "Sand Hill Road money",                      37.4419, -122.1430, "PA" ));
        locs.add(new Location("Menlo Park",       "Meta's old stomping grounds",               37.4529, -122.1817, "MP" ));
        locs.add(new Location("Redwood City",     "Peninsula hub on the rise",                 37.4852, -122.2364, "RC" ));
        locs.add(new Location("San Mateo",        "Growing tech scene",                        37.5630, -122.3255, "SM" ));
        locs.add(new Location("Burlingame",       "Almost there — can you smell the bay?",     37.5841, -122.3661, "BU" ));
        locs.add(new Location("South San Francisco", "The Industrial City",                   37.6547, -122.4077, "SSF"));
        locs.add(new Location("San Francisco",    "The Series A pitch awaits!",                37.7749, -122.4194, "SF" ));
        return locs;
    }

    // =====================================================================
    // INPUT HELPER
    // =====================================================================

    /**
     * Reads an integer from the player within [min, max].
     * Re-prompts on bad input instead of crashing.
     *
     * WHY scanner.nextLine() NOT scanner.nextInt():
     * scanner.nextInt() leaves a dangling newline character in the input buffer.
     * The next scanner.nextLine() call would then return "" immediately, causing
     * "Press Enter to continue" to appear to skip. Using nextLine() every time
     * avoids this classic Java Scanner gotcha.
     */
    private int readInt(int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.print("  Please enter a number between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("  Invalid input. Please enter a number: ");
            }
        }
    }
}
