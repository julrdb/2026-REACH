package com.siliconvalleytrail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SaveManager.java — Handles saving and loading game state to/from a file.
 *
 * WHY PROPERTIES FILE (not Java Serialization):
 * Java's ObjectOutputStream can save any object to a binary file, but binary
 * files are hard to debug — you can't open them in a text editor. A .properties
 * file is plain text (key=value pairs), so you can open savegame.properties in any
 * editor and see exactly what was saved. This makes debugging much easier.
 *
 * Example savegame.properties contents:
 *   day=4
 *   cash=47500
 *   morale=97
 *   coffee=26
 *   hype=90
 *   bugs=0
 *   currentLocation=2
 *   daysWithoutCoffee=0
 *
 * TRY-WITH-RESOURCES:
 * The "try (FileInputStream in = ...)" syntax is called "try-with-resources."
 * It automatically closes the file when the try block ends, even if an error
 * occurs. Without it, we'd need a "finally { in.close(); }" block.
 *
 * All methods are static (like Display) because SaveManager has no state of its own.
 */
public class SaveManager {

    private static final String SAVE_FILE = "savegame.properties";

    /**
     * Saves the current game state to savegame.properties.
     *
     * @param state The current game state
     * @return true if saved successfully, false if an error occurred
     */
    public static boolean saveGame(GameState state) {
        Properties props = new Properties();
        props.setProperty("day",               String.valueOf(state.getDay()));
        props.setProperty("cash",              String.valueOf(state.getCash()));
        props.setProperty("morale",            String.valueOf(state.getMorale()));
        props.setProperty("coffee",            String.valueOf(state.getCoffee()));
        props.setProperty("hype",              String.valueOf(state.getHype()));
        props.setProperty("bugs",              String.valueOf(state.getBugs()));
        props.setProperty("currentLocation",   String.valueOf(state.getCurrentLocationIndex()));
        props.setProperty("daysWithoutCoffee", String.valueOf(state.getDaysWithoutCoffee()));

        // Try-with-resources: the FileOutputStream closes automatically
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            props.store(out, "Silicon Valley Trail Save File");
            return true;
        } catch (IOException e) {
            System.out.println("  [Error] Could not save game: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads a saved game from savegame.properties.
     *
     * @return A restored GameState, or null if no save file exists or loading fails
     */
    public static GameState loadGame() {
        if (!hasSaveFile()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SAVE_FILE)) {
            props.load(in);

            // Reconstruct the GameState from the saved values
            // The second argument to getProperty() is a default value —
            // used if the key is missing (protects against corrupt/old save files)
            GameState state = new GameState();
            state.setDay(              parseInt(props, "day",               1));
            state.setCash(             parseInt(props, "cash",              GameState.STARTING_CASH));
            state.setMorale(           parseInt(props, "morale",            GameState.STARTING_MORALE));
            state.setCoffee(           parseInt(props, "coffee",            GameState.STARTING_COFFEE));
            state.setHype(             parseInt(props, "hype",              50));
            state.setBugs(             parseInt(props, "bugs",              0));
            state.setCurrentLocationIndex(parseInt(props, "currentLocation", 0));
            state.setDaysWithoutCoffee(parseInt(props, "daysWithoutCoffee", 0));
            return state;

        } catch (IOException e) {
            System.out.println("  [Error] Could not load game: " + e.getMessage());
            return null;
        }
    }

    /** Returns true if a save file exists on disk. */
    public static boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    /** Deletes the save file (called after a game ends so old saves don't persist). */
    public static void deleteSaveFile() {
        File f = new File(SAVE_FILE);
        if (f.exists()) f.delete();
    }

    // Helper: parse an int from Properties, or return the default if missing/broken
    private static int parseInt(Properties props, String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
