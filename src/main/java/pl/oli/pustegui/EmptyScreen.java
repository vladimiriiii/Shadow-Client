package pl.oli.pustegui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class EmptyScreen extends Screen {
    private static final int PANEL_WIDTH = 390;
    private static final int PANEL_HEIGHT = 208;
    private static final int ROW_HEIGHT = 34;
    private static final int LIST_TOP = 48;
    private static final int LIST_BOTTOM = PANEL_HEIGHT - 24;
    private static final int VISIBLE_ROWS = (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT;
    private ClientModules.Module waitingForBind;
    private int scrollOffset;
    private boolean komarSettingsOpen;
    private boolean tridentSettingsOpen;
    private boolean maceDamageSettingsOpen;

    public EmptyScreen() { super(Text.literal("Shadow Client GUI")); }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Keep the world visible without Minecraft's default background blur or dim overlay.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int left = (width - panelWidth) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        fillRoundedRect(context, left - 14, top - 14, left + panelWidth + 14, top + PANEL_HEIGHT + 14,
                22, 0x14A64DFF);
        fillRoundedRect(context, left - 9, top - 9, left + panelWidth + 9, top + PANEL_HEIGHT + 9,
                18, 0x28A64DFF);
        fillRoundedRect(context, left - 4, top - 4, left + panelWidth + 4, top + PANEL_HEIGHT + 4,
                14, 0x48A64DFF);
        fillRoundedRect(context, left - 1, top - 1, left + panelWidth + 1, top + PANEL_HEIGHT + 1,
                13, 0xFF8048D8);
        fillRoundedRect(context, left, top, left + panelWidth, top + PANEL_HEIGHT, 12, 0xFF0D0B12);
        context.drawTextWithShadow(textRenderer, Text.literal("SHADOW CLIENT"), left + 14, top + 12, 0xFFE4C6FF);
        context.drawTextWithShadow(textRenderer, Text.literal("LPM: on/off | SPM: bind | PPM: ustawienia"),
                left + 14, top + 29, 0xFFAAA3B5);

        if (komarSettingsOpen) {
            renderKomarSettings(context, left, top, panelWidth);
            super.render(context, mouseX, mouseY, delta);
            return;
        }
        if (tridentSettingsOpen) {
            renderTridentSettings(context, left, top, panelWidth);
            super.render(context, mouseX, mouseY, delta);
            return;
        }
        if (maceDamageSettingsOpen) {
            renderMaceDamageSettings(context, left, top, panelWidth);
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        ClientModules.Module[] modules = ClientModules.Module.values();
        int visibleEnd = Math.min(modules.length, scrollOffset + VISIBLE_ROWS);
        for (int index = scrollOffset; index < visibleEnd; index++) {
            ClientModules.Module module = modules[index];
            int rowTop = top + LIST_TOP + (index - scrollOffset) * ROW_HEIGHT;
            boolean hovered = mouseX >= left + 10 && mouseX <= left + panelWidth - 10
                    && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT - 2;
            int background = module.enabled() ? 0xFF34234A : (hovered ? 0xFF24212B : 0xFF19171E);
            fillRoundedRect(context, left + 10, rowTop, left + panelWidth - 10,
                    rowTop + ROW_HEIGHT - 3, 7, background);
            context.drawTextWithShadow(textRenderer, Text.literal(module.label()), left + 18, rowTop + 9,
                    module.enabled() ? 0xFFE3C5FF : 0xFFE8E5EC);
            if (module == ClientModules.Module.KOMAR) {
                String interval = KomarModule.automaticFirework() ? "Rakieta: AUTO"
                        : "Rakieta: " + KomarModule.fireworkIntervalSeconds() + " s";
                context.drawTextWithShadow(textRenderer, Text.literal(interval), left + 112, rowTop + 9,
                        0xFFCFC4DD);
            } else if (module == ClientModules.Module.TRIDENT_BOOST) {
                context.drawTextWithShadow(textRenderer,
                        Text.literal("Zasieg: " + TridentBoostModule.distanceBlocks() + " blokow"),
                        left + 112, rowTop + 9, 0xFFCFC4DD);
            } else if (module == ClientModules.Module.MACE_DAMAGE) {
                context.drawTextWithShadow(textRenderer,
                        Text.literal("Upadek: " + MaceDamageModule.fallHeight() + " blokow"),
                        left + 112, rowTop + 9, 0xFFCFC4DD);
            }
            String bind = waitingForBind == module ? "KLAWISZ / MYSZ..." : module.bindName();
            context.drawTextWithShadow(textRenderer, Text.literal(bind), left + panelWidth - 142,
                    rowTop + 9, waitingForBind == module ? 0xFFFFD479 : 0xFFB5AEC0);
        }
        if (modules.length > VISIBLE_ROWS) {
            int trackTop = top + LIST_TOP;
            int trackBottom = top + LIST_BOTTOM;
            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(18, trackHeight * VISIBLE_ROWS / modules.length);
            int maxScroll = modules.length - VISIBLE_ROWS;
            int thumbTop = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
            fillRoundedRect(context, left + panelWidth - 5, trackTop, left + panelWidth - 3,
                    trackBottom, 1, 0xFF332D3B);
            fillRoundedRect(context, left + panelWidth - 5, thumbTop, left + panelWidth - 3,
                    thumbTop + thumbHeight, 1, 0xFFB58AE8);
        }
        context.drawTextWithShadow(textRenderer, Text.literal("Rolka: przewijaj listę  |  Lewy Alt: GUI  |  Esc: zamknij"),
                left + 14, top + PANEL_HEIGHT - 17, 0xFF8D8797);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (waitingForBind != null) {
            ClientModules.setMouseButton(waitingForBind, button);
            waitingForBind = null;
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int left = (width - panelWidth) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        if (komarSettingsOpen || tridentSettingsOpen || maceDamageSettingsOpen) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (tridentSettingsOpen) {
                    if (inside(mouseX, mouseY, left + 72, top + 94, left + 116, top + 120))
                        TridentBoostModule.adjustDistance(-1);
                    else if (inside(mouseX, mouseY, left + 274, top + 94, left + 318, top + 120))
                        TridentBoostModule.adjustDistance(1);
                    else if (inside(mouseX, mouseY, left + 135, top + 166, left + panelWidth - 135, top + 193))
                        tridentSettingsOpen = false;
                } else if (maceDamageSettingsOpen) {
                    if (inside(mouseX, mouseY, left + 72, top + 94, left + 116, top + 120))
                        MaceDamageModule.adjustFallHeight(-1);
                    else if (inside(mouseX, mouseY, left + 274, top + 94, left + 318, top + 120))
                        MaceDamageModule.adjustFallHeight(1);
                    else if (inside(mouseX, mouseY, left + 135, top + 166, left + panelWidth - 135, top + 193))
                        maceDamageSettingsOpen = false;
                } else {
                    if (inside(mouseX, mouseY, left + 72, top + 82, left + 116, top + 108))
                        KomarModule.adjustFireworkInterval(-1);
                    else if (inside(mouseX, mouseY, left + 274, top + 82, left + 318, top + 108))
                        KomarModule.adjustFireworkInterval(1);
                    else if (inside(mouseX, mouseY, left + 24, top + 125, left + panelWidth - 24, top + 153))
                        KomarModule.toggleAutomaticFirework();
                    else if (inside(mouseX, mouseY, left + 135, top + 166, left + panelWidth - 135, top + 193))
                        komarSettingsOpen = false;
                }
            }
            return true;
        }
        int visibleRow = (int) ((mouseY - (top + LIST_TOP)) / ROW_HEIGHT);
        if (mouseX < left + 10 || mouseX > left + panelWidth - 10 || visibleRow < 0
                || visibleRow >= VISIBLE_ROWS
                || mouseY >= top + LIST_TOP + (visibleRow + 1) * ROW_HEIGHT - 2
                || mouseY >= top + LIST_BOTTOM || scrollOffset + visibleRow >= ClientModules.Module.values().length) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        ClientModules.Module module = ClientModules.Module.values()[scrollOffset + visibleRow];
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (module == ClientModules.Module.KOMAR) komarSettingsOpen = true;
            else if (module == ClientModules.Module.TRIDENT_BOOST) tridentSettingsOpen = true;
            else if (module == ClientModules.Module.MACE_DAMAGE) maceDamageSettingsOpen = true;
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) waitingForBind = module;
        else ClientModules.toggle(module, client);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, ClientModules.Module.values().length - VISIBLE_ROWS);
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int left = (width - panelWidth) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        boolean overList = mouseX >= left && mouseX <= left + panelWidth
                && mouseY >= top + LIST_TOP && mouseY < top + LIST_BOTTOM;
        if (overList && maxScroll > 0 && verticalAmount != 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((komarSettingsOpen || tridentSettingsOpen || maceDamageSettingsOpen)
                && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            komarSettingsOpen = false;
            tridentSettingsOpen = false;
            maceDamageSettingsOpen = false;
            return true;
        }
        if (waitingForBind != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) waitingForBind = null;
            else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                ClientModules.unbind(waitingForBind);
                waitingForBind = null;
            }
            else {
                ClientModules.setKey(waitingForBind, keyCode);
                waitingForBind = null;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static void fillRoundedRect(DrawContext context, int left, int top, int right, int bottom,
                                        int radius, int color) {
        int width = right - left;
        int height = bottom - top;
        int cornerRadius = Math.min(radius, Math.min(width, height) / 2);
        for (int row = 0; row < height; row++) {
            int inset = 0;
            if (row < cornerRadius) {
                double dy = cornerRadius - row - 0.5;
                inset = (int) Math.ceil(cornerRadius - Math.sqrt(cornerRadius * cornerRadius - dy * dy));
            } else if (row >= height - cornerRadius) {
                double dy = row - (height - cornerRadius) + 0.5;
                inset = (int) Math.ceil(cornerRadius - Math.sqrt(cornerRadius * cornerRadius - dy * dy));
            }
            context.fill(left + inset, top + row, right - inset, top + row + 1, color);
        }
    }

    private void renderTridentSettings(DrawContext context, int left, int top, int panelWidth) {
        context.drawTextWithShadow(textRenderer, Text.literal("TRIDENTBOOST - USTAWIENIA"), left + 14, top + 54,
                0xFFE4C6FF);
        context.drawTextWithShadow(textRenderer, Text.literal("Przytrzymaj PPM przez 0.5 s i pusc, aby wystartowac."),
                left + 34, top + 70, 0xFFCFC4DD);
        context.drawTextWithShadow(textRenderer, Text.literal("Maksymalny zasieg lotu (1-30 blokow)"), left + 72,
                top + 78, 0xFFCFC4DD);
        fillRoundedRect(context, left + 72, top + 94, left + 116, top + 120, 6, 0xFF34234A);
        fillRoundedRect(context, left + 274, top + 94, left + 318, top + 120, 6, 0xFF34234A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("-"), left + 94, top + 101, 0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(TridentBoostModule.distanceBlocks() + " blokow"), left + panelWidth / 2,
                top + 101, 0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("+"), left + 296, top + 101, 0xFFE8E5EC);
        fillRoundedRect(context, left + 135, top + 166, left + panelWidth - 135, top + 193, 7, 0xFF24212B);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Wroc"), left + panelWidth / 2,
                top + 173, 0xFFB5AEC0);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Zasieg jest limitem lotu po wypuszczeniu tridentu."), left + panelWidth / 2,
                top + PANEL_HEIGHT - 17, 0xFF8D8797);
    }

    private void renderMaceDamageSettings(DrawContext context, int left, int top, int panelWidth) {
        context.drawTextWithShadow(textRenderer, Text.literal("MACEDAMAGE - USTAWIENIA"), left + 14, top + 54,
                0xFFE4C6FF);
        context.drawTextWithShadow(textRenderer, Text.literal("Symulowana wysokosc upadku (4-100 blokow)"),
                left + 50, top + 78, 0xFFCFC4DD);
        fillRoundedRect(context, left + 72, top + 94, left + 116, top + 120, 6, 0xFF34234A);
        fillRoundedRect(context, left + 274, top + 94, left + 318, top + 120, 6, 0xFF34234A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("-"), left + 94, top + 101, 0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(MaceDamageModule.fallHeight() + " blokow"), left + panelWidth / 2,
                top + 101, 0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("+"), left + 296, top + 101, 0xFFE8E5EC);
        fillRoundedRect(context, left + 135, top + 166, left + panelWidth - 135, top + 193, 7, 0xFF24212B);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Wroc"), left + panelWidth / 2,
                top + 173, 0xFFB5AEC0);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Serwer zdalny sam weryfikuje wysokosc upadku."), left + panelWidth / 2,
                top + PANEL_HEIGHT - 17, 0xFF8D8797);
    }

    private void renderKomarSettings(DrawContext context, int left, int top, int panelWidth) {
        context.drawTextWithShadow(textRenderer, Text.literal("KOMAR — USTAWIENIA"), left + 14, top + 54,
                0xFFE4C6FF);
        context.drawTextWithShadow(textRenderer, Text.literal("Stały interwał rakiety (1–10 s)"), left + 72,
                top + 70, 0xFFCFC4DD);
        fillRoundedRect(context, left + 72, top + 82, left + 116, top + 108, 6, 0xFF34234A);
        fillRoundedRect(context, left + 274, top + 82, left + 318, top + 108, 6, 0xFF34234A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("−"), left + 94, top + 89, 0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(KomarModule.fireworkIntervalSeconds() + " s"), left + panelWidth / 2, top + 89,
                0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("+"), left + 296, top + 89, 0xFFE8E5EC);

        String autoLabel = "Automatyczna fajerwerka: " + (KomarModule.automaticFirework() ? "WŁ." : "WYŁ.");
        fillRoundedRect(context, left + 24, top + 125, left + panelWidth - 24, top + 153, 7,
                KomarModule.automaticFirework() ? 0xFF49305D : 0xFF24212B);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(autoLabel), left + panelWidth / 2,
                top + 134, 0xFFE8E5EC);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Wróć"), left + panelWidth / 2,
                top + 173, 0xFFB5AEC0);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Auto używa rakiety, gdy dystans lub prędkość tego wymaga."), left + panelWidth / 2,
                top + PANEL_HEIGHT - 17, 0xFF8D8797);
    }

    private static boolean inside(double x, double y, int left, int top, int right, int bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
