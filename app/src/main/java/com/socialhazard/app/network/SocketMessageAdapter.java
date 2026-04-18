package com.socialhazard.app.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.RoomSession;
import com.socialhazard.app.util.RequestIdGenerator;

import java.util.ArrayList;
import java.util.List;

public final class SocketMessageAdapter {

    private final Gson gson = new Gson();
    private final RequestIdGenerator requestIdGenerator;

    public SocketMessageAdapter(RequestIdGenerator requestIdGenerator) {
        this.requestIdGenerator = requestIdGenerator;
    }

    public String createCreateRoom(String displayName, String avatarId, int targetScore) {
        JsonObject payload = new JsonObject();
        payload.addProperty("displayName", displayName);
        payload.addProperty("avatarId", avatarId);
        payload.addProperty("targetScore", targetScore);
        return envelope("CreateRoom", payload);
    }

    public String createJoinRoom(String roomCode, String displayName, String avatarId) {
        JsonObject payload = new JsonObject();
        payload.addProperty("roomCode", roomCode);
        payload.addProperty("displayName", displayName);
        payload.addProperty("avatarId", avatarId);
        return envelope("JoinRoom", payload);
    }

    public String createReconnect(RoomSession session, long lastKnownStateVersion) {
        JsonObject payload = authorizedPayload(session);
        payload.addProperty("lastKnownStateVersion", lastKnownStateVersion);
        return envelope("ReconnectRoom", payload);
    }

    public String createRequestStateSync(RoomSession session, long lastKnownStateVersion, String reason) {
        JsonObject payload = authorizedPayload(session);
        payload.addProperty("lastKnownStateVersion", lastKnownStateVersion);
        payload.addProperty("reason", reason);
        return envelope("RequestStateSync", payload);
    }

    public String createToggleReady(RoomSession session, boolean ready) {
        JsonObject payload = authorizedPayload(session);
        payload.addProperty("ready", ready);
        return envelope("ToggleReady", payload);
    }

    public String createStartGame(RoomSession session) {
        return envelope("StartGame", authorizedPayload(session));
    }

    public String createSubmitCard(RoomSession session, String roundId, List<String> cardIds) {
        JsonObject payload = authorizedPayload(session);
        payload.addProperty("roundId", roundId);
        JsonArray cardIdArray = new JsonArray();
        if (cardIds != null) {
            for (String cardId : cardIds) {
                cardIdArray.add(cardId);
            }
        }
        payload.add("cardIds", cardIdArray);
        return envelope("SubmitCard", payload);
    }

    public String createJudgePick(RoomSession session, String roundId, String submissionId) {
        JsonObject payload = authorizedPayload(session);
        payload.addProperty("roundId", roundId);
        payload.addProperty("submissionId", submissionId);
        return envelope("JudgePick", payload);
    }

    public String createLeaveRoom(RoomSession session) {
        return envelope("LeaveRoom", authorizedPayload(session));
    }

    public ParsedServerMessage parse(String rawMessage) {
        JsonObject root = JsonParser.parseString(rawMessage).getAsJsonObject();
        String type = optString(root, "type");
        String requestId = optString(root, "requestId");
        JsonObject payload = optObject(root, "payload");

        if ("RoomCreated".equals(type) || "RoomJoined".equals(type) || "ReconnectedState".equals(type)) {
            RoomConnection connection = parseRoomConnection(payload);
            return ParsedServerMessage.connection(type, requestId, connection);
        }
        if ("RoomStateUpdated".equals(type)
                || "GameStarted".equals(type)
                || "PromptShown".equals(type)
                || "HandUpdated".equals(type)
                || "SubmissionAccepted".equals(type)
                || "JudgePhaseStarted".equals(type)
                || "WinnerChosen".equals(type)
                || "RoundAdvanced".equals(type)
                || "GameOver".equals(type)) {
            return ParsedServerMessage.roomState(type, requestId, parseRoomState(payload));
        }
        if ("ErrorMessage".equals(type)) {
            return ParsedServerMessage.error(
                    requestId,
                    optString(payload, "code"),
                    optString(payload, "message")
            );
        }
        if ("RoomLeft".equals(type)) {
            return ParsedServerMessage.roomLeft(requestId, optString(payload, "roomCode"), optString(payload, "playerId"));
        }
        if ("SessionReplaced".equals(type)) {
            return ParsedServerMessage.sessionClosed(requestId, optString(payload, "reason"));
        }
        if ("Pong".equals(type)) {
            return ParsedServerMessage.pong(requestId);
        }
        return ParsedServerMessage.ignored(type, requestId);
    }

