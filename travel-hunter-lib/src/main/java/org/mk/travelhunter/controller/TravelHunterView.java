package org.mk.travelhunter.controller;

import java.util.Collection;

import org.mk.travelhunter.HotelIdentifier;
import org.mk.travelhunter.TravelDeal;
import org.mk.travelhunter.tracker.DealTracker;

public interface TravelHunterView {
	//TODO Review all wordings AND consider splitting this in multiple views
	void displayDealTracker(DealTracker dealTracker);
	void displayHotels(Collection<HotelIdentifier> hotelIdentifiers);//TODO Should be reactive
	void clearAllSearchResults();
	void displaySearchCompletedNotification();
	void displaySearchError(Throwable ex);
	void addSearchResult(TravelDeal deal);
	void deleteDealTracker(DealTracker dealTracker);
	
}
