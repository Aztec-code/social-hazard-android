# Deployment

## Backend

The Spring Boot backend is the system of record for multiplayer play. For v1 it uses in-memory storage, so horizontal scaling should be treated carefully because room state is not shared between instances.

### Requirements

- Java 21 runtime
- HTTPS and WSS termination at the edge
- sticky routing or single-instance hosting for v1
- Linux user and directories for the backend process

### Recommended Topology

1. Deploy a single Spring Boot instance behind a reverse proxy.
2. Terminate TLS at the proxy or platform edge.
3. Bind Spring Boot only on `127.0.0.1:8081`.
4. Reverse proxy `api.fluxcloud.dev/ws` to the backend WebSocket endpoint.
5. Reverse proxy `api.fluxcloud.dev/health` to the backend health endpoint.

## Deployment Artifacts

The repository now includes VPS-ready backend artifacts:

- [server/src/main/resources/application-prod.yml](../server/src/main/resources/application-prod.yml): production Spring profile with environment-variable overrides
- [server/deploy/social-hazard.env.example](../server/deploy/social-hazard.env.example): environment variable template
- [server/deploy/social-hazard.service](../server/deploy/social-hazard.service): `systemd` unit
- [server/deploy/nginx.api.fluxcloud.dev.conf](../server/deploy/nginx.api.fluxcloud.dev.conf): Nginx reverse proxy and WebSocket config for `api.fluxcloud.dev`

## 1. Application Production Config

Run the backend with the `prod` profile. The production profile defaults to:

- `SERVER_ADDRESS=127.0.0.1`
- `SERVER_PORT=8081`
- `SPRING_PROFILES_ACTIVE=prod`
- `SOCIAL_HAZARD_ALLOWED_ORIGIN_PATTERNS=https://api.fluxcloud.dev`

That keeps Spring Boot private to the VPS while Nginx exposes only:

- `https://api.fluxcloud.dev/health`
- `wss://api.fluxcloud.dev/ws`

The concrete config lives in:

- [server/src/main/resources/application-prod.yml](../server/src/main/resources/application-prod.yml)
- [server/deploy/social-hazard.env.example](../server/deploy/social-hazard.env.example)

## 2. systemd Service File

The provided unit runs the Spring Boot jar from `/opt/social-hazard/social-hazard-server.jar` and reads environment variables from `/etc/social-hazard/social-hazard.env`.

File:

- [server/deploy/social-hazard.service](../server/deploy/social-hazard.service)

## 3. Nginx Config

The provided Nginx site is for `api.fluxcloud.dev` and:

- redirects HTTP to HTTPS
- proxies `GET /health` to `http://127.0.0.1:8081/health`
- proxies `GET /ws` to `http://127.0.0.1:8081/ws` with WebSocket upgrade headers

File:

- [server/deploy/nginx.api.fluxcloud.dev.conf](../server/deploy/nginx.api.fluxcloud.dev.conf)

If Certbot manages your TLS block, merge the `location` rules into the generated `server` block instead of duplicating certificate directives.

## 4. Deployment Commands

Suggested paths:

- app directory: `/opt/social-hazard`
- environment file: `/etc/social-hazard/social-hazard.env`
- service name: `social-hazard`

Initial VPS setup:

```bash
sudo apt-get update
sudo apt-get install -y openjdk-21-jre-headless nginx
sudo useradd --system --home /opt/social-hazard --shell /usr/sbin/nologin socialhazard
sudo mkdir -p /opt/social-hazard /etc/social-hazard
sudo chown socialhazard:socialhazard /opt/social-hazard
```

Build the jar locally or on the VPS:

```bash
./gradlew -p server clean bootJar
```

Install the backend and config files:

```bash
sudo cp server/build/libs/social-hazard-server.jar /opt/social-hazard/social-hazard-server.jar
sudo chown socialhazard:socialhazard /opt/social-hazard/social-hazard-server.jar

sudo cp server/deploy/social-hazard.env.example /etc/social-hazard/social-hazard.env
sudo chmod 640 /etc/social-hazard/social-hazard.env
sudo chown root:socialhazard /etc/social-hazard/social-hazard.env

sudo cp server/deploy/social-hazard.service /etc/systemd/system/social-hazard.service
sudo cp server/deploy/nginx.api.fluxcloud.dev.conf /etc/nginx/sites-available/api.fluxcloud.dev.conf
sudo ln -sf /etc/nginx/sites-available/api.fluxcloud.dev.conf /etc/nginx/sites-enabled/api.fluxcloud.dev.conf
```

Enable and start everything:

```bash
sudo systemctl daemon-reload
sudo systemctl enable social-hazard
sudo systemctl start social-hazard
sudo nginx -t
sudo systemctl reload nginx
```

Basic verification:

```bash
curl http://127.0.0.1:8081/health
curl https://api.fluxcloud.dev/health
```

## 5. Restart And Update Commands

Restart the backend:

```bash
sudo systemctl restart social-hazard
sudo systemctl status social-hazard --no-pager
sudo journalctl -u social-hazard -n 100 --no-pager
```

Update the jar after a new build:

```bash
./gradlew -p server clean bootJar
sudo cp server/build/libs/social-hazard-server.jar /opt/social-hazard/social-hazard-server.jar
sudo chown socialhazard:socialhazard /opt/social-hazard/social-hazard-server.jar
sudo systemctl restart social-hazard
```

Reload Nginx after config changes:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

Reload systemd if the unit changes:

```bash
sudo systemctl daemon-reload
sudo systemctl restart social-hazard
```

## 6. Operational Notes

- Because room state is in memory, a restart ends active rooms.
- Stale disconnected rooms are cleaned up automatically.
- Reconnect behavior depends on the player token issued at room create or join time.
- Logging should retain socket errors and room lifecycle events for debugging.

## Android Client

The Android app currently reads its production WebSocket endpoint from `BuildConfig`:

```text
wss://api.fluxcloud.dev/ws
```

For staging or local development, change the build config field in `app/build.gradle.kts` or introduce per-build-type configuration later.

## Future Hardening

Likely next deployment steps:

- shared room storage or a database
- structured metrics and request tracing
- rate limiting on socket connect and room creation
- moderation and content flagging tools
- signed reconnect tokens or short-lived session credentials
