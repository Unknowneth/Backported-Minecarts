package com.notunanancyowen.minecart.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class MinecartRenderState {
    public float xRot;
    public float yRot;
    public long offsetSeed;
    public int hurtDir;
    public float hurtTime;
    public float damageTime;
    public int displayOffset;
    public BlockState displayBlockState = Blocks.AIR.getDefaultState();
    @Nullable
    public Vec3d renderPos;
    @Nullable
    public Vec3d posOnRail;
    @Nullable
    public Vec3d frontPos;
    @Nullable
    public Vec3d backPos;

    public double x;
    public double y;
    public double z;
    public float ageInTicks;
    public float boundingBoxWidth;
    public float boundingBoxHeight;
    public float eyeHeight;
    public double distanceToCameraSq;
    public boolean isInvisible;
    public boolean isDiscrete;
    public boolean displayFireAnimation;
    @Nullable
    public Vec3d passengerOffset;
}
