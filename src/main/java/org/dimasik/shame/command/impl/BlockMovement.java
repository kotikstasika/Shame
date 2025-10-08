package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.utils.Parser;

import java.util.List;

public class BlockMovement extends SubCommand {
    public BlockMovement(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            leavePNF(sender);
            return;
        }

        Location location = target.getLocation();
        location.setYaw(180000000 + location.getYaw());
        target.teleport(location);
        sender.sendMessage(Parser.color("&b&l▶ &fДвижения &6заблокированы&f."));
    }

    @Override
    public List<String> getTabCompletes(int args) {
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 0;
    }

    @Override
    public String getUsage() {
        return "";
    }
}
