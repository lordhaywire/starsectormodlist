package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.hullmods.acs_frigatehangar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class acs_fleetlogistic2 extends BaseHullMod implements HullModFleetEffect {

	public static final float BURN_BONUS = 1;
	public static final float SENSOR_PROFILE = 200f;
    public static float MIN_CR = 0.1f;

    private static final float FUEL_ADDED_VAGABOND = 500f;
    private static int BURN_LEVEL_BONUS = 1;
    private static final float MAINTENANCE_MULT_VAGABOND = 0.25f;
    private static final float DP_BONUS = 0.05f;
    private boolean isTrue = false;

    public static final String ID = "acs_fleetgantry_bonus";

    // the fleet buff code is mostly stolen from Approlight (which very much inspired the effect)
    // it's way cleaner than the "use a manager campaign plugin" method that I was going to use
    // (this also presents a clean way for hullmods to buff all sorts of other ship stats for your whole fleet)

    //IE: I stole the codes from Apex which in turn stole from Approlight, either way thanks

	
	@Override
    public void advanceInCampaign(CampaignFleetAPI fleet)
    {
        if (!fleet.isInCurrentLocation())
            return;
        int bonus = getBonus(fleet);
        fleet.getCustomData().put(ID, bonus);
        if (bonus >= 1)
        {
            boolean sync = false;
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
            {
                if (member.getVariant() != null && !member.isMothballed())
                {
                    BuffManagerAPI.Buff buff = member.getBuffManager().getBuff(ID);
                    if (buff instanceof acs_fleetbonus)
                    {
                        ((acs_fleetbonus) buff).update();
                    } else {
                        member.getBuffManager().addBuff(new acs_fleetbonus(0.1f));
                        sync = true;
                    }
                }
            }
            if (sync) fleet.forceSync();
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        stats.getDynamic().getStat("numFrigBays").modifyFlat(id, 2); //this will result in a value of 2, as dynamic stats have a starting value of 1 when they're made
        
    }

    @Override
    public boolean withAdvanceInCampaign() { return true; }

    @Override
    public boolean withOnFleetSync() { return false; }

    @Override
    public void onFleetSync(CampaignFleetAPI campaignFleetAPI) {}

    public int getBonus(CampaignFleetAPI fleet)
    {
        if (Global.getSector() == null)
            return 0;
        if (fleet == null)
            return 0;
        int numLogisticBonus = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            if (!member.isMothballed() && member.getVariant().hasHullMod("acs_fleetlogistic2"))
                numLogisticBonus++;
        }
        return numLogisticBonus;
    }

    public static class acs_fleetbonus implements BuffManagerAPI.Buff
    {
        private float duration;

        public acs_fleetbonus(float duration)
        {
            this.duration = duration;
        }

        public void update()
        {
            duration = 0.1f;
        }

        @Override
        public void apply(FleetMemberAPI member)
        {
            if (member == null || member. getFleetData() == null || member.getFleetData().getFleet() == null)
                return;
            if (!member.getFleetData().getFleet().getCustomData().containsKey(ID))
                return;
            //float bonus = (float)member.getFleetData().getFleet().getCustomData().get(ID);

            member.getStats().getSuppliesPerMonth().modifyMult(member.getId(), MAINTENANCE_MULT_VAGABOND);
            member.getStats().getMaxBurnLevel().modifyFlat(member.getId(), BURN_LEVEL_BONUS);
            member.getStats().getSuppliesToRecover().modifyMult(member.getId(), DP_BONUS);
            // if (member.getStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).) {
                
            // }
            member.getStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(member.getId(), DP_BONUS);

            //member.getStats().getRepairRatePercentPerDay().modifyMult(ID, bonus);
            //member.getStats().getBaseCRRecoveryRatePercentPerDay().modifyMult(ID, bonus);
        }


        @Override
        public String getId()
        {
            return ID;
        }

        @Override
        public boolean isExpired()
        {
            return duration < 0f;
        }

        @Override
        public void advance(float amount)
        {
            duration -= amount;
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) MAINTENANCE_MULT_VAGABOND + "%";
        if (index == 1) return "" + (int) DP_BONUS + "%";
        if (index == 2) return "" + "2";
        if (index == 3) return "" + (int) BURN_LEVEL_BONUS;
		return null;

        
	}


}

