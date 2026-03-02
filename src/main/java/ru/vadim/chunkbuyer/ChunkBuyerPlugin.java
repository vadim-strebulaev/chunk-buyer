package ru.vadim.chunkbuyer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkBuyerPlugin extends JavaPlugin {
    private ClaimService claimService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        claimService = new ClaimService(getConfig().getDouble("chunk-price", 100.0D));
        loadData();

        ChunkCommand chunkCommand = new ChunkCommand(this, claimService);
        if (getCommand("chunkbuyer") != null) {
            getCommand("chunkbuyer").setExecutor(chunkCommand);
            getCommand("chunkbuyer").setTabCompleter(chunkCommand);
        }

        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(claimService), this);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void loadData() {
        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) {
            return;
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection balancesSection = data.getConfigurationSection("balances");
        if (balancesSection != null) {
            for (String key : balancesSection.getKeys(false)) {
                claimService.setBalance(UUID.fromString(key), balancesSection.getDouble(key));
            }
        }

        ConfigurationSection claimsSection = data.getConfigurationSection("claims");
        if (claimsSection != null) {
            for (String world : claimsSection.getKeys(false)) {
                ConfigurationSection worldSection = claimsSection.getConfigurationSection(world);
                if (worldSection == null) {
                    continue;
                }
                for (String chunkKey : worldSection.getKeys(false)) {
                    String[] parts = chunkKey.split(":");
                    if (parts.length != 2) {
                        continue;
                    }
                    try {
                        int x = Integer.parseInt(parts[0]);
                        int z = Integer.parseInt(parts[1]);
                        UUID owner = UUID.fromString(worldSection.getString(chunkKey, ""));
                        claimService.setClaim(new ChunkId(world, x, z), owner);
                    } catch (IllegalArgumentException ignored) {
                        // skip invalid entry
                    }
                }
            }
        }

        ConfigurationSection membersSection = data.getConfigurationSection("members");
        if (membersSection != null) {
            for (String world : membersSection.getKeys(false)) {
                ConfigurationSection worldSection = membersSection.getConfigurationSection(world);
                if (worldSection == null) {
                    continue;
                }
                for (String chunkKey : worldSection.getKeys(false)) {
                    String[] parts = chunkKey.split(":");
                    if (parts.length != 2) {
                        continue;
                    }
                    try {
                        int x = Integer.parseInt(parts[0]);
                        int z = Integer.parseInt(parts[1]);
                        Set<UUID> chunkMembers = new HashSet<>();
                        for (String value : worldSection.getStringList(chunkKey)) {
                            chunkMembers.add(UUID.fromString(value));
                        }
                        claimService.setMembers(new ChunkId(world, x, z), chunkMembers);
                    } catch (IllegalArgumentException ignored) {
                        // skip invalid entry
                    }
                }
            }
        }

        claimService.setExplosionDamageEnabled(data.getBoolean("explosion-damage-enabled", false));
    }

    private void saveData() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            return;
        }

        File file = new File(getDataFolder(), "data.yml");
        YamlConfiguration data = new YamlConfiguration();

        for (Map.Entry<UUID, Double> entry : claimService.getBalancesSnapshot().entrySet()) {
            data.set("balances." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChunkId, UUID> entry : claimService.getClaimsSnapshot().entrySet()) {
            ChunkId id = entry.getKey();
            data.set("claims." + id.world() + "." + id.x() + ":" + id.z(), entry.getValue().toString());
        }

        for (Map.Entry<ChunkId, Set<UUID>> entry : claimService.getMembersSnapshot().entrySet()) {
            ChunkId id = entry.getKey();
            data.set(
                    "members." + id.world() + "." + id.x() + ":" + id.z(),
                    entry.getValue().stream().map(UUID::toString).toList());
        }

        data.set("explosion-damage-enabled", claimService.isExplosionDamageEnabled());

        try {
            data.save(file);
        } catch (IOException e) {
            getLogger().warning("Failed to save claim data: " + e.getMessage());
        }
    }
}
