package org.dimasik.shame.modules.impl;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.shame.Shame;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.modules.Module;
import org.dimasik.shame.utils.Pair;
import org.dimasik.shame.utils.ParticleDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class BanModule extends Module {

    public static final HashSet<UUID> frozenPlayers = new HashSet<>();
    public static final HashMap<Player, BukkitTask> banTasks = new HashMap<>();
    public static final HashMap<Player, String> reasons = new HashMap<>();
    public static final HashMap<Player, Boolean> booleans = new HashMap<>();
    public static final HashMap<Player, Boolean> instant = new HashMap<>();

    public BanModule() {
        super.registerListener();
    }

    public static void startBanAnimation(Player player, String reason, Boolean ban) {
        UUID playerId = player.getUniqueId();
        if (frozenPlayers.contains(playerId)) {
            return;
        }
        frozenPlayers.add(playerId);
        reasons.put(player, reason);
        booleans.put(player, ban);
        Location startLocation = player.getLocation().clone();

        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 140, 1, false, false, false));
        if(ban) {
            player.setAllowFlight(false);
            player.setGliding(false);
            player.setFlying(false);
            player.setSwimming(false);
        }


        BukkitTask bukkitTask = new BukkitRunnable() {
            public int ticks = 0;
            final Location origin = startLocation.clone();

            @Override
            public void run() {
                if (ticks >= 105 || player.getLocation().getBlock().isLiquid() || instant.getOrDefault(player, false)) {
                    finishBanAnimation(player, reason, ban, player.getLocation());
                }
                if (ticks >= 110 || player.getLocation().getBlock().isLiquid() || instant.getOrDefault(player, false)) {
                    this.cancel();
                    return;
                }

                if(ban) {
                    player.setAllowFlight(false);
                    player.setGliding(false);
                    player.setFlying(false);
                    player.setSwimming(false);
                }

                for (ParticleDataStorage.Frame frame : ParticleDataStorage.startAnimation) {
                    if (frame.tick == ticks) {
                        for (ParticleDataStorage.P p : frame.particles) {
                            Location spawnLoc = origin.clone().add(p.x, p.y, p.z);
                            if (p.color == -1) {
                                player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, spawnLoc, 1, 0, 0, 0, 0, null, true);
                            } else {
                                player.getWorld().spawnParticle(Particle.REDSTONE, spawnLoc, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(p.color), 1.0f), true);
                            }
                        }
                        for (ParticleDataStorage.S s : frame.sounds) {
                            try {
                                Sound sound = Sound.valueOf(s.sound);
                                player.getWorld().playSound(origin, sound, s.vol, s.pitch);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(Shame.getInstance(), 0L, 1L);
        banTasks.put(player, bukkitTask);
    }

    public static void finishBanAnimation(Player player, String reason, Boolean ban, Location location) {
        if (banTasks.get(player) != null) {
            banTasks.remove(player).cancel();
        }
        instant.remove(player);
        frozenPlayers.remove(player.getUniqueId());

        player.removePotionEffect(PotionEffectType.LEVITATION);

        if (banTasks.get(player) != null) {
            banTasks.remove(player).cancel();
        }
        instant.remove(player);
        frozenPlayers.remove(player.getUniqueId());
        player.teleport(location.clone().add(0, 4, 0));
        player.removePotionEffect(PotionEffectType.LEVITATION);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(0, 3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(1, 3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(2, 3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(-1, 3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(-2, 3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(0, -3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(1, -3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(2, -3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(-1, -3, 0), 5, 0, 0, 0, 0, null, true);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().clone().add(-2, -3, 0), 5, 0, 0, 0, 0, null, true);

        if (ban) {
            String broadcastCmd = Config.replace(Config.getString("ban.broadcast", "litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом."), new Pair<>("%player%", player.getName()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " " + reason);
            player.setHealth(0);
            if (!broadcastCmd.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCmd);
            }
        }
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            String reason = reasons.getOrDefault(event.getPlayer(), "Не указана");
            Boolean ban = booleans.getOrDefault(event.getPlayer(), true);
            finishBanAnimation(event.getPlayer(), reason, ban, event.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer() == null) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getX() != event.getFrom().getX()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer() == null) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCmd(PlayerTeleportEvent event) {
        if (event.getPlayer() == null) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
