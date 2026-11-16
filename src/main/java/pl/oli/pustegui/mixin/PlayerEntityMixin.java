package pl.oli.pustegui.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.oli.pustegui.ClientModules;
import pl.oli.pustegui.MaceDamageModule;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin {
    @Unique private float shadowClient$previousFallDistance;
    @Unique private boolean shadowClient$overrodeFallDistance;

    @Inject(method = "attack", at = @At("HEAD"))
    private void shadowClient$applyMaceFallDamage(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof ServerPlayerEntity
                && ClientModules.Module.MACE_DAMAGE.enabled()
                && player.getMainHandStack().isOf(Items.MACE)) {
            shadowClient$previousFallDistance = player.fallDistance;
            player.fallDistance = MaceDamageModule.fallHeight();
            shadowClient$overrodeFallDistance = true;
        }
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void shadowClient$restoreFallDistance(Entity target, CallbackInfo ci) {
        if (shadowClient$overrodeFallDistance) {
            ((PlayerEntity) (Object) this).fallDistance = shadowClient$previousFallDistance;
            shadowClient$overrodeFallDistance = false;
        }
    }
}
