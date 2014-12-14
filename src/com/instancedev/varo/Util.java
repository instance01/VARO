package com.instancedev.varo;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Util {

	public static void clearInv(Player p) {
		p.getInventory().clear();
		p.updateInventory();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.updateInventory();
	}

	public static void teleportPlayerFixed(final Player p, Location l) {
		if (p.isInsideVehicle()) {
			Entity ent = p.getVehicle();
			p.leaveVehicle();
			ent.eject();
		}
		p.teleport(l, TeleportCause.PLUGIN);
		p.setFallDistance(-1F);
		p.setVelocity(new Vector(0D, 0D, 0D));
		l.getWorld().refreshChunk(l.getChunk().getX(), l.getChunk().getZ());
		p.setFireTicks(0);
		p.setHealth(20D);
	}

	public static void saveComponent(JavaPlugin plugin, String team, Location comploc) {
		String base = "spawns." + team;
		plugin.getConfig().set(base + ".world", comploc.getWorld().getName());
		plugin.getConfig().set(base + ".location.x", comploc.getX());
		plugin.getConfig().set(base + ".location.y", comploc.getY());
		plugin.getConfig().set(base + ".location.z", comploc.getZ());
		plugin.getConfig().set(base + ".location.yaw", comploc.getYaw());
		plugin.getConfig().set(base + ".location.pitch", comploc.getPitch());
		plugin.saveConfig();
	}

}
