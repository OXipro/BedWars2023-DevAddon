# BedWars2023-DevAddon

A developer & server testing addon for **BedWars2023** to help developers and testers manage test games, arena events, timers, inventories, beds, generator drops, and more.

## Features

- **Event Management**: Set or immediately skip/trigger the next arena event (`DIAMOND_GENERATOR_TIER_II`, `EMERALD_GENERATOR_TIER_II`, `BEDS_DESTROY`, `ENDER_DRAGON`, `GAME_END`, etc.).
- **Timer Control**: Pause, resume, stop, start, set, or adjust remaining seconds for arena countdowns (lobby start & game events).
- **EnderChest Clearing**: Instantly clear a player's enderchest inventory.
- **Arena Lifecycle**: Force start games, restart arenas, list active arenas, and inspect detailed arena debug info.
- **Bed Management**: Manually destroy or restore any team's bed on the fly.
- **Upgrades Manager**: Apply instant team upgrades (Sharpness, Protection IV, Haste, Heal Pool, or Max all).
- **Testing Gear & Items**: Give iron, gold, diamonds, emeralds, enchanted weapons, armor sets, golden apples, and blocks.
- **Generator Testing**: Trigger instant resource drops at diamond, emerald, and team island generators.
- **Player & Spectator Utilities**: Force respawn, kill player, or toggle spectator/player status during testing.

## Commands

| Command | Description |
|---|---|
| `/bwdev help` | Show all dev commands |
| `/bwdev nextevent <set\|skip\|trigger\|list> [event] [arena]` | Set or immediately fire the next arena event |
| `/bwdev timer <pause\|resume\|set\|add\|status> [seconds] [arena]` | Control arena countdown timer |
| `/bwdev clearec <player>` | Clear a player's enderchest |
| `/bwdev arena <start\|stop\|restart\|info\|list> [arena]` | Manage arena lifecycle |
| `/bwdev bed <destroy\|restore> <team_color> [arena]` | Destroy or restore a team bed |
| `/bwdev upgrade <max\|sharpness\|protection\|haste\|heal> <team_color> [arena]` | Set team upgrades |
| `/bwdev give <iron\|gold\|diamond\|emerald\|sword\|bow\|armor\|gapple\|blocks> [amt] [player]` | Give dev items |
| `/bwdev spawngen [iron\|gold\|diamond\|emerald\|all] [arena]` | Force generator drops |
| `/bwdev kill <player>` | Kill player in arena |
| `/bwdev respawn <player>` | Force respawn player |
| `/bwdev spectate [player]` | Toggle spectator mode |
| `/bwdev info [arena]` | Detailed arena inspection |

> Also available as aliases `/dev`, `/bedwarsdev`, or sub-command `/bw dev <subcommand>`.

## Permissions

- `bw.dev` (default: OP) - Access to all DevAddon commands.
