package com.notunanancyowen.minecart.dataholders;

import net.minecraft.entity.Entity;

public interface PreExtractor {
    void extract(Entity entity, float partialTicks);
}
