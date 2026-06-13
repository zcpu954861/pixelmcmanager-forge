# AGENTS.md

This repository is a Forge 1.20.1 / Java 17 server-side mod project.

Hard rules:
- Keep mod id as pixelmcmanager.
- Keep Java 17 compatibility.
- Do not require client installation.
- Do not add gameplay content, blocks, items, entities, GUI, Mixin, or networking unless explicitly requested.
- Keep player stats in the active world save root under pixelmcmanager/player_stats.json.
- Keep total online time based on real wall-clock milliseconds, not ticks.
- Prefer small, reviewable changes.
- Always run a Gradle build before claiming success.
