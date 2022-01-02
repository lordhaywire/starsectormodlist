package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.util.UW_Util;
import java.awt.Color;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_CabalUpgrades extends BaseHullMod {

    private static final float ARMOR_MULT = 1.05f;
    private static final float FLUX_MULT = 1.05f;
    private static final float HANDLING_MULT = 1.05f;
    private static final float LOGISTICS_PENALTY = 50f;
    private static final float SHIELD_MULT = 1.05f;

    private static final float MAX_SPARKLE_CHANCE_PER_SECOND_PER_CELL = 0.75f;
    private static final Color SPARK_COLOR = new Color(190, 60, 255, 175);
    private static final float SPARK_DURATION = 0.2f;
    private static final float SPARK_RADIUS = 4f;

    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        float fluxLevel = ship.getFluxTracker().getFluxLevel();

        ArmorGridAPI armorGrid = ship.getArmorGrid();
        Color color = new Color(SPARK_COLOR.getRed(), SPARK_COLOR.getGreen(), SPARK_COLOR.getBlue(), UW_Util.clamp255((int) (SPARK_COLOR.getAlpha() * (1f - fluxLevel))));
        for (int x = 0; x < armorGrid.getGrid().length; x++) {
            for (int y = 0; y < armorGrid.getGrid()[0].length; y++) {
                float armorLevel = armorGrid.getArmorValue(x, y);
                if (armorLevel <= 0f) {
                    continue;
                }

                float chance = amount * (1f - fluxLevel) * MAX_SPARKLE_CHANCE_PER_SECOND_PER_CELL * armorLevel / armorGrid.getMaxArmorInCell();
                if (Math.random() >= chance) {
                    continue;
                }

                float cellSize = armorGrid.getCellSize();
                Vector2f cellLoc = getCellLocation(ship, x, y);
                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
                if (CollisionUtils.isPointWithinBounds(cellLoc, ship)) {
                    engine.addHitParticle(cellLoc, ship.getVelocity(), 0.5f * SPARK_RADIUS * (float) Math.random() + SPARK_RADIUS, 1f, SPARK_DURATION,
                            UW_Util.colorJitter(color, 50f));
                }
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyMult(id, ARMOR_MULT);
        stats.getHullBonus().modifyMult(id, ARMOR_MULT);

        stats.getFluxCapacity().modifyMult(id, FLUX_MULT);
        stats.getFluxDissipation().modifyMult(id, FLUX_MULT);

        stats.getShieldUpkeepMult().modifyMult(id, 1f / SHIELD_MULT);
        stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_MULT);
        stats.getShieldTurnRateMult().modifyMult(id, SHIELD_MULT);
        stats.getShieldArcBonus().modifyMult(id, SHIELD_MULT);
        stats.getShieldDamageTakenMult().modifyMult(id, 1 / SHIELD_MULT);
        stats.getPhaseCloakActivationCostBonus().modifyMult(id, 1 / SHIELD_MULT);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1 / SHIELD_MULT);

        stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
        stats.getAcceleration().modifyMult(id, HANDLING_MULT);
        stats.getDeceleration().modifyMult(id, HANDLING_MULT);
        stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
        stats.getTurnAcceleration().modifyMult(id, HANDLING_MULT);

        stats.getSuppliesPerMonth().modifyPercent(id, LOGISTICS_PENALTY);
        stats.getSuppliesToRecover().modifyPercent(id, LOGISTICS_PENALTY);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + Math.round((ARMOR_MULT - 1f) * 100f) + "%";
        }
        if (index == 1) {
            return "" + Math.round((FLUX_MULT - 1f) * 100f) + "%";
        }
        if (index == 2) {
            return "" + Math.round((HANDLING_MULT - 1f) * 100f) + "%";
        }
        if (index == 3) {
            return "" + Math.round((SHIELD_MULT - 1f) * 100f) + "%";
        }
        if (index == 4) {
            return "" + Math.round(LOGISTICS_PENALTY) + "%";
        }
        return null;
    }
}
