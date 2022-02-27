Improved Mobs 1.10.3
================
- TenshiLib 1.6.0 compat
- Fabric: You will need to update your configs.  
  A backup of the old config will be created
- Fix player difficulty reset on death

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