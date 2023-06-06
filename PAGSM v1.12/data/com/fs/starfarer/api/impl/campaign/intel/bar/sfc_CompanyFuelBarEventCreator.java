package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.sfc_CompanyFuelBarEvent;

public class sfc_CompanyFuelBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new sfc_CompanyFuelBarEvent();
	}

	@Override
	public float getBarEventFrequencyWeight() {
		return super.getBarEventFrequencyWeight() * 0.5f;
	}
	
}
