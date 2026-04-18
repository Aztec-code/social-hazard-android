package com.socialhazard.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialhazard.server.exception.GameException;
import com.socialhazard.server.model.api.client.ClientEnvelope;
import com.socialhazard.server.service.GameRoomService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameEventRouterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void routesToggleReadyAliasToReadyStateHandler() throws Exception {
        GameRoomService gameRoomService = mock(GameRoomService.class);
        when(gameRoomService.setReadyState(eq("session-1"), eq("req-1"), any())).thenReturn(java.util.List.of());
        GameEventRouter router = new GameEventRouter(objectMapper, gameRoomService);

        router.route(
                "session-1",
                new ClientEnvelope(
                        "ToggleReady",
                        "req-1",
                        objectMapper.readTree("""
                                {
                                  "roomCode": "R001",
                                  "playerId": "player-1",
                                  "playerToken": "token-1",
                                  "ready": true
                                }
                                """)
                )
        );

        verify(gameRoomService).setReadyState(eq("session-1"), eq("req-1"), any());
    }

    @Test
    void rejectsUnknownEventTypes() {
        GameEventRouter router = new GameEventRouter(objectMapper, mock(GameRoomService.class));

        assertThrows(
                GameException.class,
                () -> router.route(
                        "session-1",
                        new ClientEnvelope("NotReal", "req-1", objectMapper.createObjectNode())
                )
        );
    }
}
