package scripts.campaign.rulecmd;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.MagicSettings;
import ids.Roider_Ids.Roider_FleetTypes;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_MemFlags;
import java.util.List;
import java.util.Map;
import java.util.Random;
import scripts.campaign.fleets.Roider_ExpeditionTrap.Roider_ExpeditionTrapCreator;

/**
 * Author: SafariJohn
 */
public class Roider_ExpeditionLoot extends BaseCommandPlugin {
	private InteractionDialogAPI dialog;
	protected TextPanelAPI text;
	private SectorEntityToken entity;
	private DebrisFieldTerrainPlugin debris;
	private Map<String, MemoryAPI> memoryMap;
    private Random random;
    private boolean major;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
        text = dialog.getTextPanel();
		this.memoryMap = memoryMap;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;

		entity = dialog.getInteractionTarget();

		Object test = entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_DEBRIS_FIELD);
		if (test instanceof DebrisFieldTerrainPlugin) {
			debris = (DebrisFieldTerrainPlugin) test;
		} else {
            return false;
        }

        random = new Random(entity.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED));

        major = entity.getMemoryWithoutUpdate().contains(Roider_MemFlags.EXPEDITION_LOOT_MAJOR);

        switch (command) {
            case "descDebris": printDesc(); break;
            case "minefieldDisable": minefieldConsequence(); break;
            case "scanDebris": spawnLoot(); break;
//            case "transmitterTrap": return scanMajorDebris();
        }


        return true;
    }

    private void printDesc() {
		float daysLeft = debris.getDaysLeft();
		if (daysLeft >= 1000) {
			text.addParagraph("The field appears stable and will not drift apart any time soon.");
		} else {
			String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
			text.addParagraph("The field is unstable, but should not drift apart for " + atLeastTime + ".");
		}

        if (major) {
            text.addPara("Long-range sensors indicate there is "
                        + "nothing of value, but your sensors officer "
                        + "reports a stray IFF ping. A tighter scan "
                        + "reveals several suspicious signatures "
                        + "near the debris cloud - mines.");

            text.addPara("There must be something very valuable "
                        + "hidden here, but anyone who bothered to "
                        + "lay a minefield meant business. Shooting "
                        + "your way through the mines would surely "
                        + "cause the stash to be destroyed as well.");
        } else {
            text.addPara("Long-range sensors indicate there is "
                        + "nothing of value here. Then your sensors "
                        + "officer reports a suspicious variation "
                        + "on the phase sensors - perhaps a stealth "
                        + "satellite being used as a nav beacon?");

            text.addPara("Many roiders have recently begun using "
                        + "such devices in their salvage operations. "
                        + "A close scan should reveal anything of "
                        + "interest.");
        }
    }

    private void minefieldConsequence() {
        // Do nothing for now
    }

    private void spawnLoot() {
        StarSystemAPI system = entity.getStarSystem();
        AddedEntity loot = createLootDrop(system);

        loot.entity.setMemory(entity.getMemory());
        loot.entity.setDiscoverable(true);
        loot.entity.setDiscoveryXP(0f);

        loot.entity.setOrbit(debris.getEntity().getOrbit().makeCopy());
        system.addEntity(loot.entity);

        String faction = entity.getMemoryWithoutUpdate().getString(Roider_MemFlags.EXPEDITION_FACTION);
        String source = entity.getMemoryWithoutUpdate().getString(Roider_MemFlags.EXPEDITION_MARKET);

        Roider_ExpeditionTrapCreator creator;
        if (major) creator = new Roider_ExpeditionTrapCreator(random,
                    1f, Roider_FleetTypes.MAJOR_EXPEDITION,
                    faction, source, 10, 20, true);
        else creator = new Roider_ExpeditionTrapCreator(random,
                    0.9f, Roider_FleetTypes.MINING_FLEET,
                    faction, source, 7, 14, false);

		SpecialCreationContext context = new SpecialCreationContext();

		Object specialData = creator.createSpecial(loot.entity, context);
		if (specialData != null) {
			Misc.setSalvageSpecial(loot.entity, specialData);
		}

//        debris.gete.setContainingLocation(null);
//        system.removeEntity(entity);

        debris.setScavenged(true);
//        debris.getEntity().setOrbit(loot.entity.getOrbit());

        if (major) {
            if (loot.entityType.equals(Entities.WRECK)) {
                text.addPara("You discover a "
                            + loot.entity.getName().toLowerCase()
                            + " hidden in the debris field.",
                            Misc.getHighlightColor(),
                            loot.entity.getName().toLowerCase());
            } else {
                text.addPara("You discover a "
                            + loot.entity.getName().toLowerCase()
                            + " hidden in the debris field, much to your "
                            + "amazement!",
                            Misc.getHighlightColor(),
                            loot.entity.getName().toLowerCase());
            }
        } else {
            text.addPara("You discover a "
                        + loot.entity.getName().toLowerCase()
                        + " hidden in the debris field.",
                        Misc.getHighlightColor(),
                        loot.entity.getName().toLowerCase());
        }
    }

    private AddedEntity createLootDrop(StarSystemAPI system) {
        // Copy location
        EntityLocation loc = new EntityLocation();
        loc.location = entity.getLocation();
        loc.orbit = debris.getEntity().getOrbit().makeCopy();
        loc.type = LocationType.IN_SMALL_NEBULA;

        if (major) {
            WeightedRandomPicker<String> picker = new WeightedRandomPicker();
            picker.add(Entities.STATION_RESEARCH, 0.1f);
            picker.add(Entities.STATION_MINING, 1f);
            picker.add(Entities.ORBITAL_HABITAT, 5f);
            picker.add(Entities.WRECK, 50f);
            String type = picker.pick();

            if (type.equals(Entities.WRECK)) {
                List<String> factions = MagicSettings.getList(Roider_Settings.MAGIC_ID,
                            Roider_Settings.EXPEDITION_LOOT_FACTIONS);

                picker.clear();
                picker.addAll(factions);
                int iter = 0;
                do {
                    if (iter > 110) break;
                    
                    String faction = picker.pick();
                    DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
                    iter++;

                    if (params != null) {
                        if (params.ship.getVariant().getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                            continue;
                        }

                        CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, system,
                                                        Entities.WRECK, Factions.NEUTRAL, params);
                        entity.setDiscoverable(true);
                        BaseThemeGenerator.setEntityLocation(entity, loc, Entities.WRECK);

                        AddedEntity added = new AddedEntity(entity, null, Entities.WRECK);
                        return added;
                    }
                } while (true);
            }

            return addStation(loc, system, type, Factions.NEUTRAL);

        } else {
            WeightedRandomPicker<String> picker = new WeightedRandomPicker();
            picker.add(Entities.WRECK, 50f);
			picker.add(Entities.WEAPONS_CACHE, 4f);
            picker.add(Entities.WEAPONS_CACHE_SMALL, 10f);
            picker.add(Entities.WEAPONS_CACHE_HIGH, 4f);
            picker.add(Entities.WEAPONS_CACHE_SMALL_HIGH, 10f);
            picker.add(Entities.WEAPONS_CACHE_LOW, 4f);
            picker.add(Entities.WEAPONS_CACHE_SMALL_LOW, 10f);
            picker.add(Entities.SUPPLY_CACHE, 4f);
            picker.add(Entities.SUPPLY_CACHE_SMALL, 10f);
            picker.add(Entities.EQUIPMENT_CACHE, 4f);
            picker.add(Entities.EQUIPMENT_CACHE_SMALL, 10f);
            String type = picker.pick();

            if (type.equals(Entities.WRECK)) {
                List<String> factions = MagicSettings.getList(Roider_Settings.MAGIC_ID,
                            Roider_Settings.EXPEDITION_LOOT_FACTIONS);

                picker.clear();
                picker.addAll(factions);
                int iter = 0;
                do {
                    if (iter > 110) break;

                    String faction = picker.pick();
                    DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
                    iter++;

                    if (params != null) {
                        if (params.ship.getVariant().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                            continue;
                        }

                        CustomCampaignEntityAPI wreck = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, system,
                                                        Entities.WRECK, Factions.NEUTRAL, params);
                        wreck.setDiscoverable(true);
                        BaseThemeGenerator.setEntityLocation(wreck,
                                    loc, Entities.WRECK);

                        AddedEntity added = new AddedEntity(wreck,
                                    null, Entities.WRECK);
                        return added;
                    }
                } while (true);
            }


            return BaseThemeGenerator.addEntity(random, system,
                        loc, type, Factions.NEUTRAL);
        }
    }

	public AddedEntity addStation(EntityLocation loc,
                StarSystemAPI system, String customEntityId,
                String factionId) {
		if (loc == null) return null;

		AddedEntity station = BaseThemeGenerator.addEntity(random,
                    system, loc, customEntityId, factionId);

		SectorEntityToken focus = station.entity.getOrbitFocus();
		if (focus instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) focus;

			boolean nearStar = planet.isStar()
                        && station.entity.getOrbit() != null
                        && station.entity.getCircularOrbitRadius() < 5000;

			if (planet.isStar() && !nearStar) {
//				station.entity.setFacing(random.nextFloat() * 360f);
//				convertOrbitNoSpin(station.entity);
			} else {
				BaseThemeGenerator.convertOrbitPointingDown(station.entity);
			}
		}

//		station.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_DEFENDER_FACTION, Factions.REMNANTS);
//		station.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_DEFENDER_PROB, 1f);

		return station;
	}

}
