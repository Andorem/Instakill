# Instakill
### *A fun small plugin for creating lethal items*
Version 1.0 compatibable with Bukkit 1.8+

## About

Instakill is a simple plugin that allows you to create custom items that will kill any nearby mobs within a certain radius when right-clicked. Customize the item name, lore, and whether or not it is single-use and the mobs will drop items.

## Installing

1. **Download** the latest Jar and place it in your server's plugin folder.
2. **Restart** your server to generate default configuration files `config.yml` and `items.yml`.
3. **Customize** the configuration's item ID, name, and lore, and kill options (See [Setting up](#setting-up)).

## How to use

### Obtaining an Instakill item

Any item created manually or using commands in-game that matches any set of custom name, lore, and ID will become a usable instakill item. 

Alternatively, you can give yourself or others one instantly with `/ik give` (Type `/ik list` to see available items).

### Using the item's kill effects

Right-click anywhere in-game while holding your item to instantly kill all mobs near you (in a specified radius).

## Commands

All commands can also be executed with **/instakill**.

* **/ik give [username] \[item name\] (amount)** - Instally gives the player one or more Instakill item. 
* **/ik list** - List all available Instakill items that you can use.
* **/ik reload** - Reload the `config.yml` and `items.yml` configuration files.

## Permissions

* **instakill.use.\<item-name\>** - Allows you to use specific Instakill item by right-clicking.
* **instakill.use.*** - Allows you to use all Instakill items.
* **instakill.give** - Allows you to use the /ik give command.
* **instakill.list** - Allows you to use the /ik list command.
* **instakill.reload** - Allows you to use the /ik reload command.

## Setting up
`config.yml` contains general plugin settings and default values for Instakill items.

In order to create a custom Instakill item, you must set it up within the `items.yml`. After editing and saving the file, either restart the server or type `/ik reload` to update the plugin's settings.

After setting up the item in the configuration file, give those who you wish to be able to use the item the `instakill.use.<item-name>` permission.

### config.yml
```
# General settings and default item options (for specific items, use items.yml)
debug: false       # Sends debug info to console
kill-radius: 20    # Kill all mobs within this radius of blocks
mob-drops: true    # True or false. Should mobs drop items when instakilled?
consume-item: true # True or false. Should the item disappear after one use, or stay in the inventory?
```

### items.yml
```
# To create an Instakill item, give it is own section in item.yml, including:

itemSectionTitle: # Used for /ik give command and permission names. Can be anything.
id: ID # string. You can also use data values (e.g. enchanted golden apple = 322:1).
name: Custom display name
lore:
  - Lore descriptions must be in list format.
  - They can also be multiline.
kill-radius: Optional override of default set in config.yml.
mob-drops: Optional override of default set in config.yml
consume-item: Optional override of default set in config.yml
```

#### Note on item permissions
Each Instakill item has its own individual permission in order to use it. By default, it is set to false.
The permission name will be the custom item section title, no punctuation: "instakill.use.itemsection"
To let a player use all items, give them the permission: "instakill.use.*"



\[[Source Code](https://github.com/Andorem/NotifyUser)\]
