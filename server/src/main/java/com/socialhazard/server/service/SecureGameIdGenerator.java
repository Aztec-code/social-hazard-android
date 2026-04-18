package com.socialhazard.server.service;

import com.socialhazard.server.exception.GameException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;

@Component
public class SecureGameIdGenerator implements GameIdGenerator {

    private static final char[] ROOM_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private final SecureRandom random = new SecureRandom();

    @Override
    public String newRoomCode(Set<String> existingCodes) {
        for (int attempt = 0; attempt < 10_000; attempt++) {
            StringBuilder code = new StringBuilder(4);
            for (int index = 0; index < 4; index++) {
                code.append(ROOM_CODE_ALPHABET[random.nextInt(ROOM_CODE_ALPHABET.length)]);
            }
            String candidate = code.toString();
            if (!existingCodes.contains(candidate)) {
                return candidate;
            }
        }
        throw new GameException("ROOM_CODE_EXHAUSTED", "Unable to allocate a unique room code.");
    }

    @Override
    public String newPlayerId() {
        return "p_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @Override
    public String newPlayerToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String newMatchId() {
        return "m_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @Override
    public String newRoundId() {
        return "r_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @Override
    public String newSubmissionId() {
        return "s_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
