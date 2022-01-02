package scripts.combat;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Roider_DreadLoaderStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS = 50f;
	public static final float ROF_BONUS = 33f;
	public static final float FLUX_BONUS = 25f;

    public static final Map<String, String> GLOW_MAPS = new HashMap<>();
    static {
        GLOW_MAPS.put("roider_sheriff", "sheriff_glow1");
        GLOW_MAPS.put("roider_marza", "marza_glow");
    }

    public static final Color GLOW_COLOR = new Color(255, 0, 0, 255);

	public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

		float damageBonus = 1f + (DAMAGE_BONUS / 100f) * effectLevel;
		stats.getBallisticWeaponDamageMult().modifyMult(id, damageBonus);

        float rofBonus = 1f + (ROF_BONUS / 100f) * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id, rofBonus);

        float fluxBonus = 1f - (FLUX_BONUS / 100f) * effectLevel;
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, fluxBonus);

        //Ensures we have a ship
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        // Copied visuals code with permission from MesoTroniK's tiandong_fluxOverrideStats.java
        WeaponAPI glowDeco = null;
        for (WeaponAPI weapon : ship.getAllWeapons())
        {
            if (weapon.getId().endsWith("_glow") && weapon.getId().startsWith("roider_"))
            {
                glowDeco = weapon;
            }
        }

        ship.setJitter(ship, GLOW_COLOR, effectLevel / 30f, 6, 30f);
        ship.setJitterUnder(ship, GLOW_COLOR, effectLevel / 8f, 10, 50f);
        if (glowDeco != null && glowDeco.getAnimation() != null)
        {
            glowDeco.getSprite().setAdditiveBlend();
            glowDeco.getAnimation().setAlphaMult(effectLevel);
            glowDeco.getAnimation().setFrame(1);
        }
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponDamageMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
	}

	public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS * effectLevel;
        float rofBonus = ROF_BONUS * effectLevel;
        float fluxBonus = FLUX_BONUS * effectLevel;
		if (index == 0) {
			return new ShipSystemStatsScript.StatusData("-" + (int) fluxBonus + "% ballistic flux cost" , false);
		} else if (index == 1) {
			return new ShipSystemStatsScript.StatusData("+" + (int) rofBonus + "% ballistic rate of fire" , false);
        } else if (index == 2) {
			return new ShipSystemStatsScript.StatusData("+" + (int) bonusPercent + "% ballistic weapon damage" , false);
        }
		return null;
	}
}
