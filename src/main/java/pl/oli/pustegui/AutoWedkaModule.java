package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

final class AutoWedkaModule {
    private static final double SEARCH_RANGE_SQUARED = 32.0 * 32.0;
    private static final long ACTION_DELAY_MS = 1_000L;
    private static final long MISSED_HOOK_REEL_DELAY_MS = 1_800L;
    private static boolean aimedForCast;
    private static long lastRodActionTime;
    private static int slotToRestore = -1;

    private AutoWedkaModule() {}

    static void restorePendingSlot(MinecraftClient client) {
        if (slotToRestore < 0) return;
        if (client.player == null || client.getNetworkHandler() == null) {
            slotToRestore = -1;
            return;
        }
        client.player.getInventory().selectedSlot = slotToRestore;
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotToRestore));
        slotToRestore = -1;
    }

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.AUTO_WEDKA.enabled() || client.player == null || client.world == null) {
            aimedForCast = false;
            lastRodActionTime = 0;
            return;
        }
        if (client.currentScreen != null || client.interactionManager == null
                || client.getNetworkHandler() == null) return;

        var player = client.player;
        AbstractClientPlayerEntity target = findNearestTarget(client, player);
        if (target == null) {
            aimedForCast = false;
            return;
        }

        long now = System.currentTimeMillis();
        FishingBobberEntity hook = player.fishHook;
        if (hook != null) {
            aimedForCast = false;
            if (hook.getHookedEntity() == target || now - lastRodActionTime < MISSED_HOOK_REEL_DELAY_MS) return;
            int rodSlot = findRodSlot(player);
            if (rodSlot >= 0) useRod(client, player, rodSlot, now);
            return;
        }

        int rodSlot = findRodSlot(player);
        if (rodSlot < 0 || now - lastRodActionTime < ACTION_DELAY_MS) {
            aimedForCast = false;
            return;
        }

        aimAtTarget(player, target);
        if (!aimedForCast) {
            aimedForCast = true;
            return;
        }
        useRod(client, player, rodSlot, now);
        aimedForCast = false;
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

    private static void aimAtTarget(AbstractClientPlayerEntity player, AbstractClientPlayerEntity target) {
        Vec3d direction = target.getEyePos().subtract(player.getEyePos());
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(direction.y, Math.max(horizontalDistance, 0.001)));
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setHeadYaw(yaw);
        player.setBodyYaw(yaw);
    }

    private static void useRod(MinecraftClient client, AbstractClientPlayerEntity player, int rodSlot, long now) {
        int previousSlot = player.getInventory().selectedSlot;
        if (rodSlot != previousSlot) {
            player.getInventory().selectedSlot = rodSlot;
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(rodSlot));
        }

        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);

        if (rodSlot != previousSlot) {
            slotToRestore = previousSlot;
        }
        lastRodActionTime = now;
    }

    private static int findRodSlot(AbstractClientPlayerEntity player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getStack(slot).isOf(Items.FISHING_ROD)) return slot;
        }
        return -1;
    }
}
