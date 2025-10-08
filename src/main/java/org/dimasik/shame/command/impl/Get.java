package org.dimasik.shame.command.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.anticheatstatus.AntiCheatStatus;
import org.dimasik.shame.Shame;
import org.dimasik.shame.command.SubCommand;

import java.util.List;

public class Get extends SubCommand {
    public Get(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if(args.length < 2){
            leaveUsage(sender);
            return;
        }

        String playerName = args[1];
        try {
            if (Shame.getInstance().getDatabaseManager().nicknameExists(playerName).get()){
                sender.sendMessage("Статус игрока: 100%");
            }
            else {
                int points = AntiCheatStatus.getInstance().databaseManager.getPlayerPoints(playerName).get();
                int maxPoints = AntiCheatStatus.getInstance().configManager.getInt("ban.value", 600);
                int result = (int) Math.round((double) points / maxPoints * 100);
                sender.sendMessage("Статус игрока: " + result + "%");
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
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
