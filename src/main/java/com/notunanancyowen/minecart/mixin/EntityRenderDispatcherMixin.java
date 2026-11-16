package com.notunanancyowen.minecart.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.notunanancyowen.minecart.dataholders.PreExtractor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin<E extends Entity> {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getPositionOffset(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"))
    public void extract(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Local EntityRenderer<? super E> renderer){
        if(renderer instanceof PreExtractor extractor) extractor.extract(entity, tickDelta);
    }
}
