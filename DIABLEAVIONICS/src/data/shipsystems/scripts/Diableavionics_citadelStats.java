package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_citadelStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getMaxSpeed().modifyMult(id, 1-(effectLevel*0.75f));
        stats.getMaxTurnRate().modifyPercent(id, effectLevel*50f);

        stats.getShieldDamageTakenMult().modifyMult(id, 1-(effectLevel*0.75f));

        stats.getWeaponTurnRateBonus().modifyPercent(id, effectLevel*50f);
        stats.getAutofireAimAccuracy().modifyPercent(id, effectLevel);
        stats.getMaxRecoilMult().modifyMult(id, 1-(effectLevel*0.5f));
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, effectLevel*50f);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, effectLevel*50f);
        
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1-(effectLevel*0.75f));
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1-(effectLevel*0.75f));
        
        stats.getMissileWeaponRangeBonus().modifyPercent(id, effectLevel*50f);
        stats.getMissileGuidance().modifyPercent(id, effectLevel*50f);
        
        stats.getFluxDissipation().modifyMult(id, 1-effectLevel);
//        stats.getShieldUpkeepMult().modifyMult(id, 1-(effectLevel*0.75f));
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);

        stats.getShieldDamageTakenMult().unmodify(id);

        stats.getWeaponTurnRateBonus().unmodify(id);
        stats.getAutofireAimAccuracy().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        
        stats.getMissileWeaponRangeBonus().unmodify(id);
        stats.getMissileGuidance().unmodify(id);
        
        stats.getFluxDissipation().unmodify(id);
//        stats.getShieldUpkeepMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
//        if (index == 0) {
//            return new StatusData(txt("citadel"), false);
//        }
        if (index == 0) {
            return new StatusData( txt("+") + Math.round(effectLevel*50) + txt("citadel1"), false);
        }
        if (index == 1) {
            return new StatusData( txt("-") + Math.round(effectLevel*75) + txt("citadel2"), false);
        }
        if (index == 2) {
            return new StatusData( txt("+") + Math.round(effectLevel*75) + txt("citadel3"), false);
        }
        if (index == 3) {
            return new StatusData( txt("-") + Math.round(effectLevel*75) + txt("citadel4"), true);
        }
        if (index == 4) {
            return new StatusData( txt("-") + Math.round(effectLevel*100) + txt("citadel5"), true);
        }
        return null;
    }
}