    private String envelope(String type, JsonObject payload) {
        JsonObject root = new JsonObject();
        root.addProperty("type", type);
        root.addProperty("requestId", requestIdGenerator.nextId());
        root.add("payload", payload);
        return gson.toJson(root);
    }

    private JsonObject authorizedPayload(RoomSession session) {
        JsonObject payload = new JsonObject();
        payload.addProperty("roomCode", session.getRoomCode());
        payload.addProperty("playerId", session.getPlayerId());
        payload.addProperty("playerToken", session.getPlayerToken());
        return payload;
    }

    private RoomConnection parseRoomConnection(JsonObject payload) {
        GameSessionState.RoomState roomState = parseRoomState(optObject(payload, "state"));
        String playerId = optString(payload, "playerId");
        GameSessionState.Player player = roomState == null ? null : roomState.findPlayer(playerId);
        String displayName = player == null ? "" : player.getDisplayName();
        String avatarId = player == null ? "" : player.getAvatarId();
        return new RoomConnection(
                new RoomSession(
                        optString(payload, "roomCode"),
                        playerId,
                        optString(payload, "playerToken"),
                        displayName,
                        avatarId
                ),
                roomState
        );
    }

    private GameSessionState.RoomState parseRoomState(JsonObject payload) {
        if (payload == null) {
            return null;
        }

        List<GameSessionState.Player> players = new ArrayList<>();
        JsonArray playerArray = optArray(payload, "players");
        if (playerArray != null) {
            for (JsonElement element : playerArray) {
                JsonObject player = element.getAsJsonObject();
                players.add(new GameSessionState.Player(
                        optString(player, "playerId"),
                        optString(player, "displayName"),
                        optString(player, "avatarId"),
                        optInt(player, "seatIndex"),
                        optBoolean(player, "host"),
                        optBoolean(player, "ready"),
                        optBoolean(player, "connected"),
                        optInt(player, "score"),
                        optBoolean(player, "judge"),
                        optBoolean(player, "submitted")
                ));
            }
        }

        List<GameSessionState.Score> scoreboard = new ArrayList<>();
        JsonArray scoreArray = optArray(payload, "scoreboard");
        if (scoreArray != null) {
            for (JsonElement element : scoreArray) {
                JsonObject score = element.getAsJsonObject();
                scoreboard.add(new GameSessionState.Score(
                        optString(score, "playerId"),
                        optString(score, "displayName"),
                        optString(score, "avatarId"),
                        optInt(score, "score"),
                        optInt(score, "rank")
                ));
            }
        }

        GameSessionState.Viewer viewer = parseViewer(optObject(payload, "viewer"));
        GameSessionState.Round round = parseRound(optObject(payload, "round"));

        return new GameSessionState.RoomState(
                optString(payload, "roomCode"),
                GameSessionState.Phase.fromRaw(optString(payload, "phase")),
                GameSessionState.Mode.fromRaw(optString(payload, "mode")),
                optInt(payload, "targetScore"),
                optLong(payload, "stateVersion"),
                optString(payload, "hostPlayerId"),
                optBoolean(payload, "canStart"),
                optInt(payload, "playerCount"),
                optInt(payload, "connectedPlayers"),
                players,
                viewer,
                round,
                scoreboard
        );
    }

    private GameSessionState.Viewer parseViewer(JsonObject viewerObject) {
        if (viewerObject == null) {
            return null;
        }
        List<GameSessionState.AnswerCard> hand = new ArrayList<>();
        JsonArray handArray = optArray(viewerObject, "hand");
        if (handArray != null) {
            for (JsonElement element : handArray) {
                JsonObject card = element.getAsJsonObject();
                hand.add(new GameSessionState.AnswerCard(
                        optString(card, "cardId"),
                        optString(card, "text")
                ));
            }
        }
        return new GameSessionState.Viewer(
                optString(viewerObject, "playerId"),
                optBoolean(viewerObject, "canStartGame"),
                optBoolean(viewerObject, "canToggleReady"),
                optBoolean(viewerObject, "canRename"),
                optBoolean(viewerObject, "canSubmitCard"),
                optBoolean(viewerObject, "canJudgePick"),
                hand
        );
    }

