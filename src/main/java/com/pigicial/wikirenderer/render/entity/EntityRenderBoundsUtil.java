package com.pigicial.wikirenderer.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.mixin.access.LevelRendererAccessor;
import com.pigicial.wikirenderer.util.CornerData;
import com.pigicial.wikirenderer.util.DrawEntityDataCache;
import com.pigicial.wikirenderer.util.DrawProjectionDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EntityRenderBoundsUtil {

    public static EntityVertexPositionTracker currentBufferSource = new EntityVertexPositionTracker();

    @Nullable
    public static EntityVertexBounds getPositionOffsetBasedBounds(Entity entity) {
        try {
            WikiRenderer.inBoundsCalculation = true;
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            EntityRenderState entityRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, tickDelta);
            CameraRenderState cameraRenderState = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).wikirenderer$getLevelRenderState().cameraRenderState;

            return getPositionOffsetBasedBounds(entity, entityRenderState, cameraRenderState);
        } finally {
            WikiRenderer.inBoundsCalculation = false;
        }
    }

    @Nullable
    public static EntityVertexBounds getPositionOffsetBasedBounds(Entity entity, EntityRenderState renderState, CameraRenderState cameraRenderState) {
        Vec3 position = entity.position();
        return getBounds(renderState, cameraRenderState, position.x, position.y, position.z);
    }

    // this gets the actual bounds of the rendered entity, rather than relying on extremely flaky and inconsistent bounding box data
    @Nullable
    public static EntityVertexBounds getBounds(EntityRenderState renderState, CameraRenderState cameraRenderState, double xOffset, double yOffset, double zOffset) {
        try {
            WikiRenderer.inBoundsCalculation = true;
            SubmitNodeStorage tempStorage = new SubmitNodeStorage();
            Minecraft.getInstance().getEntityRenderDispatcher().submit(renderState, cameraRenderState, xOffset, yOffset, zOffset, new PoseStack(), tempStorage);

            return submitVertexData(tempStorage);
        } finally {
            WikiRenderer.inBoundsCalculation = false;
        }
    }

    @Nullable
    public static CornerData getDrawnBounds(CameraRenderState cameraRenderState, DrawEntityDataCache entityDrawData, DrawProjectionDataCache projectionData) {
        EntityVertexPositionTracker.MODEL_VIEW_PROJECTION = projectionData.getModelViewProjectionMatrix();
        EntityVertexPositionTracker.SCREEN_WIDTH = projectionData.width();
        EntityVertexPositionTracker.SCREEN_HEIGHT = projectionData.height();

        Vec3 offset = entityDrawData.offset();
        PoseStack poseStack = entityDrawData.poseStack();
        EntityRenderState renderState = entityDrawData.renderState();
        boolean sprite = entityDrawData.sprite();
        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        List<Runnable> partVisibilityCallbacks = new ArrayList<>();
        if (sprite) {
            WikiRenderer.inSpriteEntityDraw = true;
            EntityRenderer<?, ?> renderer = renderDispatcher.getRenderer(renderState);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
                EntitySpriteModelVisibilityUtil.hideNonHeadParts(livingEntityRenderer, partVisibilityCallbacks);
            }
        }

        SubmitNodeStorage tempStorage = new SubmitNodeStorage();
        renderDispatcher.submit(renderState, cameraRenderState, offset.x, offset.y, offset.z, poseStack, tempStorage);
        EntityVertexBounds vertexBounds = submitVertexData(tempStorage);

        WikiRenderer.inSpriteEntityDraw = false;
        partVisibilityCallbacks.forEach(Runnable::run);
        EntityVertexPositionTracker.MODEL_VIEW_PROJECTION = null;
        EntityVertexPositionTracker.SCREEN_WIDTH = null;
        EntityVertexPositionTracker.SCREEN_HEIGHT = null;
        if (vertexBounds == null) {
            return null;
        } else {
            AABB bounds = vertexBounds.getBounds();
            return new CornerData((int) bounds.minX, (int) bounds.minY, (int) bounds.maxX, (int) bounds.maxY);
        }
    }

    private static EntityVertexBounds submitVertexData(SubmitNodeStorage tempStorage) {
        try {
            WikiRenderer.inBoundsCalculation = true;
            EntityVertexPositionTracker.BOUNDS = null;
            EntityRenderBoundsUtil.currentBufferSource = new EntityVertexPositionTracker();

            FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.featureRenderDispatcher();
            featureRenderDispatcher.prepareFrame(tempStorage);

            return EntityVertexPositionTracker.BOUNDS;
        } finally {
            WikiRenderer.inBoundsCalculation = false;
        }
    }

    public static boolean isNametagOnlyRenderedData(Entity entity) {
        try {
            WikiRenderer.inBoundsCalculation = true;
            EntityRenderState entityRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, 0);
            CameraRenderState cameraRenderState = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).wikirenderer$getLevelRenderState().cameraRenderState;

            SubmitNodeStorage tempStorage = new SubmitNodeStorage();
            Minecraft.getInstance().getEntityRenderDispatcher().submit(entityRenderState, cameraRenderState, 0, 0, 0, new PoseStack(), tempStorage);

            EntityVertexPositionTracker.BOUNDS = null;
        } finally {
            WikiRenderer.inBoundsCalculation = false;
        }

        // todo: replace this
        /*
        for (SubmitNodeCollection collection : tempStorage.getSubmitsPerOrder().values()) {
            renderSolids(collection);

            MODEL_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource, OUTLINE_BUFFER_SOURCE, currentBufferSource);
            MODEL_PART_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource, OUTLINE_BUFFER_SOURCE, currentBufferSource);
            TEXT_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource);
            ITEM_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource, OUTLINE_BUFFER_SOURCE);
            BLOCK_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource, Minecraft.getInstance().getModelManager().getBlockStateModelSet(), OUTLINE_BUFFER_SOURCE, currentBufferSource, Minecraft.getInstance().gameRenderer.getGameRenderState().optionsRenderState);
            CUSTOM_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource);
        }

        if (EntityVertexPositionTracker.BOUNDS == null) {
            for (SubmitNodeCollection collection : tempStorage.getSubmitsPerOrder().values()) {
                NAME_TAG_FEATURE_RENDERER.renderTranslucent(collection, currentBufferSource, Minecraft.getInstance().font);
            }

            return EntityVertexPositionTracker.BOUNDS != null;
        }

         */

        return false;
    }
}
