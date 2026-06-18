package com.pigicial.wikirenderer.mixin.world;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.area.AreaSelectionHelper;
import com.pigicial.wikirenderer.render.area.WorldBlockMesh;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeOutline()V"
            )
    )
    public void drawAreaSelection(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, ResourceHandle entityOutlineTarget, FeatureRenderDispatcher.PreparedFrame featureFrame, ResourceHandle translucentTarget, ResourceHandle mainTarget, ResourceHandle itemEntityTarget, ResourceHandle particleTarget, CallbackInfo ci) {
        AreaSelectionHelper.renderSelectionBox();
    }

    @Inject(method = "close", at = @At(value = "HEAD"))
    public void resetTerrainSampler1(CallbackInfo ci) {
        if (WorldBlockMesh.terrainSampler != null) {
            WorldBlockMesh.terrainSampler.close();
            WorldBlockMesh.terrainSampler = null;
        }
    }

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/textures/GpuSampler;close()V"
            )
    )
    public void resetTerrainSampler2(CallbackInfo ci) {
        if (WorldBlockMesh.terrainSampler != null) {
            WorldBlockMesh.terrainSampler.close();
            WorldBlockMesh.terrainSampler = null;
        }
    }

    @Inject(method = "particlesTarget", at = @At("HEAD"), cancellable = true)
    private void overrideParticlesTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (WikiRenderer.mainTargetOverride != null) {
            cir.setReturnValue(WikiRenderer.mainTargetOverride);
        }
    }
}
