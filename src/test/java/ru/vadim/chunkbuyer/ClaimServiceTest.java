package ru.vadim.chunkbuyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClaimServiceTest {
    @Test
    void buyChunkConsumesBalanceAndSetsOwner() {
        ClaimService service = new ClaimService(100.0D);
        UUID player = UUID.randomUUID();
        ChunkId chunkId = new ChunkId("world", 1, 2);
        service.addBalance(player, 150.0D);

        ClaimService.BuyResult result = service.buyChunk(player, chunkId);

        assertEquals(ClaimService.BuyResult.SUCCESS, result);
        assertEquals(50.0D, service.getBalance(player));
        assertEquals(player, service.getOwner(chunkId));
    }

    @Test
    void buyChunkFailsWhenAlreadyClaimed() {
        ClaimService service = new ClaimService(100.0D);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ChunkId chunkId = new ChunkId("world", 1, 2);
        service.addBalance(first, 100.0D);
        service.addBalance(second, 100.0D);
        service.buyChunk(first, chunkId);

        ClaimService.BuyResult result = service.buyChunk(second, chunkId);

        assertEquals(ClaimService.BuyResult.ALREADY_CLAIMED, result);
        assertEquals(100.0D, service.getBalance(second));
    }

    @Test
    void buyChunkFailsWhenBalanceTooLow() {
        ClaimService service = new ClaimService(100.0D);
        UUID player = UUID.randomUUID();
        ChunkId chunkId = new ChunkId("world", 1, 2);
        service.addBalance(player, 10.0D);

        ClaimService.BuyResult result = service.buyChunk(player, chunkId);

        assertEquals(ClaimService.BuyResult.INSUFFICIENT_FUNDS, result);
        assertNull(service.getOwner(chunkId));
    }

    @Test
    void addMemberAllowsModifyInOwnedClaim() {
        ClaimService service = new ClaimService(100.0D);
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        ChunkId chunkId = new ChunkId("world", 4, 8);
        service.setClaim(chunkId, owner);

        ClaimService.AddMemberResult result = service.addMember(owner, chunkId, member);

        assertEquals(ClaimService.AddMemberResult.SUCCESS, result);
        assertTrue(service.canModify(member, chunkId));
    }

    @Test
    void addMemberFailsWhenCallerIsNotOwner() {
        ClaimService service = new ClaimService(100.0D);
        UUID owner = UUID.randomUUID();
        UUID notOwner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        ChunkId chunkId = new ChunkId("world", 4, 8);
        service.setClaim(chunkId, owner);

        ClaimService.AddMemberResult result = service.addMember(notOwner, chunkId, member);

        assertEquals(ClaimService.AddMemberResult.NOT_OWNER, result);
        assertFalse(service.canModify(member, chunkId));
    }

    @Test
    void explosionDamageToggleCanBeChanged() {
        ClaimService service = new ClaimService(100.0D);

        assertFalse(service.isExplosionDamageEnabled());
        service.setExplosionDamageEnabled(true);
        assertTrue(service.isExplosionDamageEnabled());
    }
}
