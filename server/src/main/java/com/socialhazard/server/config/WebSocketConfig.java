package com.socialhazard.server.config;

import com.socialhazard.server.websocket.GameWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String WS_ROOT_PATH = "/ws";
    private static final String WS_GAME_PATH = "/ws/game";

    private final GameWebSocketHandler gameWebSocketHandler;
    private final GameServerProperties properties;

    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler, GameServerProperties properties) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.properties = properties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOriginPatterns = properties.getAllowedOriginPatterns().toArray(String[]::new);

        registry.addHandler(gameWebSocketHandler, WS_ROOT_PATH)
                .setAllowedOriginPatterns(allowedOriginPatterns);

        registry.addHandler(gameWebSocketHandler, WS_GAME_PATH)
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }
}
