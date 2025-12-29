package com.notunanancyowen.minecart.mixin;

import com.notunanancyowen.minecart.MinecartController;
import com.notunanancyowen.minecart.MoveMinecartAlongTrackS2CPacket;
import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
    @Shadow protected abstract void syncEntityData();
    @Shadow @Final private Entity entity;
    @Shadow @Final private TrackedPosition trackedPos;
    @Shadow private int trackingTick;
    @Shadow @Final private Consumer<Packet<?>> receiver;
    @Shadow private Vec3d velocity;
    @Shadow private int lastYaw;
    @Shadow private int lastPitch;
    @Shadow protected abstract void sendSyncPacket(Packet<?> packet);
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"), cancellable = true)
    private void overrideTickForMinecart(CallbackInfo ci) {
        if(!entity.hasVehicle() && entity instanceof ImprovedMinecart im) {
            int i = MathHelper.floor(this.entity.getYaw() * 256.0F / 360.0F);
            int j = MathHelper.floor(this.entity.getPitch() * 256.0F / 360.0F);
            boolean bl = Math.abs(i - this.lastYaw) >= 1 || Math.abs(j - this.lastPitch) >= 1;
            tickExperimentalMinecart(im.getController(), (byte)i, (byte)j, bl);
            this.trackingTick++;
            if (this.entity.velocityModified) {
                this.entity.velocityModified = false;
                this.sendSyncPacket(new EntityVelocityUpdateS2CPacket(this.entity));
            }
            ci.cancel();
        }
    }
    @Unique
    private void tickExperimentalMinecart(MinecartController controller, byte yaw, byte pitch, boolean changedAngles) {
        this.syncEntityData();
        if (controller.stagingLerpSteps.isEmpty()) {
            Vec3d vec3d = this.entity.getVelocity();
            double d = vec3d.squaredDistanceTo(this.velocity);
            Vec3d vec3d2 = this.entity.getSyncedPos();
            boolean bl = this.trackedPos.subtract(vec3d2).lengthSquared() >= 7.6293945E-6F;
            boolean bl2 = bl || this.trackingTick % 60 == 0;
            if (bl2 || changedAngles || d > 1.0E-7) {
                this.receiver
                        .accept(new MoveMinecartAlongTrackS2CPacket(
                                        this.entity.getId(),
                                        List.of(new MinecartController.Step(this.entity.getPos(), this.entity.getVelocity(), this.entity.getYaw(), this.entity.getPitch(), 1.0F))
                                )

                        );
            }
        } else {
            this.receiver.accept(new MoveMinecartAlongTrackS2CPacket(this.entity.getId(), List.copyOf(controller.stagingLerpSteps)));
            controller.stagingLerpSteps.clear();
        }

        this.lastYaw = yaw;
        this.lastPitch = pitch;
        this.trackedPos.setPos(this.entity.getPos());
    }
}
