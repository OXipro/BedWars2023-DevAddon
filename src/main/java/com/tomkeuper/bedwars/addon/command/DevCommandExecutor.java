package com.tomkeuper.bedwars.addon.command;

import com.tomkeuper.bedwars.addon.DevAddon;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.NextEvent;
import com.tomkeuper.bedwars.api.arena.generator.GeneratorType;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.arena.team.TeamEnchant;
import com.tomkeuper.bedwars.api.tasks.PlayingTask;
import com.tomkeuper.bedwars.api.tasks.StartingTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class DevCommandExecutor implements CommandExecutor, TabCompleter {

    private final DevAddon plugin;
    private final BedWars bedWars;

    public DevCommandExecutor(DevAddon plugin, BedWars bedWars) {
        this.plugin = plugin;
        this.bedWars = bedWars;
    }

    private String msg(String msg) {
        return ChatColor.translateAlternateColorCodes('&', "&8[&bBedWars&3Dev&8] &r" + msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, args);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bw.dev")) {
            sender.sendMessage(msg("&cYou do not have permission to use BedWars Dev commands! (&ebw.dev&c)"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "nextevent":
            case "event":
                handleNextEvent(sender, args);
                break;
            case "timer":
                handleTimer(sender, args);
                break;
            case "clearenderchest":
            case "clearec":
            case "ec":
                handleClearEnderChest(sender, args);
                break;
            case "arena":
                handleArena(sender, args);
                break;
            case "bed":
                handleBed(sender, args);
                break;
            case "give":
            case "items":
                handleGive(sender, args);
                break;
            case "upgrade":
            case "upgrades":
                handleUpgrade(sender, args);
                break;
            case "spawngen":
            case "drop":
                handleSpawnGen(sender, args);
                break;
            case "kill":
                handleKill(sender, args);
                break;
            case "respawn":
                handleRespawn(sender, args);
                break;
            case "spectate":
            case "spec":
                handleSpectate(sender, args);
                break;
            case "info":
            case "status":
                handleInfo(sender, args);
                break;
            default:
                sender.sendMessage(msg("&cUnknown subcommand '&e" + args[0] + "&c'. Use &b/bwdev help &cfor a list of commands."));
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3=== &bBedWars Testing Server Dev Tools &3==="));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev nextevent <set|skip|trigger|list> [event] [arena] &7- Manage next arena event"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev timer <pause|resume|set|add|status> [seconds] [arena] &7- Control arena timer"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev clearec <player> &7- Clear player's enderchest inventory"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev arena <start|stop|restart|info|list> [arena] &7- Arena lifecycle controls"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev bed <destroy|restore> <team> [arena] &7- Bed destroy & restore controls"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev upgrade <max|sharpness|protection|haste|forge|heal> <team> [arena] &7- Team upgrades"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev give <iron|gold|diamond|emerald|sword|bow|armor|gapple|blocks> [amt] [player] &7- Testing gear"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev spawngen [iron|gold|diamond|emerald|all] [arena] &7- Force generator drops"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev kill <player> &7- Force kill player in arena"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev respawn <player> &7- Force respawn player"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/bwdev spectate [player] &7- Toggle spectator mode in arena"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3=============================================="));
    }

    private IArena resolveArena(CommandSender sender, String[] args, int arenaArgIndex) {
        if (args.length > arenaArgIndex) {
            String arenaName = args[arenaArgIndex];
            IArena arena = bedWars.getArenaUtil().getArenaByName(arenaName);
            if (arena != null) {
                return arena;
            }
        }
        if (sender instanceof Player) {
            IArena arena = bedWars.getArenaUtil().getArenaByPlayer((Player) sender);
            if (arena != null) {
                return arena;
            }
        }
        return null;
    }

    private void handleNextEvent(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev nextevent <set <event>|skip|trigger|list> [arena]"));
            return;
        }

        String action = args[1].toLowerCase();

        if (action.equals("list")) {
            sender.sendMessage(msg("&aAvailable BedWars Events:"));
            for (NextEvent event : NextEvent.values()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7- &e" + event.name()));
            }
            return;
        }

        if (action.equals("set")) {
            if (args.length < 3) {
                sender.sendMessage(msg("&cUsage: &e/bwdev nextevent set <EVENT_NAME> [arena]"));
                return;
            }
            String eventStr = args[2].toUpperCase();
            NextEvent nextEvent;
            try {
                nextEvent = NextEvent.valueOf(eventStr);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(msg("&cUnknown event '&e" + eventStr + "&c'. Use &b/bwdev nextevent list &cto see valid events."));
                return;
            }

            IArena arena = resolveArena(sender, args, 3);
            if (arena == null) {
                sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                return;
            }

            if (arena.getStatus() != GameState.playing) {
                sender.sendMessage(msg("&cArena '&e" + arena.getArenaName() + "&c' is not currently in playing state!"));
                return;
            }

            arena.setNextEvent(nextEvent);
            sender.sendMessage(msg("&aSuccessfully set next event in arena '&e" + arena.getArenaName() + "&a' to &b" + nextEvent.name() + "&a."));
            return;
        }

        if (action.equals("skip") || action.equals("trigger") || action.equals("fire")) {
            IArena arena = resolveArena(sender, args, 2);
            if (arena == null) {
                sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                return;
            }

            if (arena.getStatus() != GameState.playing) {
                sender.sendMessage(msg("&cArena '&e" + arena.getArenaName() + "&c' is not currently playing!"));
                return;
            }

            NextEvent current = arena.getNextEvent();
            if (current == null) {
                sender.sendMessage(msg("&cNo next event is scheduled for arena '&e" + arena.getArenaName() + "&c'."));
                return;
            }

            // Trigger the next event action on the arena
            sender.sendMessage(msg("&aTriggering event &b" + current.name() + "&a for arena '&e" + arena.getArenaName() + "&a'..."));
            triggerNextEvent(arena, current);
            sender.sendMessage(msg("&aEvent &b" + current.name() + "&a triggered! New next event is &e" + (arena.getNextEvent() != null ? arena.getNextEvent().name() : "None") + "&a."));
            return;
        }

        sender.sendMessage(msg("&cUsage: &e/bwdev nextevent <set <event>|skip|trigger|list> [arena]"));
    }

    private void triggerNextEvent(IArena arena, NextEvent current) {
        switch (current) {
            case DIAMOND_GENERATOR_TIER_II:
                for (IGenerator g : arena.getOreGenerators()) {
                    if (g.getType() == GeneratorType.DIAMOND) {
                        g.setDelay(g.getDelay() / 2);
                        g.setAmount(g.getAmount() + 1);
                    }
                }
                arena.setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_II);
                break;
            case EMERALD_GENERATOR_TIER_II:
                for (IGenerator g : arena.getOreGenerators()) {
                    if (g.getType() == GeneratorType.EMERALD) {
                        g.setDelay(g.getDelay() / 2);
                        g.setAmount(g.getAmount() + 1);
                    }
                }
                arena.setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_III);
                break;
            case DIAMOND_GENERATOR_TIER_III:
                for (IGenerator g : arena.getOreGenerators()) {
                    if (g.getType() == GeneratorType.DIAMOND) {
                        g.setAmount(g.getAmount() + 1);
                    }
                }
                arena.setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_III);
                break;
            case EMERALD_GENERATOR_TIER_III:
                for (IGenerator g : arena.getOreGenerators()) {
                    if (g.getType() == GeneratorType.EMERALD) {
                        g.setAmount(g.getAmount() + 1);
                    }
                }
                arena.setNextEvent(NextEvent.BEDS_DESTROY);
                break;
            case BEDS_DESTROY:
                for (ITeam team : arena.getTeams()) {
                    if (!team.isBedDestroyed()) {
                        team.setBedDestroyed(true);
                    }
                }
                arena.setNextEvent(NextEvent.ENDER_DRAGON);
                break;
            case ENDER_DRAGON:
                arena.setNextEvent(NextEvent.GAME_END);
                break;
            case GAME_END:
                arena.checkWinner();
                arena.changeStatus(GameState.restarting);
                break;
            default:
                break;
        }
    }

    private void handleTimer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev timer <pause|resume|set|add|status> [seconds] [arena]"));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "pause":
            case "stop": {
                IArena arena = resolveArena(sender, args, 2);
                if (arena == null) {
                    sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                    return;
                }
                boolean paused = plugin.getTimerManager().pauseTimer(arena);
                if (paused) {
                    sender.sendMessage(msg("&aTimer &epaused&a for arena '&b" + arena.getArenaName() + "&a'."));
                } else {
                    sender.sendMessage(msg("&cTimer for arena '&e" + arena.getArenaName() + "&c' is already paused or has no active countdown task."));
                }
                break;
            }
            case "resume":
            case "start": {
                IArena arena = resolveArena(sender, args, 2);
                if (arena == null) {
                    sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                    return;
                }
                boolean resumed = plugin.getTimerManager().resumeTimer(arena);
                if (resumed) {
                    sender.sendMessage(msg("&aTimer &eresumed&a for arena '&b" + arena.getArenaName() + "&a'."));
                } else {
                    sender.sendMessage(msg("&cTimer for arena '&e" + arena.getArenaName() + "&c' is not paused or cannot be resumed."));
                }
                break;
            }
            case "set": {
                if (args.length < 3) {
                    sender.sendMessage(msg("&cUsage: &e/bwdev timer set <seconds> [arena]"));
                    return;
                }
                int seconds;
                try {
                    seconds = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(msg("&cInvalid number: '&e" + args[2] + "&c'."));
                    return;
                }
                IArena arena = resolveArena(sender, args, 3);
                if (arena == null) {
                    sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                    return;
                }
                boolean success = plugin.getTimerManager().setTimer(arena, seconds);
                if (success) {
                    sender.sendMessage(msg("&aTimer set to &e" + seconds + " seconds&a for arena '&b" + arena.getArenaName() + "&a'."));
                } else {
                    sender.sendMessage(msg("&cCould not set timer for arena '&e" + arena.getArenaName() + "&c'. No active countdown task."));
                }
                break;
            }
            case "add": {
                if (args.length < 3) {
                    sender.sendMessage(msg("&cUsage: &e/bwdev timer add <seconds> [arena]"));
                    return;
                }
                int seconds;
                try {
                    seconds = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(msg("&cInvalid number: '&e" + args[2] + "&c'."));
                    return;
                }
                IArena arena = resolveArena(sender, args, 3);
                if (arena == null) {
                    sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                    return;
                }
                boolean success = plugin.getTimerManager().addTimer(arena, seconds);
                if (success) {
                    sender.sendMessage(msg("&aAdjusted timer by &e" + seconds + " seconds&a for arena '&b" + arena.getArenaName() + "&a'."));
                } else {
                    sender.sendMessage(msg("&cCould not adjust timer for arena '&e" + arena.getArenaName() + "&c'. No active countdown task."));
                }
                break;
            }
            case "status": {
                IArena arena = resolveArena(sender, args, 2);
                if (arena == null) {
                    sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
                    return;
                }
                boolean isPaused = plugin.getTimerManager().isPaused(arena);
                int timeRemaining = plugin.getTimerManager().getRemainingSeconds(arena);
                sender.sendMessage(msg("&3=== Arena Timer Status: &b" + arena.getArenaName() + " &3==="));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Status: &e" + arena.getStatus().name()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Timer Paused: " + (isPaused ? "&cYes" : "&aNo")));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Seconds Left in current phase: &e" + (timeRemaining >= 0 ? timeRemaining : "N/A")));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Next Event: &e" + (arena.getNextEvent() != null ? arena.getNextEvent().name() : "None")));
                break;
            }
            default:
                sender.sendMessage(msg("&cUsage: &e/bwdev timer <pause|resume|set|add|status> [seconds] [arena]"));
                break;
        }
    }

    private void handleClearEnderChest(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev clearec <player>"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(msg("&cPlayer '&e" + targetName + "&c' not found or offline."));
            return;
        }

        // Clear vanilla / player enderchest
        target.getEnderChest().clear();

        // Also check if the player is in an active Bedwars arena team with a team chest/inventory
        IArena arena = bedWars.getArenaUtil().getArenaByPlayer(target);
        if (arena != null) {
            ITeam team = arena.getTeam(target);
            if (team != null) {
                // Team enderchest inventory is cleared if applicable
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYour EnderChest was cleared by a developer."));
            }
        }

        sender.sendMessage(msg("&aEnderChest of player &e" + target.getName() + " &awas successfully cleared!"));
    }

    private void handleArena(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev arena <start|stop|restart|info|list> [arena]"));
            return;
        }

        String action = args[1].toLowerCase();

        if (action.equals("list")) {
            List<IArena> arenas = bedWars.getArenaUtil().getArenas();
            sender.sendMessage(msg("&3=== BedWars Arenas (" + arenas.size() + ") ==="));
            for (IArena a : arenas) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        " &7- &b" + a.getArenaName() + " &7[&e" + a.getStatus().name() + "&7] (" +
                                a.getPlayers().size() + "/" + a.getMaxPlayers() + " players)"));
            }
            return;
        }

        IArena arena = resolveArena(sender, args, 2);
        if (arena == null) {
            sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
            return;
        }

        switch (action) {
            case "start":
                if (arena.getStatus() == GameState.playing) {
                    sender.sendMessage(msg("&cArena '&e" + arena.getArenaName() + "&c' is already playing!"));
                    return;
                }
                StartingTask st = arena.getStartingTask();
                if (st != null) {
                    st.setCountdown(1);
                } else {
                    arena.changeStatus(GameState.playing);
                }
                sender.sendMessage(msg("&aForce starting arena '&e" + arena.getArenaName() + "&a'!"));
                break;
            case "stop":
            case "restart":
                arena.changeStatus(GameState.restarting);
                sender.sendMessage(msg("&aRestarting arena '&e" + arena.getArenaName() + "&a'!"));
                break;
            case "info":
                handleInfoForArena(sender, arena);
                break;
            default:
                sender.sendMessage(msg("&cUsage: &e/bwdev arena <start|stop|restart|info|list> [arena]"));
                break;
        }
    }

    private void broadcastArena(IArena arena, String message) {
        for (Player p : arena.getPlayers()) {
            p.sendMessage(message);
        }
        for (Player p : arena.getSpectators()) {
            p.sendMessage(message);
        }
    }

    private void handleBed(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(msg("&cUsage: &e/bwdev bed <destroy|restore> <team_color> [arena]"));
            return;
        }

        String action = args[1].toLowerCase();
        String teamColorName = args[2].toUpperCase();

        IArena arena = resolveArena(sender, args, 3);
        if (arena == null) {
            sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
            return;
        }

        ITeam targetTeam = null;
        for (ITeam team : arena.getTeams()) {
            if (team.getName().equalsIgnoreCase(teamColorName) || team.getColor().name().equalsIgnoreCase(teamColorName)) {
                targetTeam = team;
                break;
            }
        }

        if (targetTeam == null) {
            sender.sendMessage(msg("&cTeam '&e" + teamColorName + "&c' not found in arena '&e" + arena.getArenaName() + "&c'."));
            return;
        }

        if (action.equals("destroy")) {
            if (targetTeam.isBedDestroyed()) {
                sender.sendMessage(msg("&cBed of team &e" + targetTeam.getName() + " &cis already destroyed!"));
                return;
            }
            targetTeam.setBedDestroyed(true);
            broadcastArena(arena, ChatColor.translateAlternateColorCodes('&', "&fBed of team &e" + targetTeam.getName() + " &fwas destroyed by a developer!"));
            sender.sendMessage(msg("&aBed of team &e" + targetTeam.getName() + " &adestroyed!"));
        } else if (action.equals("restore")) {
            if (!targetTeam.isBedDestroyed()) {
                sender.sendMessage(msg("&cBed of team &e" + targetTeam.getName() + " &cis not destroyed!"));
                return;
            }
            targetTeam.setBedDestroyed(false);
            broadcastArena(arena, ChatColor.translateAlternateColorCodes('&', "&fBed of team &e" + targetTeam.getName() + " &fwas restored by a developer!"));
            sender.sendMessage(msg("&aBed of team &e" + targetTeam.getName() + " &arestored!"));
        } else {
            sender.sendMessage(msg("&cUsage: &e/bwdev bed <destroy|restore> <team_color> [arena]"));
        }
    }

    private void handleUpgrade(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(msg("&cUsage: &e/bwdev upgrade <max|sharpness|protection|haste|heal> <team_color> [arena]"));
            return;
        }

        String upgradeType = args[1].toLowerCase();
        String teamColorName = args[2].toUpperCase();

        IArena arena = resolveArena(sender, args, 3);
        if (arena == null) {
            sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
            return;
        }

        ITeam targetTeam = null;
        for (ITeam team : arena.getTeams()) {
            if (team.getName().equalsIgnoreCase(teamColorName) || team.getColor().name().equalsIgnoreCase(teamColorName)) {
                targetTeam = team;
                break;
            }
        }

        if (targetTeam == null) {
            sender.sendMessage(msg("&cTeam '&e" + teamColorName + "&c' not found in arena '&e" + arena.getArenaName() + "&c'."));
            return;
        }

        switch (upgradeType) {
            case "max":
                for (Player p : targetTeam.getMembers()) {
                    for (ItemStack item : p.getInventory().getContents()) {
                        if (item != null && item.getType().name().contains("SWORD")) {
                            item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                        }
                    }
                    for (ItemStack armor : p.getInventory().getArmorContents()) {
                        if (armor != null && armor.getType() != Material.AIR) {
                            armor.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                        }
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 999999, 1), true);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 999999, 0), true);
                }
                sender.sendMessage(msg("&aMaximized combat & effect upgrades for team &e" + targetTeam.getName() + "&a!"));
                break;
            case "sharpness":
            case "sharp":
                for (Player p : targetTeam.getMembers()) {
                    for (ItemStack item : p.getInventory().getContents()) {
                        if (item != null && item.getType().name().contains("SWORD")) {
                            item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                        }
                    }
                }
                sender.sendMessage(msg("&aApplied Sharpness upgrade to team &e" + targetTeam.getName() + "&a!"));
                break;
            case "protection":
            case "prot":
                for (Player p : targetTeam.getMembers()) {
                    for (ItemStack armor : p.getInventory().getArmorContents()) {
                        if (armor != null && armor.getType() != Material.AIR) {
                            armor.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                        }
                    }
                }
                sender.sendMessage(msg("&aApplied Protection IV upgrade to team &e" + targetTeam.getName() + "&a!"));
                break;
            case "haste":
                for (Player p : targetTeam.getMembers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 999999, 1), true);
                }
                sender.sendMessage(msg("&aApplied Haste upgrade to team &e" + targetTeam.getName() + "&a!"));
                break;
            case "heal":
            case "healpool":
                for (Player p : targetTeam.getMembers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 999999, 0), true);
                }
                sender.sendMessage(msg("&aApplied Heal Pool upgrade to team &e" + targetTeam.getName() + "&a!"));
                break;
            default:
                sender.sendMessage(msg("&cUnknown upgrade type '&e" + upgradeType + "&c'. Options: max, sharpness, protection, haste, heal."));
                break;
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev give <iron|gold|diamond|emerald|sword|bow|armor|gapple|blocks> [amount] [player]"));
            return;
        }

        String itemType = args[1].toLowerCase();
        int amount = 64;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException ignored) {
            }
        }

        Player target = null;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[3]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        }

        if (target == null) {
            sender.sendMessage(msg("&cPlease specify an online player to give items to."));
            return;
        }

        switch (itemType) {
            case "iron":
                target.getInventory().addItem(new ItemStack(Material.IRON_INGOT, amount));
                break;
            case "gold":
                target.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, amount));
                break;
            case "diamond":
            case "diamonds":
                target.getInventory().addItem(new ItemStack(Material.DIAMOND, amount));
                break;
            case "emerald":
            case "emeralds":
                target.getInventory().addItem(new ItemStack(Material.EMERALD, amount));
                break;
            case "sword":
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                target.getInventory().addItem(sword);
                break;
            case "bow":
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
                bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
                target.getInventory().addItem(bow);
                target.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                break;
            case "armor":
                if (target.getInventory() != null) {
                    target.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                    target.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                    target.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                    target.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                }
                break;
            case "gapple":
            case "apple":
                target.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, Math.min(amount, 64)));
                break;
            case "blocks":
            case "wool":
                target.getInventory().addItem(new ItemStack(Material.WOOL, amount));
                break;
            default:
                sender.sendMessage(msg("&cUnknown item '&e" + itemType + "&c'. Options: iron, gold, diamond, emerald, sword, bow, armor, gapple, blocks."));
                return;
        }

        sender.sendMessage(msg("&aGave &e" + itemType + " (" + amount + ") &ato &b" + target.getName() + "&a!"));
    }

    private void handleSpawnGen(CommandSender sender, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "all";
        IArena arena = resolveArena(sender, args, 2);
        if (arena == null) {
            sender.sendMessage(msg("&cCould not find arena. Please specify arena name or be inside an arena."));
            return;
        }

        int count = 0;
        for (IGenerator gen : arena.getOreGenerators()) {
            if (type.equals("all") || gen.getType().name().equalsIgnoreCase(type)) {
                gen.dropItem(gen.getLocation());
                count++;
            }
        }

        for (ITeam team : arena.getTeams()) {
            for (IGenerator gen : team.getGenerators()) {
                if (type.equals("all") || gen.getType().name().equalsIgnoreCase(type)) {
                    gen.dropItem(gen.getLocation());
                    count++;
                }
            }
        }

        sender.sendMessage(msg("&aTriggered &e" + count + " &agenerator drops (&b" + type + "&a) in arena '&e" + arena.getArenaName() + "&a'!"));
    }

    private void handleKill(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev kill <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(msg("&cPlayer '&e" + args[1] + "&c' not found or offline."));
            return;
        }

        IArena arena = bedWars.getArenaUtil().getArenaByPlayer(target);
        if (arena == null) {
            target.setHealth(0.0);
            sender.sendMessage(msg("&aPlayer &e" + target.getName() + " &awas killed."));
            return;
        }

        ITeam team = arena.getTeam(target);
        if (team != null) {
            target.damage(1000.0);
            sender.sendMessage(msg("&aPlayer &e" + target.getName() + " &a(Team " + team.getName() + ") was killed in arena &b" + arena.getArenaName() + "&a."));
        } else {
            target.setHealth(0.0);
            sender.sendMessage(msg("&aPlayer &e" + target.getName() + " &awas killed."));
        }
    }

    private void handleRespawn(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/bwdev respawn <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(msg("&cPlayer '&e" + args[1] + "&c' not found or offline."));
            return;
        }

        IArena arena = bedWars.getArenaUtil().getArenaByPlayer(target);
        if (arena == null) {
            sender.sendMessage(msg("&cPlayer &e" + target.getName() + " &cis not in a BedWars arena."));
            return;
        }

        ITeam team = arena.getTeam(target);
        if (team != null) {
            team.firstSpawn(target);
            target.setHealth(20.0);
            target.setFoodLevel(20);
            sender.sendMessage(msg("&aForced respawn of player &e" + target.getName() + " &aat team &b" + team.getName() + " &aspawn."));
        } else {
            sender.sendMessage(msg("&cPlayer &e" + target.getName() + " &cdoes not belong to a team in this arena."));
        }
    }

    private void handleSpectate(CommandSender sender, String[] args) {
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(msg("&cPlease specify player name: /bwdev spectate <player>"));
            return;
        }

        if (target == null) {
            sender.sendMessage(msg("&cPlayer not found or offline."));
            return;
        }

        IArena arena = bedWars.getArenaUtil().getArenaByPlayer(target);
        if (arena == null) {
            sender.sendMessage(msg("&cPlayer &e" + target.getName() + " &cis not inside a BedWars arena."));
            return;
        }

        if (arena.isSpectator(target)) {
            arena.removeSpectator(target, true);
            ITeam team = arena.getTeam(target);
            if (team != null) {
                team.reJoin(target);
            } else {
                arena.addPlayer(target, true);
            }
            sender.sendMessage(msg("&aPlayer &e" + target.getName() + " &atoggled from spectator to player."));
        } else {
            arena.addSpectator(target, true, target.getLocation());
            sender.sendMessage(msg("&aPlayer &e" + target.getName() + " &atoggled to spectator mode."));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        IArena arena = resolveArena(sender, args, 1);
        if (arena == null) {
            sender.sendMessage(msg("&cCould not find arena. Specify an arena name: /bwdev info <arena>"));
            return;
        }
        handleInfoForArena(sender, arena);
    }

    private void handleInfoForArena(CommandSender sender, IArena arena) {
        sender.sendMessage(msg("&3=== Arena Info: &b" + arena.getArenaName() + " &3==="));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Status: &e" + arena.getStatus().name()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7World: &e" + arena.getWorldName()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Players: &e" + arena.getPlayers().size() + "/" + arena.getMaxPlayers()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Spectators: &e" + arena.getSpectators().size()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Next Event: &e" + (arena.getNextEvent() != null ? arena.getNextEvent().name() : "None")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Timer Paused: " + (plugin.getTimerManager().isPaused(arena) ? "&cYes" : "&aNo")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Teams:"));
        for (ITeam team : arena.getTeams()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "   &8- &b" + team.getName() + " &7(Color: &e" + team.getColor().name() +
                            "&7, Bed: " + (team.isBedDestroyed() ? "&cDestroyed" : "&aAlive") +
                            "&7, Members: &e" + team.getMembers().size() + "&7)"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("bw.dev")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                    "help", "nextevent", "timer", "clearec", "arena",
                    "bed", "give", "upgrade", "spawngen", "kill", "respawn", "spectate", "info"
            );
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("nextevent") || sub.equals("event")) {
            if (args.length == 2) {
                return Arrays.asList("set", "skip", "trigger", "list").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
                return Arrays.stream(NextEvent.values())
                        .map(Enum::name)
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if ((args.length == 4 && args[1].equalsIgnoreCase("set")) || (args.length == 3 && !args[1].equalsIgnoreCase("set"))) {
                return getArenaNames(args[args.length - 1]);
            }
        }

        if (sub.equals("timer")) {
            if (args.length == 2) {
                return Arrays.asList("pause", "resume", "set", "add", "status").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3 && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add"))) {
                return Arrays.asList("10", "30", "60", "120", "300");
            }
            if (args.length >= 3) {
                return getArenaNames(args[args.length - 1]);
            }
        }

        if (sub.equals("clearec") || sub.equals("clearenderchest") || sub.equals("ec") || sub.equals("kill") || sub.equals("respawn")) {
            if (args.length == 2) {
                return getOnlinePlayerNames(args[1]);
            }
        }

        if (sub.equals("arena")) {
            if (args.length == 2) {
                return Arrays.asList("start", "stop", "restart", "info", "list").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3) {
                return getArenaNames(args[2]);
            }
        }

        if (sub.equals("bed")) {
            if (args.length == 2) {
                return Arrays.asList("destroy", "restore").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3) {
                return Arrays.stream(TeamColor.values())
                        .map(Enum::name)
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 4) {
                return getArenaNames(args[3]);
            }
        }

        if (sub.equals("upgrade") || sub.equals("upgrades")) {
            if (args.length == 2) {
                return Arrays.asList("max", "sharpness", "protection", "haste", "forge", "heal").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3) {
                return Arrays.stream(TeamColor.values())
                        .map(Enum::name)
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 4) {
                return getArenaNames(args[3]);
            }
        }

        if (sub.equals("give") || sub.equals("items")) {
            if (args.length == 2) {
                return Arrays.asList("iron", "gold", "diamond", "emerald", "sword", "bow", "armor", "gapple", "blocks").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3) {
                return Arrays.asList("1", "16", "32", "64");
            }
            if (args.length == 4) {
                return getOnlinePlayerNames(args[3]);
            }
        }

        if (sub.equals("spawngen") || sub.equals("drop")) {
            if (args.length == 2) {
                return Arrays.asList("iron", "gold", "diamond", "emerald", "all").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3) {
                return getArenaNames(args[2]);
            }
        }

        if (sub.equals("spectate") || sub.equals("spec")) {
            if (args.length == 2) {
                return getOnlinePlayerNames(args[1]);
            }
        }

        if (sub.equals("info") || sub.equals("status")) {
            if (args.length == 2) {
                return getArenaNames(args[1]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> getArenaNames(String query) {
        return bedWars.getArenaUtil().getArenas().stream()
                .map(IArena::getArenaName)
                .filter(name -> name.toLowerCase().startsWith(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames(String query) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
