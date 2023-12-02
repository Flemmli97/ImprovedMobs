# Improved Mobs 
[![](http://cf.way2muchnoise.eu/full_282639_Forge_%20.svg)![](http://cf.way2muchnoise.eu/versions/282639.svg)](https://www.curseforge.com/minecraft/mc-mods/improved-mobs)  
[![](http://cf.way2muchnoise.eu/full_552968_Fabric_%20.svg)![](http://cf.way2muchnoise.eu/versions/552968.svg)](https://www.curseforge.com/minecraft/mc-mods/improved-mobs-fabric)  
[![](https://img.shields.io/modrinth/dt/23MovWyi?logo=modrinth&label=Modrinth)![](https://img.shields.io/modrinth/game-versions/23MovWyi?logo=modrinth&label=Latest%20for)](https://modrinth.com/mod/improved-mobs)  
[![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/8Cx26tfWNs)

Improved Mobs mod for minecraft.

Provides challenging tweaks to mobs to make them way harder than usual

To use this mod as a dependency add the following snippet to your build.gradle:  
```groovy
repositories {
    maven {
        name = "Flemmli97"
        url "https://gitlab.com/api/v4/projects/21830712/packages/maven"
    }
}

dependencies {    
    //Fabric/Loom==========    
    modImplementation("io.github.flemmli97:improvedmobs:${minecraft_version}-${mod_version}-${mod_loader}")
    
    //Forge==========    
    compile fg.deobf("io.github.flemmli97:improvedmobs:${minecraft_version}-${mod_version}-${mod_loader}")
}
```