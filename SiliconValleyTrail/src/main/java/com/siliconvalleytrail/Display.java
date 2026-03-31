package com.siliconvalleytrail;

import java.util.List;
import java.util.Scanner;

/**
 * Display.java — Handles all printed output in one place.
 *
 * WHY ALL STATIC METHODS:
 * Display has no state of its own — it just formats and prints things.
 * Making the methods static means you call them as Display.printTitle()
 * rather than needing to create a new Display() object first. You've seen
 * this pattern with Math.max() and Arrays.sort() in your CS111B course.
 *
 * WHY SEPARATE FROM Game.java:
 * Mixing game logic and display logic in one class makes both harder to change.
 * If we later wanted to add colors or swap to a GUI, we'd only edit Display.java.
 *
 * LINE LENGTHS:
 * All separator lines are exactly 60 characters so the game looks clean
 * on a standard 80-column terminal.
 */
public class Display {

    private static final String LINE     = "============================================================";
    private static final String THIN     = "------------------------------------------------------------";
    private static final String ALERT    = "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
    private static final String GAMEOVER = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    // =====================================================================
    // MENUS
    // =====================================================================

    public static void printMainMenu() {
        System.out.println(LINE);
        System.out.println("  SILICON VALLEY TRAIL");
        System.out.println(LINE);
        System.out.println("1. New Game");
        System.out.println("2. Load Game");
        System.out.println("3. How to Play");
        System.out.println("4. Quit");
        System.out.print("Enter choice (1-4): ");
    }

    public static void printHowToPlay() {
        System.out.println(LINE);
        System.out.println("  HOW TO PLAY");
        System.out.println(LINE);
        System.out.println("Lead your startup from San Jose to San Francisco");
        System.out.println("to pitch for Series A funding!");
        System.out.println();
        System.out.println("RESOURCES:");
        System.out.println("  Cash    - Hits $0     -> GAME OVER (out of funding)");
        System.out.println("  Morale  - Hits 0      -> GAME OVER (team quits)");
        System.out.println("  Coffee  - 0 for 2 days -> GAME OVER (team stops)");
        System.out.println("  Bugs    - Hits 20     -> GAME OVER (unusable product)");
        System.out.println("  Hype    - No lose condition, but helps your final score");
        System.out.println();
        System.out.println("ACTIONS EACH DAY:");
        System.out.println("  Travel    Move to the next city. Costs morale + coffee.");
        System.out.println("  Rest      Recover morale. Uses extra coffee.");
        System.out.println("  Work      Squash bugs. Tires the team.");
        System.out.println("  Marketing Boost hype. Costs money.");
        System.out.println("  Supplies  Buy coffee. Costs money.");
        System.out.println();
        System.out.println("An event fires every time you travel to a new city.");
        System.out.println("Weather affects your team each day automatically.");
        System.out.println();
        System.out.println("WIN: Reach San Francisco with resources intact!");
        System.out.println(LINE);
    }

    public static void printIntro() {
        System.out.println(LINE);
        System.out.println("   SILICON VALLEY TRAIL");
        System.out.println(LINE);
        System.out.println("Your scrappy startup team is heading from");
        System.out.println("San Jose to San Francisco for a Series A pitch!");
        System.out.println();
        System.out.println("Resources:");
        System.out.println("  Cash     Don't run out!");
        System.out.println("  Morale   Keep your team happy.");
        System.out.println("  Coffee   Essential fuel. 2 days without = game over.");
        System.out.println("  Hype     Public interest in your startup.");
        System.out.println("  Bugs     Keep them under 20 or the product fails.");
        System.out.println();
        System.out.println("Good luck, founder.");
        System.out.println(LINE);
    }

    // =====================================================================
    // DAY DISPLAY
    // =====================================================================

    public static void printDayHeader(GameState state, Location location) {
        System.out.println();
        System.out.println(LINE);
        System.out.printf("  Day %d  |  %s%n", state.getDay(), location.getName());
        System.out.println("  " + location.getDescription());
        System.out.println(LINE);

        // Resource row 1
        System.out.printf("  Cash: $%,d   Morale: %d/100   Coffee: %d%n",
                state.getCash(), state.getMorale(), state.getCoffee());

        // Resource row 2 — show bugs with a warning if high
        String bugsDisplay = state.getBugs() + "/" + GameState.MAX_BUGS;
        if (state.getBugs() >= 15) bugsDisplay += " [CRITICAL!]";
        else if (state.getBugs() >= 10) bugsDisplay += " [WARNING]";
        System.out.printf("  Hype: %d/100   Bugs: %s%n", state.getHype(), bugsDisplay);

        // Coffee warning
        if (state.getDaysWithoutCoffee() == 1) {
            System.out.println();
            System.out.println("  *** WARNING: No coffee yesterday! One more day = GAME OVER ***");
        }

        // Progress bar
        printProgressBar(state.getCurrentLocationIndex(), 10);
        System.out.println(LINE);
    }

    private static void printProgressBar(int current, int total) {
        int percent = (current * 100) / total;
        int filled  = current * 20 / total;
        System.out.print("  Progress: [");
        for (int i = 0; i < 20; i++) {
            System.out.print(i < filled ? "#" : "-");
        }
        System.out.println("] " + percent + "% to San Francisco");
    }

    // =====================================================================
    // WEATHER
    // =====================================================================

