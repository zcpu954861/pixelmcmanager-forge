# PixelMC Manager

A lightweight Forge 1.20.1 / Java 17 server-side welcome message mod for PixelMC / Vanilla Era.

## Environment

- Minecraft 1.20.1
- Forge 47.3.33
- Java 17
- Mod ID: `pixelmcmanager`
- Server-side only

## Features

- Configurable join welcome messages
- Separate first-join and returning-player messages
- Player login count tracking
- Total online time tracking
- Text placeholders
- `&` color and format codes
- HEX colors such as `&#64D8FF`
- Simple two-color gradients
- `/pixelmcmanager reload`
- `/pixelmcmanager preview`
- `/pixelmcmanager` player stats queries

PixelMC Manager does not require client installation. The mod metadata keeps `displayTest="IGNORE_SERVER_VERSION"`.

## Files

- Config: `config/pixelmcmanager.json`
- Player stats: `<world-save-root>/pixelmcmanager/player_stats.json`

Player stats are stored inside the current world save root, not in global `config/` and not in the server root. Total online time is accumulated from real wall-clock milliseconds, not server ticks. Online sessions are checkpointed periodically with `statsAutoSaveSeconds` in `config/pixelmcmanager.json` defaulting to 30 seconds, so an abnormal stop should lose at most the time since the previous checkpoint rather than the whole session.

When `enabled=false`, welcome messages are skipped, but player stats are still maintained.

## Migration

On startup, PixelMC Manager copies legacy files when the new path does not exist:

- `config/pixelmcwelcome.json` -> `config/pixelmcmanager.json`
- `<world-save-root>/pixelmcwelcome/player_stats.json` -> `<world-save-root>/pixelmcmanager/player_stats.json`

Legacy files are not deleted. If both old and new files exist, the new `pixelmcmanager` path is used.

## Commands

- `/pixelmcmanager reload`
- `/pixelmcmanager preview`
- `/pixelmcmanager preview first`
- `/pixelmcmanager preview returning`
- `/pixelmcmanager`
- `/pixelmcmanager logincount`
- `/pixelmcmanager logincount <player>`
- `/pixelmcmanager logintime`
- `/pixelmcmanager logintime <player>`
- `/pixelmcmanager stopserver <time>`
- `/pixelmcmanager stopserver cancel`
- `/pixelmcmanager stopserver status`
- `/pixelmcmanager maintenance <time>`
- `/pixelmcmanager maintenance now`
- `/pixelmcmanager maintenance off`
- `/pixelmcmanager maintenance status`
- `/pixelmcmanager audit`
- `/pixelmcmanager audit last`
- `/pixelmcmanager audit last <count>`
- `/pixelmcmanager save`
- `/pixelmcmanager stats`

Command permission levels are configurable in `config/pixelmcmanager.json`. `reload`, `preview`, player stat queries, audit, stopserver, maintenance, save, server stats, and the reserved announcement permission each have an independent OP level from 0 to 4. Missing fields use defaults; invalid or out-of-range values fall back to defaults and are logged. Dangerous commands default to OP level 4. `reload` and all `/pixelmcmanager` queries can be run from console. `preview` requires a player.

`logincount` shows all recorded players sorted by login count descending. `logintime` shows all recorded players sorted by total online time descending. The `<player>` argument supports tab completion from all recorded player names, including offline historical players.

The old `/pixelmcwelcome` command has been removed. Use `/pixelmcmanager`.

`stopserver <time>` requires OP level 4. `<time>` means how long until all players are kicked and the server enters maintenance. The real server stop happens 15 seconds after maintenance starts. Supported formats are `s`, `m`, and `h`, for example:

- `/pixelmcmanager stopserver 30s`
- `/pixelmcmanager stopserver 10m`
- `/pixelmcmanager stopserver 1h`

