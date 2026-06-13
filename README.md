# PixelMC Welcome

A lightweight Forge 1.20.1 / Java 17 server-side welcome message mod for PixelMC / Vanilla Era.

## Environment

- Minecraft 1.20.1
- Forge 47.3.33
- Java 17
- Mod ID: `pixelmcwelcome`
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
- `/pixelmcwelcome reload`
- `/pixelmcwelcome preview`

PixelMC Welcome does not require client installation. The mod metadata keeps `displayTest="IGNORE_SERVER_VERSION"`.

## Files

- Config: `config/pixelmcwelcome.json`
- Player stats: `<world-save-root>/pixelmcwelcome/player_stats.json`

When `enabled=false`, welcome messages are skipped, but player stats are still maintained.

## Commands

- `/pixelmcwelcome reload`
- `/pixelmcwelcome preview`
- `/pixelmcwelcome preview first`
- `/pixelmcwelcome preview returning`

All commands require OP level 2. `reload` can be run from console. `preview` requires a player.

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
