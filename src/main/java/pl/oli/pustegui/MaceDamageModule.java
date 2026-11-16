package pl.oli.pustegui;

/** Settings for the server-side mace damage override in an integrated world. */
public final class MaceDamageModule {
    private static int fallHeight = 100;

    private MaceDamageModule() {}

    public static int fallHeight() {
        return fallHeight;
    }

    public static void adjustFallHeight(int amount) {
        fallHeight = Math.max(4, Math.min(100, fallHeight + amount));
    }
}
