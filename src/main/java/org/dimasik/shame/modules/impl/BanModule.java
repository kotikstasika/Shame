package org.dimasik.shame.modules.impl;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dimasik.shame.Shame;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.modules.Module;
import org.dimasik.shame.utils.Pair;

import java.util.*;

public class BanModule extends Module {

    public static final HashSet<UUID> frozenPlayers = new HashSet<>();
    public static final HashMap<Player, BukkitTask> banTasks = new HashMap<>();
    public static final HashMap<Player, String> reasons = new HashMap<>();
    public static final HashMap<Player, Boolean> booleans = new HashMap<>();
    public static final HashMap<Player, Boolean> instant = new HashMap<>();

    public BanModule(){
        super.registerListener();
    }

    public static void startBanAnimation(Player player, String reason, Boolean ban) {
        UUID playerId = player.getUniqueId();
        if(frozenPlayers.contains(playerId)){
            return;
        }
        frozenPlayers.add(playerId);
        reasons.put(player, reason);
        booleans.put(player, ban);
        Location startLocation = player.getLocation().clone();

        Color[] colors = {
                Color.fromRGB(0xFB0808), Color.fromRGB(0xFB1608), Color.fromRGB(0xFB2408), Color.fromRGB(0xFB3208), Color.fromRGB(0xFB4108), Color.fromRGB(0xFB4F08), Color.fromRGB(0xFB5D08), Color.fromRGB(0xFB6B08),
        };

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2f, 0.5f);
        int limit = 100;
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, limit * 20, 1, false, false, false));
        player.setAllowFlight(false);
        player.setGliding(false);
        player.setFlying(false);
        player.setSwimming(false);

        BukkitTask bukkitTask = new BukkitRunnable() {
            public int ticks = 0;
            double rotation = 0;
            double tiltAngle = 0;
            Location particleOrigin = startLocation.clone().subtract(0, 0, 0);

            @Override
            public void run() {
                if (ticks >= limit || player.getLocation().getBlock().isLiquid() || instant.getOrDefault(player, false)) {
                    finishBanAnimation(player, reason, ban, particleOrigin);
                    this.cancel();
                }
                player.setGliding(false);
                player.setFlying(false);
                player.setSwimming(false);
                particleOrigin.add(0, 0.1, 0);
                double radius = 1.5;
                int particleCount = 12;
                double angleIncrement = (2 * Math.PI) / particleCount;
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i * angleIncrement + rotation) % (2 * Math.PI);
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Vector circlePoint = new Vector(x, 0, z);
                    Vector rotatedPoint = rotateVectorAroundAxis(circlePoint, new Vector(1, 0, 0), Math.toRadians(40)).rotateAroundY(Math.toRadians(tiltAngle));
                    Color color = colors[(i + ticks) % colors.length];
                    player.getWorld().spawnParticle(Particle.REDSTONE, particleOrigin.clone().add(rotatedPoint), 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1), true);
                }
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i * angleIncrement + rotation) % (2 * Math.PI);
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Vector circlePoint = new Vector(x, 0, z);
                    Vector rotatedPoint = rotateVectorAroundAxis(circlePoint, new Vector(1, 0, 0), Math.toRadians(-40)).rotateAroundY(Math.toRadians(tiltAngle));
                    Color color = colors[(i + ticks) % colors.length];
                    player.getWorld().spawnParticle(Particle.REDSTONE, particleOrigin.clone().add(rotatedPoint), 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1), true);
                }

                rotation -= Math.toRadians(6);

                tiltAngle -= 3;

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
        player.teleport(location.clone().add(0, 4, 0));
        player.removePotionEffect(PotionEffectType.LEVITATION);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2f, 2f);
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
        player.setHealth(0);

        if (ban) {
            String broadcastCmd = Config.replace(Config.getString("ban.broadcast", "litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом."), new Pair<>("%player%", player.getName()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " " + reason);
            if(!broadcastCmd.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCmd);
            }
        }
    }

    public static List<Location> getCircle(Location location, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        double increment = 6.283185307179586D / points;
        for (int i = 0; i < points; i++) {
            double angle = i * increment;
            double x = location.getX() + Math.cos(angle) * radius;
            double z = location.getZ() + Math.sin(angle) * radius;
            locations.add(new Location(location.getWorld(), x, location.getY(), z));
        }
        return locations;
    }

    private static Vector rotateVectorAroundAxis(Vector vector, Vector axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = axis.dot(vector);

        Vector cross = axis.getCrossProduct(vector);

        return vector.clone().multiply(cos)
                .add(cross.multiply(sin))
                .add(axis.multiply(dot * (1 - cos)));
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
        if(event.getPlayer() == null) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if(event.getTo().getX() != event.getFrom().getX() || event.getTo().getX() != event.getFrom().getX()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent event) {
        if(event.getPlayer() == null) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCmd(PlayerTeleportEvent    event) {
        if(event.getPlayer() == null) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
