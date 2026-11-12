package pl.oli.pustegui;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

/** Shared state and input handling for the modules shown in the click GUI. */
public final class ClientModules {
    public enum Module {
        BOAT_FLY("BoatFly"), KOMAR("Komar"), KILLAURA("Killaura"), ESP("MinecraftESP");

        private final String label;
        private boolean enabled;
        private int key = GLFW.GLFW_KEY_UNKNOWN;
        private int mouseButton = -1;

        Module(String label) { this.label = label; }
        public String label() { return label; }
        public boolean enabled() { return enabled; }
        public int key() { return key; }
        public int mouseButton() { return mouseButton; }
        public String bindName() {
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                String name = GLFW.glfwGetKeyName(key, 0);
                return name == null ? "KEY " + key : name.toUpperCase();
            }
            return mouseButton >= 0 ? "MYSZ " + (mouseButton + 1) : "BRAK";
        }
    }

    private static final Map<Module, Boolean> PREVIOUS_DOWN = new EnumMap<>(Module.class);
    private static final Map<Module, Boolean> PREVIOUS_MOUSE_DOWN = new EnumMap<>(Module.class);

    private ClientModules() {}

    public static void initialize() {
        for (Module module : Module.values()) {
            PREVIOUS_DOWN.put(module, false);
            PREVIOUS_MOUSE_DOWN.put(module, false);
        }
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES
                .register(MinecraftEspModule::render);
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT
                .register(ModuleToggleHud::render);
    }

    public static void tick(MinecraftClient client) {
        long window = client.getWindow().getHandle();
        if (client.currentScreen != null) {
            for (Module module : Module.values()) {
                PREVIOUS_DOWN.put(module, false);
                PREVIOUS_MOUSE_DOWN.put(module, false);
            }
        } else {
            for (Module module : Module.values()) {
                boolean keyDown = module.key != GLFW.GLFW_KEY_UNKNOWN
                        && GLFW.glfwGetKey(window, module.key) == GLFW.GLFW_PRESS;
                boolean mouseDown = module.mouseButton >= 0
                        && GLFW.glfwGetMouseButton(window, module.mouseButton) == GLFW.GLFW_PRESS;
                if ((keyDown && !PREVIOUS_DOWN.getOrDefault(module, false))
                        || (mouseDown && !PREVIOUS_MOUSE_DOWN.getOrDefault(module, false))) {
                    toggle(module, client);
                }
                PREVIOUS_DOWN.put(module, keyDown);
                PREVIOUS_MOUSE_DOWN.put(module, mouseDown);
            }
        }
        BoatFlyModule.tick(client);
        KomarModule.tick(client);
        KillauraModule.tick(client);
    }

    public static void toggle(Module module, MinecraftClient client) {
        module.enabled = !module.enabled;
        if (module == Module.KOMAR && !module.enabled) KomarModule.reset();
        ModuleToggleHud.show(module, module.enabled);
    }

    public static void setKey(Module module, int key) {
        module.key = key;
        module.mouseButton = -1;
        PREVIOUS_DOWN.put(module, true);
        PREVIOUS_MOUSE_DOWN.put(module, false);
    }

    public static void setMouseButton(Module module, int mouseButton) {
        module.key = GLFW.GLFW_KEY_UNKNOWN;
        module.mouseButton = mouseButton;
        PREVIOUS_DOWN.put(module, false);
        PREVIOUS_MOUSE_DOWN.put(module, true);
    }

    public static void unbind(Module module) {
        module.key = GLFW.GLFW_KEY_UNKNOWN;
        module.mouseButton = -1;
        PREVIOUS_DOWN.put(module, false);
        PREVIOUS_MOUSE_DOWN.put(module, false);
    }
}
