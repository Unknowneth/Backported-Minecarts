package com.notunanancyowen.minecart;

import net.minecraft.network.packet.PacketType;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecartBackport {
    public static PacketType<MoveMinecartAlongTrackS2CPacket> MOVE_MINECART_ALONG_TRACK;
    public static final String MOD_ID = "minecart_backport";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static GameRules.Key<GameRules.IntRule> MINECART_MAX_SPEED;
    public static void init() {
        LOGGER.info("Backported new minecarts");
    }
}
