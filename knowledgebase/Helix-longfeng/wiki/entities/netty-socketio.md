---
title: Netty-socketio
type: entity
tags: [library, java, netty, websocket, socket.io]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Open-source Java implementation of the Socket.IO server protocol, built on Netty. Used here as the backbone for [[Push Notification Architecture]]'s web push layer.

## At a glance

- Artifact: `com.corundumstudio:netty-socketio` (article cites 2.0.11).
- Purpose: serve Socket.IO clients over long-lived connections (WebSocket with fallbacks).
- Scale claim in article: ~50,000 concurrent connections per 16-core / 32 GB node.

## Configuration highlights (from §4.1.1)

- Boss threads + worker threads sized via config (`bossCount`, `workCount`) — Netty's Reactor pattern.
- Heartbeat: `pingInterval=30s`, `pingTimeout=60s` — chosen to stay under common Nginx/LB idle timeouts.
- ACK confirmations enabled — important messages require client-side receipt for at-least-once semantics.

## Deployment pattern

- Nginx front-door: stream module + `ip_hash` load balancing to pin a user's connection; `proxy_http_version 1.1` + `Upgrade` / `Connection: upgrade` headers for WebSocket handshake. Nginx also terminates TLS.
- Cluster: ≥3 nodes behind Nginx.
- Session registry: Redis stores the user → {node, connection} mapping with a 30 s TTL so the cluster can route server-side sends to the right node.

## Why it's the web push choice

- **Proven protocol.** Socket.IO clients exist for every browser and ship with reconnect + heartbeat + ack built in.
- **Netty throughput.** Reactor pattern + zero-copy keeps per-connection memory low, which is what makes 50k/node credible.
- **Fallback transports.** If plain WebSocket is blocked (corporate proxies), Socket.IO falls back to long-polling.

## Related

- [[Push Notification Architecture]]
- [[Offline Push]] — server-side cache is what Netty-socketio drains on reconnect
- [[Multi-Channel Fallback]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §4.1
