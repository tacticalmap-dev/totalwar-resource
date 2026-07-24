package com.flowingsun.warresource.module.ore.town;

import com.flowingsun.modernwar.api.map.MapDivideStateApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;

public class ModernWarMapLookup {
    public Optional<String> findTownId(MinecraftServer server, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Optional<Integer> nodeNumber = MapDivideStateApi.findNodeContainingChunk(server, chunkPos.x, chunkPos.z);
        if (nodeNumber.isEmpty()) {
            return Optional.empty();
        }

        return MapDivideStateApi.findTownContainingNode(server, nodeNumber.get());
    }
}
