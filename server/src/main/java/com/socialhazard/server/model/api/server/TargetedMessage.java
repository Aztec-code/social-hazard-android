package com.socialhazard.server.model.api.server;

public record TargetedMessage(String sessionId, ServerEnvelope message, boolean closeAfterSend) {
}
