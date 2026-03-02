package ru.vadim.chunkbuyer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClaimService {
    public enum BuyResult {
        SUCCESS,
        ALREADY_CLAIMED,
        INSUFFICIENT_FUNDS
    }

    public enum AddMemberResult {
        SUCCESS,
        UNCLAIMED,
        NOT_OWNER,
        SELF
    }

    private final double chunkPrice;
    private final Map<ChunkId, UUID> claims = new HashMap<>();
    private final Map<ChunkId, Set<UUID>> members = new HashMap<>();
    private final Map<UUID, Double> balances = new HashMap<>();
    private boolean explosionDamageEnabled;

    public ClaimService(double chunkPrice) {
        this.chunkPrice = chunkPrice;
    }

    public double getChunkPrice() {
        return chunkPrice;
    }

    public double getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, 0.0D);
    }

    public void addBalance(UUID playerId, double amount) {
        if (amount <= 0) {
            return;
        }
        balances.put(playerId, getBalance(playerId) + amount);
    }

    public void setBalance(UUID playerId, double amount) {
        if (amount <= 0) {
            balances.remove(playerId);
            return;
        }
        balances.put(playerId, amount);
    }

    public BuyResult buyChunk(UUID playerId, ChunkId chunkId) {
        if (claims.containsKey(chunkId)) {
            return BuyResult.ALREADY_CLAIMED;
        }

        double balance = getBalance(playerId);
        if (balance < chunkPrice) {
            return BuyResult.INSUFFICIENT_FUNDS;
        }

        balances.put(playerId, balance - chunkPrice);
        claims.put(chunkId, playerId);
        return BuyResult.SUCCESS;
    }

    public boolean unclaim(UUID playerId, ChunkId chunkId) {
        UUID owner = claims.get(chunkId);
        if (owner == null || !owner.equals(playerId)) {
            return false;
        }
        claims.remove(chunkId);
        members.remove(chunkId);
        return true;
    }

    public UUID getOwner(ChunkId chunkId) {
        return claims.get(chunkId);
    }

    public boolean canModify(UUID playerId, ChunkId chunkId) {
        UUID owner = claims.get(chunkId);
        if (owner == null || owner.equals(playerId)) {
            return true;
        }
        Set<UUID> chunkMembers = members.get(chunkId);
        return chunkMembers != null && chunkMembers.contains(playerId);
    }

    public Map<ChunkId, UUID> getClaimsSnapshot() {
        return new HashMap<>(claims);
    }

    public Map<ChunkId, Set<UUID>> getMembersSnapshot() {
        Map<ChunkId, Set<UUID>> snapshot = new HashMap<>();
        for (Map.Entry<ChunkId, Set<UUID>> entry : members.entrySet()) {
            snapshot.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return snapshot;
    }

    public Map<UUID, Double> getBalancesSnapshot() {
        return new HashMap<>(balances);
    }

    public void setClaim(ChunkId chunkId, UUID playerId) {
        claims.put(chunkId, playerId);
    }

    public void setMembers(ChunkId chunkId, Set<UUID> chunkMembers) {
        if (chunkMembers == null || chunkMembers.isEmpty()) {
            members.remove(chunkId);
            return;
        }
        members.put(chunkId, new HashSet<>(chunkMembers));
    }

    public AddMemberResult addMember(UUID ownerId, ChunkId chunkId, UUID memberId) {
        UUID owner = claims.get(chunkId);
        if (owner == null) {
            return AddMemberResult.UNCLAIMED;
        }
        if (!owner.equals(ownerId)) {
            return AddMemberResult.NOT_OWNER;
        }
        if (owner.equals(memberId)) {
            return AddMemberResult.SELF;
        }
        members.computeIfAbsent(chunkId, ignored -> new HashSet<>()).add(memberId);
        return AddMemberResult.SUCCESS;
    }

    public boolean isExplosionDamageEnabled() {
        return explosionDamageEnabled;
    }

    public void setExplosionDamageEnabled(boolean explosionDamageEnabled) {
        this.explosionDamageEnabled = explosionDamageEnabled;
    }
}
