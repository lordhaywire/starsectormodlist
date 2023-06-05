package scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import exerelin.campaign.ExerelinSetupData;
import static ids.Roider_Ids.DESC;
import ids.Roider_Ids.Roider_Entities;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_MemFlags;
import java.util.*;
import org.magiclib.util.MagicSettings;
import scripts.Roider_ModPlugin;
import scripts.campaign.bases.Roider_RoiderHQBaseIntel;
import scripts.world.systems.*;
import static scripts.world.systems.Roider_Atka.ROCKPIPER_PERCH;
public class Roider_Gen {

    public static void generate(SectorAPI sector) {
        generateStarSystems(sector);
    }

    public static void placeFringeRoiderHQs(SectorAPI sector) {
        int num = MagicSettings.getInteger(Roider_Settings.MAGIC_ID,
                    Roider_Settings.MAX_FRINGE_HQS);

        for (int i = 0; i < num; i++) {
            StarSystemAPI system = pickSystemForRoiderBase();
            if (system == null) {
                i--;
                continue;
            }

            Roider_RoiderHQBaseIntel intel = new Roider_RoiderHQBaseIntel(system, Roider_Factions.ROIDER_UNION);
            intel.init();
            if (intel.isDone()) i--;
        }
    }

    private static void generateStarSystems(SectorAPI sector) {
        Roider_Atka.generate(sector);
        Roider_Attu.generate(sector);
        Roider_Ounalashka.generate(sector);
        Roider_Kiska.generate(sector);
    }

    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI roider = sector.getFaction(Roider_Factions.ROIDER_UNION);

        // Don't set anything if Nexerelin is randomizing
        if (Roider_ModPlugin.hasNexerelin) {
            ExerelinSetupData data = ExerelinSetupData.getInstance();

            if (data.randomStartRelationships) return;
        }

        roider.setRelationship(Factions.INDEPENDENT, RepLevel.FRIENDLY);
        roider.setRelationship(Factions.TRITACHYON, RepLevel.HOSTILE);
        roider.setRelationship(Factions.PIRATES, RepLevel.VENGEFUL);
        roider.setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
        roider.setRelationship(Factions.REMNANTS, RepLevel.HOSTILE);
        roider.setRelationship(Factions.DERELICT, RepLevel.HOSTILE);
        roider.setRelationship(Factions.HEGEMONY, RepLevel.FAVORABLE);
        roider.setRelationship(Factions.REMNANTS, RepLevel.HOSTILE);
        roider.setRelationship(Factions.OMEGA, RepLevel.HOSTILE);

        // Mod factions
        roider.setRelationship(Roider_Factions.THI, RepLevel.FAVORABLE);

