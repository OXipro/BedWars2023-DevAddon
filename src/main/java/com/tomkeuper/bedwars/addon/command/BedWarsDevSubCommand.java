package com.tomkeuper.bedwars.addon.command;

import com.tomkeuper.bedwars.addon.DevAddon;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BedWarsDevSubCommand extends SubCommand {

    private final DevCommandExecutor executor;

    public BedWarsDevSubCommand(ParentCommand parent, String name, DevCommandExecutor executor) {
        super(parent, name);
        this.executor = executor;
        setPriority(20);
        setPermission("bw.dev");
        setDisplayInfo(null);
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        return executor.execute(sender, args);
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender sender, BedWars api) {
        return sender.hasPermission("bw.dev");
    }
}
