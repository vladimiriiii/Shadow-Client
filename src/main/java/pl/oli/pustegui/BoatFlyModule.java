package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;

final class BoatFlyModule {
    private static final double SPEED = 0.35;
    private BoatFlyModule() {}

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.BOAT_FLY.enabled() || client.player == null || client.world == null
                || client.currentScreen != null) return;
        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;
        double forward = axis(client.options.forwardKey.isPressed(), client.options.backKey.isPressed());
        double strafe = axis(client.options.leftKey.isPressed(), client.options.rightKey.isPressed());
        double vertical = axis(client.options.jumpKey.isPressed(), client.options.sneakKey.isPressed());
        float yaw = (float) Math.toRadians(client.player.getYaw());
        double x = -Math.sin(yaw) * forward + Math.cos(yaw) * strafe;
        double z = Math.cos(yaw) * forward + Math.sin(yaw) * strafe;
        double length = Math.sqrt(x * x + z * z);
        if (length > 1) { x /= length; z /= length; }
        boat.setVelocity(x * SPEED, vertical * SPEED, z * SPEED);
    }

    private static double axis(boolean positive, boolean negative) {
        return (positive ? 1 : 0) - (negative ? 1 : 0);
    }
}
