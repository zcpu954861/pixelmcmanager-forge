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

All commands require OP level 2. `reload` and all `/pixelmcmanager` queries can be run from console. `preview` requires a player.

`logincount` shows all recorded players sorted by login count descending. `logintime` shows all recorded players sorted by total online time descending. The `<player>` argument supports tab completion from all recorded player names, including offline historical players.

The old `/pixelmcwelcome` command has been removed. Use `/pixelmcmanager`.

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
