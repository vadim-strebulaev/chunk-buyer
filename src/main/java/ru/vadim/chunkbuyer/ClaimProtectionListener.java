package ru.vadim.chunkbuyer;

import java.util.Iterator;
import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ClaimProtectionListener implements Listener {
    private final ClaimService claimService;

    public ClaimProtectionListener(ClaimService claimService) {
        this.claimService = claimService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!claimService.canModify(event.getPlayer().getUniqueId(), toChunkId(event.getBlock()))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This is чужой приват.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!claimService.canModify(event.getPlayer().getUniqueId(), toChunkId(event.getBlockPlaced()))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This is чужой приват.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        UUID actor = getActor(event.getEntity());
        filterExplosionBlocks(event.blockList().iterator(), actor);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        filterExplosionBlocks(event.blockList().iterator(), null);
    }

    private void filterExplosionBlocks(Iterator<Block> iterator, UUID actor) {
        while (iterator.hasNext()) {
            Block block = iterator.next();
            UUID owner = claimService.getOwner(toChunkId(block));
            if (owner != null && (actor == null || !owner.equals(actor))) {
                iterator.remove();
            }
        }
    }

    private UUID getActor(Entity entity) {
        if (entity instanceof Player player) {
            return player.getUniqueId();
        }
        return null;
    }

    private ChunkId toChunkId(Block block) {
        return new ChunkId(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
    }
}