        // Various pirate mod factions and the like
        roider.setRelationship(Roider_Factions.CABAL, -1f);
        roider.setRelationship(Roider_Factions.ARS, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.COLONIAL_PIRATES, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.SCY, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.BLADE_BREAKERS, RepLevel.VENGEFUL);
        roider.setRelationship(Roider_Factions.EXIPIRATED, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.CARTEL, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.CRYSTANITE_PIRATES, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.GMDA, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.GMDA_PATROL, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.GREY_GOO, RepLevel.VENGEFUL);
        roider.setRelationship(Roider_Factions.JUNK_HOUNDS, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.JUNK_PIRATES, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.JUNK_JUNKBOYS, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.JUNK_TECHNICIANS, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.TEMPLARS, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.NULL_ORDER, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.VAMPIRES, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.WEREWOLVES, RepLevel.HOSTILE);
        roider.setRelationship(Roider_Factions.ZOMBIES, RepLevel.HOSTILE);
    }

    public static void addCoreDives(SectorAPI sector) {
        addDive(sector, "Samarra", "orthrus", Roider_Industries.DIVES);
        addDive(sector, "Samarra", "tigra_city", Roider_Industries.DIVES);
        addDive(sector, "Westernesse", "ailmar", Roider_Industries.DIVES);
        addDive(sector, "Magec", "new_maxios", Roider_Industries.DIVES);
        addDive(sector, "Magec", "kantas_den", Roider_Industries.DIVES);
        addDive(sector, "Eos Exodus", "baetis", Roider_Industries.DIVES);
        markClaimed(sector, "Eos Exodus", "daedaleon"); // Block mining on Daedaleon
        addDive(sector, "Galatia", "derinkuyu_station", Roider_Industries.DIVES);
        addDive(sector, "Corvus", "corvus_pirate_station", Roider_Industries.DIVES);

//        markSystemClaimed(sector, "Shaanxi"); // Tiandong Heavy Industries system
    }

    public static void addNexRandomModeDives() {
        SectorAPI sector = Global.getSector();

        Map<String, Float> factions = new HashMap<>();
        factions.put(Roider_Factions.ROIDER_UNION, 1f);
        factions.put(Factions.HEGEMONY, 0.1f); // Very small chance for Heg dives or Union HQ
        factions.put(Factions.INDEPENDENT, 0.4f);
        factions.put(Factions.PIRATES, 0.2f);

        List<MarketAPI> roiderMarkets = new ArrayList<>();
        for (MarketAPI market : sector.getEconomy().getMarketsInGroup(null)) {
            if (factions.containsKey(market.getFactionId())) {

                if (new Random().nextFloat() < factions.get(market.getFactionId())) {
                    roiderMarkets.add(market);
                }
            }
        }

        for (MarketAPI market : roiderMarkets) {
            if (market.getStarSystem() == null) continue;
            if (market.getPrimaryEntity() == null) continue;

            String industryId = Roider_Industries.DIVES;

            MilitaryBase milBase = (MilitaryBase) market.getIndustry(Industries.MILITARYBASE);
            if (milBase == null) milBase = (MilitaryBase) market.getIndustry(Industries.HIGHCOMMAND);

            // Replace all Roider Union military with Union HQs
            if (market.getFactionId().equals(Roider_Factions.ROIDER_UNION)
                        && milBase != null) {
                milBase.unapply();
                market.removeIndustry(milBase.getId(), null, false);
                market.removeSubmarket(Submarkets.GENERIC_MILITARY);
                removeMilBaseCommander(market);

                industryId = Roider_Industries.UNION_HQ;

            // Chance for Union HQ on non-Roider markets
            } else if (!market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION)
                        && canAddRandomUnionHQ(market.getSize(), null)) {

                int indCount = 0;
                for (Industry ind : market.getIndustries()) {
                    if (ind.isIndustry()) indCount++;
                }

                if (indCount >= Misc.getMaxIndustries(market)) {
                    if (market.hasIndustry(Industries.MILITARYBASE)
                                && milBase != null) {
                        milBase.unapply();
                        market.removeIndustry(milBase.getId(), null, false);
                        market.removeSubmarket(Submarkets.GENERIC_MILITARY);
                        removeMilBaseCommander(market);
                        indCount--;
                    }
                }

                if (indCount < Misc.getMaxIndustries(market)) industryId = Roider_Industries.UNION_HQ;
            }

            addDive(sector, market.getStarSystem().getId(),
                        market.getPrimaryEntity().getId(), industryId);
        }

    }

    private static boolean canAddRandomUnionHQ(int marketSize, Random random) {
        if (random == null) random = new Random();

        float chance;
        switch (marketSize) {
            case  1:
            case  2:
            case  3: chance = 0.1f;
                break;
            case  4: chance = 0.4f;
                break;
            case  5: chance = 0.8f;
                break;
            case  6: chance = 0.9f;
                break;
            case  7: chance = 0.8f;
                break;
            case  8: chance = 0.5f;
                break;
            case  9:
            case 10:
            default: chance = 0.3f;
        }

        return random.nextFloat() < chance;
    }

    private static void removeMilBaseCommander(MarketAPI market) {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        for (PersonAPI person : market.getPeopleCopy()) {
            if (person.getPostId().equals(Ranks.POST_BASE_COMMANDER)
                        && person.getFaction() == market.getFaction()
                        && ip.getData(person).getCheckedOutFor().contains("permanent_staff")) {

				market.getCommDirectory().removePerson(person);
				market.removePerson(person);
				ip.getData(person).getLocation().setMarket(null);
				ip.returnPerson(person, "permanent_staff");
				ip.removePerson(person);
            }
        }
    }

    private static void addDive(SectorAPI sector, String systemId, String entityId, String industryId) {
        StarSystemAPI system = sector.getStarSystem(systemId);
        SectorEntityToken entity = null;
        MarketAPI market = null;

        if (system != null) entity = system.getEntityById(entityId);
        if (entity != null) market = entity.getMarket();
        if (market != null && !market.isPlanetConditionMarketOnly()) market.addIndustry(industryId);
    }

    private static void markClaimed(SectorAPI sector, String systemId, String entityId) {
        StarSystemAPI system = sector.getStarSystem(systemId);
        SectorEntityToken entity = null;

        if (system != null) entity = system.getEntityById(entityId);
        if (entity != null) entity.getMemoryWithoutUpdate().set(Roider_MemFlags.CLAIMED, true);
    }

    private static void markSystemClaimed(SectorAPI sector, String systemId) {
        StarSystemAPI system = sector.getStarSystem(systemId);
        if (system != null) {
            for (PlanetAPI planet : system.getPlanets()) {
                planet.getMemoryWithoutUpdate().set(Roider_MemFlags.CLAIMED, true);
            }
        }
    }

    public static void addNexRandomRockpiper() {
        // Find Roider capital
        MarketAPI capital = null;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!market.getFactionId().equals(Roider_Factions.ROIDER_UNION)) continue;

            if (!market.hasIndustry(Roider_Industries.UNION_HQ)) continue;

            if (capital == null || market.getSize() > capital.getSize()) {
                capital = market;
            }
        }
        if (capital == null) return;


        // Must have a shipyard
        boolean hasShipyard = capital.hasIndustry(Industries.HEAVYINDUSTRY)
                    || capital.hasIndustry(Industries.ORBITALWORKS);
        if (!hasShipyard) {
            boolean added = false;

            // First see if it can be added directly
            int indCount = 0;
            for (Industry ind : capital.getIndustries()) {
                if (ind.isIndustry()) indCount++;
            }
            if (indCount < Misc.getMaxIndustries(capital)) {
                capital.addIndustry(Industries.ORBITALWORKS);
                added = true;
            }

            // Otherwise try to replace something
            if (!added) added = replaceIndustry(capital,
                        "commerce", Industries.ORBITALWORKS);
            if (!added) added = replaceIndustry(capital,
                        Industries.MINING, Industries.ORBITALWORKS);
            if (!added) added = replaceIndustry(capital,
                        Industries.LIGHTINDUSTRY, Industries.ORBITALWORKS);
            if (!added) added = replaceIndustry(capital,
                        Industries.FUELPROD, Industries.ORBITALWORKS);
            if (!added) added = replaceIndustry(capital,
                        Industries.TECHMINING, Industries.ORBITALWORKS);
            if (!added) added = replaceIndustry(capital,
                        Industries.REFINING, Industries.ORBITALWORKS);

            // Failed to add a shipyard
            if (!added) return;
        }

        // Time to get to business
        SectorEntityToken primary = capital.getPrimaryEntity();
        if (primary instanceof PlanetAPI) {
            PlanetAPI planet = (PlanetAPI) primary;

            // ? Increase size to minimum (200), if needed
//            if (planet.getRadius() < 200f) {
//
//                // If the capital is a moon of too small a world, cancel
//                SectorEntityToken parent = planet.getOrbitFocus();
//                if (!(parent.isStar() || parent.isSystemCenter())) {
//                    float parentRadius = parent.getRadius();
//                    if (parentRadius <= 200f) return;
//                }
//
//
//                planet.setRadius(200f);
//            }

            // ? Adjust radius of any satellites, like the junk ring
            // Let's see what happens for now

            // Add Rockpiper Perch and hook it up
            SectorEntityToken roiderStation = planet.getStarSystem().addCustomEntity(ROCKPIPER_PERCH.id,
                        ROCKPIPER_PERCH.name, Roider_Entities.ROCKPIPER_PERCH, Roider_Factions.ROIDER_UNION);

            roiderStation.setCircularOrbit(planet, 145, planet.getRadius() + 160f, Float.MAX_VALUE);
//            roiderStation.setCircularOrbitPointingDown(korovin, 45 + 120, 380, 28);
            roiderStation.setCustomDescriptionId(ROCKPIPER_PERCH.id + DESC);

            roiderStation.setMarket(capital);

            capital.getConnectedEntities().add(roiderStation);

        // If no planet, replace primary entity with Rockpiper Perch
            // Hope this doesn't come up.
        } else {
            SectorEntityToken roiderStation = primary.getStarSystem().addCustomEntity(ROCKPIPER_PERCH.id,
                        ROCKPIPER_PERCH.name, "roider_station_rockpiper", Roider_Factions.ROIDER_UNION);
            roiderStation.setCustomDescriptionId(ROCKPIPER_PERCH.id + DESC);

            // Save its orbit
            OrbitAPI orbit = primary.getOrbit();

            roiderStation.setMarket(capital);

            capital.getConnectedEntities().add(roiderStation);
            capital.setPrimaryEntity(roiderStation);
            capital.getConnectedEntities().remove(primary);

            if (orbit.getFocus() instanceof PlanetAPI) {
                roiderStation.setCircularOrbit(orbit.getFocus(), primary.getCircularOrbitAngle(),
                            primary.getCircularOrbitRadius(), primary.getCircularOrbitPeriod());
            } else {
                // This could be a weird-ass orbit, but oh well
                roiderStation.setCircularOrbit(orbit.getFocus(), 145,
                            orbit.getFocus().getRadius() + 160f, Float.MAX_VALUE);
            }

            // Remove the old entity
            Misc.fadeAndExpire(primary);
        }

        // Make sure battlestation attaches
        // And make sure it is a star fortress
        boolean replaced = false;
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.STARFORTRESS, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.STARFORTRESS_HIGH, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.STARFORTRESS_MID, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.BATTLESTATION, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.BATTLESTATION_HIGH, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.BATTLESTATION_MID, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.ORBITALSTATION, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.ORBITALSTATION_HIGH, Industries.STARFORTRESS);
        if (!replaced) replaced = replaceIndustry(capital,
                    Industries.ORBITALSTATION_MID, Industries.STARFORTRESS);

        if (replaced) capital.advance(1f);
    }

    private static boolean replaceIndustry(MarketAPI market, String id, String newId) {
        if (market == null) return false;
        if (!market.hasIndustry(id)) return false;

        market.removeIndustry(id, null, false);
        market.advance(1f);
        market.addIndustry(newId);
        return true;
    }

	public static StarSystemAPI pickSystemForRoiderBase() {
        Random random = new Random();
		WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<>(random);
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(random);

		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (days < 45f) continue;
			if (system.getCenter().getMemoryWithoutUpdate().contains(PirateBaseManager.RECENTLY_USED_FOR_BASE)) continue;

			float weight = 0f;
			if (system.hasTag(Tags.THEME_MISC_SKIP)) {
				weight = 1f;
			} else if (system.hasTag(Tags.THEME_MISC)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_RUINS)) {
				weight = 5f;
			}
			if (weight <= 0f) continue;

			float usefulStuff = system.getCustomEntitiesWithTag(Tags.OBJECTIVE).size() +
								system.getCustomEntitiesWithTag(Tags.STABLE_LOCATION).size();
			if (usefulStuff <= 0) continue;

			if (Misc.hasPulsar(system)) continue;
			if (Misc.getMarketsInLocation(system).size() > 0) continue;

            LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights = new LinkedHashMap<>();
            weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 1f);
            weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 1f);
            weights.put(BaseThemeGenerator.LocationType.IN_RING, 1f);
            weights.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 1f);

            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> locs
                        = BaseThemeGenerator.getLocations(random, system, new HashSet<SectorEntityToken>(), 200f, weights);
            if (locs.isEmpty()) continue;


			float dist = system.getLocation().length();



