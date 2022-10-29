# List of Features

Below list contains all features which Eagle Factions.

## Storage

- HOCON (Recommended)
- H2
- MySQL
- MariaDB

## Gameplay Features

- Allies and Enemies
- Factions' chests
- Territory Claiming (Protection)
- Factions Map. It is a command that prints a simple map in the chat for you. You can also claim chunks from it.
- Eagle Feather - A command only item that can be used to open a container (chest) in enemy territory. In summary, it let's you bypass protection system.
As a server admin, you can sell this item to normal players.
- Attacking other factions (at this moment, only simple territory unclaiming.
- PVPLogger
- Config file lets you specify if people can use /f attack only at night.
- Config file lets you specify if people should be able to enter territory of faction that members are offline/not in the game.
- Config file lets you toggle friendly fire between alliances.

## Power System

- Every player has some amount of power
- Power can be used to claim territories (chunks)
- Attack enemy factions. If your power is higher than enemy power then you can unclaim their territory.

##### Changeable from file

- Start power
- Max Power
- Power increment per minute
- Power award for kill
- Power penalty for teamkill
- Power penalty for death

## Chat

- Faction's tags or Faction's names can be displayed before player's nickname in the chat.
- Faction's tags can be coloured. This can be changed in the config file. 
If colored tags are not enabled then enemy factions get red tag, allies get blue tag and player's .
- Players can switch to alliance chat or internal faction chat
- Config file lets you specify if after switching to internal chat players will only see messages from that chat. 
That means, messages from other players will not be visible if you switched to faction chat.
- Config file lets you turn off factions' chests.

# Home

- Faction's home can be set by faction's leader and officers
- Config file lets you change how often player can teleport to faction's home and how much time player need to wait before being teleported.
- Config file lets you specify if players should be teleported to faction's home after death.
- Config file lets you specify if players can teleport to faction's home from different world.

## Protection System

- SafeZone protects blocks from being destroyed by players and mobs. It also protects players from receiving damage.
- WarZone protects blocks from being destroyed but allows players to attack each other.
- Faction's claims protects territory from other (enemy) players.
- Config file lets you specify which blocks and items should be ignored by Protection System.
- Multiple worlds are also supported. You can mark one world as a SafeZone world and another as a WarZone world and so on.

## Messages

- Almost every message that comes from the plugin can be edited and replaced with your own words.
- Translations are also available.

## Placeholders

- If you install PlaceholdersAPI on your server, you will be able to use plenty of placeholders from Eagle Factions. Check out the wiki to see which placeholders are available.

## Integrations

- Dynmap (shows factions' claims and homes)
- Bluemap (shows factions' claims)