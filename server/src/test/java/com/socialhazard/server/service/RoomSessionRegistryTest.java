package com.socialhazard.server.service;

import com.socialhazard.server.model.domain.SessionBinding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomSessionRegistryTest {

    @Test
    void bindLookupAndUnbindAreConsistent() {
        RoomSessionRegistry registry = new RoomSessionRegistry();

        registry.bind("session-1", "R001", "player-1");

        assertTrue(registry.isBound("session-1"));
        assertEquals(1, registry.count());

        SessionBinding binding = registry.find("session-1");
        assertEquals("R001", binding.roomCode());
        assertEquals("player-1", binding.playerId());

        SessionBinding removed = registry.unbind("session-1");
        assertEquals("session-1", removed.sessionId());
        assertFalse(registry.isBound("session-1"));
        assertEquals(0, registry.count());
        assertNull(registry.find("session-1"));
    }

    @Test
    void bindReplacesExistingSessionState() {
        RoomSessionRegistry registry = new RoomSessionRegistry();

        registry.bind("session-1", "R001", "player-1");
        registry.bind("session-1", "R002", "player-2");

        SessionBinding binding = registry.find("session-1");
        assertEquals("R002", binding.roomCode());
        assertEquals("player-2", binding.playerId());
        assertEquals(1, registry.count());
    }
}
