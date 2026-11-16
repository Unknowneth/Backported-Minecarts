package com.notunanancyowen.minecart.dataholders;

import com.notunanancyowen.minecart.MoveMinecartAlongTrackS2CPacket;

public interface ClientPlayNetworkPatch {
    void onMoveMinecartAlongTrack(MoveMinecartAlongTrackS2CPacket packet);
}
