package org.dimasik.shame.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.shame.command.SubCommand;
import org.dimasik.shame.modules.impl.DeleteChunkModule;
import org.dimasik.shame.utils.Parser;

import java.util.List;

public class DeleteChunk extends SubCommand {
    public DeleteChunk(String name){
        super(name);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            leavePNF(sender);
            return;
        }

        sender.sendMessage(Parser.color("&b&l▶ &fЧанк &6удален&f."));
        DeleteChunkModule.deleteChunk(target);
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
