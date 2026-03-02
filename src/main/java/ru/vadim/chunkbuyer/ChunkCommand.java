package ru.vadim.chunkbuyer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ChunkCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = Arrays.asList("buy", "unclaim", "info", "balance", "give", "addmember", "explosions", "borders");

    private final ChunkBuyerPlugin plugin;
    private final ClaimService claimService;

    public ChunkCommand(ChunkBuyerPlugin plugin, ClaimService claimService) {
        this.plugin = plugin;
        this.claimService = claimService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/chunkbuyer <buy|unclaim|info|balance|give|addmember|explosions|borders>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if ("give".equals(subcommand) || "explosions".equals(subcommand)) {
            return handleGive(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        ChunkId chunkId = new ChunkId(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        UUID playerId = player.getUniqueId();

        switch (subcommand) {
            case "buy" -> {
                ClaimService.BuyResult result = claimService.buyChunk(playerId, chunkId);
                if (result == ClaimService.BuyResult.SUCCESS) {
                    player.sendMessage("Chunk purchased for " + claimService.getChunkPrice() + " donate coins.");
                } else if (result == ClaimService.BuyResult.ALREADY_CLAIMED) {
                    player.sendMessage("This chunk is already claimed.");
                } else {
                    player.sendMessage("Not enough donate coins. Price: " + claimService.getChunkPrice());
                }
                return true;
            }
            case "unclaim" -> {
                if (claimService.unclaim(playerId, chunkId)) {
                    player.sendMessage("Chunk unclaimed.");
                } else {
                    player.sendMessage("You can unclaim only your own chunk.");
                }
                return true;
            }
            case "info" -> {
                UUID owner = claimService.getOwner(chunkId);
                if (owner == null) {
                    player.sendMessage("Chunk is not claimed.");
                } else {
                    OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(owner);
                    player.sendMessage("Chunk owner: " + (ownerPlayer.getName() == null ? owner.toString() : ownerPlayer.getName()));
                }
                return true;
            }
            case "balance" -> {
                player.sendMessage("Donate balance: " + claimService.getBalance(playerId));
                return true;
            }
            case "addmember" -> {
                if (args.length < 2) {
                    player.sendMessage("Usage: /chunkbuyer addmember <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target.getUniqueId().equals(playerId)) {
                    player.sendMessage("You are already owner.");
                    return true;
                }
                ClaimService.AddMemberResult result = claimService.addMember(playerId, chunkId, target.getUniqueId());
                if (result == ClaimService.AddMemberResult.SUCCESS) {
                    player.sendMessage("Player " + args[1] + " added to this claim.");
                } else if (result == ClaimService.AddMemberResult.UNCLAIMED) {
                    player.sendMessage("Chunk is not claimed.");
                } else if (result == ClaimService.AddMemberResult.NOT_OWNER) {
                    player.sendMessage("You can add members only in your own claim.");
                } else {
                    player.sendMessage("You are already owner.");
                }
                return true;
            }
            case "borders" -> {
                highlightChunkBorders(player, chunk);
                player.sendMessage("Claim borders highlighted.");
                return true;
            }
            default -> {
                sender.sendMessage("Unknown subcommand.");
                return true;
            }
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if ("explosions".equals(args[0].toLowerCase())) {
            return handleExplosions(sender, args);
        }
        if (!sender.hasPermission("chunkbuyer.admin")) {
            sender.sendMessage("No permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /chunkbuyer give <player> <amount>");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Amount must be a number.");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("Amount must be positive.");
            return true;
        }

        claimService.addBalance(target.getUniqueId(), amount);
        sender.sendMessage("Added " + amount + " donate coins to " + target.getName());
        target.sendMessage("You received " + amount + " donate coins.");
        return true;
    }

    private boolean handleExplosions(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("Only OP can use this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /chunkbuyer explosions <on|off>");
            return true;
        }
        boolean enabled;
        if ("on".equalsIgnoreCase(args[1])) {
            enabled = true;
        } else if ("off".equalsIgnoreCase(args[1])) {
            enabled = false;
        } else {
            sender.sendMessage("Usage: /chunkbuyer explosions <on|off>");
            return true;
        }
        claimService.setExplosionDamageEnabled(enabled);
        sender.sendMessage("Explosion damage in claims is now " + (enabled ? "enabled" : "disabled") + ".");
        return true;
    }

    private void highlightChunkBorders(Player player, Chunk chunk) {
        World world = chunk.getWorld();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        double[] xCorners = new double[] {baseX, baseX + 16.0D};
        double[] zCorners = new double[] {baseZ, baseZ + 16.0D};

        for (double x : xCorners) {
            for (double z : zCorners) {
                for (int y = minY; y <= maxY; y += 4) {
                    world.spawnParticle(Particle.END_ROD, x, y + 0.2D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        int y = Math.max(minY, Math.min(maxY - 1, player.getLocation().getBlockY() + 1));
        for (int offset = 0; offset <= 16; offset += 16) {
            double x = baseX + offset;
            world.spawnParticle(Particle.VILLAGER_HAPPY, x, y, baseZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(Particle.VILLAGER_HAPPY, x, y, baseZ + 16.0D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            double z = baseZ + offset;
            world.spawnParticle(Particle.VILLAGER_HAPPY, baseX, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(Particle.VILLAGER_HAPPY, baseX + 16.0D, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(input)) {
                    result.add(subcommand);
                }
            }
            return result;
        }
        if (args.length == 2 && "explosions".equalsIgnoreCase(args[0])) {
            return Arrays.asList("on", "off");
        }
        return List.of();
    }
}
