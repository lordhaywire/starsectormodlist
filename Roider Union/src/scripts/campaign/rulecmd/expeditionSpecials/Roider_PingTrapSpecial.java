package scripts.campaign.rulecmd.expeditionSpecials;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;

/**
 * Author: SafariJohn
 */
public class Roider_PingTrapSpecial extends BaseSalvageSpecial {


	public static class Roider_PingTrapSpecialData implements SalvageSpecialData {
		public Roider_PingTrapSpecialData() {
		}

		public SalvageSpecialPlugin createSpecialPlugin() {
			return new Roider_PingTrapSpecial();
		}
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);

		if (random.nextFloat() > 0.5f) {
            addText("As your salvage crews begin their work, a device inside the $shortName " +
					"broadcasts your fleet composition and current position for all and sundry to see.");
            Global.getSector().addPing(entity, "roider_ping_trap");
            Global.getSoundPlayer().playUISound("ui_sensor_burst_on", 1f, 1f);
            Global.getSector().getPlayerFleet().addScript(new Roider_PingTrapScript());
        } else {
			if (random.nextFloat() > 0.5f) {
				addText("Your salvage crews discover a device set to broadcast your fleet composition when " +
						"tripped by an alarm system, but the two aren't hooked up. " +
						"Scattered tools indicate the trap-makers left in a hurry.");
			} else {
				addText("Your salvage crews discover a device set to broadcast your fleet composition when " +
						"tripped by an alarm system. The alarm went off as intended, but the scanner " +
						"fried the power supply before the broadcast was ready.");
			}
        }

        setDone(true);
        setEndWithContinue(true);
        setShowAgain(false);
	}
}
