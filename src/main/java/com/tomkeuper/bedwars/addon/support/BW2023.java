package com.tomkeuper.bedwars.addon.support;

import com.tomkeuper.bedwars.addon.DevAddon;
import com.tomkeuper.bedwars.api.addon.Addon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BW2023 extends Addon {

    public static BW2023 instance;

    public BW2023() {
        instance = this;
    }

    @Override
    public String getAuthor() {
        return DevAddon.getInstance().getDescription().getAuthors().get(0);
    }

    @Override
    public Plugin getPlugin() {
        return DevAddon.getInstance();
    }

    @Override
    public String getVersion() {
        return DevAddon.getInstance().getDescription().getVersion();
    }

    @Override
    public String getDescription() {
        return DevAddon.getInstance().getDescription().getDescription();
    }

    @Override
    public String getName() {
        return DevAddon.getInstance().getDescription().getName();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().enablePlugin(DevAddon.getInstance());
    }

    @Override
    public void unload() {
        Bukkit.getPluginManager().disablePlugin(DevAddon.getInstance());
    }

}