Before maintenance starts, online players receive chat reminders at 15/10/5/4/3/2/1 minutes when applicable. During the final 10 seconds, players receive matching chat and subtitle countdown messages. At maintenance time, all online players are kicked with a custom two-line colored message, new joins are rejected with a custom two-line colored message, and the server safely runs `stop` 15 seconds later.

`/pixelmcmanager stopserver cancel` cancels a pending stop plan. If maintenance has already started but the final `stop` has not run yet, `cancel` cancels that final stop and allows players to rejoin. Running `/pixelmcmanager stopserver <time>` while a plan or maintenance flow already exists replaces the old flow and starts timing again from the new command.

`/pixelmcmanager stopserver status` displays the current stop plan without changing it. It shows whether the server is waiting for maintenance, already in the 15-second maintenance window, or past the final stop time, plus the remaining real wall-clock time, maintenance start time, and final stop time.

`/pixelmcmanager maintenance <time>` schedules maintenance mode without stopping the server. At the scheduled time all online players are kicked, new joins are rejected with a custom two-line maintenance message, and the server keeps running until `/pixelmcmanager maintenance off`.

`/pixelmcmanager maintenance now` enters maintenance immediately. `/pixelmcmanager maintenance off` cancels a pending maintenance plan or disables active maintenance. `/pixelmcmanager maintenance status` shows the current maintenance plan or active maintenance state. Maintenance plans and active maintenance state are in memory only and are not persisted across server restarts.

`/pixelmcmanager audit` and `/pixelmcmanager audit last [count]` show recent successful management operations in-game. PixelMC Manager records successful `reload`, `stopserver`, `stopserver cancel`, `maintenance <time>`, `maintenance now`, and `maintenance off` operations. Query and preview commands are not recorded. Audit records are stored in the current world save root at `<world-save-root>/pixelmcmanager/audit.jsonl` and a recent in-memory cache is loaded on server start for fast queries.

`/pixelmcmanager save` checkpoints online player stats, saves `player_stats.json`, confirms audit has no buffered writes, saves the current PixelMC Manager config state, requests the server world save flow, and records a successful `save` audit entry.

`/pixelmcmanager stats` shows recorded player count, current online/max players, today active players by system timezone, total accumulated playtime, current server uptime, and maintenance state. It checkpoints online sessions in memory before reading totals and does not write audit.

## Permission Config

```json
"permissions": {
  "reloadLevel": 3,
  "previewLevel": 2,
  "statsLevel": 2,
  "auditLevel": 4,
  "stopserverLevel": 4,
  "maintenanceLevel": 4,
  "saveLevel": 4,
  "serverStatsLevel": 2,
  "announcementLevel": 4
}
```

## Auto Announcements

Automatic announcements are disabled by default and only send chat messages when enabled. They do not use title, subtitle, actionbar, bossbar, networking, or client-side code.

```json
"announcements": {
  "enabled": false,
  "intervalMinutes": 30,
  "initialDelayMinutes": 5,
  "messages": [
    "&bQQ群：&e768322731",
    "&7文明游玩，禁止恶意破坏。",
    "&7遇到问题请联系管理员。"
  ]
}
```

When enabled, the scheduler waits `initialDelayMinutes` after startup or reload, then sends one configured message every `intervalMinutes`. Messages rotate in order, empty lists are skipped safely, and `{online}`, `{max}`, `{date}`, and `{time}` are resolved when the announcement is sent.

## Release Format

GitHub Release titles must use `PixelMC Manager Forge <mod_version>`, for example `PixelMC Manager Forge 0.3.0`. Release notes must include `## 更新内容`, `## 构建验证`, and `## 兼容版本与附件`, with exact jar asset names listed under `兼容版本与附件`.

## Placeholders

- `{player}`
- `{uuid}`
- `{online}`
- `{max}`
- `{join_count}`
- `{playtime}`
- `{playtime_hours}`
- `{playtime_minutes}`
- `{ping}`
- `{date}`
- `{time}`
- `{first_join_date}`
- `{last_join_date}`

Unknown placeholders are left unchanged.
