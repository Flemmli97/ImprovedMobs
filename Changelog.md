Improved Mobs 1.16.0
================
- Update block breaking AI + path finding. This should also make mobs dig in a more sensible way towards their target
- Mobs now can use wind charges
- Add supplementary bombs compat
- Add TaCZ guns compat
- Update item weight and quality calculation. If you want the updated weights you need to delete your current equipment.json
- Allow tags to be used in equipment.json
- Update the trigger for when mobs start flying on mounts
- Add dynamic tags. These tags are not defined via json but are created at runtime which allows better automatic entity groupings
  - `improvedmobs:hostile_mobs`: Contains all hostile mobs
  - `improvedmobs:non_hostile_mobs`: Contains all non hostile mobs
  - `improvedmobs:neutral_mobs`: Contains all neutral mobs
  - `improvedmobs:animal_mobs`: Contains all animals
- Fix some crashes with item use AI
- Trident AI now use the held trident item (which more or less buffs it)
- Improve floating point errors with difficulty which should fix it being stuck in some cases
- Fix idle block breaking not working

Improved Mobs 1.15.2
================
- Fix mob specific config empty feature set not working
- Support tags for targeting mob in auto target config
- Fix wrong config paths on fabric which also fixes equipment.json missing

Improved Mobs 1.15.1
================
- Fix fabric attachment not registered
- Update lib

Improved Mobs 1.15.0
================
- Add support for difficulty range config. See updated comment for the difficulty increase config
- Rewrite difficulty to use doubles instead of floats
- Rewrite Item AI handling
- Fix crossbow ai not working
- Fix projectile and explosion damage modifier
- Reduce size variation

Improved Mobs 1.14.2
================
- Hotfix: Fix nullpointer with config (for real this time...)

Improved Mobs 1.14.1
================
- Hotfix: Fix nullpointer with config

Improved Mobs 1.14.0
================
- Big config overhaul. Do note that old config will need to be updated!
  - Comments should be clearer now too
  - Various configs now are able to use **math expression** to allow more customization
    - While the config will tell you if expressions are supported an indicator also is that values are strings instead of numbers
    - Usable variables for expression contains
      - `difficulty`: The current difficulty
      - `distance_spawn` : The distance to spawn
      - `distance_origin`: The distance to [0,0,0]
      - `distance_center`: The distance to the position defined in the config
    - For usable functions see https://github.com/Flemmli97/TenshiLib/wiki/Math-Expressions
  - Attributes config part is now moved to datapack.
    - Found under `data/improvedmobs/config/attributes.json`
    - Allows modification of arbitrary attributes now
  - Fabric now requires forge config api port
  - Add a way to override various properties for entities via datapack for example
    - Overriding enabled features defined in the config
    - Entity based difficulty attribute modifications
    - Entity based block break list
    - Check out the wiki how to use
  - Changes:
    - `Punish Time Skip` -> `Consider Time Skip`
    - Flags are now called Features. Should be more clear what they mean
    - Following above `Flag Blacklist` -> `Feature Blacklist`
    - All individual flag whitelist are now grouped into one config: `Feature Whitelist`
    - Auto target config can now accept tags
  - Added equipment drop chance config. `Should drop equipment` removed
- Add `improvedmobs:no_steal` block tag in addition to config
- Overhaul pathfinding. Should be more correct now
- Redid equipment config default weights/quality. The old config will still work!
- Add powerscale integration (replaces scaling health)
- Add runecraftory integration
- Fix mobs not climbing ladders
- Fix pause state not saved on players
- Fix LevelZ conflict

Improved Mobs 1.13.5
================
- Fix tnt destroy blocks config not working
- Fabric: Fix mixin crash due to missing impl

Improved Mobs 1.13.4
================
- Fix some packet stuff causing server to be unjoinable on fabric
- Forge: Fix difficulty impl not registered so difficulty was always 0
- Wrap repair ingredient calc...
- Fix flag config initialization not saved to file
- Add PLAYERSUM difficulty:
  - Sum of all nearby players
  - This can overshoot the theoretical max value (e.g. default 250) as this the max not hardcoded
- Add `simulate` and `pause/unpause` command:
  - `simulate` Simulates the given difficulty step increases. 
  - `pause/unpause`: (Un)Pauses difficulty increase for the given target
- Add vanilla regional clamped difficulty. Useful for e.g. `Majrusz's Progressive Difficulty` which uses it

Improved Mobs 1.13.3.b
================
- Fix packet on fabric
- Fix playerex integration

Improved Mobs 1.13.3
================
- Update PlayerEX integration

