package com.pigicial.wikirenderer.mixin.entity;

import com.pigicial.wikirenderer.render.entity.EntityVertexPositionTracker;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagFeatureRendererMixin {

    @Inject(method = "buildGroup", at = @At("HEAD"))
    public void wikirenderer$onBuildStart(FeatureFrameContext context, List<NameTagFeatureRenderer.Submit> submits, CallbackInfo ci) {
        EntityVertexPositionTracker.renderingText = true;
    }

    @Inject(method = "buildGroup", at = @At("RETURN"))
    public void wikirenderer$onBuildEnd(FeatureFrameContext context, List<NameTagFeatureRenderer.Submit> submits, CallbackInfo ci) {
        EntityVertexPositionTracker.renderingText = false;
    }
}
