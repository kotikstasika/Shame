package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.modules.impl.BlackScreenModule;
import org.dimasik.shame.utils.Parser;

import java.util.List;

public class BlackScreen extends SubCommand {
    public BlackScreen(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            leavePNF(sender);
            return;
        }

        try {
            int time = Integer.parseInt(args[2]);
            BlackScreenModule.blackScreen(target, time);
            sender.sendMessage(Parser.color("&b&l▶ &fИгроку &6" + target.getName() + " &fпоказан черный экран на &6" + time + " секунд&f."));
        }
        catch (NumberFormatException e){
            sender.sendMessage(Parser.color("&b&l▶ &fДлительность не является &6целым числом&f."));
        }
    }

    @Override
    public List<String> getTabCompletes(int args) {
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "[длительность]";
    }
}
