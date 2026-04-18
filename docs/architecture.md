# Architecture

## Overview

Social Hazard is a room-code multiplayer game with a Java Android client and a Java Spring Boot backend. The backend is authoritative for every piece of room and match state. The Android client renders the UI directly from server snapshots instead of attempting to reconstruct rules locally.

## Android Client

The Android app follows a straightforward MVVM-style structure:

- `ui` owns activities, fragments, adapters, transitions, and XML binding
- `viewmodel` converts `GameSessionState` into explicit, screen-specific `UiState`
- `repository` coordinates demo mode, saved room session, reconnect, and outbound game actions
- `network` handles WebSocket transport and JSON message translation
- `model` holds immutable session, room, and presentation models

### Rendering Model

The important interview-friendly idea is that each screen observes one `LiveData<UiState>`. Fragments are intentionally thin:

- bind clicks
- observe `UiState`
- render text, button enablement, and list contents

This keeps business logic in the viewmodel and repository layers and makes each screen easy to explain.

## Backend

The backend uses Spring Boot WebSocket support with in-memory room storage.

### Request Flow

1. A client sends a JSON event over WebSocket.
2. `GameWebSocketHandler` deserializes the envelope.
3. `GameEventRouter` maps the event type to a service method.
4. `GameRoomService` validates the request and coordinates room lifecycle behavior.
5. `MatchEngine` applies gameplay rules when a match is active.
6. `GameMessageFactory` creates recipient-specific snapshots.
7. `GameMessageSender` pushes responses back to connected sockets.

### Core Responsibilities

- `GameRoomService`: room lifecycle orchestration, authorization, reconnect handling, and broadcast decisions
- `RoomSessionRegistry`: socket-to-player attachment tracking
- `MatchEngine`: prompts, hands, submissions, judge decisions, scoring, and round advancement
- `OriginalCardCatalog`: loads original prompt and answer decks from JSON resources
- `RoomCleanupScheduler`: removes stale disconnected rooms

## Game State Ownership

The backend owns:

- room codes
- player seats
- host privileges
- ready state
- prompt deck and answer deck order
- card dealing and hand refill
- submission acceptance
- shuffled judge choices
- scoring and match completion

The client owns:

- rendering
- local selection state
- reconnect intent
- offline demo scaffolding

## Demo Mode

The Android app includes a hidden solo demo mode that bypasses networking and drives the same screens from curated local sample states. This is useful for recruiter walkthroughs, screenshots, and portfolio demos when a backend is unavailable.
