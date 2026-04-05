package com.siliconvalleytrail;

/**
 * Main.java — The entry point of the program.
 *
 * WHY THIS EXISTS:
 * Java requires exactly one class to have the "main" method that the JVM calls
 * when you run the program. Keeping it tiny (just one line) is a common pattern:
 * the real logic lives in Game.java. This keeps Main clean and easy to find.
 */
public class Main {

    public static void main(String[] args) {
        Env.init();
        Game game = new Game();
        game.start();
    }
}
