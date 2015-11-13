# Instakill
### *A fun small plugin for creating lethal items*
Version 1.0 compatibable with Bukkit 1.8+

## About

Instakill is a simple plugin that allows you to create custom items that will kill any nearby mobs within a certain radius when right-clicked.

## Installing

1. **Download** the latest Jar and place it in your server's plugin folder.
2. **Restart** your server to generate a default configuration file in `.../Instakill/config.yml`.
3. **Customize** the configuration's item ID, name, and lore, and kill options (See [Setting up](#setting-up)).

## How to use

### Obtaining an Instakill item

Any item created normally or using commands in-game that matches the config options will become a usable instakill item. 

Alternatively, you can give yourself or others one instantly with `/ik give`.

### Using the item's kill effects

Right-click anywhere in-game while holding your item to instantly kill all mobs near you (in a specified radius).

## Commands

All commands can also be executed with **/instakill**.

* **/ik give (username) (amount)** - Instally gives the player one or more Instakill item. Defaults to OP only. 

## Permissions

* **instakill.use** - Allows you to right-click with an Instakill item to kill mobs.
* **instakill.give** - Allows you to use `/ik give` for yourself or other players.

## Setting up

In order to create a non-default Instakill item, you must set it up within the `config.yml`. After editing and saving the file, either restart the server or type `/ik reload` to update the plugin's settings.

After setting up the item in the configuration file, give those who you wish to be able to use the item the `instakill.use` permission.

### Config.yml

```
item-id: 368                # ID number of instakill item
item-name: Lethal Pearl     # Custom name to display for item
item-lore:                  # Custom lore/description of item
  - Kills all nearby mobs
kill-radius: 25             # Kill all mobs within this radius of blocks
mob-drops: true             # True or false. Should mobs drop items when instakilled?
consume-item: true          # True or false. Should the item disappear after one use, or stay in the inventory?
```




\[[Source Code](https://github.com/Andorem/NotifyUser)\]
