package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

final class AntiKostkaModule {
    private static final int HOTBAR_SIZE = 9;
    private static final int HOTBAR_SLOT_OFFSET = 36;
    private static final int MOVED_ITEM_THRESHOLD = 5;
    private static final long CHECK_INTERVAL_MS = 2_000L;
    private static ItemStack[] windowStartLayout;
    private static ItemStack[] lastObservedLayout;
    private static long windowStartTime;
    private static int movedItemsInWindow;
    private static boolean restorePending;

    private AntiKostkaModule() {}

    static void tick(MinecraftClient client) {
        if (!ClientModules.Module.ANTI_KOSTKA.enabled() || client.player == null || client.world == null) {
            resetTracking();
            return;
        }

        PlayerEntity player = client.player;
        long now = System.currentTimeMillis();
        if (windowStartLayout == null) {
            beginWindow(snapshot(player), now);
            return;
        }

        ItemStack[] currentLayout = snapshot(player);
        if (restorePending && client.currentScreen == null && client.interactionManager != null) {
            applyLayout(client, player, restoreLayout(windowStartLayout, currentLayout));
            beginWindow(snapshot(player), now);
            return;
        }
        movedItemsInWindow += countMovedItems(lastObservedLayout, currentLayout);
        lastObservedLayout = currentLayout;

        if (movedItemsInWindow >= MOVED_ITEM_THRESHOLD) {
            if (client.currentScreen == null && client.interactionManager != null) {
                applyLayout(client, player, restoreLayout(windowStartLayout, currentLayout));
                beginWindow(snapshot(player), now);
            } else {
                restorePending = true;
            }
        } else if (now - windowStartTime >= CHECK_INTERVAL_MS) {
            beginWindow(currentLayout, now);
        }
    }

    private static void resetTracking() {
        windowStartLayout = null;
        lastObservedLayout = null;
        windowStartTime = 0;
        movedItemsInWindow = 0;
        restorePending = false;
    }

    private static void beginWindow(ItemStack[] layout, long now) {
        windowStartLayout = layout;
        lastObservedLayout = layout;
        windowStartTime = now;
        movedItemsInWindow = 0;
        restorePending = false;
    }

    private static ItemStack[] snapshot(PlayerEntity player) {
        ItemStack[] layout = new ItemStack[HOTBAR_SIZE];
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            layout[slot] = player.getInventory().getStack(slot).copy();
        }
        return layout;
    }

    private static int countMovedItems(ItemStack[] previous, ItemStack[] current) {
        boolean[] matchedCurrent = new boolean[HOTBAR_SIZE];
        int moved = 0;
        for (int previousSlot = 0; previousSlot < HOTBAR_SIZE; previousSlot++) {
            ItemStack previousStack = previous[previousSlot];
            if (previousStack.isEmpty()) continue;

            int currentSlot = findMatchingSlot(previousStack, current, matchedCurrent, previousSlot);
            if (currentSlot >= 0) {
                matchedCurrent[currentSlot] = true;
                if (currentSlot != previousSlot) moved++;
            }
        }
        return moved;
    }

    private static ItemStack[] restoreLayout(ItemStack[] previous, ItemStack[] current) {
        ItemStack[] restored = new ItemStack[HOTBAR_SIZE];
        boolean[] usedCurrent = new boolean[HOTBAR_SIZE];
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) restored[slot] = ItemStack.EMPTY;

        for (int previousSlot = 0; previousSlot < HOTBAR_SIZE; previousSlot++) {
            if (previous[previousSlot].isEmpty()) continue;
            int currentSlot = findMatchingSlot(previous[previousSlot], current, usedCurrent, previousSlot);
            if (currentSlot >= 0) {
                restored[previousSlot] = current[currentSlot].copy();
                usedCurrent[currentSlot] = true;
            }
        }

        for (int currentSlot = 0; currentSlot < HOTBAR_SIZE; currentSlot++) {
            if (usedCurrent[currentSlot] || current[currentSlot].isEmpty()) continue;
            int destination = currentSlot;
            if (!restored[destination].isEmpty()) destination = findEmptySlot(restored);
            if (destination >= 0) restored[destination] = current[currentSlot].copy();
        }
        return restored;
    }

    private static int findMatchingSlot(ItemStack stack, ItemStack[] candidates, boolean[] used, int preferredSlot) {
        if (preferredSlot >= 0 && preferredSlot < HOTBAR_SIZE && !used[preferredSlot]
                && ItemStack.areItemsAndComponentsEqual(stack, candidates[preferredSlot])) {
            return preferredSlot;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (!used[slot] && ItemStack.areItemsAndComponentsEqual(stack, candidates[slot])) return slot;
        }
        return -1;
    }

    private static int findEmptySlot(ItemStack[] layout) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (layout[slot].isEmpty()) return slot;
        }
        return -1;
    }

    private static void applyLayout(MinecraftClient client, PlayerEntity player, ItemStack[] desired) {
        for (int destination = 0; destination < HOTBAR_SIZE; destination++) {
            ItemStack desiredStack = desired[destination];
            if (desiredStack.isEmpty()
                    || ItemStack.areItemsAndComponentsEqual(desiredStack,
                    player.getInventory().getStack(destination))) continue;

            int source = findCurrentSlot(player, desiredStack, destination);
            if (source >= 0 && source != destination) {
                client.interactionManager.clickSlot(player.currentScreenHandler.syncId,
                        HOTBAR_SLOT_OFFSET + destination, source, SlotActionType.SWAP, player);
            }
        }
    }

    private static int findCurrentSlot(PlayerEntity player, ItemStack stack, int preferredSlot) {
        if (ItemStack.areItemsAndComponentsEqual(stack, player.getInventory().getStack(preferredSlot))) {
            return preferredSlot;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (ItemStack.areItemsAndComponentsEqual(stack, player.getInventory().getStack(slot))) return slot;
        }
        return -1;
    }
}
