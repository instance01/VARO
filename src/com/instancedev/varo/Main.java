package com.instancedev.varo;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin implements Listener {

	// This is a server-wide plugin and can only be used once each server

	boolean started;

	Main m;
	VARO v;

	public void onEnable() {
		m = this;
		v = new VARO(this);
		Bukkit.getPluginManager().registerEvents(this, this);

		this.getConfig().addDefault("config.started", false);
		// Example:
		this.getConfig().addDefault("players.InstanceLabs.team", "Test");
		this.getConfig().addDefault("players.InstanceLabs.temp_time", 0);
		this.getConfig().addDefault("players.InstanceLabs.was_teleported", false);

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		this.started = this.getConfig().getBoolean("config.started");
		
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("varo")) {
			if (!sender.isOp()) {
				return true;
			}
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("start")) {
						v.startCountdown();
					} else if (args[0].equalsIgnoreCase("setspawn")) {
						if (args.length > 1) {
							String base = args[1] + "." + (getConfig().isSet("spawns." + args[1] + ".") ? 1 : 0);
							Util.saveComponent(this, base, player.getLocation());
							sender.sendMessage(ChatColor.GREEN + "Saved spawn for: " + base);
						}
					} else if (args[0].equalsIgnoreCase("stop")) {
						v.stop();
					}
				}
			}
		}
		return true;
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (!started && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlotType() == SlotType.RESULT) {
			if (event.getCurrentItem().getType() == Material.GOLDEN_APPLE && event.getCurrentItem().getData().getData() == (byte) 1) {
				event.setCancelled(true);
				((Player) event.getWhoClicked()).sendMessage(Messages.golden_apple_blocked.getMSG());
			}
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!getConfig().isSet("players." + event.getPlayer().getName()) && !event.getPlayer().isOp()) {
			event.disallow(Result.KICK_OTHER, "");
		}
	}

	@EventHandler
	public void onPlayerJoinServer(final PlayerJoinEvent event) {
		if (v.isRegistered(event.getPlayer())) {
			v.teleportToTeamSpawn(event.getPlayer());
		} else {
			Bukkit.broadcastMessage(event.getPlayer().getName() + " is not registered as a VARO player!");
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event.getDamage() > 0.5D && !event.isCancelled()) {
				Util.playBloodEffect(p);
			}
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {

	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!event.getEntity().isOp()) {
			event.getEntity().setBanned(true);
		}
		event.getEntity().kickPlayer(Messages.thanks_for_playing.getMSG());
	}

}
