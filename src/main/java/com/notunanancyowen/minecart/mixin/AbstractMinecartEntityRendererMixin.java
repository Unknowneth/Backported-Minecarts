package com.notunanancyowen.minecart.mixin;

import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public abstract class AbstractMinecartEntityRendererMixin<T extends AbstractMinecartEntity & ImprovedMinecart> extends EntityRenderer<T> {
    @Shadow protected abstract void renderBlock(T entity, float delta, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Shadow @Final protected EntityModel<T> model;

    protected AbstractMinecartEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    @Inject(method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.AFTER), cancellable = true)
    private void overrideRender(T abstractMinecartEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        matrixStack.push();
        long l = abstractMinecartEntity.getId() * 493286711L;
        l = l * l * 4392167121L + l * 98761L;
        float h = (((float)(l >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float j = (((float)(l >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float k = (((float)(l >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        matrixStack.translate(h, j, k);
        if(abstractMinecartEntity instanceof ImprovedMinecart im) {
            im.getController().tick();
            boolean hasLerp = im.getController().hasCurrentLerpSteps();
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(hasLerp ? im.getController().getLerpedYaw(g) : abstractMinecartEntity.getYaw(g)));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(hasLerp ? -im.getController().getLerpedPitch(g) : -abstractMinecartEntity.getPitch(g)));
        }
        matrixStack.translate(0.0F, 0.375F, 0.0F);
        float p = abstractMinecartEntity.getDamageWobbleTicks() - g;
        float q = abstractMinecartEntity.getDamageWobbleStrength() - g;
        if (q < 0.0F) {
            q = 0.0F;
        }
        if (p > 0.0F) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(p) * p * q / 10.0F * abstractMinecartEntity.getDamageWobbleSide()));
        }
        int r = abstractMinecartEntity.getBlockOffset();
        BlockState blockState = abstractMinecartEntity.getContainedBlock();
        if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
            matrixStack.push();
            matrixStack.scale(0.75F, 0.75F, 0.75F);
            matrixStack.translate(-0.5F, (r - 8) / 16.0F, 0.5F);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            this.renderBlock(abstractMinecartEntity, g, blockState, matrixStack, vertexConsumerProvider, i);
            matrixStack.pop();
        }
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setAngles(abstractMinecartEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(abstractMinecartEntity)));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
        matrixStack.pop();
        ci.cancel();
    }
    @Override public Vec3d getPositionOffset(T entity, float tickDelta) {
        if(entity instanceof ImprovedMinecart im && im.getController().hasCurrentLerpSteps()) {
            //im.getController().tick();
            return super.getPositionOffset(entity, tickDelta).add(im.getController().getLerpedPosition(tickDelta).subtract(MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()), MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()), MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ())));
        }
        return super.getPositionOffset(entity, tickDelta);
    }
}
