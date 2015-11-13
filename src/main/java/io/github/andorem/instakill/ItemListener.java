package io.github.andorem.instakill;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
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
	Material killItem;
	int killItemID;
	List<String> killItemLore;
	String killItemName;
	double killRadius;
	boolean mobDropsEnabled;
	boolean isConsumable;

	public ItemListener(Instakill plugin) {
		this.plugin = plugin;
		FileConfiguration config = plugin.getConfig();
		killItemID = config.getInt("item-id");
		killItem = Material.getMaterial(killItemID);
		killItemName = config.getString("item-name");
		killItemLore = config.getStringList("item-lore");
		killRadius =config .getDouble("kill-radius");
		mobDropsEnabled = config.getBoolean("mob-drops");
		isConsumable = config.getBoolean("consume-item");
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("instakill.use")) {
			if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				ItemStack itemInHand = player.getItemInHand();

				if (itemInHand.getType() == killItem && itemInHand.hasItemMeta()) {
					ItemMeta itemMeta = itemInHand.getItemMeta();
					if (itemMeta.getDisplayName().equals(killItemName) && itemMeta.getLore().equals(killItemLore)) {
						List<Entity> nearbyEntities = player.getNearbyEntities(killRadius, killRadius, killRadius);
						for (Entity entity : nearbyEntities) {
							if (entity instanceof LivingEntity && !(entity instanceof Player)) {
								Damageable mob = (Damageable) entity;
								if (!mobDropsEnabled) mob.setHealth(0);
								else mob.damage(99999999);
							}
						}
						if (isConsumable) player.getInventory().remove(itemInHand);
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public ItemStack createKillItem(int itemAmount) {
		ItemStack newItem = new ItemStack(killItem, itemAmount);
		ItemMeta itemMeta = newItem.getItemMeta();
		itemMeta.setDisplayName(killItemName);
		itemMeta.setLore(killItemLore);
		newItem.setItemMeta(itemMeta);
		return newItem;
	}
}
