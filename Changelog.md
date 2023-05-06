Improved Mobs 1.11.0
================
- Update to 1.19.4
<i>
  - The water/flying mounts (aka Guardians and Parrots) are now server sided entities.  
    Allows for more flexibility like e.g. make it so mobs riding them cant suffocate in ceilings/walls.  
    On the client they are displayed as Guardians and Phantoms
  - Add config option for if mobs should break blocks when idle
  - ./improvedmobs command now prints the current difficulty to chat
  - **-->Config names got changed!<--** some ambiguity cleared. Older configs will get overwritten
  - Added playerEx and LevelZ support: Will use player levels from the mod instead of the difficulty from this mod
    Note: The GLOBAL "Difficulty type" config will then instead be the same as PLAYERMEAN
  </i>

Improved Mobs 1.10.16
================
- Use entities random instead of the levels which should fix random crashes with it

Improved Mobs 1.10.15
================
- Fix projectiles doing no dmg if projectile increase is disabled

Improved Mobs 1.10.14
================
- Fix adding blocks to breaklist not working
- Update to 1.19

Improved Mobs 1.10.13
================
- Fix projectile dmg mult not working. This time for real
- Make parrot riding ai better
- Config Difficulty location:  
  Allow difficulty bar to be placed relative to screen

Improved Mobs 1.10.12
================
- Fix unlimited max value for projectile dmg bonus not working
- Add guardian ai chance config. So not every mob can ride a guardian

Improved Mobs 1.10.11
================
- Fix unlimited max value for attribute not working

Improved Mobs 1.10.10
================
- Forge: Fixing problems with capability loading on player clone

Improved Mobs 1.10.9
================
- Fix potential nullpointer in pathfinding

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