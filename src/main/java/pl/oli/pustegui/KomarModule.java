package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

final class KomarModule {
    private static final double SEARCH_RANGE_SQUARED = 128.0 * 128.0;
    private static final double FOLLOW_DISTANCE = 4.0;
    private static final float MAX_YAW_SPEED = 5.5F;
    private static final float MAX_PITCH_SPEED = 2.5F;
    private static final float YAW_ACCELERATION = 0.65F;
    private static final float PITCH_ACCELERATION = 0.3F;
    private static final float YAW_RESPONSE = 0.28F;
    private static final float PITCH_RESPONSE = 0.25F;
    private static AbstractClientPlayerEntity target;
    private static float yawTurnSpeed;
    private static float pitchTurnSpeed;
    private static int fireworkIntervalSeconds = 3;
    private static long lastFireworkTime;

    private KomarModule() {}

    static void reset() {
        target = null;
        lastFireworkTime = 0;
        yawTurnSpeed = 0;
        pitchTurnSpeed = 0;
    }

    static int fireworkIntervalSeconds() {
        return fireworkIntervalSeconds;
    }

    static void cycleFireworkInterval() {
        fireworkIntervalSeconds = fireworkIntervalSeconds % 10 + 1;
    }

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.KOMAR.enabled() || client.player == null || client.world == null) {
            reset();
            return;
        }
        if (client.currentScreen != null) return;

        var player = client.player;
        if (!player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) || !player.isGliding()) return;

        if (!isValidTarget(client, player, target)) target = findNearestTarget(client, player);
        if (target == null) return;

        steerTowardTarget(player, target);
        tryUseFirework(client, player);
    }

    private static boolean isValidTarget(MinecraftClient client, AbstractClientPlayerEntity player,
                                         AbstractClientPlayerEntity candidate) {
        return candidate != null && candidate != player && candidate.isAlive() && !candidate.isSpectator()
                && client.world.getPlayers().contains(candidate);
    }

    private static AbstractClientPlayerEntity findNearestTarget(MinecraftClient client,
                                                                 AbstractClientPlayerEntity player) {
        AbstractClientPlayerEntity nearest = null;
        double nearestDistance = SEARCH_RANGE_SQUARED;
        for (AbstractClientPlayerEntity candidate : client.world.getPlayers()) {
            if (candidate == player || !candidate.isAlive() || candidate.isSpectator()) continue;
            double distance = player.squaredDistanceTo(candidate);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static void steerTowardTarget(AbstractClientPlayerEntity player,
                                          AbstractClientPlayerEntity target) {
        Vec3d playerCenter = player.getPos().add(0, player.getHeight() * 0.5, 0);
        Vec3d targetCenter = target.getPos().add(0, target.getHeight() * 0.5, 0);
        Vec3d targetVelocity = target.getVelocity();

        double horizontalSpeed = Math.sqrt(player.getVelocity().x * player.getVelocity().x
                + player.getVelocity().z * player.getVelocity().z);
        double distance = playerCenter.distanceTo(targetCenter);
        double leadTicks = clamp(distance / Math.max(horizontalSpeed, 0.5), 0.0, 10.0);
        Vec3d predictedCenter = targetCenter.add(targetVelocity.multiply(leadTicks));

        Vec3d targetHeading = new Vec3d(targetVelocity.x, 0, targetVelocity.z);
        if (targetHeading.lengthSquared() < 0.0025) {
            double yaw = Math.toRadians(target.getYaw());
            targetHeading = new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw));
        } else {
            targetHeading = targetHeading.normalize();
        }

        Vec3d destination = predictedCenter.subtract(targetHeading.multiply(FOLLOW_DISTANCE));
        Vec3d direction = destination.subtract(playerCenter);
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float desiredYaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(direction.y,
                Math.max(horizontalDistance, 0.001)));

        float yawError = wrap(desiredYaw - player.getYaw());
        float pitchError = desiredPitch - player.getPitch();
        yawTurnSpeed = smoothTurnSpeed(yawError, yawTurnSpeed, MAX_YAW_SPEED,
                YAW_ACCELERATION, YAW_RESPONSE);
        pitchTurnSpeed = smoothTurnSpeed(pitchError, pitchTurnSpeed, MAX_PITCH_SPEED,
                PITCH_ACCELERATION, PITCH_RESPONSE);
        if (Math.abs(yawError) < 0.2F && Math.abs(yawTurnSpeed) < 0.08F) yawTurnSpeed = 0;
        if (Math.abs(pitchError) < 0.2F && Math.abs(pitchTurnSpeed) < 0.08F) pitchTurnSpeed = 0;

        float yaw = player.getYaw() + yawTurnSpeed;
        float pitch = player.getPitch() + pitchTurnSpeed;
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setHeadYaw(yaw);
        player.setBodyYaw(yaw);
    }

    private static void tryUseFirework(MinecraftClient client, AbstractClientPlayerEntity player) {
        long now = System.currentTimeMillis();
        if (now - lastFireworkTime < fireworkIntervalSeconds * 1_000L || client.interactionManager == null) return;

        int rocketSlot = findFireworkSlot(player);
        if (rocketSlot < 0) return;

        int previousSlot = player.getInventory().selectedSlot;
        if (rocketSlot != previousSlot) {
            player.getInventory().selectedSlot = rocketSlot;
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(rocketSlot));
        }

        client.interactionManager.interactItem(player, Hand.MAIN_HAND);

        if (rocketSlot != previousSlot) {
            player.getInventory().selectedSlot = previousSlot;
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
        lastFireworkTime = now;
    }

    private static int findFireworkSlot(AbstractClientPlayerEntity player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getStack(slot).isOf(Items.FIREWORK_ROCKET)) return slot;
        }
        return -1;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float smoothTurnSpeed(float error, float currentSpeed, float maxSpeed,
                                         float acceleration, float response) {
        float desiredSpeed = clamp(error * response, -maxSpeed, maxSpeed);
        float speedChange = clamp(desiredSpeed - currentSpeed, -acceleration, acceleration);
        return currentSpeed + speedChange;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float wrap(float value) {
        value %= 360;
        if (value >= 180) value -= 360;
        if (value < -180) value += 360;
        return value;
    }
}
