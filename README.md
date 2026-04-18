# Social Hazard

Social Hazard is a real-time multiplayer Android party game built with Java and XML, backed by a self-hosted Java server running on a VPS. The project focuses on polished mobile UX, room-code multiplayer, and fast WebSocket-based game state sync.

## Project Overview

Social Hazard is designed as a full-stack multiplayer game project rather than a UI-only prototype. Players create or join private rooms, sync live match state over WebSockets, and move through a server-authoritative multiplayer flow from lobby to gameplay to results. The app uses original branding, original content, and a polished dark interface for live play.

## Features

- Real-time multiplayer with short room codes
- WebSocket-based live state sync between Android client and Java backend
- Server-authoritative room, round, and score flow
- Create room, join room, ready-up, start match, and leave room flows
- Reconnect-aware session handling
- Hidden solo demo mode for screenshots and offline preview
- Original branding and original game content
- Polished dark UI built with Java fragments and XML layouts

## Tech Stack

- Android app: Java
- UI: XML layouts, Material components, ViewBinding
- Architecture: ViewModel, LiveData, repository-based screen state flow
- Networking: OkHttp WebSocket client
- Backend: Java
- Deployment: self-hosted VPS

## Architecture

The project is split into two main parts:

- `app/`: Android client
- `server/`: Java backend

On the Android side, fragments handle UI, view models own screen state, and the repository coordinates multiplayer events, local persistence, demo mode, and socket updates. On the backend side, the server owns room lifecycle, match progression, and synchronized multiplayer state, then pushes updates back to clients over WebSockets.

## Multiplayer Flow

1. A player creates a room or joins an existing room using a room code.
2. The Android app connects to the backend and establishes the player session.
3. The backend manages lobby state, ready status, and match start conditions.
4. Once the game starts, gameplay state is synchronized in real time over WebSockets.
5. The backend remains authoritative for rounds, judging, scoring, and results.
6. Clients react to live updates and render the current match state.

## Screenshots

Place screenshots here once ready:

- <img width="1080" height="1543" alt="Screenshot_20260418_081659_Social Hazard" src="https://github.com/user-attachments/assets/d9dffdfc-e746-4acd-b220-412353178e39" />

- `[Create / Join Room Screenshot]`
- `[Lobby Screenshot]`
- `[Gameplay Screenshot]`
- `[Judge / Result Screenshot]`
- `[Final Scoreboard Screenshot]`

## Links

- APK: `[Add APK link here]`
- Website: `[Add website link here]`
- GitHub: `[Add GitHub repo link here]`

## Setup Instructions

### Android Client

1. Open the project in Android Studio.
2. Make sure the backend URL configuration points to either your local server or deployed VPS.
3. Build the app:

```powershell
.\gradlew.bat :app:assembleDebug
```

4. Install and run on an emulator or Android device.

### Backend

1. Go to the `server/` project.
2. Configure environment values for your host, port, and deployment mode.
3. Start the backend locally or deploy it to your VPS.
4. Make sure the Android client points to the correct WebSocket endpoint.

## Backend Deployment Summary

The backend is self-hosted on a VPS and serves as the authoritative multiplayer server. It handles room creation, player sessions, live game progression, and WebSocket synchronization. In production, the backend is deployed behind a public domain and exposed through secure WebSocket and HTTP endpoints.

## Future Improvements

- Persistent storage for rooms, sessions, and match history
- Player accounts and identity management
- Match analytics and admin tooling
- Expanded content packs and game modes
- Improved reconnect recovery and fault tolerance
- Play Store-ready release pipeline

## Why I Built This

`[Fill this in manually]`

## Biggest Challenge

`[Fill this in manually]`

## What I Learned

`[Fill this in manually]`
