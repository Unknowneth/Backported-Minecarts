package com.notunanancyowen.minecart.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import com.notunanancyowen.minecart.MinecartController;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.*;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//@SuppressWarnings("all")
@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends VehicleEntity implements ImprovedMinecart {
    @Shadow private boolean yawFlipped;

    @Shadow protected abstract boolean willHitBlockAt(BlockPos pos);

    @Shadow private boolean onRail;

    @Shadow private Vec3d clientVelocity;

    @Shadow private int clientInterpolationSteps;

    @Shadow private double clientX;

    @Shadow private double clientY;

    @Shadow private double clientZ;

    @Shadow private double clientYaw;

    @Shadow private double clientPitch;

    public AbstractMinecartEntityMixin(EntityType<?> type, World world) {
        super(type, world);
        controller = new MinecartController<>();
        controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        yawFlipped = false;
    }
    @Unique private static final ImmutableMap<EntityPose, ImmutableList<Integer>> DISMOUNT_FREE_Y_SPACES_NEEDED = ImmutableMap.of(
            EntityPose.STANDING, ImmutableList.of(0, 1, -1), EntityPose.CROUCHING, ImmutableList.of(0, 1, -1), EntityPose.SWIMMING, ImmutableList.of(0, 1)
    );
    @Unique private MinecartController controller = new MinecartController<>();
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void initController(EntityType<? extends AbstractMinecartEntity> entityType, World world, CallbackInfo ci) {
        controller = new MinecartController<>();
        controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        yawFlipped = false;
    }
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;DDD)V", at = @At("TAIL"))
    private void initPositionCorrectly(EntityType type, World world, double x, double y, double z, CallbackInfo ci) {
        BlockPos blockPos = getRailOrMinecartPos();
        BlockState blockState = world.getBlockState(blockPos);
        lastRenderX = x;
        lastRenderY = y;
        lastRenderZ = z;
        if(controller.minecart == null) controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        controller.adjustToRail(blockPos, blockState, true);
    }
    @Override public Vec3d applySlowdown(Vec3d velocity) {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        double d = this.controller.getSpeedRetention();
        Vec3d vec3d = velocity.multiply(d, 0.0, d);
        if (this.isTouchingWater()) {
            vec3d = vec3d.multiply(0.95F);
        }
        return vec3d;
    }

    @Override public Vec3d getLastRenderPos() {
        return new Vec3d(lastRenderX, lastRenderY, lastRenderZ);
    }
    @Override public MinecartController getController() {
        return controller;
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    private void overrideTick(CallbackInfo ci) {
        if (this.getDamageWobbleTicks() > 0) {
            this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
        }
        if (this.getDamageWobbleStrength() > 0.0F) {
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
        }
        if (this.clientInterpolationSteps > 0) {
            this.lerpPosAndRotation(this.clientInterpolationSteps, this.clientX, this.clientY, this.clientZ, this.clientYaw, this.clientPitch);
            this.clientInterpolationSteps--;
        }
        this.attemptTickInVoid();
        this.tickPortalTeleportation();
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        else this.controller.tick();
        this.updateWaterState();
        if (this.isInLava()) {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5;
        }
        this.firstUpdate = false;
        ci.cancel();
    }
    @Override public void tickBlockCollision() {
        checkBlockCollision();
    }

    @Override
    public void setOnRail(boolean onRail) {
        this.onRail = onRail;
    }

    @Override
    public boolean isYawFlipped() {
        return this.yawFlipped;
    }

    @Override
    public void setYawFlipped(boolean yawFlipped) {
        this.yawFlipped = yawFlipped;
    }

    @Override public void applyGravityForMinecart() {
        double d = this.getFinalGravity();
        if (d != 0.0) {
            this.setVelocity(this.getVelocity().add(0.0, -d, 0.0));
        }
    }

    @Override public double getMaxSpeed(ServerWorld serverWorld) {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        return this.controller.getMaxSpeed(serverWorld);
    }

    @Override
    public void moveOffRail(ServerWorld world) {
        double d = this.getMaxSpeed(world);
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(MathHelper.clamp(vec3d.x, -d, d), vec3d.y, MathHelper.clamp(vec3d.z, -d, d));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.5));
        }

        this.move(MovementType.SELF, this.getVelocity());
        if (!this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.95));
        }
    }

    @Override
    public double moveAlongTrack(BlockPos pos, RailShape shape, double remainingMovement) {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        return this.controller.moveAlongTrack(pos, shape, remainingMovement);
    }

    @Override
    public BlockPos getRailOrMinecartPos() {
        int i = MathHelper.floor(this.getX());
        int j = MathHelper.floor(this.getY());
        int k = MathHelper.floor(this.getZ());
        double d = this.getY() - 0.1 - 1.0E-5F;
        if (this.getWorld().getBlockState(BlockPos.ofFloored(i, d, k)).isIn(BlockTags.RAILS)) {
            j = MathHelper.floor(d);
        }
        return new BlockPos(i, j, k);
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        this.controller.setLerpTargetVelocity(x, y, z);
    }

    @Override
    public void moveOnRail(ServerWorld world) {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        this.controller.moveOnRail(world);
    }

    @Override
    public Vec3d getMovement() {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        return this.controller.limitSpeed(super.getMovement());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.yawFlipped = nbt.getBoolean("FlippedRotation");
        this.firstUpdate = nbt.getBoolean("HasTicked");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("FlippedRotation", this.yawFlipped);
        nbt.putBoolean("HasTicked", this.firstUpdate);
    }
    @Override public Vec3d getLaunchDirection(BlockPos railPos) {
        BlockState blockState = this.getWorld().getBlockState(railPos);
        if (blockState.isOf(Blocks.POWERED_RAIL) && (Boolean)blockState.get(PoweredRailBlock.POWERED)) {
            RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
            if (railShape == RailShape.EAST_WEST) {
                if (this.willHitBlockAt(railPos.west())) {
                    return new Vec3d(1.0, 0.0, 0.0);
                }

                if (this.willHitBlockAt(railPos.east())) {
                    return new Vec3d(-1.0, 0.0, 0.0);
                }
            } else if (railShape == RailShape.NORTH_SOUTH) {
                if (this.willHitBlockAt(railPos.north())) {
                    return new Vec3d(0.0, 0.0, 1.0);
                }

                if (this.willHitBlockAt(railPos.south())) {
                    return new Vec3d(0.0, 0.0, -1.0);
                }
            }

            return Vec3d.ZERO;
        } else {
            return Vec3d.ZERO;
        }
    }
    @Override public boolean isFirstUpdate() {
        return firstUpdate;
    }
    @Override
    public void move(MovementType type, Vec3d movement) {
        Vec3d vec3d = this.getPos().add(movement);
        super.move(type, movement);
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        boolean bl = this.controller.handleCollision();
        if (bl) {
            super.move(type, vec3d.subtract(this.getPos()));
        }
        if (type.equals(MovementType.PISTON)) {
            this.onRail = false;
        }
    }
    @Override
    public boolean isRideable() {
        return getType().equals(EntityType.MINECART);
    }
    @Inject(method = "updatePassengerForDismount", at = @At("HEAD"))
    private void updatePassengerForDismount(LivingEntity passenger, CallbackInfoReturnable<Vec3d> cir) {
        Direction direction = this.getMovementDirection();
        if (direction.getAxis() == Direction.Axis.Y) super.updatePassengerForDismount(passenger);
        else {
            int[][] is = Dismounting.getDismountOffsets(direction);
            BlockPos blockPos = this.getBlockPos();
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            ImmutableList<EntityPose> immutableList = passenger.getPoses();

            for (EntityPose entityPose : immutableList) {
                EntityDimensions entityDimensions = passenger.getDimensions(entityPose);
                float f = Math.min(entityDimensions.width(), 1.0F) / 2.0F;

                for (int i : DISMOUNT_FREE_Y_SPACES_NEEDED.get(entityPose)) {
                    for (int[] js : is) {
                        mutable.set(blockPos.getX() + js[0], blockPos.getY() + i, blockPos.getZ() + js[1]);
                        double d = this.getWorld()
                                .getDismountHeight(Dismounting.getCollisionShape(this.getWorld(), mutable), () -> Dismounting.getCollisionShape(this.getWorld(), mutable.down()));
                        if (Dismounting.canDismountInBlock(d)) {
                            Box box = new Box(-f, 0.0, -f, f, entityDimensions.height(), f);
                            Vec3d vec3d = Vec3d.ofCenter(mutable, d);
                            if (Dismounting.canPlaceEntityAt(this.getWorld(), passenger, box.offset(vec3d))) {
                                passenger.setPose(entityPose);
                                return;
                            }
                        }
                    }
                }
            }

            double e = this.getBoundingBox().maxY;
            mutable.set(blockPos.getX(), e, blockPos.getZ());

            for (EntityPose entityPose2 : immutableList) {
                double g = passenger.getDimensions(entityPose2).height();
                int j = MathHelper.ceil(e - mutable.getY() + g);
                double h = Dismounting.getCeilingHeight(mutable, j, pos -> this.getWorld().getBlockState(pos).getCollisionShape(this.getWorld(), pos));
                if (e + g <= h) {
                    passenger.setPose(entityPose2);
                    break;
                }
            }

            super.updatePassengerForDismount(passenger);
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void overridePushAway(Entity entity, CallbackInfo ci) {
        ci.cancel();
        if (!this.getWorld().isClient) {
            if (!entity.noClip && !this.noClip) {
                if (!this.hasPassenger(entity)) {
                    double d = entity.getX() - this.getX();
                    double e = entity.getZ() - this.getZ();
                    double f = d * d + e * e;
                    if (f >= 1.0E-4F) {
                        f = Math.sqrt(f);
                        d /= f;
                        e /= f;
                        double g = 1.0 / f;
                        if (g > 1.0) {
                            g = 1.0;
                        }

                        d *= g;
                        e *= g;
                        d *= 0.1F;
                        e *= 0.1F;
                        d *= 0.5;
                        e *= 0.5;
                        if (entity instanceof AbstractMinecartEntity abstractMinecartEntity) {
                            this.pushAwayFromMinecart(abstractMinecartEntity, d, e);
                        } else {
                            this.addVelocity(-d, 0.0, -e);
                            entity.addVelocity(d / 4.0, 0.0, e / 4.0);
                        }
                    }
                }
            }
        }
    }
    @Unique private void pushAwayFromMinecart(AbstractMinecartEntity entity, double xDiff, double zDiff) {
        Vec3d vec3d3 = this.getVelocity();
        Vec3d vec3d4 = entity.getVelocity();
        if (entity instanceof ImprovedMinecart i && i.isSelfPropelling() && !this.isSelfPropelling()) {
            this.setVelocity(vec3d3.multiply(0.2, 1.0, 0.2));
            this.addVelocity(vec3d4.x - xDiff, 0.0, vec3d4.z - zDiff);
            entity.setVelocity(vec3d4.multiply(0.95, 1.0, 0.95));
        } else if (entity instanceof ImprovedMinecart i && !i.isSelfPropelling() && this.isSelfPropelling()) {
            entity.setVelocity(vec3d4.multiply(0.2, 1.0, 0.2));
            entity.addVelocity(vec3d3.x + xDiff, 0.0, vec3d3.z + zDiff);
            this.setVelocity(vec3d3.multiply(0.95, 1.0, 0.95));
        } else {
            double g = (vec3d4.x + vec3d3.x) / 2.0;
            double h = (vec3d4.z + vec3d3.z) / 2.0;
            this.setVelocity(vec3d3.multiply(0.2, 1.0, 0.2));
            this.addVelocity(g - xDiff, 0.0, h - zDiff);
            entity.setVelocity(vec3d4.multiply(0.2, 1.0, 0.2));
            entity.addVelocity(g + xDiff, 0.0, h + zDiff);
        }
    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At("HEAD"), cancellable = true)
    private void overrideTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci) {
        ci.cancel();
        this.setPos(x, y, z);
        this.setYaw(yaw % 360.0F);
        this.setPitch(pitch % 360.0F);
    }

    @Inject(method = "getLerpTargetX", at = @At("HEAD"), cancellable = true)
    private void overrideLerpTargetX(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(getX());
    }

    @Inject(method = "getLerpTargetY", at = @At("HEAD"), cancellable = true)
    private void overrideLerpTargetY(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(getY());
    }

    @Inject(method = "getLerpTargetZ", at = @At("HEAD"), cancellable = true)
    private void overrideLerpTargetZ(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(getZ());
    }

    @Inject(method = "getLerpTargetPitch", at = @At("HEAD"), cancellable = true)
    private void overrideLerpTargetPitch(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(getPitch());
    }

    @Inject(method = "getLerpTargetYaw", at = @At("HEAD"), cancellable = true)
    private void overrideLerpTargetYaw(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(getYaw());
    }

    @Inject(method = "setVelocityClient", at = @At("HEAD"), cancellable = true)
    private void overrideSetVelocityClient(double x, double y, double z, CallbackInfo ci) {
        ci.cancel();
        setVelocity(x, y, z);
    }
    @Override public boolean isSelfPropelling() {
        return getType().equals(EntityType.FURNACE_MINECART);
    }

    @Inject(method = "getMovementDirection", at = @At("HEAD"), cancellable = true)
    private void overrideMovementDirection(CallbackInfoReturnable<Direction> cir) {
        if(this.controller.minecart == null) this.controller.minecart = (AbstractMinecartEntity & ImprovedMinecart)(Object)this;
        cir.setReturnValue(controller.getHorizontalFacing());
    }
}
