<-Instructions on how to enable the various optional features included in the mod, must be enabled before a new game start->

By default the Hiver faction is unplayable but if you follow these steps you will be able to play as a Hiver with a fully functional market. You will be on good standing with the Hivers however the faction itself will remain enemies with all other factions. As the player you will have some factions that are neutral to favorable as well as several enemies. 

Each step is modular and it is up to you what level of functionality you would like. For example. if you just want to flag them as playable so they show up on random core world mode but want them still hidden on the intel tab and don't care for the portraits being used then just enable the first change.
--------------------------------------------------------------------------------------------------------------------
First off, use Notepad++ or a java complier. If you make changes with standard notepad or some other editor it may mess with the Java code.

From the mods tab in you Starsector folder go to Hiver Swarm\data\config\exerelinfactionConfig\HIVER.json and open it with your editor. Navigate to line 3 "startingFaction":false and set it to true, and save. Now go to Hiver Swarm\data\campaign and open rules.csv and delete lines 27-31 see below spoiler for the lines to delete if you are unsure:

hiver_cmsn_askForCommissionOpt_disable,PopulateOptions,"$isPerson
Nex_Commission personCanGiveCommission
!Nex_Commission hasFactionCommission
$faction.id == HIVER","SetEnabled cmsn_askCommission false
SetTooltip cmsn_askCommission ""The Hivers do not take in outsiders.""",,,
--------------------------------------------------------------------------------------------------------------------
To enable the Hiver faction to show on the intel tab: Go to Hiver Swarm\data\world\factions\HIVER.faction and open it with your editor on line 14 you will see "showInIntelTab":false,  change the false, to true, and save the file.
--------------------------------------------------------------------------------------------------------------------
To enable the Hiver playable portraits shown below when you make a new character: Go to Hiver Swarm\data\world\factions\ - now the tricky part, you will see a file named xxplayer.faction - rename that to player.faction
--------------------------------------------------------------------------------------------------------------------
The mod comes with two optional files that make Ships and Weapons purchasable and available at the Arms Dealer for players that play as Hivers or just want them at the Arms Dealer. In order to enable this feature;

-Find a file named purchasable_ship_data.csv located in data/hulls - If you delete the current ship_data.csv and rename the optional file ship_data.csv it will make all Hiver ships purchasable and available at the Arms Dealer

-Find a file named purchasable_weapon.data_csv located in data/weapons If you delete the current weapon_data.csv and rename the optional file weapon_data.csv it will make all Hiver weapons purchasable and available at the Arms Dealer
--------------------------------------------------------------------------------------------------------------------
The mod comes with two optional files that make Ships and Weapons unrecoverable for players that prefer not to have Hiver Technology in their fleets. In order to enable this feature;

-Find a file named unrecoverable_ship_data.csv located in data/hulls - If you delete the current ship_data.csv and rename the optional file ship_data.csv it will make all Hiver ships and their BP unrecoverable

-Find a file named unrecoverable_weapon.data_csv located in data/weapons If you delete the current weapon_data.csv and rename the optional file weapon_data.csv it will make all Hiver weapons unrecoverable
--------------------------------------------------------------------------------------------------------------------
To disable their blueprints from dropping in salvage or as loot

-Find a file named special_items.csv located in data/campaign and delete,rename or move it - select the file named nodrop_special_items.csv in the folder, rename it to special_items.csv and you are good to go
--------------------------------------------------------------------------------------------------------------------
The mod comes with an all mechanical looking alternate ship and weapon pack for players that prefer not to have bugs on their screen.To install the alternate pack:

Unpack the Replacement Ships.rar found in the main folder over your Hiver Swarm folder and accept overwrite. This will install both the ship and weapon pack. If you only want one or the other then you would first need to unzip into a temporary folder, delete the subfolders you do not want (data/hulls, data/variants and graphics/ships to delete the ships or data/weapons and graphics/weapons to delete the weapons then re-zip and install as above or just move the folders manually if desired
--------------------------------------------------------------------------------------------------------------------