Improved Mobs 1.13.2
================
- Fix some problems with list config on forge causing config resets
- Fix projectile/explosion damage increase not applying to player on fabric
- Fix Flag config not updating correctly after config modification with tags
- Fix crash with epic fight
- Fix some stuff with flying and water mounts
- Fix crash with underwater block breaking
- Fix packets not being optional

Improved Mobs 1.13.1
================
- Fix crash with mining speed attribute for block breaking

Improved Mobs 1.13.0
================
- Port to 1.21.1
- Redo integration mod for difficulty:
  - Config changed from true/false to OFF/ON/ADD
  - OFF and ON behave as old true/false but ADD allows stacking on top of existing ones for even more mayham
  - LevelZ and PlayerEX now has a multiplier config
- Allow pehkui integration to be more configurable with size chance and flag for individual mobs
- Disable overlay being shown with f3 debug toggled
- Allow negative values for difficulty increase. To use that use `|` as a delimiter instead of `-`
- Add config for line of sight requirements
- Add breaking speed modifier config
- Add armor trim chance

Improved Mobs 1.12.4.b
================
- Fabric: Fix client packet registering on server

Improved Mobs 1.12.4
================
- Update to 1.20.6
- `equipment.json` got rewritten! If you have the old version a backup will be created.  
~~- Modify target goal instead of remove and replace which should fix anconcurrent exception~~

Improved Mobs 1.12.3
================
- Add `Ignore Harvest Check` to allow breaker mobs to break blocks regardless of the tool
- Add `TNT Block Destruction`. If true tnt thrown by mobs will destroy blocks
- Redo the calculation of default equipment weights (Existing configs will not be changed)

Improved Mobs 1.12.2
================
- Improve mobs seeing through transparent blocks:
  - Only do the check during targeting which fixes problems with e.g. skeletons shooting at glass

Improved Mobs 1.12.1
================
- Fix centerPos config on forge

Improved Mobs 1.12.0
================
- Redid various configs!
- Entity flag list now accepts tags. Prefix them with #
- Tags in block break list need to be prefixed with a #. Old configs not using # for tags will not work anymore
- Add increase per block for distance based difficulty.
  You can add a triple x-y-z in `Difficulty Increase` instead where z is the increase per block.
- Add block tags which mobs can see through `improvedmobs:see_through`:
  Default include glass and leaves
- Serverside entities are handled on creation instead of load now which might fix some stuff

Improved Mobs 1.11.7
================
- New config `Restore delay` allowing broken blocks to be restored after certain time.  
  Default is 0 (disabled).  
  Block entity data will not be restored (e.g. chests will spill their contents on being broken 
  but the content will not be restored)  
- Add option to ignore mobs from spawner from being affected by this mod
- Fix enchanting not working
- Potentially fixing concurrent exception with ridden mobs
- Fix line of sight check reversed when using neutral aggressive check
- Fix block breaking progress not working properly
- Fix a nullpointer with custom pathing
- Use a custom water navigation for the guardian mount:
  Fixes an edge case where if a zombie converts to drowned while riding the guardian mount it will crash the game

Improved Mobs 1.11.6
================
- Fix tenshilib dependency crash
- Add pehkui support. disabled by default
- Add enchantment blacklist for equipment
- Fix configured mod list configs not working

Improved Mobs 1.11.5
================
- `DISTANCE` and `DISTANCESPAWN` difficulty type now updating the difficulty bar on clients

Improved Mobs 1.11.4
================
- Add `DISTANCE` and `DISTANCESPAWN` difficulty type.  
  Both values change how `Difficulty Increase` config is handled:  
  For a value pair of "x-y" x is the min distance y applies and y is the difficulty at the distance.  
  If difficulty type is `DISTANCE` then `Center Position` is the reference point for the distance, else
  the reference point is the world spawn.
- Add `Steal Block Blacklist` to blacklist blocks from being stolen from

Improved Mobs 1.11.3
================
- Port 1.20.1  
~~- Fix Boundingbox for flying summons not calculated correctly~~
~~- Fix flying ai checking for target pos instead of path end pos~~

Improved Mobs 1.11.2
================
- Fix mobs in some cases riding phantoms despite no benefit
- Add tag `improved:armor_equippable`. Adding mobs to this tag allows the player to give them armor similar 
  to giving pets armor.
- New config value: `Explosion Damage Increase` and `Max Explosion Damage`.
  So explosion damage are scaled with difficulty now. Default max multiplier is a 75% damage increase
  achieved at 250 difficulty

Improved Mobs 1.11.1
================
- Fix projectile dmg multiplier sometimes being 0
- Fix some attribute config comments being wrong
- Nullcheck for goal modification
- Fix 2 configs having the same name and causing issues:
  The config for the USEITEM flag whitelist has been renamed to `Use Flag Whitelist`
- Fix difficulty sometimes stuck with high playtime (hopefully)

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
