# Silicon Valley Trail

A text-based startup survival game inspired by The Oregon Trail. Guide your scrappy team from San Jose to San Francisco for a Series A pitch — managing cash, morale, coffee, hype, and bugs along the way.

Built in Java for the REACH Program take-home assessment.

---

## Quick Start

### Prerequisites
- Java 11 or higher  (`java -version`)
- Maven 3.6 or higher  (`mvn -version`)

### Run the Game

```bash
cd SiliconValleyTrail
mvn package -q
java -jar target/silicon-valley-trail-1.0-SNAPSHOT.jar
```

### Run Tests

```bash
mvn test
```

---

## API Keys

No API keys are required. The game uses [Open-Meteo](https://open-meteo.com/) for live weather — it's free and keyless.

If the API is unavailable (no internet, timeout), the game automatically falls back to randomized mock weather. You will never see a crash or an error from the API failing.

See `.env.example` for notes on adding future API integrations.

---

## Architecture Overview

```
src/main/java/com/siliconvalleytrail/
├── Main.java          Entry point — creates Game and calls start()
├── Game.java          Main game loop; coordinates all other classes
├── GameState.java     All mutable game data (cash, morale, coffee, hype, bugs)
├── Location.java      A city on the trail (name, description, GPS coords)
├── Weather.java       Weather data + gameplay effect calculations
├── WeatherService.java  Calls Open-Meteo API; falls back to mock on failure
├── Event.java         One random event with 1-3 player choices
├── EventSystem.java   Owns and selects all 20 events (16 general + 4 weather)
├── Display.java       All printed output (no System.out.println in other classes)
└── SaveManager.java   Saves/loads GameState via a plain-text .properties file

src/test/java/com/siliconvalleytrail/
├── GameStateTest.java      25 tests for resource clamping, win/lose conditions
├── EventSystemTest.java    Tests for event loading, applyOutcome(), weather matching
└── WeatherServiceTest.java Tests for JSON parsing, mock fallback, penalty calculations
```

### Dependencies

| Library | Purpose | Version |
|---------|---------|---------|
| JUnit Jupiter | Unit testing | 5.10.0 |

No other external libraries. JSON is parsed manually from the API response using standard `String` operations, keeping the dependency footprint minimal.

---

## Game Flow

```
Main Menu
├── New Game  →  GameState initialized with starting resources
│                  Loop:
│                  1. Fetch live weather (Open-Meteo API or mock)
│                  2. Display: status, weather, ASCII map, action menu
│                  3. Player chooses: Travel / Rest / Work / Market / Supplies / Save / Quit
│                  4. Apply weather effects at end of turn
│                  5. If traveled: trigger random event
│                  6. Check win (reached SF) or lose (cash/morale/coffee/bugs)
│                  7. Advance day
│
├── Load Game →  Reads savegame.properties, restores GameState, resumes loop
├── How to Play
└── Quit
```

---

## Design Notes

### Game Loop & Balance

The game takes a minimum of 10 turns to win (one travel per city). Starting resources are tuned so the player must make tradeoffs:

- Coffee (50 units) depletes ~3/travel + 5/rest — requires roughly 2 supply runs to finish comfortably.
- Cash ($50,000) drains from events (server outage, counter-offers) and marketing.
- Morale drops from travel and bad decisions; resting recovers it but costs coffee.
- Bugs accumulate from weather and bad event choices; fixing them costs coffee and morale.

The interdependence of resources means there's no "always correct" strategy.

### API Integration — Open-Meteo

Open-Meteo was chosen because it requires no API key, has no rate limits for small projects, and returns accurate real-world weather for the Bay Area cities on the trail.

**How weather affects gameplay:**
- Rainy/stormy: -8 morale, -2 coffee, +1-2 bugs per turn
- Foggy: -3 morale
- Heat wave (>92°F): -10 morale, +1 bug
- Cold (<50°F): +3 coffee consumption

Additionally, weather-specific events (thunderstorm sprint, fog delay, heat wave) have a 30% chance of appearing on matching weather days.

**Fallback:** If the API call fails for any reason (no internet, timeout, HTTP error), `WeatherService.getMockWeather()` returns a randomized but realistic Bay Area weather object. The player never sees an error.

### Data Modeling

`GameState` is the single source of truth for all mutable game data. All resource changes go through `applyChanges()`, which enforces clamping (no negative cash, morale capped at 100). This means it's impossible to accidentally put the game in an invalid state.

`SaveManager` uses Java's `Properties` class to write/read a plain key=value text file (`savegame.properties`). This format is human-readable — players can inspect or edit it, and it handles missing/corrupt keys with safe defaults.

### Error Handling

| Failure | Response |
|---------|----------|
| API timeout (5s) | Falls back to mock weather; game continues |
| HTTP non-200 response | Falls back to mock weather |
| JSON parsing failure | Falls back to mock weather |
| Save file corrupt/missing keys | Loads default values instead of crashing |
| Player enters non-number input | Re-prompts without crashing |

### Tradeoffs & "If I Had More Time"

- **No color output.** ANSI escape codes would add visual polish but require testing across terminals. The emojis provide sufficient visual variety without that risk.
- **Events don't react to game state.** A future improvement: conditional events (e.g., a "bug bounty" event only appears when bugs > 5, or a "VC pitch" event only appears when hype > 70).
- **Single save slot.** A multi-slot system could be added with a save name prompt.
- **No difficulty modes.** Adding Easy/Hard would just mean adjusting the constants in `GameState`.
- **Hacker News API.** The prompt suggested Hacker News for hype events. I used weather (Open-Meteo) because it gives geographically relevant data tied to the actual cities on the route, making gameplay feel more grounded in the real Bay Area.

---

## How AI Was Used

Claude Code (Anthropic) was used to scaffold this project. The game's architecture, class design, event writing, balance tuning, and all comments were written with AI assistance. The student will review, explain, and iterate on all the code at the interview.
