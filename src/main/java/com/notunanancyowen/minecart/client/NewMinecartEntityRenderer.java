package com.notunanancyowen.minecart.client;

import com.notunanancyowen.minecart.MinecartController;
import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.MinecartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class NewMinecartEntityRenderer<T extends AbstractMinecartEntity> extends EntityRenderer<T> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/minecart.png");
    protected final EntityModel<T> model;
    private final BlockRenderManager blockRenderManager;
    private final MinecartRenderState renderState = new MinecartRenderState();
    public NewMinecartEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
        super(ctx);
        this.shadowRadius = 0.7F;
        this.model = new MinecartEntityModel(ctx.getPart(layer));
        this.blockRenderManager = ctx.getBlockRenderManager();
    }

    public Identifier getTexture(T abstractMinecartEntity) {
        return TEXTURE;
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        cartExtractRenderState(entity, tickDelta);
        render(entity, renderState, matrices, vertexConsumers, light);
    }

    public void render(T entity, MinecartRenderState minecartRenderState, MatrixStack poseStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        poseStack.push();
        long l = minecartRenderState.offsetSeed;
        float f = (((float)(l >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float g = (((float)(l >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float h = (((float)(l >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(f, g, h);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(minecartRenderState.yRot));
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-minecartRenderState.xRot));
        poseStack.translate(0.0F, 0.375F, 0.0F);

        float j = minecartRenderState.hurtTime;
        if (j > 0.0F) {
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(j) * j * minecartRenderState.damageTime / 10.0F * minecartRenderState.hurtDir));
        }

        BlockState blockState = minecartRenderState.displayBlockState;
        if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
            poseStack.push();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5F, (minecartRenderState.displayOffset - 8) / 16.0F, 0.5F);
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            this.renderBlock(blockState, poseStack, vertexConsumerProvider, i);
            poseStack.pop();
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setAngles(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(entity)));
        this.model.render(poseStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
        poseStack.pop();
    }
    public void cartExtractRenderState(T abstractMinecart, float f){
        superExtractRenderState(abstractMinecart, f);
        if(!(abstractMinecart instanceof ImprovedMinecart im)) return;

        MinecartController<?> newMinecartBehavior = im.getController();
        if (newMinecartBehavior.hasCurrentLerpSteps()) {
            renderState.renderPos = newMinecartBehavior.getLerpedPosition(f);
            renderState.xRot = newMinecartBehavior.getLerpedPitch(f);
            renderState.yRot = newMinecartBehavior.getLerpedYaw(f);
        } else {
            renderState.renderPos = null;
            renderState.xRot = abstractMinecart.getPitch();
            renderState.yRot = abstractMinecart.getYaw();
        }
        long l = abstractMinecart.getId() * 493286711L;
        renderState.offsetSeed = l * l * 4392167121L + l * 98761L;
        renderState.hurtTime = abstractMinecart.getDamageWobbleTicks() - f;
        renderState.hurtDir = abstractMinecart.getDamageWobbleSide();
        renderState.damageTime = Math.max(abstractMinecart.getDamageWobbleTicks() - f, 0.0F);
        renderState.displayOffset = abstractMinecart.getBlockOffset();
        renderState.displayBlockState = abstractMinecart.getContainedBlock();
    }
    public void superExtractRenderState(T entity, float f) {
        renderState.x = MathHelper.lerp(f, entity.prevX, entity.getX());
        renderState.y = MathHelper.lerp(f, entity.prevY, entity.getY());
        renderState.z = MathHelper.lerp(f, entity.prevZ, entity.getZ());
        renderState.isInvisible = entity.isInvisible();
        renderState.ageInTicks = entity.age + f;
        renderState.boundingBoxWidth = entity.getWidth();
        renderState.boundingBoxHeight = entity.getHeight();
        renderState.eyeHeight = entity.getStandingEyeHeight();
        renderState.passengerOffset = null;
        renderState.distanceToCameraSq = this.dispatcher.getSquaredDistanceToCamera(entity);
        renderState.isDiscrete = entity.isSneaky();
    }
    protected void renderBlock(BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        this.blockRenderManager.renderBlockAsEntity(state, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
    }

    @Override
    public Vec3d getPositionOffset(T entity, float tickDelta) {
        Vec3d vec3 = super.getPositionOffset(entity, tickDelta);
        return renderState.renderPos != null
                ? vec3.add(
                renderState.renderPos.x - renderState.x,
                renderState.renderPos.y - renderState.y,
                renderState.renderPos.z - renderState.z
        )
                : vec3;
    }
}