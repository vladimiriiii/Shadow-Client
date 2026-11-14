package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

final class AutoArmorModule {
    private static final double LOW_DURABILITY_THRESHOLD = 0.20;
    private static final long ACTION_DELAY_MS = 50L;
    private static long nextActionTime;

    private AutoArmorModule() {}

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.AUTO_ARMOR.enabled() || client.player == null || client.world == null) {
            nextActionTime = 0;
            return;
        }
        if (client.currentScreen != null || client.interactionManager == null) return;

        long now = System.currentTimeMillis();
        if (now < nextActionTime) return;

        PlayerEntity player = client.player;
        for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack equipped = player.getEquippedStack(equipmentSlot);
            if (!equipped.isEmpty() && remainingDurability(equipped) >= LOW_DURABILITY_THRESHOLD) continue;

            int replacementIndex = findReplacement(player, equipmentSlot, equipped);
            if (replacementIndex < 0) continue;

            int armorScreenSlot = armorScreenSlot(equipmentSlot);
            int inventoryScreenSlot = inventoryScreenSlot(replacementIndex);
            int syncId = player.currentScreenHandler.syncId;
            client.interactionManager.clickSlot(syncId, inventoryScreenSlot, 0, SlotActionType.PICKUP, player);
            client.interactionManager.clickSlot(syncId, armorScreenSlot, 0, SlotActionType.PICKUP, player);
            if (!equipped.isEmpty()) {
                client.interactionManager.clickSlot(syncId, inventoryScreenSlot, 0, SlotActionType.PICKUP, player);
            }
            nextActionTime = now + ACTION_DELAY_MS;
            return;
        }
    }

    private static int findReplacement(PlayerEntity player, EquipmentSlot equipmentSlot, ItemStack equipped) {
        int bestIndex = -1;
        double bestDurability = equipped.isEmpty() ? -1.0 : remainingDurability(equipped);

        for (int inventoryIndex = 0; inventoryIndex < 36; inventoryIndex++) {
            ItemStack candidate = player.getInventory().getStack(inventoryIndex);
            EquippableComponent equippable = candidate.get(DataComponentTypes.EQUIPPABLE);
            if (equippable == null || equippable.slot() != equipmentSlot || candidate.getMaxDamage() <= 0
                    || candidate.getDamage() >= candidate.getMaxDamage()) continue;

            double durability = remainingDurability(candidate);
            if (durability > bestDurability) {
                bestDurability = durability;
                bestIndex = inventoryIndex;
            }
        }
        return bestIndex;
    }

    private static double remainingDurability(ItemStack stack) {
        if (stack.isEmpty() || stack.getMaxDamage() <= 0) return 1.0;
        return (double) (stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage();
    }

    private static int inventoryScreenSlot(int inventoryIndex) {
        return inventoryIndex < 9 ? inventoryIndex + 36 : inventoryIndex;
    }

    private static int armorScreenSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> throw new IllegalArgumentException("Not an armor slot: " + equipmentSlot);
        };
    }
}
