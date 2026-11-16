package com.notunanancyowen.minecart.mixin;

import com.notunanancyowen.minecart.client.NewMinecartEntityRenderer;
import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import com.notunanancyowen.minecart.dataholders.PreExtractor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public abstract class AbstractMinecartEntityRendererMixin<T extends AbstractMinecartEntity & ImprovedMinecart> extends EntityRenderer<T> implements PreExtractor {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(EntityRendererFactory.Context ctx, EntityModelLayer layer, CallbackInfo ci){
        renderer = new NewMinecartEntityRenderer<>(ctx, layer);
    }
    @Unique private NewMinecartEntityRenderer<T> renderer;
    protected AbstractMinecartEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    @Inject(method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.AFTER), cancellable = true)
    private void overrideRender(T abstractMinecartEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        renderer.render(abstractMinecartEntity, f, g, matrixStack, vertexConsumerProvider, i);
        ci.cancel();
    }
    @Override public void extract(Entity entity, float partialTicks) {
        renderer.cartExtractRenderState((T)entity, partialTicks);
    }
    @Override public Vec3d getPositionOffset(T entity, float tickDelta) {
        return renderer.getPositionOffset(entity, tickDelta);
    }
}
