package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;

final class TridentBoostModule {
    private static final int MIN_CHARGE_TICKS = 10;
    private static int distanceBlocks = 15;
    private static int chargeTicks;
    private static boolean charging;
    private static boolean flying;
    private static Vec3d flightStart;
    private static int flightDistance;

    private TridentBoostModule() {}

    static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || client.world == null) {
            resetCharge();
            flying = false;
            return;
        }

        tickFlight(player);
        if (!ClientModules.Module.TRIDENT_BOOST.enabled() || client.currentScreen != null) {
            resetCharge();
            return;
        }

        ItemStack trident = player.getMainHandStack();
        if (!trident.isOf(Items.TRIDENT)) trident = player.getOffHandStack();
        if (!trident.isOf(Items.TRIDENT) || player.isTouchingWater()) {
            resetCharge();
            return;
        }

        Registry<Enchantment> enchantments = client.world.getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment riptideEnchantment = enchantments.get(Enchantments.RIPTIDE);
        if (riptideEnchantment == null) {
            resetCharge();
            return;
        }
        RegistryEntry<Enchantment> riptide = enchantments.getEntry(riptideEnchantment);
        int level = EnchantmentHelper.getLevel(riptide, trident);
        if (level <= 0) {
            resetCharge();
            return;
        }

        if (client.options.useKey.isPressed()) {
            charging = true;
            chargeTicks = Math.min(MIN_CHARGE_TICKS, chargeTicks + 1);
            return;
        }

        if (charging) {
            if (chargeTicks >= MIN_CHARGE_TICKS) launch(player, level);
            resetCharge();
        }
    }

    static int distanceBlocks() {
        return distanceBlocks;
    }

    static void adjustDistance(int amount) {
        distanceBlocks = Math.max(1, Math.min(30, distanceBlocks + amount));
    }

    private static void launch(PlayerEntity player, int enchantmentLevel) {
        Vec3d direction = player.getRotationVec(1.0F);
        double speed = (1.5 + enchantmentLevel) * (0.85 + distanceBlocks / 60.0);
        player.setVelocity(direction.multiply(speed));
        player.velocityModified = true;
        player.fallDistance = 0.0F;
        flightStart = player.getPos();
        flightDistance = distanceBlocks;
        flying = true;
    }

    private static void tickFlight(PlayerEntity player) {
        if (!flying || flightStart == null) return;
        if (player.getPos().distanceTo(flightStart) >= flightDistance) {
            player.setVelocity(Vec3d.ZERO);
            player.velocityModified = true;
            flying = false;
            flightStart = null;
        }
    }

    private static void resetCharge() {
        charging = false;
        chargeTicks = 0;
    }
}
