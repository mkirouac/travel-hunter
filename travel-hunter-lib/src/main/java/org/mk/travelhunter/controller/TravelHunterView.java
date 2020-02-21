package org.mk.travelhunter.controller;

import java.util.Collection;
import java.util.List;

import org.mk.travelhunter.HotelIdentifier;
import org.mk.travelhunter.TravelDeal;
import org.mk.travelhunter.dealtracker.DealTracker;

public interface TravelHunterView {
	//TODO Review all wordings AND consider splitting this in multiple views
	void displayDealTracker(DealTracker dealTracker);
	void displayHotels(Collection<HotelIdentifier> hotelIdentifiers);//TODO Should be reactive
	void clearAllSearchResults();
	void displaySearchCompletedNotification();
	void displaySearchError(Throwable ex);
	void addSearchResult(TravelDeal deal);
	void addSearchResults(List<TravelDeal> deals);
	void deleteDealTracker(DealTracker dealTracker);
	
	
}
