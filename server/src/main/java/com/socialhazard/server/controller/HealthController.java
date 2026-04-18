package com.socialhazard.server.controller;

import com.socialhazard.server.model.api.server.HealthResponse;
import com.socialhazard.server.service.GameRoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final GameRoomService gameRoomService;

    public HealthController(GameRoomService gameRoomService) {
        this.gameRoomService = gameRoomService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return gameRoomService.getHealth();
    }
}
