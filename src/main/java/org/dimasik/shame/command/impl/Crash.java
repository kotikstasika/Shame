package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.modules.impl.CrashModule;
import org.dimasik.shame.utils.Parser;

import java.util.List;

public class Crash extends SubCommand {
    public Crash(String name){
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            leavePNF(sender);
            return;
        }

        String action = args[2];
        switch (action.toUpperCase()){
            case "PARTICLE":
                CrashModule.startCrash(target);
                sender.sendMessage(Parser.color("&b&l▶ &fИгроку &6" + target.getName() + " &fназначен краш с типом &6" + args[2].toUpperCase() + "&f."));
                break;
            case "PACKET":
                if(args.length < 4){
                    sender.sendMessage(Parser.color("&b&l▶ &fИспользование: &6/shame " + getName() + " [игрок] " + getUsage() + " [сила]"));
                    return;
                }
                try{
                    int power = Integer.parseInt(args[3]);
                    CrashModule.startPacketCrash(target, power);
                    sender.sendMessage(Parser.color("&b&l▶ &fИгроку &6" + target.getName() + " &fназначен краш с типом &6" + args[2].toUpperCase() + "&f."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Parser.color("&b&l▶ &fДлительность не является &6целым числом&f."));
                }
                break;
            default:
                leaveUsage(sender);
                break;
        }
    }

    @Override
    public List<String> getTabCompletes(int args) {
        switch (args){
            case 1:
                return List.of("PARTICLE", "PACKET");
        }
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "[PARTICLE/PACKET]";
    }
}
