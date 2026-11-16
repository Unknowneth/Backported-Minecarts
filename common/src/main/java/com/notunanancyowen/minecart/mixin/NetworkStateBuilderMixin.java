package com.notunanancyowen.minecart.mixin;

import com.notunanancyowen.minecart.MinecartBackport;
import com.notunanancyowen.minecart.MoveMinecartAlongTrackS2CPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.NetworkStateBuilder;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(NetworkStateBuilder.class)
public abstract class NetworkStateBuilderMixin<T extends PacketListener, B extends ByteBuf> {
    @Inject(method = "addBundle", at = @At("TAIL"))
    private <P extends BundlePacket<? super T>, D extends BundleSplitterPacket<? super T>> void addMinecartNetCode(PacketType<P> id, Function<Iterable<Packet<? super T>>, P> bundler, D splitter, CallbackInfoReturnable<NetworkStateBuilder<T, B>> cir) {
        try {
            ((NetworkStateBuilder)cir.getReturnValue()).add(MinecartBackport.MOVE_MINECART_ALONG_TRACK, MoveMinecartAlongTrackS2CPacket.PACKET_CODEC);
        }
        catch (Throwable ignore) {

        }
    }
}
