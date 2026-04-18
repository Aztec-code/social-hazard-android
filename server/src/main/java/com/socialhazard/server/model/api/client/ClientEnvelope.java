package com.socialhazard.server.model.api.client;

import com.fasterxml.jackson.databind.JsonNode;

public record ClientEnvelope(String type, String requestId, JsonNode payload) {
}
