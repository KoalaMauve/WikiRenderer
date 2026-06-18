package com.pigicial.wikirenderer.mixin.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.entity.EntityRenderBoundsUtil;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTypeFeatureRenderer.class)
public class RenderTypeFeatureRendererMixin {

    @Inject(method = "getVertexBuilder", at = @At("HEAD"), cancellable = true)
    public final void wikirenderer$getVertexBuilder(RenderType renderType, CallbackInfoReturnable<VertexConsumer> cir) {
        if (WikiRenderer.inBoundsCalculation && EntityRenderBoundsUtil.currentBufferSource != null) {
            cir.setReturnValue(EntityRenderBoundsUtil.currentBufferSource);
        }
    }
}
