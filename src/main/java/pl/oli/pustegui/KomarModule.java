package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

final class KomarModule {
    private static final double SEARCH_RANGE_SQUARED = 128.0 * 128.0;
    private static final double FOLLOW_DISTANCE = 3.5;
    private static AbstractClientPlayerEntity target;

    private KomarModule() {}

    static void reset() { target = null; }

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.KOMAR.enabled() || client.player == null || client.world == null
                || client.currentScreen != null) {
            reset();
            return;
        }
        var player = client.player;
        if (!player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) || !player.isGliding()) return;
        if (target == null || !target.isAlive() || target.isSpectator()
                || player.squaredDistanceTo(target) > SEARCH_RANGE_SQUARED
                || !client.world.getPlayers().contains(target)) {
            target = null;
            double nearest = SEARCH_RANGE_SQUARED;
            for (AbstractClientPlayerEntity candidate : client.world.getPlayers()) {
                if (candidate == player || !candidate.isAlive() || candidate.isSpectator()) continue;
                double distance = player.squaredDistanceTo(candidate);
                if (distance < nearest) { nearest = distance; target = candidate; }
            }
        }
        if (target == null) return;

        Vec3d targetVelocity = target.getVelocity();
        Vec3d horizontal = new Vec3d(targetVelocity.x, 0, targetVelocity.z);
        if (horizontal.lengthSquared() < 0.0025) {
            double yaw = Math.toRadians(target.getYaw());
            horizontal = new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw));
        } else horizontal = horizontal.normalize();
        Vec3d destination = target.getPos().add(0, target.getHeight() * 0.5, 0)
                .subtract(horizontal.multiply(FOLLOW_DISTANCE));
        Vec3d delta = destination.subtract(player.getPos().add(0, player.getHeight() * 0.5, 0));
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float desiredYaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(delta.y, Math.max(horizontalDistance, 0.001)));
        player.setYaw(player.getYaw() + clamp(wrap(desiredYaw - player.getYaw()) * 0.18f, -6, 6));
        player.setPitch(player.getPitch() + clamp((desiredPitch - player.getPitch()) * 0.18f, -3.5f, 3.5f));
    }

    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private static float wrap(float value) {
        value %= 360;
        if (value >= 180) value -= 360;
        if (value < -180) value += 360;
        return value;
    }
}
