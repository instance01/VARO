package com.instancedev.varo;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin implements Listener {

	// This is a server-wide plugin and can only be used once each server

	boolean started;
	int countdown = 30;
	BukkitTask task_temp = null;

	Main m;

	public void onEnable() {
		m = this;
		Bukkit.getPluginManager().registerEvents(this, this);
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
						startCountdown();
					} else if (args[0].equalsIgnoreCase("setspawn")) {
						if (args.length > 1) {
							String base = args[1] + "." + (getConfig().isSet("spawns." + args[1] + ".") ? 1 : 0);
							Util.saveComponent(this, base, player.getLocation());
							sender.sendMessage(ChatColor.GREEN + "Saved spawn for: " + base);
						}
					} else if (args[0].equalsIgnoreCase("stop")) {
						if (task_temp != null) {
							task_temp.cancel();
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (!p.isOp()) {
									p.kickPlayer(Messages.varo_stopped.getMSG());
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	public void startCountdown() {
		task_temp = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				if (countdown == 30 || countdown == 15 || countdown == 10 || countdown < 6) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(Messages.starting_msg.getMSG().replaceAll("<count>", Integer.toString(countdown)));
					}
				}
				countdown--;
			}
		}, 20L, 20L);
	}

	public void start() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setHealth(20D);
			p.setLevel(0);
			p.setExp(0F);
			Util.clearInv(p);
			p.setFoodLevel(20);
			p.setSaturation(2F);
			if (p.getAllowFlight()) {
				p.setFlying(false);
			}
			p.setGameMode(GameMode.SURVIVAL);
		}
		started = true;
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
		if (getConfig().isSet("players." + event.getPlayer().getName())) {
			final String team = getConfig().getString("players." + event.getPlayer().getName());
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					Util.teleportPlayerFixed(event.getPlayer(), Util.getComponent(m, team));
				}
			}, 40L);
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
	public void onPlayerDeath(PlayerDeathEvent event) {
		// event.getEntity().setBanned(true);
		// event.getEntity().kickPlayer(thanks_for_playing);
	}

}
