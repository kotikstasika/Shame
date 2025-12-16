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

        Color[] colors = {Color.fromRGB(0xFF1A1A), Color.fromRGB(0xFF2620), Color.fromRGB(0xFF3326), Color.fromRGB(0xFF3F2C), Color.fromRGB(0xFF4C1A), Color.fromRGB(0xFF5910), Color.fromRGB(0xFF6610)};

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2f, 0.5f);
        int limit = 120;
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, limit + 40, 1, false, false, false));
        player.setAllowFlight(false);
        player.setGliding(false);

        player.setFlying(false);
        player.setSwimming(false);

        BukkitTask bukkitTask = new BukkitRunnable() {
            public int ticks = 0;
            double baseRotation = 0;
            double tiltYawDeg = 0;
            final Location particleOrigin = startLocation.clone().add(0, Math.max(1.4, player.getEyeHeight() - 0.2), 0);
            final Random random = new Random();
            final int sparkCount = 14;
            final double[] angleOffsets = new double[sparkCount];
            final double[] angularSpeeds = new double[sparkCount];
            final double[] radii = new double[sparkCount];
            final double[] yOffsets = new double[sparkCount];
            final double[] riseSpeeds = new double[sparkCount];
            final double[] swayPhases = new double[sparkCount];
            final double[] swayAmps = new double[sparkCount];
            final double[] radialDrifts = new double[sparkCount];
            final double[] radialDirs = new double[sparkCount];

            {
                for (int i = 0; i < sparkCount; i++) {
                    angleOffsets[i] = (Math.PI * 2.0 / sparkCount) * i;
                    angularSpeeds[i] = Math.toRadians(18) + Math.toRadians(random.nextGaussian() * 2.0);
                    radii[i] = 1.25 + random.nextDouble() * 0.05;
                    yOffsets[i] = -0.7 + random.nextDouble() * 0.2;
                    riseSpeeds[i] = 0.01 + random.nextDouble() * 0.008;
                    swayPhases[i] = random.nextDouble() * Math.PI * 2.0;
                    swayAmps[i] = 0.015 + random.nextDouble() * 0.01;
                    radialDrifts[i] = 0.001 + random.nextDouble() * 0.0015;
                    radialDirs[i] = random.nextBoolean() ? 1.0 : -1.0;
                }
            }

            @Override
            public void run() {
                if (ticks >= limit || player.getLocation().getBlock().isLiquid() || instant.getOrDefault(player, false)) {
                    finishBanAnimation(player, reason, ban, particleOrigin);
                    this.cancel();
                }
                player.setGliding(false);
                player.setFlying(false);
                player.setSwimming(false);

                particleOrigin.add(0, 0.06, 0);

                double tiltPitchRad = Math.toRadians(35);
                double tiltYawRad = Math.toRadians(tiltYawDeg);

                for (int i = 0; i < sparkCount; i++) {
                    double angle = angleOffsets[i] + Math.toRadians(tiltYawDeg * 0.2) + baseRotation;
                    double x = radii[i] * Math.cos(angle);
                    double z = radii[i] * Math.sin(angle);
                    Vector local = new Vector(x, 0, z);
                    Vector tilted = rotateVectorAroundAxis(local, new Vector(1, 0, 0), tiltPitchRad).rotateAroundY(tiltYawRad);
                    double swim = Math.sin((ticks * 0.15) + swayPhases[i]) * swayAmps[i];
                    Vector swimVec = tilted.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(swim);
                    Vector pos = tilted.clone().add(swimVec);

                    yOffsets[i] += riseSpeeds[i];
                    int segments = 8;

                    double phase = (ticks * (Math.PI * 2 / 20.0)) % (2 * Math.PI);
                    double wobble = Math.sin((ticks * 0.10) + swayPhases[i]) * Math.toRadians(3);
                    double pointAngle = (angle + wobble) % (2 * Math.PI);
                    double delta = Math.abs(Math.atan2(Math.sin(pointAngle - phase), Math.cos(pointAngle - phase)));
                    int colorIndex = (int) Math.round((double) (colors.length - 1) * (1.0 - (delta / Math.PI)));
                    Color color = colors[Math.max(0, Math.min(colors.length - 1, colorIndex))];
                    for (int s = 0; s < segments; s++) {
                        if ((s % 2) == 1) continue;
                        Vector segOffset = new Vector(0, yOffsets[i] + s * 0.14, 0);
                        Location spawnLoc = particleOrigin.clone().add(pos).add(segOffset);
                        player.getWorld().spawnParticle(Particle.REDSTONE, spawnLoc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 0.70f), true);
                    }

                    radii[i] += radialDrifts[i] * radialDirs[i];
                    if (radii[i] > 1.5) {
                        radii[i] = 1.5;
                        radialDirs[i] = -1.0;
                    }
                    if (radii[i] < 1.0) {
                        radii[i] = 1.0;
                        radialDirs[i] = 1.0;
                    }
                }

                baseRotation += Math.toRadians(1.6);
                tiltYawDeg -= 1.2;
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

        if (ban) {
            String broadcastCmd = Config.replace(Config.getString("ban.broadcast", "litebans broadcast &7[&x&F&B&0&8&0&8&lАЧ&7]&r &f%player% &fбыл наказан античитом."), new Pair<>("%player%", player.getName()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " " + reason);
            player.setHealth(0);
            if (!broadcastCmd.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCmd);
            }
        }
    }


    private static Vector rotateVectorAroundAxis(Vector vector, Vector axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = axis.dot(vector);

        Vector cross = axis.getCrossProduct(vector);

        return vector.clone().multiply(cos).add(cross.multiply(sin)).add(axis.multiply(dot * (1 - cos)));
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
