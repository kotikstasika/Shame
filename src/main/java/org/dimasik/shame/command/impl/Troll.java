package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.Shame;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.fakelags.trolling.TrollingType;
import org.dimasik.shame.utils.Parser;

import java.util.ArrayList;
import java.util.List;

public class Troll extends SubCommand {
    public Troll(String name){
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        TrollingType type;

        try {
            type = TrollingType.valueOf(args[2].toUpperCase());
        } catch (Exception ignore) {
            sender.sendMessage(Parser.color("&b&l▶ &fТип троллинга &6не найден&f. Используйте: " +
                    "&6ALL_CANCEL &f| " +
                    "&6ENTITIES_NO_MOVE &f| " +
                    "&6SPAM_SOUNDS &f| " +
                    "&6FANTOM_BLOCKS &f| " +
                    "&6SWAP_ITEMS"));
            return;
        }

        if (target == null) {
            sender.sendMessage(Parser.color("&b&l▶ &fИгрок &6не найден."));
            return;
        }

        if (Shame.getTrollingManager().isAlreadyTrollingSetup(target, type)) {
            Shame.getTrollingManager().removeTrolling(target, type);
            sender.sendMessage(Parser.color("&b&l▶ &fС игрока &6" + target.getName() + " &fснят троллинг &6" + type.name()));

            return;
        }

        Shame.getTrollingManager().addTrolling(target, type);
        sender.sendMessage(Parser.color("&b&l▶ &fИгроку &6" + target.getName() + " &fдобавлен троллинг &6" + type.name()));

    }

    @Override
    public List<String> getTabCompletes(int args) {
        switch (args){
            case 1:
                List<String> list = new ArrayList<>();
                List.of(TrollingType.values()).forEach(trollingType -> list.add(trollingType.toString()));
                return list;
        }
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "[троллинг]";
    }
}
