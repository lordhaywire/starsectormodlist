package RealisticCombat.plugins;

import RealisticCombat.scripts.WeaponSpecs;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.*;
import org.json.JSONArray;
import org.json.JSONException;
import RealisticCombat.scripts.Categorization;
import RealisticCombat.settings.DamageModel;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

import static RealisticCombat.settings.Categorization.WeaponCategory.*;
import static RealisticCombat.settings.Toggles.isEveryShipSpecToBeModified;


public final class ModPlugin extends BaseModPlugin {

    private static final float MISSILE_FLIGHT_TIME_FACTOR = 2,
                               MISSILE_FLAMEOUT_TIME_FACTOR = 2;

    private static void modifyShipHullSpecsNonFighter() {
        for (final ShipHullSpecAPI shipHullSpec : Global.getSettings().getAllShipHullSpecs())
            if (!(Categorization.isModule(shipHullSpec)
                  || Categorization.isStation(shipHullSpec)
                  || Categorization.isFighter(shipHullSpec)
                  || shipHullSpec.hasTag("doNotModify")))
                RealisticCombat.scripts.ShipSpecs.modify(shipHullSpec);
    }

    private static void modifyShipHullSpecsFighter() {
        for (final ShipHullSpecAPI shipHullSpec : Global.getSettings().getAllShipHullSpecs())
            if (Categorization.isFighter(shipHullSpec) && !shipHullSpec.hasTag("doNotModify"))
                RealisticCombat.scripts.ShipSpecs.modify(shipHullSpec);
    }

    private void modifyFighterWingSpecs() {
        for (final FighterWingSpecAPI wingSpec : Global.getSettings().getAllFighterWingSpecs())
            if (!wingSpec.hasTag("doNotModify"))
                wingSpec.setRange(RealisticCombat.settings.ShipSpecs.getFighterWingRangeFactor()
                                  * wingSpec.getRange());
    }

    private static void modifyFlightMissileSpec(final MissileSpecAPI missileSpec) {
        missileSpec.setFlameoutTime(MISSILE_FLAMEOUT_TIME_FACTOR * missileSpec.getFlameoutTime());
        missileSpec.setMaxFlightTime(MISSILE_FLIGHT_TIME_FACTOR * missileSpec.getMaxFlightTime());
        RealisticCombat.scripts.WeaponSpecs.modifyEngineSpec(Categorization.isTorpedo(missileSpec)
                ? LAUNCHER_TORPEDO : LAUNCHER_MISSILE, missileSpec.getHullSpec().getEngineSpec());
    }

