package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Holds the player at the position where Stop was enabled. */
final class StopModule {
    private static ClientPlayerEntity player;
    private static double x;
    private static double y;
    private static double z;
    private static boolean previousNoGravity;

    private StopModule() {}

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.STOP.enabled() || client.player == null || client.world == null) {
            reset();
            return;
        }

        if (player != client.player) {
            reset();
            player = client.player;
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            previousNoGravity = player.hasNoGravity();
        }

        player.setNoGravity(true);
        player.setPosition(x, y, z);
        player.setVelocity(0.0, 0.0, 0.0);
        player.fallDistance = 0.0f;
    }

    static void reset() {
        if (player != null) {
            player.setNoGravity(previousNoGravity);
            player = null;
        }
    }
}
