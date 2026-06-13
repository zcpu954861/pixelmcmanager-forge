# PixelMC Welcome Manual Test

1. Build the jar with `.\gradlew.bat clean build`.
2. Copy `build/libs/pixelmcwelcome-0.1.0.jar` into a Forge 1.20.1 server `mods/` directory.
3. Join the server from a client that does not install PixelMC Welcome.
4. On first join, confirm the `firstJoinMessages` lines are sent only to the joining player.
5. Leave and join again, then confirm `returningMessages` and `{join_count}` are correct.
6. Play for a short time, leave, and confirm `<world-save-root>/pixelmcwelcome/player_stats.json` increases `totalOnlineMillis`.
7. Edit `config/pixelmcwelcome.json`, then run `/pixelmcwelcome reload`.
8. Run `/pixelmcwelcome preview first` and `/pixelmcwelcome preview returning`.
9. Confirm `&` color codes, `&#64D8FF` style HEX colors, and `<gradient:#64D8FF:#F7D774>text</gradient>` render correctly.
10. Intentionally break `config/pixelmcwelcome.json`, run `/pixelmcwelcome reload`, and confirm the command reports failure while the server keeps running with the old valid config.
