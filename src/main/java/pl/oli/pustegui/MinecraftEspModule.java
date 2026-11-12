package pl.oli.pustegui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

final class MinecraftEspModule {
    private static final RenderLayer THROUGH_WALL_BOX = new RenderLayer(
            "minecraftesp:through_wall_box", VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS, 256, false, false,
            () -> {
                RenderPhase.POSITION_COLOR_PROGRAM.startDrawing();
                RenderPhase.ALWAYS_DEPTH_TEST.startDrawing();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                RenderSystem.depthMask(false);
            },
            () -> {
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderPhase.ALWAYS_DEPTH_TEST.endDrawing();
                RenderPhase.POSITION_COLOR_PROGRAM.endDrawing();
            }) {};

    private MinecraftEspModule() {}

    static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ClientModules.Module.ESP.enabled() || client.world == null || client.player == null
                || context.matrixStack() == null || context.consumers() == null) return;
        Vec3d cameraPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        VertexConsumer vertices = consumers.getBuffer(THROUGH_WALL_BOX);
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        try {
            for (PlayerEntity player : client.world.getPlayers()) {
                if (player == client.player) continue;
                Box box = player.getBoundingBox().expand(0.035);
                VertexRendering.drawFilledBox(matrices, vertices, box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ, 1.0F, 0.0F, 0.0F, 0.28F);
            }
        } finally {
            matrices.pop();
        }
        if (consumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw(THROUGH_WALL_BOX);
    }
}
