<h1 align="center">Tamable Foxes</h1>
<p align="center">
SpigotMC Plugin that gives you the ability to tame foxes!
</p>

### WARNING: Do not reload the plugin, you may loose foxes!!
#### NOTE: You no longer need to add the program arguments to your start file. If you previously had them, you can delete them, but I don't think it would harm if you leave them in.

### Default configuration files:
* <a href="https://github.com/SeanOMik/TamableFoxes/blob/master/Plugin/src/main/resources/config.yml">config.yml</a>
* <a href="https://github.com/SeanOMik/TamableFoxes/blob/master/Plugin/src/main/resources/language.yml">language.yml</a>
<br>

If you get any errors, <a href="https://github.com/SeanOMik/TamableFoxes/issues/new">create an issue!</a><br><br>

Have you ever wanted to tame foxes? Well, now you can! <b>Use chicken to tame</b> and sweet berries to breed them!<br><br>

## Features:
* 33% Chance of taming
* Breeding
* Wild foxes pick berry bushes
* Leaping on targets
* Tamed foxes sleep when their owner does
* Foxes follow owner
* You can shift + right-click to let the fox hold items
* Right-click to make the fox sit
* Shift Right-click with an empty hand to make the fox sleep
* If the fox is holding a totem of undying, the fox will consume it and be reborn.
* Foxes attack the owner's target
* Foxes attack the thing that attacked the owner.
* Foxes are automatically spawned inside the world. (Same areas as vanilla foxes)
* Foxes attack chickens and rabbits.
* Snow and red foxes.
* Language.yml
* Message when a tamed fox dies
* /givefox command to give foxes to other players.
* Disabling certain gameplay messages
  * You can do this by changing certain fields in `language.yml` to "disabled". The fields that can be disabled are:
    * `taming-tamed-message`
    * `taming-asking-for-name-message`
    * `taming-chosen-name-perfect`
    * `fox-doesnt-trust`

## Commands:
* /spawntamablefox [red/snow]: Spawns a tamable fox at the players' location.
* /tamablefoxes reload: Reloads
* /givefox [player name]: Give a fox to another player.

## Permissions:
* `tamablefoxes.reload`: Reloads the plugin config. Default: `op`
* `tamablefoxes.spawn`: Gives permission to run the command /spawntamablefox. Default: `op`
* `tamablefoxes.tame`: Gives the player the ability to tame a fox. Default: `Everybody`
* `tamablefoxes.tame.unlimited`: Lets players bypass the tame limit. Default: `op`
* `tamablefoxes.tame.anywhere`: Lets players bypass the banned worlds in config.yml (so they can tame in any world). Default: `op`
* `tamablefoxes.givefox.give.others`: Allows the player to give another players fox to a player with /givefox. This will ignore if the other receiving has the `tamablefoxes.givefox.receive` permission. Default: `op`
* `tamablefoxes.givefox.give`: Gives the player the ability to give foxes to other players with /givefox. Default: `Everybody`
* `tamablefoxes.givefox.receive`: Gives the player the ability to receive foxes from other players from /givefox. Default: `Everybody`

<br>

![foxes sleeping](Screenshots/foxes-sleeping-with-player.png)
![foxes sitting player holding sword](Screenshots/foxes-sitting-sword.png)
![foxes with baby looking at player](Screenshots/foxes-baby-looking-at-player.png)
![giving fox totem](Screenshots/giving-fox-item.gif)
![fox leaping towards chicken](Screenshots/fox-pouncing.gif)

## Building
To build you must have several versions of spigot built and inside your maven cache. Here's a list of commands to run to install all of the correct versions:
```
java -jar ./BuildTools.jar --rev 1.14.4
java -jar ./BuildTools.jar --rev 1.15
java -jar ./BuildTools.jar --rev 1.16.1
java -jar ./BuildTools.jar --rev 1.16.3
java -jar ./BuildTools.jar --rev 1.16.5
java -jar ./BuildTools.jar --rev 1.17 --remapped remapped-mojang
java -jar ./BuildTools.jar --rev 1.17.1 --remapped remapped-mojang
java -jar ./BuildTools.jar --rev 1.18 --remapped remapped-mojang
java -jar ./BuildTools.jar --rev 1.18.1 --remapped remapped-mojang
```
Yes it's a lot, yes it will likely take a while to build all of these spigot versions. Only versions after 1.17 use remapped jars.

## Metrics collection
![metrics](https://bstats.org/signatures/bukkit/TamableFoxes.svg)
Tamable Foxes collects anonymous server statistics through bStats, an open-source statistics service for Minecraft software. If you wish to opt-out, you can do so in the `bstats/config.yml` file.
