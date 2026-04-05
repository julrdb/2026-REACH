# Silicon Valley Trail - REACH Take Home Assessment

## Summary

Build a small, replayable game called **Silicon Valley Trail** (similar to the game "Oregon Trail"). You'll guide a scrappy startup team on a journey between any two locations, such as from San Jose to San Francisco. Each "day," the team travels, consumes resources, encounters events, and makes choices. The twist: your game must incorporate at least one public API to influence gameplay.

We're not judging art or UI polish (you can make your application a CLI, web-based, mobile-based, VR-based, etc.); we're looking for thoughtful design, clean code, good tradeoffs, and how you integrate external data. Creative additions are a bonus.

---

## Core Requirements

### Game Loop
A sequence of "days" or "turns." Each turn:
- Choose an action with a minimum of 3 options (travel, rest, hackathon, pitch VCs, detour for supplies, etc.)
- Then resolve outcomes.

### Resources & State
Track at least 3 meaningful stats. Examples:
- Cash
- Compute Credits
- Team Morale
- Coffee
- Bug Count
- Tech Debt
- Hype
- Culture

**Winning** happens when the team successfully reaches the destination. Define **losing conditions** (e.g., cash hits zero, morale collapse, run out of coffee causing a loss in two turns if not replenished, etc.).

### Map
Represent progress across at least **10 real, physical locations**. A displayed map is not a requirement.

### Events & Choices
- Implement a diverse set of events (some conditional on API data).
- Provide consequential choices (trade one resource for another, risk vs. reward, etc.).
- An event should happen at each location after movement.
- Events should be at least semi-random, if not completely randomized.

---

## Public Web API Integration (at least 1 required)

- Use live or cached responses to **change gameplay** (not just display data).
- Provide a simple fallback (mock data) so the game runs without secrets or when offline.

### Same Public APIs — a few ideas:

- **Weather** (e.g., Open-Meteo, OpenWeatherMap): Weather affects travel distance, bug rate (on-call stress), or coffee consumption.
  - *Example: Rainy day → slower travel; heat wave → morale drop unless you "buy snacks."*

- **Mapping / Geocoding / Routing** (e.g., OpenStreetMap/Nominatim, Mapbox, Google Maps Directions): Distance between checkpoints sets daily travel cost, traffic or terrain changes resource drains.
  - *Example: Long leg today → extra compute credits to auto-scale, or risk "downtime" leading to increased bugs and decreased hype.*

- **Flight / Plane tracking** (e.g., OpenSky Network): "Supply drops" arrive only if a nearby aircraft is detected, bigger drops if a cargo flight passes.
  - *Example: If an aircraft at or near your current latitude/longitude in the last hour → increase coffee and cash.*

- **News / Trends** (e.g., Hacker News Algolia): Spikes in "Hype" if your keyword trends, risk burnout if hype is high and morale is low.
  - *Example: "tacos" are trending on Hacker News → increase hype and culture.*

> **Note:** Keep API keys out of source control. Use environment variables and supply a `.env.example`.

---

## Required Features

- **Testing:** At least a few unit tests covering critical logic (events, resource updates, win/lose).
- **Documentation:** A concise README (see Deliverables) and Design Notes explaining key choices and tradeoffs.
- **Decency & safety:** Handle API errors/timeouts gracefully, no hard-coded secrets, no collection of any personal user information.

---

## Deliverables

- Source code in a **public repo** (any language/framework)
- **Screen recording** of or URL to the working application
- **README.md** including:
  - Quick start — fully explain how to get your application running from a fresh machine
  - How to set API keys, how to run with mocks
  - Brief architecture overview and dependency list
  - How to run tests
  - Example commands/inputs
  - How, if any, AI was utilized in the creation of the code and/or how the code utilizes AI, if at all
- **Design Notes** (can be a README section):
  - Game loop & balance approach
  - Why you chose your API(s) and how they affect gameplay
  - Data modeling (state, events, persistence)
  - Error handling (network failures, rate limits)
  - Tradeoffs and "if I had more time"
- Tests (unit or integration) for core mechanics

---

## Example Game Flow

