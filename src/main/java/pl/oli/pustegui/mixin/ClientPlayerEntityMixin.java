package pl.oli.pustegui.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.oli.pustegui.ClientModules;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin {
    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void shadowClient$disablePlayerBlockPush(double x, double z, CallbackInfo ci) {
        if (ClientModules.Module.NO_PUSH.enabled()) {
            ci.cancel();
        }
    }
}
