package com.notunanancyowen.minecart.mixin;

import com.notunanancyowen.minecart.MinecartBackport;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayPackets.class)
public abstract class PlayPacketsMixin {
    @Shadow
    private static <T extends Packet<ClientPlayPacketListener>> PacketType<T> s2c(String id) {
        return null;
    }
    static {
        MinecartBackport.MOVE_MINECART_ALONG_TRACK = s2c("move_minecart_along_track");
    }
}
