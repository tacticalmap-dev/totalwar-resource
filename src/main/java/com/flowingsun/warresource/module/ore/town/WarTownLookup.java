package com.flowingsun.warresource.module.ore.town;

import com.module.mapdivide.MapDivideStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;

public class WarTownLookup {
    public Optional<String> findTownId(MinecraftServer server, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Optional<Integer> nodeNumber = MapDivideStorage.findNodeContainingChunk(server, chunkPos.x, chunkPos.z);
        if (nodeNumber.isEmpty()) {
            return Optional.empty();
        }

        for (String townId : MapDivideStorage.getAllTownIds(server)) {
            if (MapDivideStorage.getTownNodeNumbers(server, townId).contains(nodeNumber.get())) {
                return Optional.of(townId);
            }
        }
        return Optional.empty();
    }
}
