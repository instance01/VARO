package com.instancedev.varo;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class VARO {

	BukkitTask task_temp = null;
	HashMap<String, Integer> pcounter = new HashMap<String, Integer>();
	HashMap<String, BukkitTask> ptask = new HashMap<String, BukkitTask>();
	int countdown = 30;

	Main m;

	public VARO(Main m) {
		this.m = m;
	}

	public void startCountdown() {
		task_temp = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				if (countdown == 30 || countdown == 15 || countdown == 10 || countdown < 6) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(Messages.starting_msg.getMSG().replaceAll("<count>", Integer.toString(countdown)));
					}
				}
				if (countdown < 1) {
					start();
					countdown = 30;
					task_temp.cancel();
					return;
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
		setStarted(true);
	}

	public void stop() {
		if (task_temp != null) {
			task_temp.cancel();
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (!p.isOp()) {
					p.kickPlayer(Messages.varo_stopped.getMSG());
				}
			}
		}
	}

	public void setStarted(boolean b) {
		m.started = b;
		m.getConfig().set("config.started", b);
		m.saveConfig();
	}

	public boolean isRegistered(Player p) {
		if (p.isOp()) {
			return true;
		}
		return m.getConfig().isSet("players." + p.getName() + ".team");
	}

	public boolean wasTeleported(Player p) {
		if (p.isOp()) {
			return true;
		}
		if (!isRegistered(p)) {
			return false;
		}
		return m.getConfig().getBoolean("players." + p.getName() + ".was_teleported");
	}

	public void setWasTeleported(Player p, boolean b) {

	}

	public void teleportToTeamSpawn(final Player p) {
		final String team = m.getConfig().getString("players." + p.getName() + ".team");
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				Util.teleportPlayerFixed(p, Util.getComponent(m, team));
			}
		}, 25L);
	}

}
