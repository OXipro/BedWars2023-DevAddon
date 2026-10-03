package com.tomkeuper.bedwars.addon.timer;

import com.tomkeuper.bedwars.addon.DevAddon;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.NextEvent;
import com.tomkeuper.bedwars.api.tasks.PlayingTask;
import com.tomkeuper.bedwars.api.tasks.StartingTask;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimerManager {

    private final DevAddon plugin;
    private final Map<String, Integer> pausedArenas = new ConcurrentHashMap<>();

    public TimerManager(DevAddon plugin) {
        this.plugin = plugin;
    }

    public boolean isPaused(IArena arena) {
        return pausedArenas.containsKey(arena.getArenaName());
    }

    public boolean pauseTimer(IArena arena) {
        if (isPaused(arena)) {
            return false;
        }

        int remaining = getRemainingSeconds(arena);
        if (remaining < 0) {
            return false;
        }

        pausedArenas.put(arena.getArenaName(), remaining);

        // Set to high number to effectively freeze
        setRawCountdown(arena, 999999);
        return true;
    }

    public boolean resumeTimer(IArena arena) {
        if (!isPaused(arena)) {
            return false;
        }

        Integer savedRemaining = pausedArenas.remove(arena.getArenaName());
        if (savedRemaining == null) {
            return false;
        }

        return setRawCountdown(arena, savedRemaining);
    }

    public boolean setTimer(IArena arena, int seconds) {
        if (isPaused(arena)) {
            pausedArenas.put(arena.getArenaName(), seconds);
            return true;
        }
        return setRawCountdown(arena, seconds);
    }

    public boolean addTimer(IArena arena, int secondsToAdd) {
        int current = getRemainingSeconds(arena);
        if (current < 0) {
            return false;
        }
        int newTime = Math.max(0, current + secondsToAdd);
        return setTimer(arena, newTime);
    }

    public int getRemainingSeconds(IArena arena) {
        if (isPaused(arena)) {
            return pausedArenas.getOrDefault(arena.getArenaName(), -1);
        }

        if (arena.getStatus() == GameState.waiting || arena.getStatus() == GameState.starting) {
            StartingTask st = arena.getStartingTask();
            if (st != null) {
                return st.getCountdown();
            }
        } else if (arena.getStatus() == GameState.playing) {
            PlayingTask pt = arena.getPlayingTask();
            if (pt != null) {
                NextEvent nextEvent = arena.getNextEvent();
                if (nextEvent != null) {
                    return getPlayingTaskCountdownForEvent(pt, nextEvent);
                }
            }
        }
        return -1;
    }

    private boolean setRawCountdown(IArena arena, int seconds) {
        if (arena.getStatus() == GameState.waiting || arena.getStatus() == GameState.starting) {
            StartingTask st = arena.getStartingTask();
            if (st != null) {
                st.setCountdown(seconds);
                return true;
            }
        } else if (arena.getStatus() == GameState.playing) {
            PlayingTask pt = arena.getPlayingTask();
            if (pt != null) {
                NextEvent nextEvent = arena.getNextEvent();
                if (nextEvent != null) {
                    return setPlayingTaskCountdownForEvent(pt, nextEvent, seconds);
                }
            }
        }
        return false;
    }

    private int getPlayingTaskCountdownForEvent(PlayingTask pt, NextEvent nextEvent) {
        switch (nextEvent) {
            case BEDS_DESTROY:
                return pt.getBedsDestroyCountdown();
            case ENDER_DRAGON:
                return pt.getDragonSpawnCountdown();
            case GAME_END:
                return pt.getGameEndCountdown();
            default:
                // Reflect field or default countdown
                return getPlayingTaskField(pt, nextEvent);
        }
    }

    private int getPlayingTaskField(PlayingTask pt, NextEvent nextEvent) {
        try {
            for (Field f : pt.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == int.class) {
                    String name = f.getName().toLowerCase();
                    if (nextEvent == NextEvent.DIAMOND_GENERATOR_TIER_II && name.contains("diamond") && (name.contains("2") || name.contains("ii"))) {
                        return f.getInt(pt);
                    }
                    if (nextEvent == NextEvent.DIAMOND_GENERATOR_TIER_III && name.contains("diamond") && (name.contains("3") || name.contains("iii"))) {
                        return f.getInt(pt);
                    }
                    if (nextEvent == NextEvent.EMERALD_GENERATOR_TIER_II && name.contains("emerald") && (name.contains("2") || name.contains("ii"))) {
                        return f.getInt(pt);
                    }
                    if (nextEvent == NextEvent.EMERALD_GENERATOR_TIER_III && name.contains("emerald") && (name.contains("3") || name.contains("iii"))) {
                        return f.getInt(pt);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return pt.getBedsDestroyCountdown();
    }

    private boolean setPlayingTaskCountdownForEvent(PlayingTask pt, NextEvent nextEvent, int seconds) {
        try {
            for (Field f : pt.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == int.class) {
                    String name = f.getName().toLowerCase();
                    if (nextEvent == NextEvent.BEDS_DESTROY && name.contains("bed")) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                    if (nextEvent == NextEvent.ENDER_DRAGON && (name.contains("dragon") || name.contains("ender"))) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                    if (nextEvent == NextEvent.GAME_END && (name.contains("game") || name.contains("end"))) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                    if (nextEvent == NextEvent.DIAMOND_GENERATOR_TIER_II && name.contains("diamond") && (name.contains("2") || name.contains("ii"))) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                    if (nextEvent == NextEvent.DIAMOND_GENERATOR_TIER_III && name.contains("diamond") && (name.contains("3") || name.contains("iii"))) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                    if (nextEvent == NextEvent.EMERALD_GENERATOR_TIER_II && name.contains("emerald") && (name.contains("2") || name.contains("ii"))) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                    if (nextEvent == NextEvent.EMERALD_GENERATOR_TIER_III && name.contains("emerald") && (name.contains("3") || name.contains("iii"))) {
                        f.setInt(pt, seconds);
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return false;
    }
}
