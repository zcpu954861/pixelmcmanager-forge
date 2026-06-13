# PixelMC Manager Manual Test

1. Build the jar with `.\gradlew.bat clean build`.
2. Copy `build/libs/pixelmcmanager-0.1.0.jar` into a Forge 1.20.1 server `mods/` directory.
3. Join the server from a client that does not install PixelMC Manager.
4. On first join, confirm the `firstJoinMessages` lines are sent only to the joining player.
5. Leave and join again, then confirm `returningMessages` and `{join_count}` are correct.
6. Play for a short time, leave, and confirm `<world-save-root>/pixelmcmanager/player_stats.json` increases `totalOnlineMillis`.
7. Edit `config/pixelmcmanager.json`, then run `/pixelmcmanager reload`.
8. Run `/pixelmcmanager preview first` and `/pixelmcmanager preview returning`.
9. Confirm `&` color codes, `&#64D8FF` style HEX colors, and `<gradient:#64D8FF:#F7D774>text</gradient>` render correctly.
10. Intentionally break `config/pixelmcmanager.json`, run `/pixelmcmanager reload`, and confirm the command reports failure while the server keeps running with the old valid config.
11. Let at least two players join, or create two realistic records in the stats file for local testing.
12. Confirm the stats file is inside the active world save root at `<world-save-root>/pixelmcmanager/player_stats.json`, not in `config/` or the server root.
13. Run `/pixelmcmanager` and confirm the help text appears.
14. Run `/pixelmcmanager logincount` and confirm all recorded players are sorted by login count descending.
15. Run `/pixelmcmanager logincount <player>` and confirm the single-player login count, UUID, first login, and last login display.
16. Run `/pixelmcmanager logintime` and confirm all recorded players are sorted by total online time descending.
17. Run `/pixelmcmanager logintime <player>` and confirm the single-player total time, login count, first login, and last login display.
18. Confirm offline recorded players appear in tab completion for both `<player>` arguments.
19. Confirm historical players that are not currently online can still be queried.
20. Confirm server lag does not affect total online time because stats use real milliseconds, not ticks.
21. Keep a player online longer than `statsAutoSaveSeconds`, then confirm `player_stats.json` is checkpointed with increased `totalOnlineMillis`.
22. Simulate an abnormal stop and restart, then confirm the whole login session was not lost; at most the time since the previous checkpoint should be missing.
23. Run the `/pixelmcmanager` query commands from the server console and confirm they do not require a player context.
24. Confirm `/pixelmcwelcome` is no longer registered.
