package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;

public class sfc_CompanyFuelBarEvent extends BaseGetCommodityBarEvent {

	//public static final float REP = 0.01f;
	
	public sfc_CompanyFuelBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		regen(market);
		
		if (!market.getFactionId().equals(Factions.DIKTAT)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.POST_AGENT;
	}

	@Override
	protected String getCommodityId() {
		return Commodities.FUEL;
	}
	
	@Override
	protected String getPersonFaction() {
		return Factions.DIKTAT;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.GROUND_CAPTAIN;
	}
	
	@Override
	protected int computeQuantity() {
		int quantity = 1000 + 1000 * random.nextInt(10);
		return quantity;
	}
	
	@Override
	protected float getPriceMult() {
		return 0.5f;
	}
	
	@Override
	protected String getPrompt() {
		return "A smiling Fuel Company representative with a TriPad is gesturing towards any nearby bar patron.";
	}
	
	@Override
	protected String getOptionText() {
		return "Approach the Fuel Company representative";
	}
	
	@Override
	protected String getMainText() {
		String heOrShe = getHeOrShe();
		String himOrHer = getHimOrHer();
		String hisOrHer = getHisOrHer();
		
		return "\"Hello and greetings, potential customer! " +
				"I hope you are having a wonderful day!\" " + Misc.ucFirst(heOrShe) + " sweeps " + hisOrHer + " arm regally " +
				"in an almost dramatic flair, somehow managing not to spill the drinks of " +
				"several patrons. You are told of an important opportunity that you have the chance of " +
				"partaking in. \"We at the Sindrian Fuel Company are having quite the sale of a recent bulk production of fuel! " +
				"Act now and you can get in on this deal!\"";
	}
	
	@Override
	protected String getMainText2() {
		String heOrShe = getHeOrShe();
		String himOrHer = getHimOrHer();
		String hisOrHer = getHisOrHer();
		
		return 
				"What this amounts to, you are told, is that there is currently a massive stockpile of Antimatter Fuel " +
				"available for sale. The representative talks on end how rare of an opportuntiy this is " +
				"and that there is currently %s units available for you to purchase. " + Misc.ucFirst(heOrShe) +
				" emphasizes that this opportunity won't last forever and that if you don't act now, " +
				"someone else may snatch up the deal. The representative finally informs you that all it will cost " +
				"you is %s. " + Misc.ucFirst(heOrShe) + " shoves a TriPad into your face and points at the dotted line.\n\n" +
				
				"\"Well? Are you ready to take this opportunity, potential customer?\"";
	}
	
	@Override
	protected String [] getMainText2Tokens() {
		return new String [] { Misc.getWithDGS(quantity), Misc.getDGSCredits(unitPrice * quantity) };
	}
	@Override
	protected Color [] getMainText2Colors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept the offer and transfer " + Misc.getDGSCredits(unitPrice * quantity) + " to the provided TriAnon account";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline the offer apologetically, saying that someone else deserves this";
	}

	@Override
	protected String getAcceptText() {
		String heOrShe = getHeOrShe();
		String himOrHer = getHimOrHer();
		String hisOrHer = getHisOrHer();

		return
				"The Fuel Company representative congratulates you on your smart decision as " + heOrShe +
				" receives the credits. Before long, a shipment of Antimatter Fuel is delivered to your fleet.";
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] {};
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] {};
	}
	
}



