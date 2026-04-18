package com.socialhazard.server.service;

import com.socialhazard.server.config.GameServerProperties;
import com.socialhazard.server.model.api.server.AnswerCardSnapshot;
import com.socialhazard.server.model.api.server.ErrorPayload;
import com.socialhazard.server.model.api.server.PlayerSnapshot;
import com.socialhazard.server.model.api.server.PromptSnapshot;
import com.socialhazard.server.model.api.server.RoomSnapshot;
import com.socialhazard.server.model.api.server.RoundSnapshot;
import com.socialhazard.server.model.api.server.ScoreSnapshot;
import com.socialhazard.server.model.api.server.ServerEnvelope;
import com.socialhazard.server.model.api.server.SubmissionSnapshot;
import com.socialhazard.server.model.api.server.TargetedMessage;
import com.socialhazard.server.model.api.server.ViewerSnapshot;
import com.socialhazard.server.model.domain.CardSubmission;
import com.socialhazard.server.model.domain.GameRoom;
import com.socialhazard.server.model.domain.RoomPhase;
import com.socialhazard.server.model.domain.RoomPlayer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
public class GameMessageFactory {

    private final GameServerProperties properties;

    public GameMessageFactory(GameServerProperties properties) {
        this.properties = properties;
    }

    public ServerEnvelope envelope(String type, String requestId, Object payload, Instant now) {
        return new ServerEnvelope(type, requestId, payload, now);
    }

    public ServerEnvelope error(String requestId, String code, String message, Instant now) {
        return new ServerEnvelope("ErrorMessage", requestId, new ErrorPayload(code, message), now);
    }

    public TargetedMessage message(String sessionId, String type, String requestId, Object payload, Instant now) {
        return new TargetedMessage(sessionId, envelope(type, requestId, payload, now), false);
    }

    public TargetedMessage closingMessage(String sessionId, String type, String requestId, Object payload, Instant now) {
        return new TargetedMessage(sessionId, envelope(type, requestId, payload, now), true);
    }

    public RoomSnapshot snapshot(GameRoom room, RoomPlayer viewer) {
        List<RoomPlayer> orderedPlayers = room.getPlayers().stream()
                .sorted(Comparator.comparing(RoomPlayer::getJoinedAt))
                .toList();

        return new RoomSnapshot(
                room.getRoomCode(),
                room.getPhase(),
                room.getMode(),
                room.getTargetScore(),
                room.getStateVersion(),
                room.getHostPlayerId(),
                room.canStartMatch(properties.getMinPlayers(), properties.getMaxPlayers()),
                room.getPlayerCount(),
                room.getConnectedPlayerCount(),
                buildPlayers(room, orderedPlayers),
                buildViewer(room, viewer),
                buildRound(room, viewer),
                buildScoreboard(orderedPlayers),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }

    private List<PlayerSnapshot> buildPlayers(GameRoom room, List<RoomPlayer> orderedPlayers) {
        return orderedPlayers.stream()
                .map(player -> new PlayerSnapshot(
                        player.getPlayerId(),
                        player.getDisplayName(),
                        player.getAvatarId(),
                        orderedPlayers.indexOf(player),
                        room.isHost(player.getPlayerId()),
                        player.isReady(),
                        player.isConnected(),
                        player.getScore(),
                        player.getPlayerId().equals(room.getJudgePlayerId()),
                        room.hasPlayerSubmitted(player.getPlayerId())
                ))
                .toList();
    }

    private ViewerSnapshot buildViewer(GameRoom room, RoomPlayer viewer) {
        return new ViewerSnapshot(
                viewer.getPlayerId(),
                room.isHost(viewer.getPlayerId()) && room.canStartMatch(properties.getMinPlayers(), properties.getMaxPlayers()),
                room.getPhase() == RoomPhase.LOBBY,
                true,
                room.getPhase() == RoomPhase.SUBMITTING
                        && !viewer.getPlayerId().equals(room.getJudgePlayerId())
                        && !room.hasPlayerSubmitted(viewer.getPlayerId()),
                room.getPhase() == RoomPhase.JUDGING
                        && viewer.getPlayerId().equals(room.getJudgePlayerId()),
                viewer.getHand().stream()
                        .map(card -> new AnswerCardSnapshot(card.cardId(), card.text()))
                        .toList()
        );
    }

    private RoundSnapshot buildRound(GameRoom room, RoomPlayer viewer) {
        if (room.getMatchId() == null || room.getPromptCard() == null || room.getRoundId() == null) {
            return null;
        }

        List<SubmissionSnapshot> submissions = switch (room.getPhase()) {
            case LOBBY -> List.of();
            case SUBMITTING -> room.getPlayerSubmissions().stream()
                    .filter(submission -> viewer.getPlayerId().equals(submission.getSubmittedByPlayerId()))
                    .map(this::visibleSubmission)
                    .toList();
            case JUDGING -> room.getJudgeChoices().stream()
                    .map(this::anonymousSubmission)
                    .toList();
            case ROUND_RESULT, GAME_OVER -> room.getJudgeChoices().stream()
                    .map(this::visibleSubmission)
                    .toList();
        };

        return new RoundSnapshot(
                room.getMatchId(),
                room.getRoundId(),
                room.getRoundNumber(),
                room.getJudgePlayerId(),
                new PromptSnapshot(
                        room.getPromptCard().promptId(),
                        room.getPromptCard().text(),
                        room.getPromptCard().pickCount()
                ),
                room.getRequiredSubmissionCount(),
                room.getPlayerSubmissions().size(),
                submissions,
                room.getWinningSubmissionId(),
                room.getWinnerPlayerId()
        );
    }

    private List<ScoreSnapshot> buildScoreboard(List<RoomPlayer> orderedPlayers) {
        List<RoomPlayer> rankedPlayers = orderedPlayers.stream()
                .sorted(Comparator.comparingInt(RoomPlayer::getScore).reversed()
                        .thenComparing(RoomPlayer::getJoinedAt))
                .toList();

        return rankedPlayers.stream()
                .map(player -> new ScoreSnapshot(
                        player.getPlayerId(),
                        player.getDisplayName(),
                        player.getAvatarId(),
                        player.getScore(),
                        rankedPlayers.indexOf(player) + 1
                ))
                .toList();
    }

    private SubmissionSnapshot visibleSubmission(CardSubmission submission) {
        return toSubmissionSnapshot(
                submission,
                submission.getSubmittedByPlayerId(),
                submission.getSubmittedByName(),
                submission.getOrigin().name()
        );
    }

    private SubmissionSnapshot anonymousSubmission(CardSubmission submission) {
        return toSubmissionSnapshot(submission, null, null, null);
    }

    private SubmissionSnapshot toSubmissionSnapshot(
            CardSubmission submission,
            @Nullable String submittedByPlayerId,
            @Nullable String submittedByName,
            @Nullable String origin
    ) {
        return new SubmissionSnapshot(
                submission.getSubmissionId(),
                submission.getAnswerCards().stream().map(card -> card.cardId()).toList(),
                submission.getDisplayText(),
                submittedByPlayerId,
                submittedByName,
                submission.isWinner(),
                origin
        );
    }
}
