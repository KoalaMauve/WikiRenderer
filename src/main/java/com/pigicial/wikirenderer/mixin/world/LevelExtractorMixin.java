package com.pigicial.wikirenderer.mixin.world;

import com.pigicial.wikirenderer.render.area.AreaRenderable;
import com.pigicial.wikirenderer.screen.RenderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {


    @Inject(method = "blockChanged", at = @At("HEAD"))
    private void onBlockChanged(BlockPos pos, int updateFlags, CallbackInfo ci) {
        if (Minecraft.getInstance().gui.screen() instanceof RenderScreen screen) {
            if (screen.renderable instanceof AreaRenderable areaRenderable) {
                for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                    for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
                        for (int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                            int myX = x >> 5;
                            int myY = y >> 5;
                            int myZ = z >> 5;
                            areaRenderable.mesh.setDirty(myX, myY, myZ, false);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void onSetBlocksDirty(int x0, int y0, int z0, int x1, int y1, int z1, CallbackInfo ci) {
        if (Minecraft.getInstance().gui.screen() instanceof RenderScreen screen) {
            if (screen.renderable instanceof AreaRenderable areaRenderable) {
                int minX = (x0 - 1) >> 5;
                int minY = (y0 - 1) >> 5;
                int minZ = (z0 - 1) >> 5;
                int maxX = (x1 + 1) >> 5;
                int maxY = (y1 + 1) >> 5;
                int maxZ = (z1 + 1) >> 5;

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            areaRenderable.mesh.setDirty(x, y, z, false);
                        }
                    }
                }
            }
        }
    }
}
