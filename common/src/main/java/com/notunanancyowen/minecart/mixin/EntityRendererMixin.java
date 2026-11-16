package com.notunanancyowen.minecart.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @ModifyReturnValue(method = "getPositionOffset", at = @At("TAIL"))
    private Vec3d offsetRider(Vec3d original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) float tickDelta) {
        if(entity.getVehicle() instanceof ImprovedMinecart im && im.getController().hasCurrentLerpSteps()) return im.getController().getLerpedPosition(tickDelta).subtract(MathHelper.lerp(tickDelta, entity.getVehicle().lastRenderX, entity.getVehicle().getX()), MathHelper.lerp(tickDelta, entity.getVehicle().lastRenderY, entity.getVehicle().getY()), MathHelper.lerp(tickDelta, entity.getVehicle().lastRenderZ, entity.getVehicle().getZ()));
        return original;
    }
}