//			float distMult = 1f - dist / 20000f;
//			if (distMult > 1f) distMult = 1f;
//			if (distMult < 0.1f) distMult = 0.1f;

			float distMult = 1f;

			if (dist > 36000f) {
				far.add(system, weight * usefulStuff * distMult);
			} else {
				picker.add(system, weight * usefulStuff * distMult);
			}
		}

		if (picker.isEmpty()) {
			picker.addAll(far);
		}

		return picker.pick();
	}

    public static void assignCustomAdmins() {
		MarketAPI market = null;

		market =  Global.getSector().getEconomy().getMarket("roider_maggies");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setFaction(Factions.PIRATES);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.SPACE_ADMIRAL);
			person.setPostId(Ranks.POST_FLEET_COMMANDER);
			person.getName().setFirst("Mad-Eye");
			person.getName().setLast("Maggie");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "roider_maggie"));
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);

			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
		}

        market = Global.getSector().getEconomy().getMarket("roider_korovin");
        if (market != null) {
            market.removePerson(market.getAdmin());

            PersonAPI person = market.getFaction().createRandomPerson(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.getName().setFirst("Llewelyn");
			person.getName().setLast("Lewis");
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);

			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
        }
    }

    public static void assignRandomAdmins() {
        // Assign admins to small ports
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//            if (market.getAdmin() != null) continue;
			if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_DO_NOT_INIT_COMM_LISTINGS)) continue;
            if (!market.getPrimaryEntity().getId().startsWith("roider")) continue;
            if (market.getSize() > 4) continue;

			PersonAPI admin = null;

            // See if we can get a station commander to do it
            if (market.getPrimaryEntity().hasTag(Tags.STATION)) {
                List<PersonAPI> people = market.getPeopleCopy();
                PersonAPI person = null;
                for (PersonAPI p : people) {
                    if (p.getPostId().equals(Ranks.POST_STATION_COMMANDER)
                                && p.getFaction() == market.getFaction()) {
                        person = p;
                        break;
                    }
                }

                if (person != null) {
                    admin = person;
                }
            }

            // Otherwise look for someone who is an admin
			if (admin == null) {
                List<PersonAPI> people = market.getPeopleCopy();
                PersonAPI person = null;
                for (PersonAPI p : people) {
                    if (p.getPost().equals(Ranks.POST_ADMINISTRATOR)
                                && p.getFaction() == market.getFaction()) {
                        person = p;
                        break;
                    }
                }

                // Otherwise create an admin
                if (person == null) {
                    person = market.getFaction().createRandomPerson();
                    person.setRankId(Ranks.CITIZEN);
                    person.setPostId(Ranks.POST_ADMINISTRATOR);

                    market.getCommDirectory().addPerson(person);
                    market.addPerson(person);
                    ip.addPerson(person);
                    ip.getData(person).getLocation().setMarket(market);
                    ip.checkOutPerson(person, "permanent_staff");
                }

                admin = person;
			}

            addSkillsAndAssignAdmin(market, admin);
        }
    }

	private static void addSkillsAndAssignAdmin(MarketAPI market, PersonAPI admin) {
		List<String> skills = Global.getSettings().getSortedSkillIds();
		if (!skills.contains(Skills.PLANETARY_OPERATIONS) ||
				!skills.contains(Skills.SPACE_OPERATIONS) ||
				!skills.contains(Skills.INDUSTRIAL_PLANNING)) {
			return;
		}

		int size = market.getSize();

		int industries = 0;
		int defenses = 0;
		boolean military = market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY);

		for (Industry curr : market.getIndustries()) {
			if (curr.isIndustry()) {
				industries++;
			}
			if (curr.getSpec().hasTag(Industries.TAG_GROUNDDEFENSES)) {
				defenses++;
			}
		}


		admin.getStats().setSkipRefresh(true);

		int num = 0;
		if (industries >= 2 || (industries == 1 && defenses == 1)) {
			admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			num++;
		}

		if (num == 0 || size >= 7) {
			if (military) {
				admin.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
			} else if (defenses > 0) {
				admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
			} else {
				// nothing else suitable, so just make sure there's at least one skill, if this wasn't already set
				admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
			}
		}

		admin.getStats().setSkipRefresh(false);
		admin.getStats().refreshCharacterStatsEffects();

		market.setAdmin(admin);
	}
}
