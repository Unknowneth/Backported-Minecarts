package com.notunanancyowen.minecart.mixin;

import com.notunanancyowen.minecart.MinecartController;
import com.notunanancyowen.minecart.MoveMinecartAlongTrackS2CPacket;
import com.notunanancyowen.minecart.dataholders.ClientPlayNetworkPatch;
import com.notunanancyowen.minecart.dataholders.ImprovedMinecart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler implements ClientPlayNetworkPatch {
    @Shadow private ClientWorld world;
    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }
    @Override public void onMoveMinecartAlongTrack(MoveMinecartAlongTrackS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler)(ClientCommonNetworkHandler)this, client);
        Entity entity = packet.getEntity(this.world);
        if (entity instanceof ImprovedMinecart abstractMinecartEntity) {
            if (!entity.isLogicalSideForUpdatingMovement()
                    && abstractMinecartEntity.getController() instanceof MinecartController experimentalMinecartController) {
                experimentalMinecartController.stagingLerpSteps.addAll(packet.lerpSteps());

                entity.setPosition(packet.lerpSteps().getLast().position());
                entity.setPitch(packet.lerpSteps().getLast().xRot());
                entity.setYaw(packet.lerpSteps().getLast().yRot());
            }
        }
    }
}
