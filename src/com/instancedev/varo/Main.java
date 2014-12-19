package com.instancedev.varo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	// This is a server-wide plugin and can only be used once each server

	boolean started;

	Main m;
	VARO v;
	AScoreboard ascore;

	public void onEnable() {
		m = this;
		v = new VARO(this);
		ascore = new AScoreboard(this);
		Bukkit.getPluginManager().registerEvents(this, this);

		this.getConfig().addDefault("config.started", false);
		// Example:
		this.getConfig().addDefault("players.InstanceLabs.team", "Test");
		this.getConfig().addDefault("players.InstanceLabs.delta_time", 0);
		this.getConfig().addDefault("players.InstanceLabs.was_teleported", false);
		this.getConfig().addDefault("players.InstanceLabs.spawn", "0");

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
					} else if (args[0].equalsIgnoreCase("reloadconfig")) {
						this.reloadConfig();
					} else if (args[0].equalsIgnoreCase("registerplayer")) {
						if (args.length > 2) {
							String team = args[2];
							String playername = args[1];
							v.registerPlayer(playername, team);
							sender.sendMessage(ChatColor.GREEN + "Successfully registered " + playername + " for team " + team + ".");
						}
					}
				} else {
					sender.sendMessage(" ");
					sender.sendMessage(ChatColor.RED + "~~ VARO ~~");
					sender.sendMessage(ChatColor.GRAY + "/varo start");
					sender.sendMessage(ChatColor.GRAY + "/varo stop");
					sender.sendMessage(ChatColor.GRAY + "/varo reloadconfig");
					sender.sendMessage(ChatColor.GRAY + "/varo setspawn <team>");
					sender.sendMessage(ChatColor.GRAY + "/varo registerplayer <player> <team>");
					sender.sendMessage(ChatColor.DARK_GRAY + "For each new player you have to register and set the spawn for his team.");
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
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.getReason().toLowerCase().contains("ban")) {
			event.setReason(Messages.thanks_for_playing.getMSG());
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getPlayer().isOp()) {
			return;
		}
		if (!v.isRegistered(event.getPlayer())) {
			event.disallow(Result.KICK_OTHER, Messages.not_registered.getMSG().replaceAll("<player>", event.getPlayer().getName()));
		} else {
			if (m.getConfig().isSet("players." + event.getPlayer().getName() + ".temp_banned")) {
				long millis = m.getConfig().getLong("players." + event.getPlayer().getName() + ".temp_banned");
				long delta = Util.millisBetween(millis);
				System.out.println(delta);
				if (delta > 0) {
					event.disallow(Result.KICK_OTHER, Messages.quota_drained.getMSG());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoinServer(final PlayerJoinEvent event) {
		if (v.isRegistered(event.getPlayer())) {
			v.teleportToTeamSpawn(event.getPlayer());
			if (started) {
				v.startPlayerCountdown(event.getPlayer());
			}
		} else {
			Bukkit.broadcastMessage(ChatColor.GRAY + event.getPlayer().getName() + " is not registered as a VARO player!");
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
				if (ev.getDamager() instanceof Player) {
					Player damager = (Player) ev.getDamager();
					if (v.sameTeam(p, damager)) {
						event.setCancelled(true);
						return;
					}
				}
			}
			if (event.getDamage() > 0.5D && !event.isCancelled()) {
				Util.playBloodEffect(p);
			}
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		System.out.println(v.pcounter.containsKey(event.getPlayer().getName()));
		if (v.pcounter.containsKey(event.getPlayer().getName()) && v.isRegistered(event.getPlayer())) {
			System.out.println(v.pcounter.get(event.getPlayer().getName()));
			v.setDeltaTime(event.getPlayer(), v.pcounter.get(event.getPlayer().getName()));
			if (v.ptask.containsKey(event.getPlayer().getName())) {
				v.ptask.get(event.getPlayer().getName()).cancel();
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!event.getEntity().isOp()) {
			event.getEntity().setBanned(true);
		}
		Entity e = event.getEntity().getKiller();
		if (e instanceof Player) {
			Player killer = (Player) e;
			getConfig().set("kills." + killer.getName() + "." + System.currentTimeMillis(), event.getEntity().getName());
			saveConfig();
		}
		event.getEntity().kickPlayer(Messages.thanks_for_playing.getMSG());
	}

}
