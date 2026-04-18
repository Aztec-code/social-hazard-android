package com.socialhazard.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RoomCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RoomCleanupScheduler.class);
    private final GameRoomService gameRoomService;

    public RoomCleanupScheduler(GameRoomService gameRoomService) {
        this.gameRoomService = gameRoomService;
    }

    @Scheduled(fixedDelayString = "${social-hazard.cleanup-interval-ms:60000}")
    public void cleanupStaleRooms() {
        int removed = gameRoomService.cleanupStaleRooms();
        if (removed > 0) {
            logger.info("Removed {} stale Social Hazard rooms.", removed);
        }
    }
}
