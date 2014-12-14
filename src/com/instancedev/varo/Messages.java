package com.instancedev.varo;

import org.bukkit.ChatColor;

public enum Messages {

	starting_msg("&7Starting in &4<count>&7!"),
	golden_apple_blocked("&cGolden Apples are banned as they're too OP!"),
	varo_stopped("&cVARO was stopped."),
	thanks_for_playing ("&cYou failed. Thanks for playing!"),
	not_registered("&cYou don't seem to be registered for VARO.");

	private String msg;

	Messages(String msg) {
		this.msg = msg;
	}

	public String getMSG() {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

}
