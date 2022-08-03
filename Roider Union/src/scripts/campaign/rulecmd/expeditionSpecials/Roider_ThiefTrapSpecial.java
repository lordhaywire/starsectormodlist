package scripts.campaign.rulecmd.expeditionSpecials;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import ids.Roider_MemFlags;

/**
 * Author: SafariJohn
 */
public class Roider_ThiefTrapSpecial extends BaseSalvageSpecial {


	public static class Roider_ThiefTrapSpecialData implements SalvageSpecialData {
		public Roider_ThiefTrapSpecialData() {
		}

		public SalvageSpecialPlugin createSpecialPlugin() {
			return new Roider_ThiefTrapSpecial();
		}
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);

		initEntityLocation();
	}

	private void initEntityLocation() {

        setDone(true);
        setEndWithContinue(true);
        setShowAgain(false);

		if (random.nextFloat() > 0.5f) {
            addText("Your salvage crews discover a miniaturized phase coil "
                        + "on the $shortName connected to an alarm system. "
                        + "Some roiders have recently begun using traps "
                        + "like this to detect theives. "
                        + "Your tactical officer reports that everything "
                        + "in the local volume, including your $shipOrFleet, "
                        + "has been contaminated with minute phase distortions.");
            markThief();
        } else {
			if (random.nextFloat() > 0.5f) {
                addText("Your salvage crews discover a miniaturized phase coil "
                            + "on the $shortName connected to an alarm system. "
                            + "Some roiders have recently begun using traps "
                            + "like this to detect theives. "
                            + "The alarm went off as intended, but the "
                            + "power supply was insufficient to activate "
                            + "the phase coil.");
            } else {
                addText("Your salvage crews discover a miniaturized phase coil "
                            + " and an alarm system on the $shortName. "
                            + "Some roiders have recently begun using traps "
                            + "like this to detect theives. "
                            + "This example was never finished - the "
                            + "trap-makers seem to have left in a hurry.");
            }
        }
	}

    private void markThief() {
        String thiefId = entity.getMemoryWithoutUpdate().getString(Roider_MemFlags.THIEF_KEY);
        if (thiefId != null) {
            playerFleet.getMemoryWithoutUpdate().set(Roider_MemFlags.THIEF_KEY + thiefId, true);
        }
    }
}
