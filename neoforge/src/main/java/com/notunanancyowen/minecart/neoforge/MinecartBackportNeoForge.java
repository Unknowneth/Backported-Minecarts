package com.notunanancyowen.minecart.neoforge;

import net.minecraft.world.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.notunanancyowen.minecart.MinecartBackport;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(MinecartBackport.MOD_ID)
public final class MinecartBackportNeoForge {
    public MinecartBackportNeoForge(IEventBus modBus) {
        MinecartBackport.init();
        modBus.addListener(this::registerGamerules);
    }
    private void registerGamerules(final FMLCommonSetupEvent event) {
        MinecartBackport.MINECART_MAX_SPEED = GameRules.register("minecartMaxSpeed", GameRules.Category.MISC, GameRules.IntRule.create(8));
    }
}
