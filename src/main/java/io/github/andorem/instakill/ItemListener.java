package io.github.andorem.instakill;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemListener implements Listener {

	Instakill plugin;
	Set<String> killItemSectionNames;
	Set<String> killItemIDs = new HashSet<String>();
	FileConfiguration itemConfig, generalConfig;

	public ItemListener(Instakill plugin) {
		plugin.debug("Creating new ItemListener...", "INFO");
		this.plugin = plugin;
		generalConfig = plugin.getConfig();
		updateFromConfig(plugin.loadConfig("items"));
	}

	Set<String> getKillItemSectionNames() {
		return killItemSectionNames;
	}
	
	Set<String> getKillItemIDs() {
		return killItemIDs;
	}
	
	Set<String> updateKillItemIDs(Set<String> itemSectionNames) {
		Set<String> updatedIDs = new HashSet<String>();
		plugin.debug("Retrieving item IDs for all items...", "INFO");
		for (String sectionName: itemSectionNames) {
			plugin.debug("Getting ID for item " + sectionName, "INFO");
			try {
				String itemID = itemConfig.getString(sectionName + ".id");
				updatedIDs.add(itemID);
				plugin.debug("Item ID " + itemID + " found", "INFO");
			}
			catch (Exception e) {
				e.printStackTrace();
				plugin.debug("Could not find item ID for " + sectionName, "WARNING");
			}
		}
		plugin.debug("Instakill item IDs retrieved.", "INFO");
		return updatedIDs;
	}
	
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		plugin.debug("PLAYER " + playerName + ": Has access to Instakill items. Watching.", "INFO");
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack itemInHand = player.getItemInHand();
			String itemType = Integer.toString(itemInHand.getTypeId());
			String itemDataValue = Short.toString(itemInHand.getDurability());
			String itemID = (itemDataValue.equals("0") ? itemType : (itemType + ":" + itemDataValue));
			plugin.debug("PLAYER " + playerName + ": Right-clicked with item " + itemID, "INFO");
			if (itemInHand.hasItemMeta()) {
				plugin.debug("PLAYER " + playerName + ": Item has metadata information. Checking if Instakill item...", "INFO");
				ItemMeta itemMeta = itemInHand.getItemMeta();
				String itemDisplayName = itemMeta.getDisplayName();
				List<String> itemLore = itemMeta.getLore();
				ConfigurationSection killItemSection = getKillItemSection(itemID, itemDisplayName, itemLore);
				if (killItemSection != null) {
					event.setCancelled(true);
					plugin.debug("PLAYER " + playerName + ": Item ID found in items.yml. Item has custom diplay name '" + itemDisplayName + "' and lore '" + itemLore.toString() + "'", "INFO");
					String itemPerm = killItemSection.getName().toLowerCase();
					if (player.hasPermission("instakill.use." + itemPerm) || player.hasPermission("instakill.use.*")) {
						double killRadius = generalConfig.getDouble("kill-radius");
						if (killItemSection.contains("kill-radius")) killRadius = killItemSection.getDouble("kill-radius"); 
						
						boolean mobDropsEnabled = generalConfig.getBoolean("mob-drops");
						if (killItemSection.contains("mob-drops")) mobDropsEnabled = killItemSection.getBoolean("mob-drops");
						
						boolean isConsumable = generalConfig.getBoolean("consume-item");
						if (killItemSection.contains("consume-item")) isConsumable = killItemSection.getBoolean("consume-item");
						
						List<Entity> nearbyEntities = player.getNearbyEntities(killRadius, killRadius, killRadius);
						int killedEntities = 0;
						for (Entity entity : nearbyEntities) {
							if (entity instanceof LivingEntity && !(entity instanceof Player)) {
								Damageable mob = (Damageable) entity;
								if (!mobDropsEnabled) mob.setHealth(0);
								else mob.damage(99999999);
								killedEntities++;
							}
						}
						plugin.debug("PLAYER " + playerName + ": Killed " + killedEntities + " entities (out of " + nearbyEntities.size() + ") within " + killRadius + " blocks of player.", "INFO");
						if (isConsumable) {
							itemInHand.setAmount(itemInHand.getAmount() - 1);
							player.setItemInHand(itemInHand);
							plugin.debug("PLAYER " + playerName + ": 1 of Instakill item consumed.", "INFO");
						}
						else {
							player.setItemInHand(itemInHand);
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You do not have permission to use this item!");
						plugin.debug("PLAYER " + playerName + ": Does not have permission to use this Instakill item! (instakill.use." + itemPerm + ")", "WARNING");
					}
					
				}
				else {
					plugin.debug("PLAYER " + playerName + ": No Instakill item match found. Ignoring.", "INFO");
				}
			}
		}
		else {
			plugin.debug("PLAYER " + playerName + ": Not right-clicking with any item. Ignoring.", "INFO");
		}
	}
	
	ItemStack createKillItem(String sectionName, int itemAmount) {
		plugin.debug("Creating new ItemStack for " + itemAmount + " of item " + sectionName, "INFO");
		
		plugin.debug("Loading item section '" + sectionName + "' from 'items.yml'...", "INFO");
		ConfigurationSection itemSection = itemConfig.getConfigurationSection(sectionName);
		if (itemSection == null) {
			plugin.debug("Item " + sectionName + " not found in 'items.yml'! Use /ik list to get valid item names.", "WARNING");
			return null;
		}
		
		plugin.debug("Getting item type and data value from '" + sectionName + "'...", "INFO");
		String[] itemData = itemSection.getString("id").split(":");
		if (itemData == null) {
			plugin.debug("Item ID for " + sectionName + " not found! Is 'id: [ID #]' set in 'items.yml'?", "WARNING");
			return null;
		}
		
		Material itemType = Material.getMaterial(Integer.parseInt(itemData[0]));
		if (itemType == null) {
			plugin.debug("Could not match specified item type ('" + itemData[0] + "') to Minecraft item!", "WARNING");
			return null;
		}
		
		short dataValue = ((itemData.length > 1) ? Short.parseShort(itemData[1]) : 0);
		plugin.debug("Using item " + itemData[0] + ":" + dataValue, "INFO");
		
		ItemStack newItem = new ItemStack(itemType, itemAmount, dataValue);
		ItemMeta itemMeta = newItem.getItemMeta();
		String itemDisplayName = itemSection.getString("name");
		List<String> itemLore = itemSection.getStringList("lore");
		if (itemDisplayName == null || itemLore.isEmpty()) {
			plugin.debug("Could not find custom name and/or lore for " + sectionName + "! Is it set in 'items.yml'?", "WARNING");
			return null;
		}
		itemMeta.setDisplayName(itemDisplayName);
		itemMeta.setLore(itemLore);
		newItem.setItemMeta(itemMeta);
		plugin.debug("ItemStack for " + itemDisplayName + " created.", "INFO");
		return newItem;
	}
	
	ConfigurationSection getKillItemSection(String itemID, String itemDisplayName, List<String> itemLore) {
		plugin.debug("Searching for information of '" + itemDisplayName + "' in 'items.yml'...", "INFO");
		for (String sectionName : killItemSectionNames) {
			plugin.debug("Matching '" + itemDisplayName + "' against section " + sectionName + "...", "INFO");
			ConfigurationSection itemSection = itemConfig.getConfigurationSection(sectionName);
			if (itemSection == null) {
				plugin.debug("Could not find section '" + sectionName + " in 'items.yml'!", "WARNING");
				return null;
			}
			if (itemSection.getString("id").equals(itemID)
			  && itemSection.getString("name").equals(itemDisplayName)
			  && itemSection.getStringList("lore").equals(itemLore)) {
				plugin.debug("Match found for " + itemDisplayName + " (id = " + itemID +")!", "INFO");
				return itemSection;
			}
		}
		return null;
	}

	String getItemsList(Set<String> itemSectionNames, FileConfiguration config) {
		plugin.debug("Creating list of Instakill items", "INFO");
		String itemsList = ChatColor.GRAY + "";
		int i = 0;
		for (String itemPath : itemSectionNames) {
			i++;
			String itemDisplayName = config.getString(itemPath + ".name");
			String itemEntry = i + ". " + ChatColor.GOLD + itemPath + ChatColor.GRAY + " - " + itemDisplayName + "\n";
			itemsList += itemEntry;
			plugin.debug("Adding to list: '" + itemDisplayName + "' from '" + itemPath + "' in 'items.yml'", "INFO");
		}
		plugin.debug("List of Instakill items created, with " + i + " entries", "INFO");
		return itemsList;
	}
	
	Set<String> allowedKillItemsFor(Player player) {
		if (player == null) {
			return killItemSectionNames;
		}
		else {
			String playerName = player.getName();
			Set<String> allowedItems = new HashSet<String>();
			for (String itemPath : killItemSectionNames) {
				if (player.hasPermission("instakill.use." + itemPath)) {
					allowedItems.add(itemPath);
					plugin.debug("PLAYER " + playerName + ": Has permission instakill.use." + itemPath, "INFO");
				}
			}
			return allowedItems;
		}
	}
	
	void updateFromConfig(FileConfiguration newConfig) {
		plugin.debug("Updating Instakill item information...", "INFO");
		itemConfig = newConfig;
		killItemSectionNames = itemConfig.getKeys(false);
		killItemIDs = updateKillItemIDs(killItemSectionNames);
		plugin.debug("Instakill items information updated.", "INFO");
	}
	
	FileConfiguration getItemConfig() {
		return itemConfig;
	}
}
