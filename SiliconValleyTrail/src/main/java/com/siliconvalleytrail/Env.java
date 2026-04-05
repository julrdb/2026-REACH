package com.siliconvalleytrail;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Resolves configuration from the OS environment first, then from a local {@code .env} file
 * in the working directory (typically the {@code SiliconValleyTrail} folder when using Maven
 * or your IDE). OS variables win so CI and production can inject secrets without a file.
 */
public final class Env {

    private static final Dotenv DOTENV = Dotenv.configure()
            .directory("./")
            .ignoreIfMissing()
            .load();

    private Env() {}

    /**
     * Ensures the {@code .env} file is loaded early. Call from {@link Main#main}.
     */
    public static void init() {
        // Class loading already initialized DOTENV; empty body keeps intent explicit.
    }

    /**
     * Returns a variable from the environment or {@code .env}, or {@code null} if unset.
     */
    public static String get(String key) {
        String fromOs = System.getenv(key);
        if (fromOs != null && !fromOs.isEmpty()) {
            return fromOs;
        }
        String fromFile = DOTENV.get(key);
        return (fromFile == null || fromFile.isEmpty()) ? null : fromFile;
    }
}
