package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.bounty.ActiveBounty;
import org.magiclib.bounty.MagicBountyCoordinator;
import org.magiclib.bounty.MagicBountyIntel;

import java.util.List;
import java.util.Map;

/**
 * Not used. This is an example of a bounty script that can run on accept and on completion.
 * This is triggered by a trigger defined in rules.csv.
 *
 * @author Wisp
 */
public class sfc_bountyArthurScript extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String sfc_titansTerror = "sfc_titansTerror";
        ActiveBounty bounty;
        PersonAPI cap = Global.getSector().getImportantPeople().getPerson("sfcarthur");

        // Try to get the bounty. The bounty id is the key of the json object that defines the bounty (eg "test_bounty").
        try {
            bounty = MagicBountyCoordinator.getInstance().getActiveBounty(sfc_titansTerror);
            MagicBountyCoordinator.getInstance().getActiveBounty(sfc_titansTerror).getFleet().getFlagship().setCaptain(cap);
            MagicBountyCoordinator.getInstance().getActiveBounty(sfc_titansTerror).getFleet().setCommander(cap);

            if (bounty == null) {
                throw new NullPointerException();
            }
        } catch (Exception ex) {
            Global.getLogger(sfc_bountyArthurScript.class).error("Unable to get MagicBounty: " + sfc_titansTerror, ex);
            return true;
        }

        //ActiveBounty.Stage bountyStage = bounty.getStage();
        //MagicBountyIntel intel = bounty.getIntel();
        //PersonAPI cap = Global.getSector().getImportantPeople().getPerson("sfcarthur");

        //MagicBountyCoordinator.getInstance().getActiveBounty(sfc_titansTerror).getFleet().getFlagship().setCaptain(cap);
        //throw new RuntimeException("it worked!");

        return true;
    }
}
