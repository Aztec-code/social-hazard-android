package com.socialhazard.server.service;

import com.socialhazard.server.model.domain.SessionBinding;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomSessionRegistry {

    private final Map<String, SessionBinding> bindings = new ConcurrentHashMap<>();

    public boolean isBound(String sessionId) {
        return bindings.containsKey(sessionId);
    }

    public SessionBinding find(String sessionId) {
        return bindings.get(sessionId);
    }

    public void bind(String sessionId, String roomCode, String playerId) {
        bindings.put(sessionId, new SessionBinding(sessionId, roomCode, playerId));
    }

    public SessionBinding unbind(String sessionId) {
        return bindings.remove(sessionId);
    }

    public void unbindIfPresent(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            bindings.remove(sessionId);
        }
    }

    public int count() {
        return bindings.size();
    }
}
