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

- <img width="4312" height="3452" alt="Screenshot_20260418_085135_Social Hazard" src="https://github.com/user-attachments/assets/d4e1de38-fac6-4f60-b562-3146efb2a999" />

-<img width="1080" height="864" alt="Screenshot_20260418_085605_Social Hazard" src="https://github.com/user-attachments/assets/9fd1c70d-27b4-49b8-bd5a-9e4b45a993a0" />

- <img width="4320" height="3408" alt="Screenshot_20260418_085014_Social Hazard" src="https://github.com/user-attachments/assets/a9ab34f4-d67d-4d24-96e4-aca8e7975db2" />

-<img width="1080" height="2225" alt="Screenshot_20260418_081307_Social Hazard" src="https://github.com/user-attachments/assets/7b440a01-06a2-4121-85b8-4a4bc474c20a" />

- <img width="998" height="1800" alt="Screenshot_20260418_084820_Social Hazard" src="https://github.com/user-attachments/assets/bc4de689-2eed-4289-9da8-492a15113411" />

## Links

- APK: https://firecloud.studio/downloads/Social_Hazard(V1).apk
- Website: https://firecloud.studio/

## Setup Instructions

### Android Client

1. Install and run on an emulator or Android device.
2. Start playing

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

I built this mainly now to distribute the game around my university and play along with my friends. Not just a boring game but something with indeliable twists, funny and something worth bonding over friends with.
I wanted a real multiplayer product, not just a local demo or game that runs locally so I made in online by running the backend through my VPS
I also was figuring out how to make real use of my website and it seems the best way is by marketing my game and this product there

## Biggest Challenge

getting live multiplayer sync working reliably(turns out it is not as easy as I thought, getting it to reliably work took the soul out of me)
wiring WebSocket backend and deployment
turning separate pieces into one downloadable product

## What I Learned

deployment matters as much as coding
presentation changes how a project is perceived
backend/API consistency and release packaging were way harder than I expect
