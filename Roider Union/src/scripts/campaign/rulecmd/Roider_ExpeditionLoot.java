package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
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
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_MemFlags;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicSettings;
import scripts.campaign.cleanup.Roider_ExpeditionMajorLootCleaner;
import scripts.campaign.fleets.expeditions.Roider_ExpeditionStashPickupScript;
import scripts.campaign.fleets.expeditions.Roider_MajorLootStashPlugin;
import scripts.campaign.rulecmd.expeditionSpecials.Roider_PingTrapSpecial.Roider_PingTrapSpecialData;
import scripts.campaign.rulecmd.expeditionSpecials.Roider_ThiefTrapSpecial.Roider_ThiefTrapSpecialData;

/**
 * Author: SafariJohn
 */
public class Roider_ExpeditionLoot extends BaseCommandPlugin {
//	private InteractionDialogAPI dialog;
	protected TextPanelAPI text;
	private SectorEntityToken entity;
//	private Map<String, MemoryAPI> memoryMap;
    private Random random;
//    private boolean major;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
//		this.dialog = dialog;
        text = dialog.getTextPanel();
//		this.memoryMap = memoryMap;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;

		entity = dialog.getInteractionTarget();

//		Object test = entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_DEBRIS_FIELD);
//		if (test instanceof DebrisFieldTerrainPlugin) {
//			debris = (DebrisFieldTerrainPlugin) test;
//		} else {
//            return false;
//        }

        random = new Random(entity.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED));

//        major = entity.getMemoryWithoutUpdate().contains(Roider_MemFlags.EXPEDITION_LOOT_MAJOR);

        switch (command) {
            case "printDesc": printDesc(); break;
            case "minefieldDisable": minefieldConsequence(); break;
            case "scanDebris": spawnLoot(); break;
//            case "transmitterTrap": return scanMajorDebris();
        }


        return true;
    }

    private void printDesc() {
//		float daysLeft = debris.getDaysLeft();
//		if (daysLeft >= 1000) {
//			text.addParagraph("The field appears stable and will not drift apart any time soon.");
//		} else {
//			String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
//			text.addParagraph("The field is unstable, but should not drift apart for " + atLeastTime + ".");
//		}

//        if (major) {
            text.addPara("The mines are blazoned with Roider Union "
                        + "emblems and are densest around a volume "
                        + "approximately the size of a capital ship.");

            text.addPara("There must be something very valuable "
                        + "hidden here, but anyone who bothers to "
                        + "lay a minefield means business. Shooting "
                        + "your way through the mines would surely "
                        + "cause the cache to be destroyed as well.");
//        } else {
//            text.addPara("Long-range sensors indicate there is "
//                        + "nothing of value here. Then your sensors "
//                        + "officer reports a suspicious variation "
//                        + "on the phase sensors - perhaps a stealth "
//                        + "satellite being used as a nav beacon?");
//
//            text.addPara("Many roiders have recently begun using "
//                        + "such devices in their salvage operations. "
//                        + "A close scan should reveal anything of "
//                        + "interest.");
//        }
    }

    private void minefieldConsequence() {
        // Do nothing for now
    }

    private void spawnLoot() {
        StarSystemAPI system = entity.getStarSystem();
        AddedEntity loot = createLootDrop(system);

        float seed = entity.getMemoryWithoutUpdate().getFloat(MemFlags.SALVAGE_SEED);
        loot.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);

        loot.entity.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT_MAJOR, true);
        loot.entity.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT, true);

        String thiefId = entity.getMemoryWithoutUpdate().getString(Roider_MemFlags.THIEF_KEY);
        loot.entity.getMemoryWithoutUpdate().set(Roider_MemFlags.THIEF_KEY, thiefId);

        loot.entity.setDiscoverable(true);
//        loot.entity.setDiscoveryXP(0f);

        loot.entity.setOrbit(entity.getOrbit().makeCopy());
        system.addEntity(loot.entity);

//        String faction = entity.getMemoryWithoutUpdate().getString(Roider_MemFlags.EXPEDITION_FACTION);
//        String source = entity.getMemoryWithoutUpdate().getString(Roider_MemFlags.EXPEDITION_MARKET);

//        Roider_ExpeditionTrapCreator creator;
////        if (major)
//            creator = new Roider_ExpeditionTrapCreator(random,
//                    1f, Roider_FleetTypes.MAJOR_EXPEDITION,
//                    faction, source, 10, 20, true);
////        else creator = new Roider_ExpeditionTrapCreator(random,
////                    0.9f, Roider_FleetTypes.MINING_FLEET,
////                    faction, source, 7, 14, false);
//
//		SpecialCreationContext context = new SpecialCreationContext();
//
//		Object specialData = creator.createSpecial(loot.entity, context);
//		if (specialData != null) {
//			Misc.setSalvageSpecial(loot.entity, specialData);
//        }

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(random);
        picker.add("thiefTrap", 10);
        picker.add("pingTrap", 10);
//            picker.add("droneDefenders", 1);

        String special = picker.pick();
        switch (special) {
            default:
            case "thiefTrap":
                Misc.setSalvageSpecial(loot.entity, new Roider_ThiefTrapSpecialData());
                break;
            case "pingTrap":
                Misc.setSalvageSpecial(loot.entity, new Roider_PingTrapSpecialData());
                break;
        }

//        debris.gete.setContainingLocation(null);
//        system.removeEntity(entity);

//        debris.setScavenged(true);
//        debris.getEntity().setOrbit(loot.entity.getOrbit());

        if (loot.entityType.equals(Entities.WRECK)) {
            text.addPara("A " + loot.entity.getName().toLowerCase() + " dephases before you.",
                        Misc.getHighlightColor(),
                        loot.entity.getName().toLowerCase());
        } else {
            text.addPara("An abandoned " + loot.entity.getName().toLowerCase()
                        + " dephases before you, much to your amazement!",
                        Misc.getHighlightColor(),
                        loot.entity.getName().toLowerCase());
        }

        Global.getSoundPlayer().playSound("system_phase_cloak_deactivate", 1f, 1f,
                    Global.getSector().getPlayerFleet().getLocation(), new Vector2f(0,0));

        SectorEntityToken temp = entity;
        entity = loot.entity;

        ((Roider_MajorLootStashPlugin) temp.getCustomPlugin()).fadeOut();
        Misc.fadeAndExpire(temp);

        // Save entity in token's data for later access
        // Have to avoid a ConcurrentModificationException
        entity.getOrbitFocus().getCustomData().put(Roider_ExpeditionStashPickupScript.STASH_ENTITY_KEY, entity);

        // Need to clean up orbit focus when the loot entity expires
        entity.addScript(new Roider_ExpeditionMajorLootCleaner(entity));
    }

    private AddedEntity createLootDrop(StarSystemAPI system) {
        // Copy location
        EntityLocation loc = new EntityLocation();
        loc.location = entity.getLocation();
        loc.orbit = entity.getOrbit().makeCopy();
        loc.type = LocationType.IN_SMALL_NEBULA;

        WeightedRandomPicker<String> picker = new WeightedRandomPicker(random);
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

                    CustomCampaignEntityAPI salEntity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, system,
                                                    Entities.WRECK, Factions.NEUTRAL, params);
                    salEntity.setDiscoverable(true);
                    BaseThemeGenerator.setEntityLocation(salEntity, loc, Entities.WRECK);

                    AddedEntity added = new AddedEntity(salEntity, null, Entities.WRECK);
                    return added;
                }
            } while (true);
        }

        return addStation(loc, system, type, Factions.NEUTRAL);
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
