# Advanced Networking
[![Badge showing the amount of downloads on modrinth](https://img.shields.io/badge/dynamic/json?color=2d2d2d&colorA=5da545&label=&suffix=%20downloads%20&query=downloads&url=https://api.modrinth.com/v2/project/IF0Y4xFw&style=flat-square&logo=modrinth&logoColor=2d2d2d)](https://modrinth.com/mod/advanced-networking)
[![Badge showing the amount of downloads on curseforge](https://cf.way2muchnoise.eu/full_689404_downloads.svg?badge_style=flat)](https://www.curseforge.com/minecraft/mc-mods/advanced-networking)
[![Badge linking to issues on github](https://img.shields.io/badge/dynamic/json?query=value&url=https%3A%2F%2Fimg.shields.io%2Fgithub%2Fissues-raw%2Fmattidragon%2Fadvancednetworking.json&label=&logo=github&color=2d2d2d&style=flat-square&labelColor=6e5494&logoColor=2d2d2d&suffix=%20issues)](https://github.com/MattiDragon/AdvancedNetworking/issues)
[![Badge linking to support on discord](https://img.shields.io/discord/760524772189798431?label=&logo=discord&color=2d2d2d&style=flat-square&labelColor=5865f2&logoColor=2d2d2d)](https://discord.gg/26T5KK2PBv)

A mod that adds systems for transfer of resources and data using a node based programming system. While inspired by [Integrated Dynamics](https://www.curseforge.com/minecraft/mc-mods/integrated-dynamics) and [XNet](https://www.curseforge.com/minecraft/mc-mods/xnet), 
this mod makes sure to stay distinct and get its own place in modpacks (even though neither of the aforementioned mods are on fabric). Originally made for Modfest Singularity.

## Getting Started 
To get started craft some crystalline compound, with an amethyst shard, a clay ball and a piece of coal.
Using that you can craft cables and controllers. Sneak-click on a cable with an empty hand to open its configuration.
There you can set the mode of each side of the cable. Make one side an interface and give it a name.
Open up the controller, add some nodes and connect them. 
Select your interface in the configuration of a node and you should have a working network.

<img src="https://github.com/MattiDragon/AdvancedNetworking/raw/1.19.3/.github/media/room.png" width="50%"/>

## Features
### Cables
Cables are a core part of the mod, they are used to connect controllers to everything they need. 
Click on them with a stick or any wrench from other mods to quickly change the mode of a side.
More advanced configuration can be accessed by sneak-clicking on the cable.
Cables automatically connect to each other and controllers, but not other blocks. For those you will need to set the cable to interface mode.

<img src="https://github.com/MattiDragon/AdvancedNetworking/raw/1.19.3/.github/media/cables.png" width="50%"/>

### Controllers
Controllers house your programs. They have a node based UI where you can perform routing of resources and all kinds of logic.
You can add nodes from the `Add Nodes` menu and it's submenus. 
Deleting nodes can be done by right-clicking and selecting `Delete` or using the `Delete Nodes` mode.
Nodes can also be duplicated from the right-clicking menu. You can freely move around by dragging and zoom by scrolling in the editor. 

<img src="https://github.com/MattiDragon/AdvancedNetworking/raw/1.19.3/.github/media/right_click_menu.png" width="50%"/>

### Resource Transfer and Streams
Some of the most important nodes are for transfer of energy, items and fluids. 
They work on with a stream system where the nodes control set the flow of resources and the actual transfer happens after all nodes have evaluated.
There are also nodes for getting the capacity and fill level of storages. These are evaluated before any transfer happens.
The filter and limit nodes allows you to control what items are transferred. 
The limit node sets a maximum number of items to transfer and the filter blocks certain items from passing through.

<img src="https://github.com/MattiDragon/AdvancedNetworking/raw/1.19.3/.github/media/fluids.png" width="50%"/>

## Other Info
### Modpack permission
You can use this mod in any modpack as long as you don't reupload the mod. You can get a direct download link from modrinth or github.

### Downloading
This mod is only officially available on [curseforge](https://www.curseforge.com/minecraft/mc-mods/advanced-networking), [modrinth](https://modrinth.com/mod/advanced-networking) and [github](https://github.com/mattidragon/advancednetworking). 
Any other sites are third-party reuploads and should not be trusted.

### Version support
I only support the versions of minecraft I'm interested in modding. Old versions may receive important patches, but I will not be backporting the mod. I might consider PRs porting the mod to different versions.

### Incompatibilities
I intend to try and stay compatible with as many mods as possible, but might abandon support for some if it becomes too hard.
* **Sodium** compatibility is only guaranteed if [indium](https://modrinth.com/mod/indium) is installed.
* **Optifine** will never be officially supported. Might work, might not.

### Forge?
No, I will not port to forge and probably won't accept any port as official. I can't and won't try to stop unofficial ports due to my license so go on if you want. 

## Licencing
The mod is licensed under the Apache License, Version 2.0. 

You are free to use the mods code in any way you want as long as you follow the license and credit me for the original (link is enough).