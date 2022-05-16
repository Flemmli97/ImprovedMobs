Improved Mobs 1.10.9
================
- Fix potential nullpointer in pathfinding
- Remove duplicate target ai regarding villagers if mob already targets villagers

Improved Mobs 1.10.7
================
- Move item name validation for equipment.json so it doesn't mess up the loading  
  Fixes the list sometimes not working
- Fix mobs not breaking blocks

Improved Mobs 1.10.6
================
- Fix crash when player data is not present usually caused by death

Improved Mobs 1.10.3
================
- Fix player difficulty reset on death
- Prevent piglins holding offhand items cause since it clashes with bartering they throw it out immediately
- Fix dividing through 0 when using PLAYERMEAN and no players around  
  Noticable with structure mobs
- Increase the player check range to 256 (doubt any modded server uses 16 render distance)

Improved Mobs 1.10.1
================
- Fix lithium incompability

Improved Mobs 1.10.0
================
- Improve vanilla pathfinding performance  
  Most noticable with big (flying) mobs (though vanilla doesnt have them)  
  This feature might become a separate mod in the future since it doesnt really fit in here

Improved Mobs 1.9.0b
================
- Legacy scaling health support.  
  Cause the more up to date one conflicts with some mods

Improved Mobs 1.9.0
================
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
- Improved pathfinding of mobs able to break blocks
- Renamed SWIMMRIDE flag to GUARDIAN
- Parrots: new flag PARROT.
  Mobs can start riding a parrot to reach normally unreachable places

Improved Mobs 1.7.10
================
- Fix a freeze during world creating if scaling health is installed
- Fix pillagers keep only shielding and do nothing else
- Axes now disable shields just like for players

Improved Mobs 1.7.9
================
- Fix Flag Blacklist not working

Improved Mobs 1.7.8
================
- Made pathfinding a bit better again
- Add "Entity Item Use Blacklist" to blacklist mob specific items.
  Made so e.g. skeletons dont use bow items since they already can use it.
  Previously was a hardcoded check.
  By default excludes bow use from vanilla skeletons, drowned trident use, pillager + piglins crossbow use and snowman snowball use
- Fix guardian crash with ice and fire mobs. They still dont go after mobs but thats another thing
- Moved the config default value init phase to World.Load so its not using empty configs for
  entities when the config is newly created

Improved Mobs 1.7.7
================
- Rewrote the item using ai registration a bit. Dont need all that stuff with classes anymore
- Add a blacklist for items which should never be used (not equipped. thats what the equipment.json is for)
    - Add the buckler item from big brains mod to the default list.
- Update scaling health dependency.
- Fix difficulty not using the config values. (very strange since i am sure i wrote the code already but it vanished...)
- Changed the block breaking ai to start from the top
- Also added config to tweak the delay between each block breaking attempt

Improved Mobs 1.7.6
================
- Put the default entity list init in try catch since it can be unstable
- Add weighted breaking tools (#55)
- Fix mobs unable to ride guardians past forge 35.1.29 (#60)
- Add a finer option to control the difficulty ("Difficulty Increase" in config)
- Probably fixed mobs glitching through walls when trying to ride guardians.
  Also made them dismount after a while if not targeting other mobs.

Improved Mobs 1.7.5
================
- fix #66

Improved Mobs 1.7.4
================
- Fix crash with item ai. fixes #51, #59, #62
- Add back scaling health integration #57
- Option to toggle the difficulty display
- Rewrite pathfinding. fixes #50, #53, #63

Improved Mobs 1.7.3
================
- Exclude slimes from water riding fix #44. Slimes dont attack anyway if they ride non slime entitys

Improved Mobs 1.7.2
================
- add prevention if entity with no hitbox try to break a block causing crashes (cause thats appearently a thing)

Improved Mobs 1.7.1
================
- Make forge complain if the dependency is missing

Improved Mobs 1.7.0
================
- Port to 1.16.3