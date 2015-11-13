package io.github.andorem.instakill;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Instakill extends JavaPlugin {

	public static File DATA_FOLDER;
	Logger log = getLogger();
	ItemListener itemListener;

	@Override
	public void onEnable() {
		DATA_FOLDER = getDataFolder();

		try {
			if (!DATA_FOLDER.exists()) {
				DATA_FOLDER.mkdir();
			}
			ensureConfigExists();
		} catch (Exception e) {
			e.printStackTrace();

		}

		itemListener = new ItemListener(this);

		getServer().getPluginManager().registerEvents(itemListener, this);

	}

	private void ensureConfigExists() {
		File file = new File(DATA_FOLDER, "config.yml");
		if (!file.exists()) {
			log.info("No config.yml found. Generating default one.");
			saveDefaultConfig();
			reloadConfig();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		boolean senderIsPlayer = (sender instanceof Player);
		Player pSender = (senderIsPlayer ? (Player) sender : null);

		if (cmd.getName().equalsIgnoreCase("ik")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("give")) {
					if (sender.hasPermission("instakill.give")) {
						if (!senderIsPlayer) {
							sender.sendMessage("You must be a player to use that command.");
						} 
						else {
							Player recipient = args.length > 1 ? getServer().getPlayer(args[1]) : pSender;
							if (recipient != null) {
								int numberOfItems;
								try {
									numberOfItems = (args.length == 3 ? Integer.parseInt(args[2]) : 1);
								}
								catch (NumberFormatException e) {
								  return false;
								}
								recipient.getInventory().addItem(itemListener.createKillItem(numberOfItems));
							}
							else {
								sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Player not found.");
							}
						}
					} 
					else
						sender.sendMessage("You do not have permission to perform this command.");
				}
			}
		}
		return true;
	}
}
