package com.siliconvalleytrail;

import java.util.ArrayList;
import java.util.List;

/**
 * Event.java — Represents a single random event with player choices.
 *
 * DESIGN:
 * Each event has:
 *   - A name and description (what happened?)
 *   - 1 to 3 choices the player can make
 *   - Each choice has effects (cashDelta, moraleDelta, etc.) and a result message
 *
 * WHY PARALLEL LISTS:
 * We store choiceDescriptions, outcomes, and outcomeMessages as three separate
 * ArrayLists that are all indexed the same way. So choice 0 = choiceDescriptions[0]
 * = outcomes[0] = outcomeMessages[0]. This mirrors how you used parallel arrays in
 * your CS111B assignments, but with ArrayList so the size is flexible.
 *
 * An alternative would be to create a separate "Choice" class — that's a valid
 * refactor for the future.
 *
 * WEATHER-DEPENDENT EVENTS:
 * Some events only appear when the weather matches (e.g., a heat-wave event only
 * triggers on hot sunny days). The EventSystem checks this before picking an event.
 */
public class Event {

    private String       name;
    private String       description;
    private List<String> choiceDescriptions;

    // outcomes[i] = { cashDelta, moraleDelta, coffeeDelta, hypeDelta, bugsDelta }
    // Using int[] (an array of 5 ints) because outcomes always have exactly 5 values.
    private List<int[]>  outcomes;

    private List<String> outcomeMessages;

    // Weather-dependent fields
    private boolean      requiresWeather;
    private String       requiredCondition;

    public Event(String name, String description) {
        this.name               = name;
        this.description        = description;
        this.choiceDescriptions = new ArrayList<>();
        this.outcomes           = new ArrayList<>();
        this.outcomeMessages    = new ArrayList<>();
        this.requiresWeather    = false;
        this.requiredCondition  = "";
    }

    /**
     * Adds one choice to this event.
     *
     * @param choiceText    What the player sees in the menu (e.g. "Fix it -$2000")
     * @param cashDelta     Change in cash  (negative = spend money)
     * @param moraleDelta   Change in morale
     * @param coffeeDelta   Change in coffee
     * @param hypeDelta     Change in hype
     * @param bugsDelta     Change in bug count (positive = more bugs, negative = fixed bugs)
     * @param resultMessage What gets printed after the player picks this choice
     */
    public void addChoice(String choiceText,
                          int cashDelta, int moraleDelta, int coffeeDelta,
                          int hypeDelta, int bugsDelta,
                          String resultMessage) {
        choiceDescriptions.add(choiceText);
        outcomes.add(new int[]{ cashDelta, moraleDelta, coffeeDelta, hypeDelta, bugsDelta });
        outcomeMessages.add(resultMessage);
    }

    /**
     * Applies the chosen outcome to the game state.
     *
     * WHY WE PASS GameState HERE:
     * The event knows what the effects ARE (stored in outcomes[]).
     * The GameState knows HOW to apply them (via applyChanges).
     * Keeping these responsibilities separate is the "tell, don't ask" principle.
     *
     * @param state       The live game state to modify
     * @param choiceIndex Which choice the player made (0-based)
     */
    public void applyOutcome(GameState state, int choiceIndex) {
        if (choiceIndex < 0 || choiceIndex >= outcomes.size()) return;
        int[] effect = outcomes.get(choiceIndex);
        state.applyChanges(effect[0], effect[1], effect[2], effect[3], effect[4]);
    }

    /** Marks this event as only appearing in specific weather. */
    public void setWeatherCondition(String condition) {
        this.requiresWeather   = true;
        this.requiredCondition = condition;
    }

    // =====================================================================
    // GETTERS
    // =====================================================================
    public String       getName()              { return name; }
    public String       getDescription()       { return description; }
    public List<String> getChoiceDescriptions(){ return choiceDescriptions; }
    public String       getOutcomeMessage(int i){ return outcomeMessages.get(i); }
    public int          getNumberOfChoices()   { return choiceDescriptions.size(); }
    public boolean      isWeatherDependent()   { return requiresWeather; }

    /** True if this weather event matches the current weather condition. */
    public boolean matchesWeather(String currentCondition) {
        return requiredCondition.equals(currentCondition);
    }
}
