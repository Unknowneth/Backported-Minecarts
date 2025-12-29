package com.notunanancyowen.minecart;

import java.util.List;

import com.notunanancyowen.minecart.dataholders.ClientPlayNetworkPatch;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record MoveMinecartAlongTrackS2CPacket(int entityId, List<MinecartController.Step> lerpSteps) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, MoveMinecartAlongTrackS2CPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT,
            MoveMinecartAlongTrackS2CPacket::entityId,
            MinecartController.Step.PACKET_CODEC.collect(PacketCodecs.toList()),
            MoveMinecartAlongTrackS2CPacket::lerpSteps,
            MoveMinecartAlongTrackS2CPacket::new
    );

    @Override
    public PacketType<MoveMinecartAlongTrackS2CPacket> getPacketId() {
        return MinecartBackport.MOVE_MINECART_ALONG_TRACK;
    }

    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        if(clientPlayPacketListener instanceof ClientPlayNetworkPatch cpnp) cpnp.onMoveMinecartAlongTrack(this);
    }

    @Nullable
    public Entity getEntity(World world) {
        return world.getEntityById(this.entityId);
    }
}