    private GameSessionState.Round parseRound(JsonObject roundObject) {
        if (roundObject == null) {
            return null;
        }

        JsonObject promptObject = optObject(roundObject, "prompt");
        GameSessionState.Prompt prompt = promptObject == null ? null : new GameSessionState.Prompt(
                optString(promptObject, "promptId"),
                optString(promptObject, "text"),
                optInt(promptObject, "pickCount")
        );

        List<GameSessionState.Submission> submissions = new ArrayList<>();
        JsonArray submissionArray = optArray(roundObject, "submissions");
        if (submissionArray != null) {
            for (JsonElement element : submissionArray) {
                JsonObject submission = element.getAsJsonObject();
                List<String> cardIds = new ArrayList<>();
                JsonArray cardIdArray = optArray(submission, "cardIds");
                if (cardIdArray != null) {
                    for (JsonElement cardIdElement : cardIdArray) {
                        cardIds.add(cardIdElement.getAsString());
                    }
                }
                submissions.add(new GameSessionState.Submission(
                        optString(submission, "submissionId"),
                        cardIds,
                        optString(submission, "text"),
                        optString(submission, "submittedByPlayerId"),
                        optString(submission, "submittedByName"),
                        optBoolean(submission, "winner"),
                        optString(submission, "origin")
                ));
            }
        }

        return new GameSessionState.Round(
                optString(roundObject, "matchId"),
                optString(roundObject, "roundId"),
                optInt(roundObject, "roundNumber"),
                optString(roundObject, "judgePlayerId"),
                prompt,
                optInt(roundObject, "requiredSubmissions"),
                optInt(roundObject, "receivedSubmissions"),
                submissions,
                optString(roundObject, "winningSubmissionId"),
                optString(roundObject, "winnerPlayerId")
        );
    }

    private static JsonObject optObject(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private static JsonArray optArray(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsJsonArray();
    }

    private static String optString(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private static boolean optBoolean(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && !element.isJsonNull() && element.getAsBoolean();
    }

    private static int optInt(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null || element.isJsonNull() ? 0 : element.getAsInt();
    }

    private static long optLong(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null || element.isJsonNull() ? 0L : element.getAsLong();
    }

    public static final class ParsedServerMessage {
        private final String type;
        private final String requestId;
        private final RoomConnection roomConnection;
        private final GameSessionState.RoomState roomState;
        private final String errorCode;
        private final String errorMessage;
        private final String reason;
        private final String roomCode;
        private final String playerId;

        private ParsedServerMessage(
                String type,
                String requestId,
                RoomConnection roomConnection,
                GameSessionState.RoomState roomState,
                String errorCode,
                String errorMessage,
                String reason,
                String roomCode,
                String playerId
        ) {
            this.type = type;
            this.requestId = requestId;
            this.roomConnection = roomConnection;
            this.roomState = roomState;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.reason = reason;
            this.roomCode = roomCode;
            this.playerId = playerId;
        }

        public static ParsedServerMessage connection(String type, String requestId, RoomConnection roomConnection) {
            return new ParsedServerMessage(type, requestId, roomConnection, null, null, null, null, null, null);
        }

        public static ParsedServerMessage roomState(String type, String requestId, GameSessionState.RoomState roomState) {
            return new ParsedServerMessage(type, requestId, null, roomState, null, null, null, null, null);
        }

        public static ParsedServerMessage error(String requestId, String errorCode, String errorMessage) {
            return new ParsedServerMessage("ErrorMessage", requestId, null, null, errorCode, errorMessage, null, null, null);
        }

        public static ParsedServerMessage roomLeft(String requestId, String roomCode, String playerId) {
            return new ParsedServerMessage("RoomLeft", requestId, null, null, null, null, null, roomCode, playerId);
        }

        public static ParsedServerMessage sessionClosed(String requestId, String reason) {
            return new ParsedServerMessage("SessionReplaced", requestId, null, null, null, null, reason, null, null);
        }

        public static ParsedServerMessage pong(String requestId) {
            return new ParsedServerMessage("Pong", requestId, null, null, null, null, null, null, null);
        }

        public static ParsedServerMessage ignored(String type, String requestId) {
            return new ParsedServerMessage(type, requestId, null, null, null, null, null, null, null);
        }

        public String getType() {
            return type;
        }

        public String getRequestId() {
            return requestId;
        }

        public RoomConnection getRoomConnection() {
            return roomConnection;
        }

        public GameSessionState.RoomState getRoomState() {
            return roomState;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getReason() {
            return reason;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public String getPlayerId() {
            return playerId;
        }
    }

    public static final class RoomConnection {
        private final RoomSession roomSession;
        private final GameSessionState.RoomState roomState;

        public RoomConnection(RoomSession roomSession, GameSessionState.RoomState roomState) {
            this.roomSession = roomSession;
            this.roomState = roomState;
        }

        public RoomSession getRoomSession() {
            return roomSession;
        }

        public GameSessionState.RoomState getRoomState() {
            return roomState;
        }
    }
}
