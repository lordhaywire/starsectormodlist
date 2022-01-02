package com.fs.starfarer.api.impl.campaign.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_StarSystemGenerator extends StarSystemGenerator {

    public Roider_StarSystemGenerator(CustomConstellationParams params) {
        super(params);
    }

    /**
     * This method correctly identifies nebula type instead of
     * randomly picking one like the base class does.
     * @param system
     * @param parentStar
     * @param age
     * @param min
     * @param max
     * @param startingRadius
     * @param nameOffset
     * @param withSpecialNames
     * @return
     */
	public static float addOrbitingEntities(StarSystemAPI system, SectorEntityToken parentStar, StarAge age,
										   int min, int max, float startingRadius,
										   int nameOffset, boolean withSpecialNames) {
		CustomConstellationParams p = new CustomConstellationParams(age);
		p.forceNebula = true; // not sure why this is here; should avoid small nebula at lagrange points though (but is that desired?)

		Roider_StarSystemGenerator gen = new Roider_StarSystemGenerator(p);
		gen.system = system;
		gen.starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, system.getStar().getSpec().getPlanetType(), false);
		gen.starAge = age;
		gen.constellationAge = age;
		gen.starAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, age.name(), true);
		gen.star = system.getStar();
        gen.systemType = system.getType();
        if (system.getType().equals(StarSystemType.NEBULA)) {
            if (age.equals(StarAge.YOUNG)) gen.nebulaType = NEBULA_BLUE;
            else if (age.equals(StarAge.OLD)) gen.nebulaType = NEBULA_AMBER;
            else gen.nebulaType = NEBULA_DEFAULT;
        } else {
            gen.nebulaType = NEBULA_NONE;
        }

		gen.systemCenter = system.getCenter();

		StarGenDataSpec starData = gen.starData;
		PlanetGenDataSpec planetData = null;
		PlanetAPI parentPlanet = null;
		if (parentStar instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) parentStar;
			if (planet.isStar()) {
				starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, planet.getSpec().getPlanetType(), false);
			} else {
				planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planet.getSpec().getPlanetType(), false);
				parentPlanet = planet;
			}
		}

		int parentOrbitIndex = -1;
		int startingOrbitIndex = 0;

		boolean addingAroundStar = parentPlanet == null;
		float r = 0;
		if (parentStar != null) {
			r = parentStar.getRadius();
		}

		float approximateExtraRadiusPerOrbit = 400f;
		if (addingAroundStar) {
			parentOrbitIndex = -1;
			startingOrbitIndex = (int) ((startingRadius  - r - STARTING_RADIUS_STAR_BASE - STARTING_RADIUS_STAR_RANGE * 0.5f) /
							     (BASE_INCR * 1.25f + approximateExtraRadiusPerOrbit));

			if (startingOrbitIndex < 0) startingOrbitIndex = 0;
		} else {
			float dist = 0f;
			if (parentPlanet.getOrbitFocus() != null) {
				dist = Misc.getDistance(parentPlanet.getLocation(), parentPlanet.getOrbitFocus().getLocation());
			}
			parentOrbitIndex = (int) ((dist - r - STARTING_RADIUS_STAR_BASE - STARTING_RADIUS_STAR_RANGE * 0.5f) /
					 		   (BASE_INCR * 1.25f + approximateExtraRadiusPerOrbit));
			startingOrbitIndex = (int) ((startingRadius - STARTING_RADIUS_MOON_BASE - STARTING_RADIUS_MOON_RANGE * 0.5f) /
					 		   (BASE_INCR_MOON * 1.25f));

			if (parentOrbitIndex < 0) parentOrbitIndex = 0;
			if (startingOrbitIndex < 0) startingOrbitIndex = 0;
		}

		int num = (int) Math.round(getNormalRandom(min, max));

		GenContext context = new GenContext(gen, system, gen.systemCenter, starData,
							parentPlanet, startingOrbitIndex, age.name(), startingRadius, MAX_ORBIT_RADIUS,
							planetData != null ? planetData.getCategory() : null, parentOrbitIndex);


		GenResult result = gen.addOrbitingEntities(context, num, false, addingAroundStar, false, false);


		Constellation c = new Constellation(ConstellationType.NORMAL, age);
		c.getSystems().add(system);
		c.setLagrangeParentMap(gen.lagrangeParentMap);
		c.setAllEntitiesAdded(gen.allNameableEntitiesAdded);
		c.setLeavePickedNameUnused(true);
		NameAssigner namer = new NameAssigner(c);
		if (withSpecialNames) {
			namer.setSpecialNamesProbability(1f);
		} else {
			namer.setSpecialNamesProbability(0f);
		}
		namer.setRenameSystem(false);
		namer.setStructuralNameOffset(nameOffset);
		namer.assignNames(null, null);

		for (SectorEntityToken entity : gen.allNameableEntitiesAdded.keySet()) {
			if (entity instanceof PlanetAPI && entity.getMarket() != null) {
				entity.getMarket().setName(entity.getName());
			}
		}

		return result.orbitalWidth * 0.5f;

	}

	public static void addSystemwideNebula(StarSystemAPI system, StarAge age) {
		CustomConstellationParams p = new CustomConstellationParams(age);
		p.forceNebula = true;

		Roider_StarSystemGenerator gen = new Roider_StarSystemGenerator(p);
		gen.system = system;
		gen.starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, system.getStar().getSpec().getPlanetType(), false);
		gen.starAge = age;
		gen.constellationAge = age;
		gen.starAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, age.name(), true);
        gen.systemType = system.getType();
        if (system.getType().equals(StarSystemType.NEBULA)) {
            if (age.equals(StarAge.YOUNG)) gen.nebulaType = NEBULA_BLUE;
            else if (age.equals(StarAge.OLD)) gen.nebulaType = NEBULA_AMBER;
            else gen.nebulaType = NEBULA_DEFAULT;
        } else {
            gen.nebulaType = NEBULA_NONE;
        }


		gen.addSystemwideNebula();

		system.setAge(age);
		system.setHasSystemwideNebula(true);
	}
}
