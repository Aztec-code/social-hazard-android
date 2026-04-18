package com.socialhazard.server.service;

import com.socialhazard.server.config.GameServerProperties;
import com.socialhazard.server.model.api.client.CreateRoomRequest;
import com.socialhazard.server.model.api.client.JudgePickRequest;
import com.socialhazard.server.model.api.client.JoinRoomRequest;
import com.socialhazard.server.model.api.client.LeaveRoomRequest;
import com.socialhazard.server.model.api.client.ReadyStateRequest;
import com.socialhazard.server.model.api.client.ReconnectRequest;
import com.socialhazard.server.model.api.client.StartMatchRequest;
import com.socialhazard.server.model.api.client.SubmitCardRequest;
import com.socialhazard.server.model.api.server.RoomConnectionPayload;
import com.socialhazard.server.model.domain.CardSubmission;
import com.socialhazard.server.model.domain.GameRoom;
import com.socialhazard.server.model.domain.RoomPhase;
import com.socialhazard.server.model.domain.RoomPlayer;
import com.socialhazard.server.repository.InMemoryRoomRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRoomServiceTest {

    private static final String TEST_AVATAR_ID = "sonic";

    @Test
    void roomLifecycleSupportsCreateStartCancelAndDelete() {
        InMemoryRoomRepository repository = new InMemoryRoomRepository();
        GameRoomService service = newService(repository);

        RoomConnectionPayload host = createdRoom(service, "session-host", "Nova", 3);
        RoomConnectionPayload guest = joinedRoom(service, "session-guest", host.roomCode(), "Juniper");
        RoomConnectionPayload guestTwo = joinedRoom(service, "session-guest-2", host.roomCode(), "Marlow");

        ready(service, "session-host", host, true);
        ready(service, "session-guest", guest, true);
        ready(service, "session-guest-2", guestTwo, true);
        service.startMatch("session-host", "req-start", new StartMatchRequest(host.roomCode(), host.playerId(), host.playerToken()));

        GameRoom room = repository.findByCode(host.roomCode()).orElseThrow();
        assertEquals(RoomPhase.SUBMITTING, room.getPhase());
        assertEquals(3, room.getPlayerCount());

        service.leaveRoom("session-guest", "req-leave-guest", new LeaveRoomRequest(host.roomCode(), guest.playerId(), guest.playerToken()));

        room = repository.findByCode(host.roomCode()).orElseThrow();
        assertEquals(RoomPhase.LOBBY, room.getPhase());
        assertEquals(2, room.getPlayerCount());

        service.leaveRoom("session-guest-2", "req-leave-guest-2", new LeaveRoomRequest(host.roomCode(), guestTwo.playerId(), guestTwo.playerToken()));

        service.leaveRoom("session-host", "req-leave-host", new LeaveRoomRequest(host.roomCode(), host.playerId(), host.playerToken()));
        assertTrue(repository.findByCode(host.roomCode()).isEmpty());
    }

    @Test
    void judgeRotatesAcrossClassicRounds() {
        InMemoryRoomRepository repository = new InMemoryRoomRepository();
        GameRoomService service = newService(repository);

        RoomConnectionPayload host = createdRoom(service, "session-host", "Nova", 3);
        RoomConnectionPayload guestOne = joinedRoom(service, "session-guest-1", host.roomCode(), "Juniper");
        RoomConnectionPayload guestTwo = joinedRoom(service, "session-guest-2", host.roomCode(), "Marlow");

        ready(service, "session-host", host, true);
        ready(service, "session-guest-1", guestOne, true);
        ready(service, "session-guest-2", guestTwo, true);
        service.startMatch("session-host", "req-start", new StartMatchRequest(host.roomCode(), host.playerId(), host.playerToken()));

        GameRoom room = repository.findByCode(host.roomCode()).orElseThrow();
        String roundId = room.getRoundId();
        assertEquals(host.playerId(), room.getJudgePlayerId());

        submitFirstCard(service, room, "session-guest-1", guestOne);
        room = repository.findByCode(host.roomCode()).orElseThrow();
        submitFirstCard(service, room, "session-guest-2", guestTwo);

        room = repository.findByCode(host.roomCode()).orElseThrow();
        assertEquals(RoomPhase.JUDGING, room.getPhase());
        String winningSubmissionId = room.getJudgeChoices().getFirst().getSubmissionId();

        service.judgePick(
                "session-host",
                "req-judge",
                new JudgePickRequest(host.roomCode(), host.playerId(), host.playerToken(), roundId, winningSubmissionId)
        );

        room = repository.findByCode(host.roomCode()).orElseThrow();
        assertEquals(RoomPhase.SUBMITTING, room.getPhase());
        assertEquals(2, room.getRoundNumber());
        assertEquals(guestOne.playerId(), room.getJudgePlayerId());
    }

    @Test
    void submittedCardsAreRefilledWhenRoundAdvances() {
        InMemoryRoomRepository repository = new InMemoryRoomRepository();
        GameRoomService service = newService(repository);

        RoomConnectionPayload host = createdRoom(service, "session-host", "Nova", 3);
        RoomConnectionPayload guestOne = joinedRoom(service, "session-guest-1", host.roomCode(), "Juniper");
        RoomConnectionPayload guestTwo = joinedRoom(service, "session-guest-2", host.roomCode(), "Marlow");

        ready(service, "session-host", host, true);
        ready(service, "session-guest-1", guestOne, true);
        ready(service, "session-guest-2", guestTwo, true);
        service.startMatch("session-host", "req-start", new StartMatchRequest(host.roomCode(), host.playerId(), host.playerToken()));

        GameRoom room = repository.findByCode(host.roomCode()).orElseThrow();
        RoomPlayer submitter = room.findPlayer(guestOne.playerId()).orElseThrow();
        String removedCardId = submitter.getHand().getFirst().cardId();
        int initialHandSize = submitter.getHand().size();

        submitFirstCard(service, room, "session-guest-1", guestOne);
        room = repository.findByCode(host.roomCode()).orElseThrow();
        submitFirstCard(service, room, "session-guest-2", guestTwo);

        room = repository.findByCode(host.roomCode()).orElseThrow();
        String winningSubmissionId = room.getJudgeChoices().getFirst().getSubmissionId();
        service.judgePick(
                "session-host",
                "req-judge",
                new JudgePickRequest(host.roomCode(), host.playerId(), host.playerToken(), room.getRoundId(), winningSubmissionId)
        );

        room = repository.findByCode(host.roomCode()).orElseThrow();
        submitter = room.findPlayer(guestOne.playerId()).orElseThrow();
        assertEquals(initialHandSize, submitter.getHand().size());
        assertFalse(submitter.getHand().stream().anyMatch(card -> card.cardId().equals(removedCardId)));
    }

    @Test
    void reconnectReturnsCurrentStateIncludingPrivateHand() {
        InMemoryRoomRepository repository = new InMemoryRoomRepository();
        GameRoomService service = newService(repository);

        RoomConnectionPayload host = createdRoom(service, "session-host", "Nova", 3);
        RoomConnectionPayload guest = joinedRoom(service, "session-guest", host.roomCode(), "Juniper");
        RoomConnectionPayload guestTwo = joinedRoom(service, "session-guest-2", host.roomCode(), "Marlow");

        ready(service, "session-host", host, true);
        ready(service, "session-guest", guest, true);
        ready(service, "session-guest-2", guestTwo, true);
        service.startMatch("session-host", "req-start", new StartMatchRequest(host.roomCode(), host.playerId(), host.playerToken()));

        GameRoom room = repository.findByCode(host.roomCode()).orElseThrow();
        long stateVersionBeforeReconnect = room.getStateVersion();

        service.handleDisconnect("session-guest");
        RoomConnectionPayload reconnectPayload = (RoomConnectionPayload) service.reconnect(
                "session-guest-new",
                "req-reconnect",
                new ReconnectRequest(host.roomCode(), null, guest.playerToken(), stateVersionBeforeReconnect)
        ).getFirst().message().payload();

        assertEquals(guest.playerId(), reconnectPayload.playerId());
        assertEquals(RoomPhase.SUBMITTING, reconnectPayload.state().phase());
        assertNotNull(reconnectPayload.state().round());
        assertFalse(reconnectPayload.state().viewer().hand().isEmpty());
        assertEquals(guest.playerId(), reconnectPayload.state().viewer().playerId());
        assertTrue(reconnectPayload.state().stateVersion() > stateVersionBeforeReconnect);
    }

    @Test
    void judgePickUpdatesScoreAndEndsGameAtTargetScore() {
        InMemoryRoomRepository repository = new InMemoryRoomRepository();
        GameRoomService service = newService(repository);

        RoomConnectionPayload host = createdRoom(service, "session-host", "Nova", 1);
        RoomConnectionPayload guestOne = joinedRoom(service, "session-guest-1", host.roomCode(), "Juniper");
        RoomConnectionPayload guestTwo = joinedRoom(service, "session-guest-2", host.roomCode(), "Marlow");

        ready(service, "session-host", host, true);
        ready(service, "session-guest-1", guestOne, true);
        ready(service, "session-guest-2", guestTwo, true);
        service.startMatch("session-host", "req-start", new StartMatchRequest(host.roomCode(), host.playerId(), host.playerToken()));

        GameRoom room = repository.findByCode(host.roomCode()).orElseThrow();
        submitFirstCard(service, room, "session-guest-1", guestOne);
        room = repository.findByCode(host.roomCode()).orElseThrow();
        submitFirstCard(service, room, "session-guest-2", guestTwo);

        room = repository.findByCode(host.roomCode()).orElseThrow();
        CardSubmission winningSubmission = room.getJudgeChoices().stream()
                .filter(submission -> guestOne.playerId().equals(submission.getSubmittedByPlayerId()))
                .findFirst()
                .orElseThrow();

        service.judgePick(
                "session-host",
                "req-judge",
                new JudgePickRequest(host.roomCode(), host.playerId(), host.playerToken(), room.getRoundId(), winningSubmission.getSubmissionId())
        );

        room = repository.findByCode(host.roomCode()).orElseThrow();
        assertEquals(RoomPhase.GAME_OVER, room.getPhase());
        assertEquals(guestOne.playerId(), room.getWinnerPlayerId());
        assertEquals(1, room.findPlayer(guestOne.playerId()).orElseThrow().getScore());
    }

    @Test
    void cleanupRemovesDisconnectedStaleRooms() {
        InMemoryRoomRepository repository = new InMemoryRoomRepository();
        GameRoomService service = newService(repository);

        RoomConnectionPayload host = createdRoom(service, "session-host", "Nova", 3);
        service.handleDisconnect("session-host");

        GameRoom room = repository.findByCode(host.roomCode()).orElseThrow();
        room.forceUpdatedAt(Instant.parse("2026-04-15T11:00:00Z"));

        int removed = service.cleanupStaleRooms();

        assertEquals(1, removed);
        assertTrue(repository.findByCode(host.roomCode()).isEmpty());
    }

    private static GameRoomService newService(InMemoryRoomRepository repository) {
        GameServerProperties properties = defaultProperties();
        StubIdGenerator idGenerator = new StubIdGenerator();
        return new GameRoomService(
                repository,
                idGenerator,
                new MatchEngine(properties, new OriginalCardCatalog(), idGenerator),
                new GameMessageFactory(properties),
                new RoomSessionRegistry(),
                Clock.fixed(Instant.parse("2026-04-15T12:00:00Z"), ZoneOffset.UTC),
                properties
        );
    }

    private static GameServerProperties defaultProperties() {
        GameServerProperties properties = new GameServerProperties();
        properties.setMinPlayers(3);
        properties.setMaxPlayers(4);
        properties.setDefaultTargetScore(5);
        properties.setMinTargetScore(1);
        properties.setMaxTargetScore(10);
        properties.setDuelHandSize(6);
        properties.setClassicHandSize(6);
        properties.setCleanupIntervalMs(60_000L);
        properties.setStaleRoomThreshold(java.time.Duration.ofMinutes(20));
        return properties;
    }

    private static RoomConnectionPayload createdRoom(GameRoomService service, String sessionId, String displayName, int targetScore) {
        return (RoomConnectionPayload) service.createRoom(
                sessionId,
                "req-create-" + displayName,
                new CreateRoomRequest(displayName, TEST_AVATAR_ID, targetScore)
        ).getFirst().message().payload();
    }

    private static RoomConnectionPayload joinedRoom(GameRoomService service, String sessionId, String roomCode, String displayName) {
        return (RoomConnectionPayload) service.joinRoom(
                sessionId,
                "req-join-" + displayName,
                new JoinRoomRequest(roomCode, displayName, TEST_AVATAR_ID)
        ).getFirst().message().payload();
    }

    private static void ready(GameRoomService service, String sessionId, RoomConnectionPayload payload, boolean ready) {
        service.setReadyState(
                sessionId,
                "req-ready-" + payload.playerId(),
                new ReadyStateRequest(payload.roomCode(), payload.playerId(), payload.playerToken(), ready)
        );
    }

    private static void submitFirstCard(GameRoomService service, GameRoom room, String sessionId, RoomConnectionPayload player) {
        RoomPlayer roomPlayer = room.findPlayer(player.playerId()).orElseThrow();
        int requiredPickCount = room.getPromptCard().pickCount();
        java.util.List<String> selectedCardIds = roomPlayer.getHand().stream()
                .limit(requiredPickCount)
                .map(card -> card.cardId())
                .toList();
        service.submitCard(
                sessionId,
                "req-submit-" + player.playerId(),
                new SubmitCardRequest(room.getRoomCode(), player.playerId(), player.playerToken(), room.getRoundId(), selectedCardIds)
        );
    }

    private static final class StubIdGenerator implements GameIdGenerator {

        private final AtomicInteger roomCounter = new AtomicInteger(1);
        private final AtomicInteger playerCounter = new AtomicInteger(1);
        private final AtomicInteger tokenCounter = new AtomicInteger(1);
        private final AtomicInteger matchCounter = new AtomicInteger(1);
        private final AtomicInteger roundCounter = new AtomicInteger(1);
        private final AtomicInteger submissionCounter = new AtomicInteger(1);

        @Override
        public String newRoomCode(Set<String> existingCodes) {
            return "R" + String.format("%03d", roomCounter.getAndIncrement());
        }

        @Override
        public String newPlayerId() {
            return "player_" + playerCounter.getAndIncrement();
        }

        @Override
        public String newPlayerToken() {
            return "token_" + tokenCounter.getAndIncrement();
        }

        @Override
        public String newMatchId() {
            return "match_" + matchCounter.getAndIncrement();
        }

        @Override
        public String newRoundId() {
            return "round_" + roundCounter.getAndIncrement();
        }

        @Override
        public String newSubmissionId() {
            return "submission_" + submissionCounter.getAndIncrement();
        }
    }
}
