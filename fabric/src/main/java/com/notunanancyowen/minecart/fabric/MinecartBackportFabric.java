package com.notunanancyowen.minecart.fabric;

import net.fabricmc.api.ModInitializer;

import com.notunanancyowen.minecart.MinecartBackport;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public final class MinecartBackportFabric implements ModInitializer {
    @Override public void onInitialize() {
        MinecartBackport.init();
        MinecartBackport.MINECART_MAX_SPEED = GameRuleRegistry.register("minecartMaxSpeed", GameRules.Category.MISC, GameRuleFactory.createIntRule(8, 1, 1000));
    }
}
