package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Shows the latest module toggle and a five-second countdown bar. */
final class ModuleToggleHud {
    private static final long DISPLAY_TIME_MS = 5_000;
    private static final int RIGHT = 10;
    private static final int BOTTOM = 12;
    private static ClientModules.Module module;
    private static boolean enabled;
    private static long shownAt;

    private ModuleToggleHud() {}

    static void show(ClientModules.Module toggledModule, boolean isEnabled) {
        module = toggledModule;
        enabled = isEnabled;
        shownAt = System.currentTimeMillis();
    }

    static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (module == null) return;

        long elapsed = System.currentTimeMillis() - shownAt;
        if (elapsed >= DISPLAY_TIME_MS) {
            module = null;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String state;
        if (module == ClientModules.Module.KILLAURA) {
            state = enabled ? " odpalona" : " wyłączona";
        } else {
            state = enabled ? " włączony" : " wyłączony";
        }
        Text message = Text.literal(module.label() + state);
        int contentWidth = client.textRenderer.getWidth(message.getString());
        int boxWidth = contentWidth + 18;
        int boxHeight = 34;
        int left = context.getScaledWindowWidth() - boxWidth - RIGHT;
        int top = context.getScaledWindowHeight() - boxHeight - BOTTOM;

        context.fill(left, top, left + boxWidth, top + boxHeight, 0xFF100D17);
        context.fill(left, top, left + boxWidth, top + 1, 0xFF654B82);
        context.drawTextWithShadow(client.textRenderer, message, left + 9, top + 6,
                enabled ? 0xFFF0E9F6 : 0xFFC9C1D3);

        int barLeft = left + 9;
        int barTop = top + 23;
        int barWidth = boxWidth - 18;
        double remaining = 1.0 - (double) elapsed / DISPLAY_TIME_MS;
        context.fill(barLeft, barTop, barLeft + barWidth, barTop + 3, 0xFF393341);
        context.fill(barLeft, barTop, barLeft + (int) (barWidth * remaining), barTop + 3,
                enabled ? 0xFFBD86FF : 0xFF8D8797);
    }
}
