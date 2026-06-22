# PixelMC Manager Manual Test

1. Build the jar with `.\gradlew.bat clean build`.
2. Copy `build/libs/pixelmcmanager-forge-1.20.1-0.3.0.jar` into a Forge 1.20.1 server `mods/` directory.
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
25. Run `/pixelmcmanager stopserver abc` and confirm it reports the valid time format.
26. Run `/pixelmcmanager stopserver 0s` and confirm it is rejected.
27. Run `/pixelmcmanager stopserver 10s` and confirm the 10 to 1 second countdown appears in both chat and subtitle with matching text.
28. At the scheduled maintenance time, confirm players are kicked with two colored lines: `服务器开始执行停机维护!` and `详细信息见QQ群:768322731`.
29. During the 15 seconds after kick, try to reconnect and confirm the join is rejected with two colored lines: `服务器即将停机维护!` and `详细信息见QQ群:768322731`.
30. Confirm the server safely stops 15 seconds after players are kicked.
31. Run `/pixelmcmanager stopserver 1m` and confirm the 1 minute chat reminder appears, then the final 10 second countdown appears.
32. Run `/pixelmcmanager stopserver 12m` and confirm it skips the 15 minute reminder but sends 10/5/4/3/2/1 minute reminders.
33. Run `/pixelmcmanager stopserver 16m` and confirm it sends 15/10/5/4/3/2/1 minute reminders.
34. Schedule a stop, then run a new `/pixelmcmanager stopserver <time>` before maintenance starts and confirm the old plan is overwritten.
35. After maintenance starts, run `/pixelmcmanager stopserver <time>` again from console and confirm it overwrites the old maintenance flow with a new plan.
36. Confirm a client without PixelMC Manager can still join normally when no maintenance is active.
37. Run `/pixelmcmanager stopserver 1m`, then `/pixelmcmanager stopserver cancel`, and confirm no countdown, kick, or stop happens.
38. Run `/pixelmcmanager stopserver 10s`, wait until players are kicked, then run `/pixelmcmanager stopserver cancel` from console before the final 15 second stop and confirm the server stays online.
39. After canceling during maintenance, confirm players can join again.
40. Run `/pixelmcmanager stopserver cancel` with no active plan and confirm it says `当前没有正在进行的服务器停机计划。`.
41. Run `/pixelmcmanager stopserver 10m`, then `/pixelmcmanager stopserver 1m`, and confirm the old plan is overwritten.
42. Confirm the overwrite feedback contains `已终止当前存在的服务器终止进程`, the new friendly duration, and the new system time.
43. Confirm reminders, countdown, kick, and final stop follow only the second plan after overwrite.
44. Type `/pixelmcmanager stopserver ` and confirm Tab suggestions include `cancel`.
45. Confirm `/pixelmcmanager stopserver 10s`, `1m`, and `1h` still work after adding `cancel`.
46. With no active plan, run `/pixelmcmanager stopserver status` and confirm it says `当前没有正在进行的服务器停机计划。`.
47. Run `/pixelmcmanager stopserver 1m`, then `/pixelmcmanager stopserver status`, and confirm it shows `等待维护开始`.
48. Confirm the status maintenance start time is the scheduled kickTime.
49. Confirm the status final stop time is kickTime plus 15 seconds.
50. Confirm the status remaining time decreases with real wall-clock time.
51. Run `/pixelmcmanager stopserver cancel`, then `/pixelmcmanager stopserver status`, and confirm it shows no active plan.
52. Run `/pixelmcmanager stopserver 10s`, wait until players are kicked but before final stop, then run `/pixelmcmanager stopserver status` from console and confirm it shows maintenance in progress.
53. Type `/pixelmcmanager stopserver ` and confirm Tab suggestions include both `status` and `cancel`.
54. Confirm the original `<time>` argument still accepts values such as `/pixelmcmanager stopserver 10s`.
55. Confirm a client without PixelMC Manager can still join normally when no maintenance is active.
56. With no active maintenance plan, run `/pixelmcmanager maintenance status`.
57. Run `/pixelmcmanager maintenance 1m`, then `/pixelmcmanager maintenance status`, and confirm it shows waiting for maintenance.
58. Confirm `/pixelmcmanager maintenance 1m` sends the 1 minute reminder and the final 10 second countdown.
59. Run `/pixelmcmanager maintenance 10s` and confirm it sends 10 to 1 second chat and subtitle countdown messages.
60. At maintenance time, confirm online players are kicked with two colored lines: `服务器开始执行维护!` and `详细信息见QQ群:768322731`.
61. While maintenance is active, confirm new joins are rejected with two colored lines: `服务器正在维护中!` and `详细信息见QQ群:768322731`.
62. While maintenance is active, run `/pixelmcmanager maintenance now` and confirm it is rejected.
63. While maintenance is active, run `/pixelmcmanager maintenance 1m` and confirm it is rejected.
64. Before scheduled maintenance starts, run `/pixelmcmanager maintenance 5m` again and confirm the old plan is replaced and timing restarts.
65. Before scheduled maintenance starts, run `/pixelmcmanager maintenance off` and confirm the plan is cancelled.
66. While maintenance is active, run `/pixelmcmanager maintenance off` and confirm it says `已解除服务器维护状态，玩家现在可以重新加入。`.
67. Type `/pixelmcmanager maintenance ` and confirm Tab suggestions include `now`, `off`, `status`, and common time values.
68. Confirm `stopserver cancel` does not disable active maintenance, and `maintenance off` does not cancel a stopserver plan.
69. Confirm a client without PixelMC Manager can still join normally when maintenance is inactive.
70. Run a successful `/pixelmcmanager reload`, then `/pixelmcmanager audit`, and confirm a reload record appears.
71. Run `/pixelmcmanager stopserver 1m`, then confirm audit shows a stopserver record.
72. Run `/pixelmcmanager stopserver cancel`, then confirm audit shows a stopserver cancel record.
73. Run `/pixelmcmanager maintenance 1m`, then confirm audit shows a maintenance record.
74. Run `/pixelmcmanager maintenance now`, then confirm audit shows a maintenance now record.
75. Run `/pixelmcmanager maintenance off`, then confirm audit shows a maintenance off record.
76. Confirm `/pixelmcmanager audit` shows the recent 10 records.
77. Confirm `/pixelmcmanager audit last` shows the recent 10 records.
78. Confirm `/pixelmcmanager audit last 20` shows up to 20 recent records.
79. Confirm `/pixelmcmanager audit last 0` and `/pixelmcmanager audit last 51` report the 1 to 50 range error.
80. Confirm status, logincount, logintime, preview, and audit query commands do not create audit records.
81. Confirm audit records are stored at `<world-save-root>/pixelmcmanager/audit.jsonl`.
82. Restart the server and confirm recent audit records can still be queried.
83. Confirm a client without PixelMC Manager can still join normally when maintenance is inactive.

