package io.github.andorem.instakill;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Instakill extends JavaPlugin {

	static File DATA_FOLDER;
	Logger log = getLogger();
	ItemListener itemListener;
	
    String chatHeader = ChatColor.AQUA + "\n======[" + ChatColor.WHITE + getName() + ChatColor.AQUA + "]======\n";
	String noPermission = ChatColor.RED + "You do not have permission to perform this command.";
	String listReminder = ChatColor.GOLD + "To list your available Instakill items, type " + ChatColor.GREEN + "/ik list" + ChatColor.GOLD + ".";
	
	FileConfiguration generalConfig;
	
	boolean debugEnabled = false;
	
	@Override
	public void onEnable() {
		DATA_FOLDER = getDataFolder();

		try {
			if (!DATA_FOLDER.exists()) {
				DATA_FOLDER.mkdir();
			}
			ensureConfigsExist("config", "items");
		} catch (Exception e) {
			e.printStackTrace();
		}

		debugEnabled = getConfig().getBoolean("debug");
		debug("==============================================================", "INFO");
		debug("Debug mode activated. Now reporting to console.", "INFO");
		debug("To disable debugging, set 'debug' in 'config.yml' to false.", "INFO");
		debug("==============================================================", "INFO");
		
		debug("Retrieving config.yml and items.yml from ~/Instakill/", "INFO");
		generalConfig = getConfig();
		
		itemListener = new ItemListener(this);
		getServer().getPluginManager().registerEvents(itemListener, this);
		debug("PlayerInteractEvent ItemListener registered.", "INFO");

	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ik")) {
			Player pSender = ((sender instanceof Player) ? (Player) sender : null);
			String senderName = sender.getName();
			if (args.length == 0) {
				if (sender.hasPermission("instakill.list")) sender.sendMessage(listReminder);
				if (sender.hasPermission("instakill.give")) return false;
			}
			else {
				
				if (args[0].equalsIgnoreCase("list")) {
					debug("FROM " + senderName + ": Requested Instakill item list", "INFO");
			
					if (sender.hasPermission("instakill.list")) {
						debug("TO " + senderName + ": Sending Instakill item list", "INFO");
						Set<String> allowedKillItems = itemListener.allowedKillItemsFor(pSender);
						String itemsList = itemListener.getItemsList(allowedKillItems, itemListener.getItemConfig());
						
						sender.sendMessage(chatHeader);
						if (sender.hasPermission("instakill.give")) {
							sender.sendMessage(ChatColor.WHITE + "To give an item, type /ik give [user] " + ChatColor.GREEN 
								+ "[" + ChatColor.GOLD + "item name" + ChatColor.GREEN + "]" + ChatColor.WHITE + " (amount)\n" + ChatColor.GRAY); 
						}
						sender.sendMessage(ChatColor.WHITE + "You can use the following Instakill items:");
						sender.sendMessage(itemsList);
						
						debug("TO " + senderName + ": Instakill item list sent", "INFO");
					}
					else { 
						sender.sendMessage(noPermission);
						debug("FROM " + senderName + ": Does not have the correct permission (instakill.list)!", "WARNING");
					}
				}
				
				else if (args[0].equalsIgnoreCase("give")) {
					debug("FROM " + senderName + ": Initiated command to give an Instakill item", "INFO");
					if (sender.hasPermission("instakill.give")) {
						if (args.length < 3) {
							sender.sendMessage(listReminder);
							debug("FROM " + senderName + ": Not enough arguments for give command (at least three)", "INFO");
							return false;
						}
						else {
						
							String itemSectionName = "";
							int numberOfItems = 1;
							Player recipient = getServer().getPlayer(args[1]);
							debug("FROM " + senderName + ": Attempting to get player " + args[1] + " from server...", "INFO");
							
							if (recipient == null) {
								sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Player not found.");
								debug("TO " + senderName + ": Player " + args[1] + " returned NULL", "WARNING");
							}
							else {
								debug("FROM " + senderName + ": Player " + args[1] + " found.", "INFO");
								itemSectionName = args[2].toLowerCase();
								if ((args.length == 4)) {
									numberOfItems = (isInt(args[3]) ? Integer.parseInt(args[3]) : 1);
								}
								debug("FROM " + senderName + ": Attempting to give " + numberOfItems + " of " + itemSectionName + " to " + recipient.getName(), "INFO");
							
								if (itemListener.getKillItemSectionNames().contains(itemSectionName)) {
									String itemName = itemListener.getItemConfig().getString(itemSectionName + ".name");
									debug("FROM " + senderName + ": " + itemName + " found in 'items.yml' under section title '" + itemSectionName + "'", "INFO");
									
									ItemStack killItem = itemListener.createKillItem(itemSectionName, numberOfItems);
									if (killItem == null) {
										sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Could not create Instakill item from specified ID.");
										debug("FROM " + senderName + ": " + "FAILED to create item '" + itemName + "'.", "WARNING");
									}
									recipient.getInventory().addItem(killItem);
									sender.sendMessage(ChatColor.GOLD + "Giving " + ChatColor.RED + numberOfItems +ChatColor.GOLD + " of " 
										+ ChatColor.RED + itemName + ChatColor.GOLD + " to " 
										+ ChatColor.DARK_RED + recipient.getName() + ChatColor.GOLD + ".");
									debug("FROM " + senderName + ": Giving " + numberOfItems + " of " + itemName + " to " + recipient.getName(), "INFO");
								}
								else {
									sender.sendMessage(ChatColor.RED + "This Instakill item does not exist!");
									if (sender.hasPermission("instakill.list")) sender.sendMessage(listReminder);
									debug("FROM " + senderName + ": Instakill item '" + itemSectionName + "' not found in items.yml", "WARNING");
								}
							}
						}
					}
					else {
						sender.sendMessage(noPermission);
						debug("FROM " + senderName + ": Does not have the correct permission (instakill.give)!", "WARNING");
					}
				}
				
				else if (args[0].equalsIgnoreCase("reload")) {
					debug("FROM " + senderName + ": Sent command to reload all configurations.", "INFO");
					
					if (sender.hasPermission("instakill.reload")) {
						debug("FROM " + senderName + ": Reloading configuration files.", "INFO");
						reloadConfig();
						updateFromConfig();
						debug("TO " + senderName + ": Configuration 'config.yml' reloaded.", "INFO");
						
						itemListener.updateFromConfig(loadConfig("items"));
						debug("TO " + senderName + ": Configuration 'items.yml' reloaded.", "INFO");
						
						sender.sendMessage(ChatColor.GREEN + "Configs reloaded.");
					}
					else {
						sender.sendMessage(noPermission);
						debug("FROM " + senderName + ": Does not have the correct permission (instakill.reload)!", "WARNING");
					}
				}
			}
		}
		return true;
	}
	
	FileConfiguration loadConfig(String configName) {
		FileConfiguration loadedConfig;
		ensureConfigsExist(configName);
		debug("Loading configuration file '" + configName + ".yml'...", "INFO");
		loadedConfig = YamlConfiguration.loadConfiguration(new File(DATA_FOLDER, configName + ".yml"));
		debug("Configuration '" + configName + ".yml' loaded.", "INFO");
		return loadedConfig;
	}
	
	void saveConfig(String configName, FileConfiguration config) {
		File file = new File(DATA_FOLDER, configName + ".yml");
		try {
			config.save(file);
		} 
		catch (IOException e) {
			e.printStackTrace();
			log.severe("Failed to save configuration to '"+ configName + ".yml'! Does it exist?");
		}
	}
	
	
	void saveDefaultConfig(String configName) {
		saveResource(configName + ".yml", false);
    }
	
	boolean configExists(String configName) {
		File file = new File(DATA_FOLDER, configName + ".yml");
		return file.exists();
	}
	
	void ensureConfigsExist(String... configNames) {
		for (String name : configNames) {
			if (!configExists(name)) {
				log.info("No " + name + ".yml found. Generating default one.");
				if (name.equals("config")) {
					saveDefaultConfig();
					reloadConfig();
				}
				else {
					saveDefaultConfig(name);
				}
			}
		}
	}
	
	void updateFromConfig() {
		generalConfig = getConfig();
		debugEnabled = generalConfig.getBoolean("debug");
	}
	
	boolean isInt(String s) {
		try {
			int num = Integer.parseInt(s);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	void debug(String debugMessage, String levelName) {
		Level level = Level.parse(levelName.toUpperCase());
		if (debugEnabled) {
			log.log(level, "[DEBUG] " + debugMessage);
		}
	}
	
}