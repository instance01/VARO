package com.instancedev.varo;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class AScoreboard {

	HashMap<String, Scoreboard> ascore = new HashMap<String, Scoreboard>();
	HashMap<String, Objective> aobjective = new HashMap<String, Objective>();

	Main m;

	public AScoreboard(Main m) {
		this.m = m;
	}

	public void updateScoreboard(final Player p) {
		if (!ascore.containsKey(p.getName())) {
			ascore.put(p.getName(), Bukkit.getScoreboardManager().getNewScoreboard());
		}
		if (!aobjective.containsKey(p.getName())) {
			aobjective.put(p.getName(), ascore.get(p.getName()).registerNewObjective(p.getName(), "dummy"));
			aobjective.get(p.getName()).setDisplaySlot(DisplaySlot.SIDEBAR);
			aobjective.get(p.getName()).setDisplayName(ChatColor.RED + "VARO");
		}

		// ascore.get(p.getName()).resetScores(ChatColor.RED + m.v.getTimeFormatted(p));
		aobjective.get(p.getName()).getScore(ChatColor.GRAY + " - ").setScore(0);

		aobjective.get(p.getName()).setDisplayName(ChatColor.RED + m.v.getTimeFormatted(p));

		if (ascore.get(p.getName()) != null) {
			p.setScoreboard(ascore.get(p.getName()));
		}
	}

	public void removeScoreboard(String arena, Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();

			p.setScoreboard(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
