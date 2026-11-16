package com.notunanancyowen.minecart.dataholders;

import com.notunanancyowen.minecart.MinecartController;
import net.minecraft.block.enums.RailShape;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface ImprovedMinecart {
    boolean isFirstUpdate();
    Vec3d getLaunchDirection(BlockPos railPos);
    void setOnRail(boolean onRail);
    boolean isYawFlipped();
    void setYawFlipped(boolean yawFlipped);
    void applyGravityForMinecart();
    void moveOnRail(ServerWorld server);
    void moveOffRail(ServerWorld server);
    double getMaxSpeed(ServerWorld world);
    double moveAlongTrack(BlockPos pos, RailShape shape, double remainingMovement);
    BlockPos getRailOrMinecartPos();
    boolean isRideable();
    boolean isSelfPropelling();
    Vec3d getLastRenderPos();
    Vec3d applySlowdown(Vec3d pos);
    void tickBlockCollision();
    MinecartController getController();
}
