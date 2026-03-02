package ru.vadim.chunkbuyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}
