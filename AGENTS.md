# AGENTS.md

This repository is a Forge 1.20.1 / Java 17 server-side mod project.

Hard rules:
- Keep mod id as pixelmcmanager.
- Keep Java 17 compatibility.
- Do not require client installation.
- Do not add gameplay content, blocks, items, entities, GUI, Mixin, or networking unless explicitly requested.
- Keep player stats in the active world save root under pixelmcmanager/player_stats.json.
- Keep total online time based on real wall-clock milliseconds, not ticks.
- Keep stopserver and maintenance states independent; stopserver join rejection has priority.
- Store audit history in the active world save root under pixelmcmanager/audit.jsonl.
- Prefer small, reviewable changes.
- Always run a Gradle build before claiming success.

GitHub Release title format:
- Forge: PixelMC Manager Forge <mod_version>
- Fabric: PixelMC Manager Fabric <mod_version>

Examples:
- PixelMC Manager Forge 0.2.2
- PixelMC Manager Fabric 0.2.2

Do not include Minecraft version or a leading "v" in the release title.
Tags may use the v-prefix, such as v0.2.2.
Jar filenames may include loader, Minecraft version, and mod version.

Release notes must include a "兼容版本与附件" section.
List each supported Minecraft version and the exact jar asset name there.
Do not use a single "适用环境" section as the only compatibility description, because the same mod version may later include multiple Minecraft versions.