## v0.3.0 Checks

84. Edit `permissions.reloadLevel`, run `/pixelmcmanager reload`, and confirm the new level is used without restarting.
85. Remove one permission field, reload, and confirm the default value is used.
86. Set one permission field to a string and one to `9`, reload, and confirm both fall back to defaults and log warnings.
87. Confirm dangerous commands `audit`, `stopserver`, `maintenance`, and `save` default to OP level 4.
88. Run `/pixelmcmanager save` and confirm the success message says `已保存 PixelMC Manager 数据并请求服务器保存世界。`.
89. After `/pixelmcmanager save`, confirm `player_stats.json` is updated and audit shows a `save` record.
90. Run `/pixelmcmanager stats` and confirm it shows recorded players, current online/max, today active players, total playtime, uptime, and maintenance state.
91. Confirm `/pixelmcmanager stats` updates online session playtime in memory but does not create an audit record.
92. Confirm automatic announcements are silent with `announcements.enabled=false`.
93. Enable announcements with a short delay and interval in a disposable test server, then confirm messages rotate in order.
94. Set `announcements.messages` to an empty list and confirm no crash and no chat spam.
95. Change announcement text, run `/pixelmcmanager reload`, and confirm the new text is used after the configured delay.
96. Confirm announcement placeholders `{online}`, `{max}`, `{date}`, and `{time}` resolve when the message is sent.
97. Run `.\gradlew.bat clean build` before packaging the Forge jar.
98. After publishing a release, download the jar asset listed under `兼容版本与附件` and confirm the filename is `pixelmcmanager-forge-1.20.1-0.3.0.jar`.