    public static void printWeather(Weather weather) {
        System.out.printf("  %s Weather: %s, %d°F%n",
                weather.getEmoji(), weather.getDescription(), (int) weather.getTemperature());

        // Warn about penalties that will apply this turn
        if (weather.getMoralePenalty() > 0) {
            System.out.println("  [Weather] Rough conditions will reduce morale this turn.");
        }
        if (weather.getTemperature() > 92) {
            System.out.println("  [Weather] Heat wave! Productivity will suffer.");
        }
    }

    // =====================================================================
    // ASCII MAP
    // =====================================================================

    /**
     * Draws a horizontal map of all locations.
     * * marks the current city, visited cities show plain, future cities are in (parens).
     *
     * Example:  [*SJ*]--[SC]--[MV]--(PA)--(SF)
     */
    public static void printMap(List<Location> locations, int currentIndex) {
        System.out.println();
        System.out.print("  MAP: ");
        for (int i = 0; i < locations.size(); i++) {
            String code = locations.get(i).getShortCode();
            if (i == currentIndex) {
                System.out.print("[*" + code + "*]");   // current
            } else if (i < currentIndex) {
                System.out.print("[" + code + "]");      // visited
            } else {
                System.out.print("(" + code + ")");      // not yet reached
            }
            if (i < locations.size() - 1) System.out.print("--");
        }
        System.out.println();
        System.out.println("        [*XX*]=current  [XX]=visited  (XX)=ahead");
    }

    // =====================================================================
    // ACTION MENU
    // =====================================================================

    public static void printActionMenu() {
        System.out.println();
        System.out.println(THIN);
        System.out.println("  What will you do today?");
        System.out.println(THIN);
        System.out.println("  1. Travel to next location   (-5 morale, -3 coffee)");
        System.out.println("  2. Rest and recover          (+15 morale, -5 coffee)");
        System.out.println("  3. Work on product           (-3 bugs, -8 coffee, -5 morale)");
        System.out.println("  4. Marketing push            (+20 hype, -$1,500)");
        System.out.println("  5. Buy supplies              (+15 coffee, +5 morale, -$500)");
        System.out.println("  6. Save game");
        System.out.println("  7. Quit to menu");
        System.out.print("  Enter choice (1-7): ");
    }

    // =====================================================================
    // EVENTS
    // =====================================================================

    public static void printEvent(Event event) {
        System.out.println();
        System.out.println(ALERT);
        System.out.println("  EVENT: " + event.getName());
        System.out.println(ALERT);
        System.out.println("  " + event.getDescription());
        System.out.println();
        List<String> choices = event.getChoiceDescriptions();
        for (int i = 0; i < choices.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + choices.get(i));
        }
        System.out.print("  Enter choice (1-" + choices.size() + "): ");
    }

    public static void printEventOutcome(String message) {
        System.out.println();
        System.out.println("  >> " + message);
    }

    // =====================================================================
    // WEATHER EFFECT FEEDBACK
    // =====================================================================

    public static void printWeatherEffect(Weather weather) {
        int morale = weather.getMoralePenalty();
        int coffee = weather.getCoffeePenalty();
        int bugs   = weather.getBugPenalty();
        if (morale > 0 || coffee > 0 || bugs > 0) {
            System.out.printf("  [Weather Effect] %s: -%d morale, -%d coffee, +%d bugs%n",
                    weather.getDescription(), morale, coffee, bugs);
        }
    }

    // =====================================================================
    // WIN / LOSE
    // =====================================================================

    public static void printGameOver(String reason) {
        System.out.println();
        System.out.println(GAMEOVER);
        System.out.println("   GAME OVER");
        System.out.println(GAMEOVER);
        System.out.println("  " + reason);
        System.out.println();
        System.out.println("  Better luck next time, founder.");
        System.out.println(GAMEOVER);
    }

    public static void printWin(GameState state) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("   YOU MADE IT TO SAN FRANCISCO!");
        System.out.println(LINE);
        System.out.println("  After " + state.getDay() + " days, your startup arrived.");
        System.out.println();
        System.out.println("  Final Stats:");
        System.out.printf("    Cash:   $%,d%n", state.getCash());
        System.out.println("    Morale: " + state.getMorale() + "/100");
        System.out.println("    Coffee: " + state.getCoffee());
        System.out.println("    Hype:   " + state.getHype() + "/100");
        System.out.println("    Bugs:   " + state.getBugs());
        System.out.println();

        // Score formula: rewards cash, morale, hype; penalizes bugs and long trips
        int score = (state.getCash() / 100)
                  + (state.getMorale() * 10)
                  + (state.getHype() * 5)
                  - (state.getBugs() * 20)
                  - (state.getDay() * 5);
        System.out.println("  Score: " + score);
        if (score > 8000) System.out.println("  Rating: UNICORN STATUS! Incredible run.");
        else if (score > 4000) System.out.println("  Rating: Series A Funded! Strong finish.");
        else if (score > 1500) System.out.println("  Rating: Seed Round Closed. Not bad.");
        else System.out.println("  Rating: Survived. Barely. The grind was real.");
        System.out.println(LINE);
    }

    // =====================================================================
    // UTILITY
    // =====================================================================

    /** Pauses and waits for the player to press Enter before continuing. */
    public static void pressEnterToContinue(Scanner scanner) {
        System.out.println();
        System.out.print("  [Press Enter to continue]");
        scanner.nextLine();
    }

    /** Clears several lines — creates visual separation between turns. */
    public static void printSpacer() {
        System.out.println();
        System.out.println();
    }
}
