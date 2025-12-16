package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.modules.impl.DamageModule;
import org.dimasik.shame.utils.Parser;

import java.util.List;

public class Damage extends SubCommand {
    public Damage(String name){
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
            double factor = Double.parseDouble(args[2]);
            if(factor == 0){
                DamageModule.editedDamages.remove(target);
                sender.sendMessage(Parser.color("&b&l▶ &fПеременная &6удалена &fс памяти."));
                return;
            }
            DamageModule.editedDamages.put(target, factor);
            sender.sendMessage(Parser.color("&b&l▶ &fПеременная &6задана&f."));
        }
        catch (NumberFormatException e){
            sender.sendMessage(Parser.color("&b&l▶ &fМножитель должен быть &6числом двойной точности&f."));
            return;
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
        return "[множитель]";
    }
}
