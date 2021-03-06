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

	public void registerPlayer(String playername, String team) {
		String base = "players." + playername;
		m.getConfig().set(base + ".team", team);
		m.getConfig().set(base + ".delta_time", 0);
		m.getConfig().set(base + ".was_teleported", false);
		m.getConfig().set(base + ".spawn", m.getConfig().isSet("teams." + team) ? "1" : "0");
		m.getConfig().set("teams." + team, team);
		m.saveConfig();
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
			startPlayerCountdown(p);
		}
		setStarted(true);
	}

	public void startPlayerCountdown(final Player p) {
		pcounter.put(p.getName(), getDeltaTime(p));
		ptask.put(p.getName(), Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				String name = p.getName();
				int secs = pcounter.get(name);
				pcounter.put(name, secs + 1);
				m.ascore.updateScoreboard(p);
				if (secs == 1170) {
					p.sendMessage(Messages.kicking_in.getMSG().replaceAll("<count>", "30"));
				}
				if (secs == 1190) {
					p.sendMessage(Messages.kicking_in.getMSG().replaceAll("<count>", "10"));
				}
				if (secs >= 1200) {
					if (!m.getConfig().isSet("players." + p.getName() + ".temp_do_ban")) {
						m.getConfig().set("players." + p.getName() + ".temp_do_ban", true);
					} else {
						m.getConfig().set("players." + p.getName() + ".temp_banned", Util.millisUntilMidnight());
						m.getConfig().set("players." + p.getName() + ".temp_do_ban", null);
					}
					m.saveConfig();
					pcounter.remove(name);
					ptask.get(name).cancel();
					p.kickPlayer(Messages.twenty_mins.getMSG());
				}
			}
		}, 20L, 20L));
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
		setStarted(false);
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
		m.getConfig().set("players." + p.getName() + ".was_teleported", b);
	}

	public void teleportToTeamSpawn(final Player p) {
		if (!wasTeleported(p)) {
			final String team = m.getConfig().getString("players." + p.getName() + ".team");
			Bukkit.getScheduler().runTaskLater(m, new Runnable() {
				public void run() {
					Util.teleportPlayerFixed(p, Util.getComponent(m, team + (isPlayerOnlineFromSameTeam(p) ? ".1" : ".0")));
				}
			}, 25L);
			setWasTeleported(p, true);
		}
	}

	public boolean isPlayerOnlineFromSameTeam(Player p) {
		final String team = m.getConfig().getString("players." + p.getName() + ".team");
		for (Player pp : Bukkit.getOnlinePlayers()) {
			final String t = m.getConfig().getString("players." + p.getName() + ".team");
			if (t.equalsIgnoreCase(team)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean sameTeam(Player p1, Player p2){
		final String team = m.getConfig().getString("players." + p1.getName() + ".team");
		final String team_ = m.getConfig().getString("players." + p2.getName() + ".team");
		if (team_.equalsIgnoreCase(team)) {
			return true;
		}
		return false;
	}

	public int getDeltaTime(Player p) {
		if (m.getConfig().isSet("players." + p.getName() + ".delta_time")) {
			int ret = m.getConfig().getInt("players." + p.getName() + ".delta_time");
			m.getConfig().set("players." + p.getName() + ".delta_time", null);
			m.saveConfig();
			return ret;
		}
		return 0;
	}

	public void setDeltaTime(Player p, int time) {
		m.getConfig().set("players." + p.getName() + ".delta_time", time);
		m.saveConfig();
	}

	public String getTimeFormatted(Player p) {
		if (pcounter.containsKey(p.getName())) {
			int secs = 1200 - pcounter.get(p.getName());
			int minutes = secs / 60;
			int remainder_secs = secs % 60;
			return "" + minutes + ":" + remainder_secs;
		}
		return "00:00";
	}

}