    private static void modifyWeaponSpecs() throws JSONException {
        final HashSet<String> idsOfModifiedMissileSpecs = new HashSet<>(),
                              idsOfModifiedPayloadWeaponSpecs = new HashSet<>();
        final JSONArray weaponDataCSV;
        try { weaponDataCSV = Global.getSettings().getMergedSpreadsheetData("id",
                    "data/weapons/weapon_data.csv"); }
        catch (final JSONException | IOException e) { e.printStackTrace(); return; }
        //modify missile launchers and payload weapons
        for (int i = 0; i < weaponDataCSV.length(); i++) {
            final JSONObject weaponDataCSVRow = weaponDataCSV.getJSONObject(i);
            if (weaponDataCSVRow.getString("tags").contains("doNotModify")) continue;
            final String weaponId = weaponDataCSVRow.getString("id");
            if (weaponId.isEmpty()) continue;
            final WeaponSpecAPI weaponSpec;
            try { weaponSpec = Global.getSettings().getWeaponSpec(weaponId); }
            catch (final Throwable t) { t.printStackTrace(); continue; }
            if (!(weaponSpec.getProjectileSpec() instanceof MissileSpecAPI)) continue;
            //modify launcher
            WeaponSpecs.modifyLauncherSpec(weaponSpec, Categorization.getLauncherWeaponCategory(
                    weaponSpec));
            //modify missile flight performance
            final MissileSpecAPI missileSpec = (MissileSpecAPI) weaponSpec.getProjectileSpec();
            final String missileId = missileSpec.getHullSpec().getHullId();
            if (!idsOfModifiedMissileSpecs.contains(missileId)) {
                modifyFlightMissileSpec(missileSpec);
                idsOfModifiedMissileSpecs.add(missileId);
            }
            //seek payload weapon
            final ShotBehaviorSpecAPI behaviorSpec = missileSpec.getBehaviorSpec();
            if (behaviorSpec == null || !behaviorSpec.getParams().has("payloadWeaponId"))
                continue;
            final String payloadWeaponId = behaviorSpec.getParams().getString(
                    "payloadWeaponId");
            final WeaponSpecAPI payloadWeaponSpec = Global.getSettings().getWeaponSpec(
                    payloadWeaponId);
            if (!payloadWeaponSpec.isBeam()) continue;
            //modify payload weapon
            WeaponSpecs.modifyBeamWeaponSpec((BeamWeaponSpecAPI) payloadWeaponSpec, true);
            idsOfModifiedPayloadWeaponSpecs.add(payloadWeaponId);
        }
        //modify remaining weapons
        for (int i = 0; i < weaponDataCSV.length(); i++) {
            final JSONObject weaponDataCSVRow = weaponDataCSV.getJSONObject(i);
            if (weaponDataCSVRow.getString("tags").contains("doNotModify")) continue;
            final String weaponId = weaponDataCSVRow.getString("id");
            if (weaponId.isEmpty()) continue;
            final WeaponSpecAPI weaponSpec;
            try { weaponSpec = Global.getSettings().getWeaponSpec(weaponId); }
            catch (final Throwable t) { t.printStackTrace(); continue; }
            if (weaponSpec.getProjectileSpec() instanceof MissileSpecAPI) continue;
            else if (!weaponSpec.isBeam()) {
                final ProjectileSpecAPI projectileSpec =
                        (ProjectileSpecAPI) weaponSpec.getProjectileSpec();
                final float muzzleVelocity = WeaponSpecs.getMuzzleVelocity(weaponSpec.getMaxRange(),
                        projectileSpec.getMoveSpeed(null, null));
                WeaponSpecs.modifyProjectileWeaponSpec(weaponSpec, muzzleVelocity);
                WeaponSpecs.modifyProjectileSpec(projectileSpec, muzzleVelocity);
            } else if (!idsOfModifiedPayloadWeaponSpecs.contains(weaponId)) {
                WeaponSpecs.modifyBeamWeaponSpec((BeamWeaponSpecAPI) weaponSpec, false);
            }
        }
    }

    @Override
    public void onApplicationLoad() throws JSONException {
        try {
            RealisticCombat.settings.Toggles.load();
            RealisticCombat.settings.Colors.load();
            RealisticCombat.settings.Map.load();
            RealisticCombat.settings.FreeCamera.load();
            RealisticCombat.settings.DamageModel.load();
            RealisticCombat.settings.ThreeDimensionalTargeting.load();
            RealisticCombat.settings.FleetRetreat.load();
            RealisticCombat.settings.Categorization.load();
            RealisticCombat.settings.WeaponSpecs.load();
            RealisticCombat.settings.ShipSpecs.load();
            RealisticCombat.settings.Indication.load();
            RealisticCombat.settings.Radar.load();
            RealisticCombat.settings.Radar.reloadRenderers();
            RealisticCombat.settings.BattleCreationPlugin.load();
        } catch (final JSONException | IOException e) {
            System.out.println("[ERROR] RealisticCombat.settings loaded incorrectly");
            e.printStackTrace();
        }

        for (final DamageType damageType : DamageType.values())
            damageType.setDescription(DamageModel.getDamageTypeDescription(damageType));

        if (isEveryShipSpecToBeModified()) modifyShipHullSpecsNonFighter();
        if (RealisticCombat.settings.Toggles.isEveryFighterSpecToBeModified()) {
            modifyShipHullSpecsFighter(); modifyFighterWingSpecs();
        } if (RealisticCombat.settings.Toggles.isEveryWeaponSpecToBeModified()) modifyWeaponSpecs();

        Global.getSettings().setBoolean("DetailedCombatResults_UseReportedDamagesOnlyV1",
                RealisticCombat.settings.Toggles.isDamageModelToBeReplaced());
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (RealisticCombat.settings.Toggles.isMapToBeModified())
            Global.getSector().registerPlugin(new CampaignPlugin());
    }
}