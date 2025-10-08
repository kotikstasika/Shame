package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.modules.impl.BanModule;
import org.dimasik.shame.utils.Pair;
import org.dimasik.shame.utils.Parser;

import java.util.List;

public class Ban extends SubCommand {
    public Ban(String name){
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if(args.length < 2){
            leaveUsage(sender);
            return;
        }

        boolean ban = !(args.length >= 3 && args[2].equalsIgnoreCase("no"));
        boolean now = String.join(" ", args).contains("-n");
        String reason = Config.getString("ban.reason", "30d &cОбнаружено сторонние ПО. &7&o(Ошибка? - свяжитесь с разработчиком &f&l@stickshield_ac_bot &7&o) --sender=&c&lАнтиЧит");
        String broadcastCmd = Config.replace(Config.getString("ban.broadcast", "litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом."), new Pair<>("%player%", args[1]));
        Player player = Bukkit.getPlayer(args[1]);
        if(player == null){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + args[1] + " " + reason);
            if(!broadcastCmd.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCmd);
            }
            return;
        }

        BanModule.instant.put(player, now);
        BanModule.startBanAnimation(player, reason, ban);
    }

    @Override
    public List<String> getTabCompletes(int args) {
        switch (args){
            case 1:
                return List.of("no", "-n");
            case 2:
                return List.of("-n");
        }
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 0;
    }

    @Override
    public String getUsage() {
        return "(no)";
    }
}
