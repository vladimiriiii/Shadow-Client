package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

final class KillauraModule {
    private static final double RANGE_SQUARED = 9.0;
    private KillauraModule() {}

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.KILLAURA.enabled() || client.player == null || client.world == null
                || client.interactionManager == null || client.currentScreen != null) return;
        PlayerEntity player = client.player;
        PlayerEntity target = null;
        double nearest = RANGE_SQUARED;
        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (candidate == player || !candidate.isAlive() || candidate.isSpectator()) continue;
            double distance = player.squaredDistanceTo(candidate);
            if (distance <= nearest) { target = candidate; nearest = distance; }
        }
        if (target == null) return;
        Vec3d direction = target.getBoundingBox().getCenter().subtract(player.getEyePos());
        float yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(direction.y,
                Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setHeadYaw(yaw);
        player.setBodyYaw(yaw);
        if (player.getAttackCooldownProgress(0.5F) >= 1.0F) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
}
