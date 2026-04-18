package com.socialhazard.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "social-hazard")
public class GameServerProperties {

    private int minPlayers = 3;
    private int maxPlayers = 4;
    private int defaultTargetScore = 5;
    private int minTargetScore = 3;
    private int maxTargetScore = 10;
    private int duelHandSize = 6;
    private int classicHandSize = 6;
    private long cleanupIntervalMs = 60_000L;
    private Duration staleRoomThreshold = Duration.ofMinutes(20);
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getDefaultTargetScore() {
        return defaultTargetScore;
    }

    public void setDefaultTargetScore(int defaultTargetScore) {
        this.defaultTargetScore = defaultTargetScore;
    }

    public int getMinTargetScore() {
        return minTargetScore;
    }

    public void setMinTargetScore(int minTargetScore) {
        this.minTargetScore = minTargetScore;
    }

    public int getMaxTargetScore() {
        return maxTargetScore;
    }

    public void setMaxTargetScore(int maxTargetScore) {
        this.maxTargetScore = maxTargetScore;
    }

    public int getDuelHandSize() {
        return duelHandSize;
    }

    public void setDuelHandSize(int duelHandSize) {
        this.duelHandSize = duelHandSize;
    }

    public int getClassicHandSize() {
        return classicHandSize;
    }

    public void setClassicHandSize(int classicHandSize) {
        this.classicHandSize = classicHandSize;
    }

    public long getCleanupIntervalMs() {
        return cleanupIntervalMs;
    }

    public void setCleanupIntervalMs(long cleanupIntervalMs) {
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    public Duration getStaleRoomThreshold() {
        return staleRoomThreshold;
    }

    public void setStaleRoomThreshold(Duration staleRoomThreshold) {
        this.staleRoomThreshold = staleRoomThreshold;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }
}
