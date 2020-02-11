package org.mk.travelhunter.controller;

import java.time.Duration;

import org.mk.travelhunter.HotelProvider;
import org.mk.travelhunter.TravelDealFilter;
import org.mk.travelhunter.tracker.DealTracker;
import org.mk.travelhunter.tracker.DealTrackingReactiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TravelHunterReactiveController implements TravelHunterController {

	
	private final HotelProvider hotels;
	private final DealTrackingReactiveService dealTrackingService;
	
	
	@Autowired
	public TravelHunterReactiveController(HotelProvider hotels, DealTrackingReactiveService dealTrackingService) {
		this.hotels = hotels;
		this.dealTrackingService = dealTrackingService;
	}


	@Override
	public void requestHotelIdentifiers(TravelHunterView source) {
		//TODO Reactive
		source.displayHotels(hotels.getHotelIdentifiers());
	}


	@Override
	public void requestDealTrackers(TravelHunterView source, String userId) {
		
		dealTrackingService.getDealTrackers(userId)
			.delayElements(Duration.ofMillis(200))//Just to show this in action
			.subscribe(dealTracker -> {
				source.displayDealTracker(dealTracker);
			});
	}


	@Override
	public void beginSavingDealTracker(TravelHunterView source, DealTracker dealTracker) {
		dealTrackingService.saveDealTracker(dealTracker)
			.subscribe(savedDealTracker -> {
				source.displayDealTracker(dealTracker);
			});
	}


	@Override
	public void beginSearchingForDeals(TravelHunterView source, TravelDealFilter filter) {
		
		//TODO Maybe we need a single flux and cancel all items should a new request arrive?
		hotels.searchDeals(filter)
			.doFirst(() -> {
				source.clearAllSearchResults();
			})
			.doOnComplete(() -> {
				source.displaySearchCompletedNotification();
			})
			.doOnError((ex) -> {
				source.displaySearchError(ex);
			})
			.subscribe(deal -> {
				source.addSearchResult(deal);
			});
	}


	@Override
	public void beginDeleteDealTracker(TravelHunterView source, DealTracker dealTracker) {
		dealTrackingService.deleteDealTracker(dealTracker)

			.doOnSuccess(s -> {
				// TODO subscribe consumer doesn't get called - need to find why. Workaround
				// here is just to delete this from the finally (which do get called)
				// TODO That would also be called on failure
				source.deleteDealTracker(dealTracker);
			})

			.subscribe(v -> {
				source.deleteDealTracker(dealTracker);
			})

		;
		
	}
	
	
}
