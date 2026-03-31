package com.siliconvalleytrail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * EventSystem.java — Owns and selects all the random events in the game.
 *
 * WHY A SEPARATE CLASS:
 * There are 18+ events, each with 2-3 choices. If we defined them inside Game.java,
 * that file would be hundreds of extra lines and very hard to read. Separating this
 * out means Game.java just calls eventSystem.getRandomEvent() and doesn't need to
 * know anything about what events exist or how they're selected.
 *
 * This is also easy to extend: want to add a new event? Just add it in loadEvents()
 * and nothing else changes.
 *
 * EVENT SELECTION LOGIC:
 * - 30% chance of a weather-specific event (if one matches today's weather)
 * - 70% chance of a random general event
 */
public class EventSystem {

    private List<Event> generalEvents;  // Can happen any day
    private List<Event> weatherEvents;  // Only when weather conditions match
    private Random      random;

    public EventSystem() {
        this.generalEvents = new ArrayList<>();
        this.weatherEvents = new ArrayList<>();
        this.random        = new Random();
        loadEvents();
    }

    /**
     * Picks a random event appropriate for the current game state and weather.
     *
     * @param state   Current game state (could be used for conditional events in future)
     * @param weather Today's weather
     * @return A randomly selected Event
     */
    public Event getRandomEvent(GameState state, Weather weather) {
        // Gather weather events that match today's condition
        List<Event> matchingWeatherEvents = new ArrayList<>();
        for (Event e : weatherEvents) {
            if (e.matchesWeather(weather.getCondition())) {
                matchingWeatherEvents.add(e);
            }
        }

        // 30% chance of a weather event (if any match)
        if (!matchingWeatherEvents.isEmpty() && random.nextInt(100) < 30) {
            return matchingWeatherEvents.get(random.nextInt(matchingWeatherEvents.size()));
        }

        // Otherwise: random general event
        return generalEvents.get(random.nextInt(generalEvents.size()));
    }

    // =====================================================================
    // ALL GAME EVENTS DEFINED HERE
    //
    // Format per event:
    //   addChoice(choiceText, cashDelta, moraleDelta, coffeeDelta, hypeDelta, bugsDelta, resultMsg)
    // =====================================================================
    private void loadEvents() {

        // --- EVENT 1: Server Outage ---
        Event serverOutage = new Event(
            "Server Outage!",
            "Your production servers went down. Angry tweets are flooding in."
        );
        serverOutage.addChoice(
            "Emergency all-hands fix  (-$2,000  -8 coffee)",
            -2000, 0, -8, -5, -3,
            "Crisis averted. The team worked through the night but stabilized everything."
        );
        serverOutage.addChoice(
            "Post a status page and wait it out",
            0, -15, 0, -10, 5,
            "Downtime stretched on. Users are not happy. Bugs piled up."
        );
        generalEvents.add(serverOutage);

        // --- EVENT 2: VC Pitch ---
        Event vcPitch = new Event(
            "VC Pitch Opportunity!",
            "A partner at a Sand Hill Road firm wants to hear your pitch. They have $500k ready."
        );
        vcPitch.addChoice(
            "Prepare and pitch  (-$2,000  -10 coffee  +$15,000 if it goes well)",
            13000, 5, -10, 25, 0,
            "Great pitch! They wire $15k seed money and ask for a follow-up."
        );
        vcPitch.addChoice(
            "Not ready yet — decline",
            0, 0, 0, -5, 0,
            "You pass. Maybe next time. They nod respectfully."
        );
        generalEvents.add(vcPitch);

        // --- EVENT 3: Hackathon ---
        Event hackathon = new Event(
            "Local Hackathon!",
            "A 48-hour hackathon is happening at the Computer History Museum. Join in?"
        );
        hackathon.addChoice(
            "Participate!  (-20 morale  -15 coffee  -3 bugs  +20 hype)",
            0, -20, -15, 20, -3,
            "You placed 2nd! Press coverage rolls in. Worth the exhaustion."
        );
        hackathon.addChoice(
            "Skip — we have real deadlines",
            0, 0, 0, 0, 0,
            "You focus on the product. Sometimes boring is the right call."
        );
        generalEvents.add(hackathon);

        // --- EVENT 4: Key Developer Quits ---
        Event keyDevQuits = new Event(
            "Lead Developer Resigns!",
            "Your lead engineer just got a $400k FAANG offer. They're leaving Friday."
        );
        keyDevQuits.addChoice(
            "Counter offer: $5k retention bonus  (-$5,000  +10 morale)",
            -5000, 10, 0, 0, 0,
            "They accepted! The team noticed you fought for them. Respect."
        );
        keyDevQuits.addChoice(
            "Wish them well and redistribute the work",
            0, -20, 0, 0, 5,
            "The remaining team is stretched thin. Bugs are creeping in."
        );
        generalEvents.add(keyDevQuits);

        // --- EVENT 5: Post Goes Viral ---
        Event viralPost = new Event(
            "LinkedIn Post Goes Viral!",
            "Your founder's post about startup culture got 50k reactions overnight!"
        );
        viralPost.addChoice(
            "Keep posting — ride the wave!  (+25 hype  +10 morale)",
            0, 10, 0, 25, 0,
            "Engagement is through the roof. Your waitlist doubled."
        );
        viralPost.addChoice(
            "Stay quiet — authenticity is dead if you chase it",
            0, 5, 0, 5, 0,
            "Your team respects the integrity. Small hype bump from word-of-mouth."
        );
        generalEvents.add(viralPost);

        // --- EVENT 6: Coffee Machine Broken ---
        Event coffeeMachineBroken = new Event(
            "Office Coffee Machine Broke!",
            "The Breville just died. The team has headaches and glazed eyes."
        );
        coffeeMachineBroken.addChoice(
            "Buy a new one  (-$500  +15 coffee  +5 morale)",
            -500, 5, 15, 0, 0,
            "New machine arrives same-day on Amazon Prime. Crisis averted."
        );
        coffeeMachineBroken.addChoice(
            "Send everyone to Philz  (-$200  +8 coffee  +8 morale)",
            -200, 8, 8, 5, 0,
            "Everyone loves Philz. Morale surges. A little expensive but worth it."
        );
        coffeeMachineBroken.addChoice(
            "Tough it out  (-15 morale  +3 bugs)",
            0, -15, 0, 0, 3,
            "Productivity tanks. Three people got headaches and left early."
        );
        generalEvents.add(coffeeMachineBroken);

        // --- EVENT 7: Technical Debt ---
        Event techDebt = new Event(
            "Technical Debt Is Crushing You!",
            "Every new feature takes three times as long now. The codebase is a maze."
        );
        techDebt.addChoice(
            "Refactor sprint  (-10 morale  -8 coffee  -5 bugs)",
            0, -10, -8, 0, -5,
            "The code is cleaner! It hurt, but future-you is grateful."
        );
        techDebt.addChoice(
            "Ignore it and ship features anyway  (+5 hype  +5 bugs)",
            0, 0, 0, 5, 5,
            "Hype goes up but the bugs are getting worse. Kicking the can."
        );
        generalEvents.add(techDebt);

        // --- EVENT 8: Product Hunt Launch ---
        Event productHunt = new Event(
            "Product Hunt Launch!",
            "Your community manager says it's the perfect time to launch on Product Hunt."
        );
        productHunt.addChoice(
            "Launch today!  (+20 hype  +10 morale  -5 coffee)",
            0, 10, -5, 20, 0,
            "You hit #2 Product of the Day! New signups flooded in."
        );
        productHunt.addChoice(
            "Wait until we fix a few more bugs first",
            0, 5, 0, 0, -1,
            "Good call. You shipped quality over hype."
        );
        generalEvents.add(productHunt);

        // --- EVENT 9: Free Office Space ---
        Event freeOffice = new Event(
            "Free Co-Working Space Offer!",
            "A Menlo Park co-working space is offering 3 months free to early startups."
        );
        freeOffice.addChoice(
            "Take the free space — save on costs  (+$3,000)",
            3000, 5, 0, 0, 0,
            "You save $3k on rent this month. Free pizza on Thursdays too."
        );
        freeOffice.addChoice(
            "Stay in the garage — no disruption",
            0, 0, 0, 0, 0,
            "Stability over savings. The team appreciates the routine."
        );
        generalEvents.add(freeOffice);

        // --- EVENT 10: Security Vulnerability ---
        Event securityBug = new Event(
            "Security Vulnerability Disclosed!",
            "A researcher found a SQL injection bug in your login flow — publicly."
        );
        securityBug.addChoice(
            "Fix immediately + write a public post-mortem  (-$1,000  +10 hype  -3 bugs)",
            -1000, 5, 0, 10, -3,
            "Users respect the radical transparency. Trust scores up."
        );
        securityBug.addChoice(
            "Patch quietly without announcing  (-2 bugs  -10 hype)",
            0, -5, 0, -10, -2,
            "Some users noticed the secrecy. Trust took a hit."
        );
        generalEvents.add(securityBug);

        // --- EVENT 11: Sponsored Lunch ---
        Event sponsoredLunch = new Event(
            "Free Team Lunch Offer!",
            "A food-delivery startup wants to sponsor lunch in exchange for a social shoutout."
        );
        sponsoredLunch.addChoice(
            "Accept — best tacos anyone's ever had  (+$500  +20 morale  +5 coffee)",
            500, 20, 5, 5, 0,
            "The tacos were incredible. The team is glowing."
        );
        sponsoredLunch.addChoice(
            "Decline — we're not for sale",
            0, 5, 0, 0, 0,
            "The team respects the integrity call. Brought their own lunch."
        );
        generalEvents.add(sponsoredLunch);

        // --- EVENT 12: API Rate Limit ---
        Event rateLimit = new Event(
            "API Rate Limit Hit!",
            "You hit your AI API limit mid-demo. The investor meeting is in 2 hours."
        );
        rateLimit.addChoice(
            "Upgrade to the paid tier immediately  (-$1,000  +5 morale)",
            -1000, 5, 0, 0, 2,
            "Problem solved. Demo goes flawlessly. Costs are up."
        );
        rateLimit.addChoice(
            "Build a quick local fallback  (-5 morale  -5 coffee  -1 bug)",
            0, -5, -5, 0, -1,
            "Hacky but it works. The team pulled off a miracle."
        );
        generalEvents.add(rateLimit);

        // --- EVENT 13: TechCrunch Coverage ---
        Event pressRelease = new Event(
            "TechCrunch Wants to Cover You!",
            "A reporter reached out about a feature on AI startups in the Bay Area."
        );
        pressRelease.addChoice(
            "Give the interview  (+20 hype  +10 morale)",
            0, 10, 0, 20, 0,
            "Great article! 'Making Waves in Silicon Valley.' Your inbox exploded."
        );
        pressRelease.addChoice(
            "Too busy — decline",
            0, 0, 0, -5, 0,
            "You pass. They wrote about your competitor instead. Unfortunate."
        );
        generalEvents.add(pressRelease);

        // --- EVENT 14: Bay Area Traffic ---
        Event traffic = new Event(
            "Bay Area Traffic Nightmare!",
            "The 101 is a parking lot. The team is stuck for 3 hours."
        );
        traffic.addChoice(
            "Hold a voice standup from the carpool lane",
            0, -5, -2, 0, -1,
            "Surprisingly productive. Remote standup from the carpool lane."
        );
        traffic.addChoice(
            "Everyone suffers in silence",
            0, -15, 0, 0, 0,
            "Brutal commute. Everyone arrived stressed and behind schedule."
        );
        generalEvents.add(traffic);

        // --- EVENT 15: Open Source Contribution ---
        Event openSource = new Event(
            "Open Source Opportunity!",
            "A popular library has a critical bug your team can fix easily."
        );
        openSource.addChoice(
            "Fix it and submit a PR  (-5 coffee  -2 bugs  +10 hype)",
            0, 10, -5, 10, -2,
            "Your PR was merged! You're a contributor now. Community loves you."
        );
        openSource.addChoice(
            "Not now — focus on our own product",
            0, 0, 0, 0, 0,
            "Fair call. You stay laser-focused."
        );
        generalEvents.add(openSource);

        // --- EVENT 16: Recruiter Poaching ---
        Event recruiter = new Event(
            "Big Tech Recruiter Targeting Your Team!",
            "A Google recruiter is DM-ing your entire team. Two are responding."
        );
        recruiter.addChoice(
            "Offer spot bonuses to retain them  (-$3,000  +15 morale)",
            -3000, 15, 0, 0, 0,
            "Team feels valued. Both stay. Worth every dollar."
        );
        recruiter.addChoice(
            "Trust that the mission is enough",
            0, -10, 0, 0, 3,
            "One person left. The backlog grew. Morale dipped."
        );
        generalEvents.add(recruiter);

        // =====================================================================
        // WEATHER-SPECIFIC EVENTS
        // These only appear when the weather matches their condition.
        // =====================================================================

        // --- WEATHER EVENT: Rainy ---
        Event rainEvent = new Event(
            "Heavy Rain Hits the Bay Area!",
            "The rain is brutal today. Commutes are miserable, spirits are low."
        );
        rainEvent.addChoice(
            "Order pizza and keep grinding  (-$200  +5 morale)",
            -200, 5, 0, 0, 0,
            "Pizza to the rescue! Team spirits lifted. Rain outside, cozy inside."
        );
        rainEvent.addChoice(
            "Let everyone work from home today  (+5 morale  +1 bug)",
            0, 5, 0, 0, 1,
            "Morale saved, but without in-person collaboration a bug slipped through."
        );
        rainEvent.setWeatherCondition("rainy");
        weatherEvents.add(rainEvent);

        // --- WEATHER EVENT: Foggy ---
        Event fogEvent = new Event(
            "SF Fog Grounds Everything!",
            "Classic Bay fog. So thick you can barely see the parking lot."
        );
        fogEvent.addChoice(
            "Push through — mission critical day  (-3 morale  -3 coffee)",
            0, -3, -3, 0, 0,
            "You battle the elements. The team respects your grit."
        );
        fogEvent.addChoice(
            "Delay start time — everyone gets extra sleep  (+5 morale)",
            0, 5, 0, 0, 0,
            "Everyone appreciated the extra hour. Morale boosted."
        );
        fogEvent.setWeatherCondition("foggy");
        weatherEvents.add(fogEvent);

        // --- WEATHER EVENT: Hot/Stormy ---
        Event stormSprint = new Event(
            "Thunderstorm Sprint!",
            "A rare Bay Area thunderstorm is raging. The team is weirdly energized."
        );
        stormSprint.addChoice(
            "Declare a Storm Sprint — all hands on deck!  (-10 coffee  -5 bugs  +10 hype)",
            0, 5, -10, 10, -5,
            "Energy through the roof! The storm sprint cleared the backlog."
        );
        stormSprint.addChoice(
            "Dial it back — can't focus with the thunder",
            0, 0, 0, 0, 0,
            "Reasonable. Sometimes you just ride it out."
        );
        stormSprint.setWeatherCondition("stormy");
        weatherEvents.add(stormSprint);

        // --- WEATHER EVENT: Sunny / Heat Wave ---
        Event heatWave = new Event(
            "Heat Wave Warning!",
            "It's 95°F and the office AC is struggling. The team is melting."
        );
        heatWave.addChoice(
            "Buy everyone iced drinks + fans  (-$300  +10 morale  +5 coffee)",
            -300, 10, 5, 5, 0,
            "You're a hero. Productivity restored. Team morale surges."
        );
        heatWave.addChoice(
            "Push through the heat",
            0, -15, 0, 0, 3,
            "The heat is brutal. Nobody can think straight. Bug count rising."
        );
        heatWave.setWeatherCondition("sunny");
        weatherEvents.add(heatWave);
    }

    // Getters (used in tests)
    public List<Event> getGeneralEvents() { return generalEvents; }
    public List<Event> getWeatherEvents() { return weatherEvents; }
}
