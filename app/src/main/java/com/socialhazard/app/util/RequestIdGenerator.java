package com.socialhazard.app.util;

import java.util.UUID;

public final class RequestIdGenerator {

    public String nextId() {
        return "req_" + UUID.randomUUID().toString().replace("-", "");
    }
}
