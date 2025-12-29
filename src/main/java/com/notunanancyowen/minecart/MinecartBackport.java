package com.notunanancyowen.minecart;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.network.packet.PacketType;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartBackport implements ModInitializer {
	public static PacketType<MoveMinecartAlongTrackS2CPacket> MOVE_MINECART_ALONG_TRACK;
	public static final String MOD_ID = "minecart-backport";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final GameRules.Key<GameRules.IntRule> MINECART_MAX_SPEED = GameRuleRegistry.register("minecartMaxSpeed", GameRules.Category.MISC, GameRuleFactory.createIntRule(8, 1, 1000));
	@Override public void onInitialize() {
		LOGGER.info("Backported new minecarts");
	}
}