package org.dimasik.shame.modules.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.dimasik.shame.Shame;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.modules.Module;

public class AddModule extends Module {
    public AddModule(){
        super.registerListener();
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event){
        if(event.isCancelled()){
            return;
        }

        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player player){
            String name = player.getName();
            Shame.getInstance().getDatabaseManager().nicknameExists(name).thenAccept((aBoolean -> {
                if(aBoolean){
                    Shame.getInstance().getDatabaseManager().deleteNickname(name);
                    Bukkit.getScheduler().runTask(Shame.getInstance(), () -> {
                        String reason = Config.getString("ban.reason", "30d &cОбнаружено сторонние ПО. &7&o(Ошибка? - свяжитесь с разработчиком &f&l@stickshield_ac_bot &7&o) --sender=&c&lАнтиЧит");
                        BanModule.instant.put(player, false);
                        BanModule.startBanAnimation(player, reason, true);
                    });
                }
            }));
        }
    }
}