```
============================================================
SILICON VALLEY TRAIL - Main Menu
============================================================
1. New Game
2. Load Game
3. Quit
Enter choice (1-3): 1

============================================================
🚀 SILICON VALLEY TRAIL 🚀
============================================================
Your scrappy startup team is embarking on a journey from
San Jose to San Francisco to pitch for Series A funding!

Manage your resources wisely:
  💰 Cash     - Don't run out!
  😊 Morale   - Keep your team happy
  ☕ Coffee   - Essential fuel (2 days without = game over)
  📢 Hype     - Public interest in your startup
  🐛 Bugs     - Keep them under control

Good luck, founder!
============================================================
Press Enter to begin your journey...

============================================================
Day 1 | San Jose
Your startup's humble garage HQ
============================================================
💰 Cash: $50,000 | 😊 Morale: 100/100 | ☕ Coffee: 50
📢 Hype: 50/100  | 🐛 Bugs: 0
📍 Progress: 0% to San Francisco
============================================================
🌤️ Weather: Foggy, 60°F
------------------------------------------------------------
What will you do?
------------------------------------------------------------
1. Travel to next location
2. Rest and recover (restore morale, use coffee)
3. Work on product (reduce bugs, use coffee)
4. Marketing push (increase hype, costs money)
5. Save game
6. Quit to menu
Enter choice (1-6): 1

🚗 Your team hits the road...
✅ Arrived at Santa Clara!

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
📰 EVENT: VC Pitch Opportunity
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
A VC firm on Sand Hill Road wants to hear your pitch!
1. Prepare and pitch ($2000, -10 coffee)
2. Not ready yet, decline
Enter choice (1-2): 1

Great pitch! They're interested in Series A.
Press Enter to continue...

============================================================
Day 2 | Santa Clara
Home of tech giants
============================================================
💰 Cash: $47,500 | 😊 Morale: 100/100 | ☕ Coffee: 37
📢 Hype: 75/100  | 🐛 Bugs: 1
📍 Progress: 10% to San Francisco
============================================================
🌤️ Weather: Foggy, 64°F
------------------------------------------------------------
What will you do?
------------------------------------------------------------
1. Travel to next location
2. Rest and recover (restore morale, use coffee)
3. Work on product (reduce bugs, use coffee)
4. Marketing push (increase hype, costs money)
5. Save game
6. Quit to menu
Enter choice (1-6): 3

💻 Your team focuses on squashing bugs...
✅ Productive day! Bugs reduced, but team is tired.

============================================================
Day 3 | Santa Clara
Home of tech giants
============================================================
💰 Cash: $47,500 | 😊 Morale: 97/100 | ☕ Coffee: 29
📢 Hype: 75/100  | 🐛 Bugs: 0
📍 Progress: 10% to San Francisco
============================================================
🌤️ Weather: Scattered clouds, 66°F
------------------------------------------------------------
What will you do?
------------------------------------------------------------
1. Travel to next location
2. Rest and recover (restore morale, use coffee)
3. Work on product (reduce bugs, use coffee)
4. Marketing push (increase hype, costs money)
5. Save game
6. Quit to menu
Enter choice (1-6): 4

📢 You launch a marketing campaign...
✅ Campaign launched! Hype increased. (Cost: $1500)

============================================================
Day 4 | Santa Clara
Home of tech giants
============================================================
💰 Cash: $46,000 | 😊 Morale: 97/100 | ☕ Coffee: 26
📢 Hype: 90/100  | 🐛 Bugs: 0
📍 Progress: 10% to San Francisco
============================================================
🌤️ Weather: Moderate rain, 58°F
The rain slows your progress and stresses the team.
------------------------------------------------------------
What will you do?
------------------------------------------------------------
1. Travel to next location
2. Rest and recover (restore morale, use coffee)
3. Work on product (reduce bugs, use coffee)
4. Marketing push (increase hype, costs money)
5. Save game
6. Quit to menu
Enter choice (1-6): 5

💾 Game saved!

[... game continues ...]

============================================================
SILICON VALLEY TRAIL - Main Menu
============================================================
1. New Game
2. Load Game
3. Quit
Enter choice (1-3): 2

✅ Game loaded successfully!
```
