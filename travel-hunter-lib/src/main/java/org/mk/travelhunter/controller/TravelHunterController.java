package org.mk.travelhunter.controller;

import org.mk.travelhunter.TravelDealFilter;
import org.mk.travelhunter.dealtracker.DealTracker;

//TODO Don't belong here
public interface TravelHunterController {

	//TODO Is there a way to autowire this, maybe using prototype? So we don't have to pass source for each method
	void requestDealTrackers(TravelHunterView source, String testUserId);
	void requestHotelIdentifiers(TravelHunterView source);
	void beginSavingDealTracker(TravelHunterView source, DealTracker dealTracker);
	void beginSearchingForDeals(TravelHunterView source, TravelDealFilter filter);
	void beginDeleteDealTracker(TravelHunterView source, DealTracker item);
}
