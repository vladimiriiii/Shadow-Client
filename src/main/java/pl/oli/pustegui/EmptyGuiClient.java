package pl.oli.pustegui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class EmptyGuiClient implements ClientModInitializer {
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadow_client.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT,
                "category.shadow_client"));
        ClientModules.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new EmptyScreen());
            }
            ClientModules.tick(client);
        });
    }
}
