# Advanced Networking
[![Badge showing the amount of downloads on modrinth](https://img.shields.io/badge/dynamic/json?color=2d2d2d&colorA=5da545&label=&suffix=%20downloads%20&query=downloads&url=https://api.modrinth.com/api/v1/mod/IF0Y4xFw&style=flat-square&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxNCIgdmlld0JveD0iMCAwIDUxMiA1MTQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+ICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGNsaXAtcnVsZT0iZXZlbm9kZCIgZD0iTTUwMy4xNiAzMjMuNTZDNTE0LjU1IDI4MS40NyA1MTUuMzIgMjM1LjkxIDUwMy4yIDE5MC43NkM0NjYuNTcgNTQuMjI5OSAzMjYuMDQgLTI2LjgwMDEgMTg5LjMzIDkuNzc5OTFDODMuODEwMSAzOC4wMTk5IDExLjM4OTkgMTI4LjA3IDAuNjg5OTQxIDIzMC40N0g0My45OUM1NC4yOSAxNDcuMzMgMTEzLjc0IDc0LjcyOTggMTk5Ljc1IDUxLjcwOThDMzA2LjA1IDIzLjI1OTggNDE1LjEzIDgwLjY2OTkgNDUzLjE3IDE4MS4zOEw0MTEuMDMgMTkyLjY1QzM5MS42NCAxNDUuOCAzNTIuNTcgMTExLjQ1IDMwNi4zIDk2LjgxOThMMjk4LjU2IDE0MC42NkMzMzUuMDkgMTU0LjEzIDM2NC43MiAxODQuNSAzNzUuNTYgMjI0LjkxQzM5MS4zNiAyODMuOCAzNjEuOTQgMzQ0LjE0IDMwOC41NiAzNjkuMTdMMzIwLjA5IDQxMi4xNkMzOTAuMjUgMzgzLjIxIDQzMi40IDMxMC4zIDQyMi40MyAyMzUuMTRMNDY0LjQxIDIyMy45MUM0NjguOTEgMjUyLjYyIDQ2Ny4zNSAyODEuMTYgNDYwLjU1IDMwOC4wN0w1MDMuMTYgMzIzLjU2WiIgZmlsbD0idmFyKC0tY29sb3ItYnJhbmQpIi8+ICA8cGF0aCBkPSJNMzIxLjk5IDUwNC4yMkMxODUuMjcgNTQwLjggNDQuNzUwMSA0NTkuNzcgOC4xMTAxMSAzMjMuMjRDMy44NDAxMSAzMDcuMzEgMS4xNyAyOTEuMzMgMCAyNzUuNDZINDMuMjdDNDQuMzYgMjg3LjM3IDQ2LjQ2OTkgMjk5LjM1IDQ5LjY3OTkgMzExLjI5QzUzLjAzOTkgMzIzLjggNTcuNDUgMzM1Ljc1IDYyLjc5IDM0Ny4wN0wxMDEuMzggMzIzLjkyQzk4LjEyOTkgMzE2LjQyIDk1LjM5IDMwOC42IDkzLjIxIDMwMC40N0M2OS4xNyAyMTAuODcgMTIyLjQxIDExOC43NyAyMTIuMTMgOTQuNzYwMUMyMjkuMTMgOTAuMjEwMSAyNDYuMjMgODguNDQwMSAyNjIuOTMgODkuMTUwMUwyNTUuMTkgMTMzQzI0NC43MyAxMzMuMDUgMjM0LjExIDEzNC40MiAyMjMuNTMgMTM3LjI1QzE1Ny4zMSAxNTQuOTggMTE4LjAxIDIyMi45NSAxMzUuNzUgMjg5LjA5QzEzNi44NSAyOTMuMTYgMTM4LjEzIDI5Ny4xMyAxMzkuNTkgMzAwLjk5TDE4OC45NCAyNzEuMzhMMTc0LjA3IDIzMS45NUwyMjAuNjcgMTg0LjA4TDI3OS41NyAxNzEuMzlMMjk2LjYyIDE5Mi4zOEwyNjkuNDcgMjE5Ljg4TDI0NS43OSAyMjcuMzNMMjI4Ljg3IDI0NC43MkwyMzcuMTYgMjY3Ljc5QzIzNy4xNiAyNjcuNzkgMjUzLjk1IDI4NS42MyAyNTMuOTggMjg1LjY0TDI3Ny43IDI3OS4zM0wyOTQuNTggMjYwLjc5TDMzMS40NCAyNDkuMTJMMzQyLjQyIDI3My44MkwzMDQuMzkgMzIwLjQ1TDI0MC42NiAzNDAuNjNMMjEyLjA4IDMwOC44MUwxNjIuMjYgMzM4LjdDMTg3LjggMzY3Ljc4IDIyNi4yIDM4My45MyAyNjYuMDEgMzgwLjU2TDI3Ny41NCA0MjMuNTVDMjE4LjEzIDQzMS40MSAxNjAuMSA0MDYuODIgMTI0LjA1IDM2MS42NEw4NS42Mzk5IDM4NC42OEMxMzYuMjUgNDUxLjE3IDIyMy44NCA0ODQuMTEgMzA5LjYxIDQ2MS4xNkMzNzEuMzUgNDQ0LjY0IDQxOS40IDQwMi41NiA0NDUuNDIgMzQ5LjM4TDQ4OC4wNiAzNjQuODhDNDU3LjE3IDQzMS4xNiAzOTguMjIgNDgzLjgyIDMyMS45OSA1MDQuMjJaIiBmaWxsPSJ2YXIoLS1jb2xvci1icmFuZCkiLz48L3N2Zz4=)](https://modrinth.com/mod/advanced-networking)
[![Badge showing the amount of downloads on curseforge](https://cf.way2muchnoise.eu/full_689404_downloads.svg?badge_style=flat)](https://www.curseforge.com/minecraft/mc-mods/advanced-networking)
[![Badge linking to issues on github](https://img.shields.io/badge/dynamic/json?query=value&url=https%3A%2F%2Fimg.shields.io%2Fgithub%2Fissues-raw%2Fmattidragon%2Fadvancednetworking.json&label=&logo=github&color=2d2d2d&style=flat-square&labelColor=6e5494&logoColor=2d2d2d&suffix=%20issues)](https://github.com/MattiDragon/AdvancedNetworking/issues)
[![Badge linking to support on discord](https://img.shields.io/discord/760524772189798431?label=&logo=discord&color=2d2d2d&style=flat-square&labelColor=5865f2&logoColor=2d2d2d)](https://discord.gg/twcHYGURda)

> ⚠️This mod is currently in beta. As of right now I have implemented enough to start processing redstone signals; controllers that house your logic and interfaces that can interact with the world.

A mod that adds systems for transfer of resources and data using a node based programming system. Originally made for Modfest Singularity.

## Getting Started
To get started craft some crystalline compound with an amethyst shard, a clay ball and a piece of coal.
Using that you can craft cables and controllers. Click on a cable with a stick or any wrench from other mods to change its mode.
Open up the controller, add some nodes and connect them. Click on an interface with an empty hand to get its id.
Configure your redstone nodes to use that id within the controller, and you should have a working network.

## Features
### Cables
Cables are a core part of the mod, they are used to connect controllers to everything they need. 

### Controllers
Controllers house your programs. They have a node based UI where you can perform routing of resources and all kinds of logic.

## Upcoming and Potential Features
Here are some features that are **not** in mod yet. Some may be added others may not. Features
outside this list could also be added.

* **Energy, item and fluid transfer**
    * Will require a bit more work than redstone, but a lot of the infra is already there
* **Costs**
    * The mod is kinda OP right now.

## Other Info
### Modpack permission
You can use this mod in any modpack as long as you don't reupload the mod. You can get a direct download link from modrinth or github.

### Other websites
This mod is only officially available on [curseforge](https://www.curseforge.com/minecraft/mc-mods/advanced-networking), [modrinth](https://modrinth.com/mod/advanced-networking) and [github](https://github.com/mattidragon/advancednetworking). 
Any other sites are third-party reuploads and should not be trusted.

### Version support
I only support the latest and mostly modded version of minecraft. Old versions may receive important patches, but I will not be backporting the mod, however, I might consider backport PRs.

### Incompatibilities
I intend to try and stay compatible with as many mods as possible, but might abandon support for some if it becomes too hard.
* **Sodium** compatibility is only guaranteed if [indium](https://modrinth.com/mod/indium) is installed.
* **Optifine** will never be officially supported. Might work, might not.

## Licencing
The mod is1 licensed under the Apache License, Version 2.0. 
I chose to switch because I want to retain ownership of my code while still allowing forks and addons to use any license they want.

You are free to use the mods code in any way you want as long as you follow the license and credit me for the original (link is enough).