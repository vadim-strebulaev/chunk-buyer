package ru.vadim.chunkbuyer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ChunkCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = Arrays.asList("buy", "unclaim", "info", "balance", "give");

    private final ChunkBuyerPlugin plugin;
    private final ClaimService claimService;

    public ChunkCommand(ChunkBuyerPlugin plugin, ClaimService claimService) {
        this.plugin = plugin;
        this.claimService = claimService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/chunkbuyer <buy|unclaim|info|balance|give>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if ("give".equals(subcommand)) {
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
            default -> {
                sender.sendMessage("Unknown subcommand.");
                return true;
            }
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
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
        return List.of();
    }
}
