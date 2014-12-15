package com.instancedev.varo;

import org.bukkit.ChatColor;

public enum Messages {

	starting_msg("&7Starting in &2<count>&7!"),
	golden_apple_blocked("&6Golden Apples &care banned as they're too OP!"),
	varo_stopped("&cVARO was stopped."),
	thanks_for_playing ("&cYou failed. Thanks for playing!"),
	not_registered("&cYou don't seem to be registered for VARO. &7If you want to join, reach out for an admin and tell him your name, &4<player>&7!"),
	kicking_in("&cYou'll be kicked in &4<count> &cseconds!"),
	twenty_mins("&cYour 20 minutes are gone, see you soon!");

	private String msg;

	Messages(String msg) {
		this.msg = msg;
	}

	public String getMSG() {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

}
