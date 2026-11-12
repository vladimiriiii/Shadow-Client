package pl.oli.pustegui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class EmptyScreen extends Screen {
    private static final int PANEL_WIDTH = 390;
    private static final int PANEL_HEIGHT = 208;
    private static final int ROW_HEIGHT = 34;
    private ClientModules.Module waitingForBind;

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
        context.fill(left - 1, top - 1, left + panelWidth + 1, top + PANEL_HEIGHT + 1, 0xFF8048D8);
        context.fill(left, top, left + panelWidth, top + PANEL_HEIGHT, 0xFF0D0B12);
        context.drawTextWithShadow(textRenderer, Text.literal("SHADOW CLIENT"), left + 14, top + 12, 0xFFE4C6FF);
        context.drawTextWithShadow(textRenderer, Text.literal("LPM: on/off | SPM: bind | PPM Komar: rakieta 1-10s"),
                left + 14, top + 29, 0xFFAAA3B5);

        int index = 0;
        for (ClientModules.Module module : ClientModules.Module.values()) {
            int rowTop = top + 48 + index * ROW_HEIGHT;
            boolean hovered = mouseX >= left + 10 && mouseX <= left + panelWidth - 10
                    && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT - 2;
            int background = module.enabled() ? 0xFF34234A : (hovered ? 0xFF24212B : 0xFF19171E);
            context.fill(left + 10, rowTop, left + panelWidth - 10, rowTop + ROW_HEIGHT - 3, background);
            context.drawTextWithShadow(textRenderer, Text.literal(module.label()), left + 18, rowTop + 9,
                    module.enabled() ? 0xFFE3C5FF : 0xFFE8E5EC);
            if (module == ClientModules.Module.KOMAR) {
                String interval = "Rakieta: " + KomarModule.fireworkIntervalSeconds() + " s";
                context.drawTextWithShadow(textRenderer, Text.literal(interval), left + 112, rowTop + 9,
                        0xFFCFC4DD);
            }
            String bind = waitingForBind == module ? "KLAWISZ / MYSZ..." : module.bindName();
            context.drawTextWithShadow(textRenderer, Text.literal(bind), left + panelWidth - 142,
                    rowTop + 9, waitingForBind == module ? 0xFFFFD479 : 0xFFB5AEC0);
            index++;
        }
        context.drawTextWithShadow(textRenderer, Text.literal("Lewy Alt otwiera GUI  |  Esc zamyka"),
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
        int row = (int) ((mouseY - (top + 48)) / ROW_HEIGHT);
        if (mouseX < left + 10 || mouseX > left + panelWidth - 10 || row < 0
                || row >= ClientModules.Module.values().length
                || mouseY >= top + 48 + (row + 1) * ROW_HEIGHT - 2) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        ClientModules.Module module = ClientModules.Module.values()[row];
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (module == ClientModules.Module.KOMAR) KomarModule.cycleFireworkInterval();
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) waitingForBind = module;
        else ClientModules.toggle(module, client);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
}
