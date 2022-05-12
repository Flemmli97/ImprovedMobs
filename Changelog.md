Improved Mobs 1.10.8
================
- Remove duplicate target ai regarding villagers if mob already targets villagers
- Add option to define default flags for entities used in config initialization
  Useful for other mods. Not really for endusers

Improved Mobs 1.10.7
================
- Move item name validation for equipment.json so it doesn't mess up the loading  
  Fixes the list sometimes not working

Improved Mobs 1.10.6
================
- Fabric: Mod menu config support
- Fix crash when player data is not present usually caused by death

Improved Mobs 1.10.5
================
- Fix config issues on fabric
- Change some config comments
- Fix default config of increase handler being faulty

Improved Mobs 1.10.4
================
- 1.18.2

Improved Mobs 1.10.3
================
- TenshiLib 1.6.0 compat
- Fabric: You will need to update your configs.  
  A backup of the old config will be created
- Fix player difficulty reset on death
- Prevent piglins holding offhand items cause since it clashes with bartering they throw it out immediately
- Fix dividing through 0 when using PLAYERMEAN and no players around  
  Noticable with structure mobs
- Increase the player check range to 256 (doubt any modded server uses 16 render distance)

Improved Mobs 1.10.2
================
- Fix difficulty not showing on server

Improved Mobs 1.10.1
================
- Fix lithium incompability

Improved Mobs 1.10.0
================
- Improve vanilla pathfinding performance  
  Most noticable with big (flying) mobs (though vanilla doesnt have them)  
  This feature might become a separate mod in the future since it doesnt really fit in here

Improved Mobs 1.9.0
================
- Internal changes
- Fix capability null crash
- Fix crash with armor ingredients
- New flag NEUTRALAGGRO to finetune mobs that are affected by the neutral aggressive feature
- new DifficultyType config:  
  Defines how the difficulty at a position is calculated. Supported values are:  
  "GLOBAL: Serverwide difficulty value  
  "PLAYERMAX: Maximum difficulty of players in a 128 radius around the position  
  "PLAYERMEAN: Average difficulty of players in a 128 radius around the position  

Improved Mobs 1.8.1
================
- Make parrot ability get triggered less
- Tweak some default equipment weights so diamond/netherite is rarer
- Add quality value to equipment config.  
  Makes it able to specify which equipment will get more  
  likely choosen with higher difficulty

Improved Mobs 1.8.0
================
- Port to 1.18
- Improved pathfinding of mobs able to break blocks
- Renamed SWIMMRIDE flag to GUARDIAN
- Parrots: new flag PARROT.
  Mobs can start riding a parrot to reach normally unreachable places