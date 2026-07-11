package com.flowingsun.warresource.module.ore.drop;

import com.flowingsun.warresource.module.ore.config.OreDropConfig;
import com.flowingsun.warresource.module.ore.config.OreDropRule;
import com.flowingsun.warresource.module.ore.town.WarTownLookup;
import com.flowingsun.warresource.tag.WarResourceTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class OreDropHandler {
    private final WarTownLookup townLookup;
    private volatile OreDropConfig config = OreDropConfig.empty();

    public OreDropHandler(WarTownLookup townLookup) {
        this.townLookup = townLookup;
    }

    public void updateConfig(OreDropConfig config) {
        this.config = config;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.isCanceled()) {
            return;
        }

        Player player = event.getPlayer();
        BlockState state = event.getState();
        if (player.isCreative() || !state.is(WarResourceTags.Blocks.ORE_SOURCE_STONES)) {
            return;
        }
        if (!ForgeHooks.isCorrectToolForDrops(state, player)) {
            return;
        }

        String townId = townLookup.findTownId(level.getServer(), event.getPos()).orElse("");
        List<OreDropRule> rules = config.rulesForTown(townId);
        double roll = level.random.nextDouble();
        double cumulativeChance = 0.0D;
        for (OreDropRule rule : rules) {
            cumulativeChance += rule.chance();
            if (roll >= cumulativeChance) {
                continue;
            }
            OreDropTarget.resolve(rule.oreName(), level.random).ifPresent(item -> {
                ItemStack stack = new ItemStack(item);
                Block.popResource(level, event.getPos(), stack);
            });
            return;
        }
    }
}
