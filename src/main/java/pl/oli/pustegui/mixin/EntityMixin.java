package pl.oli.pustegui.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.oli.pustegui.ClientModules;

@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    private void shadowClient$ignoreCobwebSlowdown(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity == MinecraftClient.getInstance().player
                && ClientModules.Module.ANTI_COBWEB.enabled()
                && state.isOf(Blocks.COBWEB)) {
            ci.cancel();
        }
    }
}
