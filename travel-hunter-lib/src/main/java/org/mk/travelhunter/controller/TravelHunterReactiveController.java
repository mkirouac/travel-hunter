package org.mk.travelhunter.controller;

import java.time.Duration;
import java.util.logging.Level;

import org.mk.travelhunter.HotelProvider;
import org.mk.travelhunter.TravelDeal;
import org.mk.travelhunter.TravelDealFilter;
import org.mk.travelhunter.dealtracker.DealTracker;
import org.mk.travelhunter.dealtracker.DealTrackingReactiveService;
import org.mk.travelhunter.security.SecurityController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class TravelHunterReactiveController implements TravelHunterController {

	
	private final HotelProvider hotels;
	private final DealTrackingReactiveService dealTrackingService;
	private final SecurityController securityController;
	
	@Autowired
	public TravelHunterReactiveController(
			HotelProvider hotels, 
			DealTrackingReactiveService dealTrackingService, 
			SecurityController securityController) {
		this.hotels = hotels;
		this.dealTrackingService = dealTrackingService;
		this.securityController = securityController;
	}


	@Override
	public void requestHotelIdentifiers(TravelHunterView source) {
		//TODO Reactive
		source.displayHotels(hotels.getHotelIdentifiers());
	}


	@Override
	public void requestDealTrackers(TravelHunterView source) {
		
		if(!securityController.isUserAuthenticated()) {
			throw new SecurityException("This operation (requestDealTrackers) is restricted to authenticated users");
		}
		
		String userId = securityController.getUserName();
		String userRealm = securityController.getAuthenticationProvider();
		
		dealTrackingService.getDealTrackers(userId, userRealm)
			.delayElements(Duration.ofMillis(200))//Just to show this in action
			.subscribe(dealTracker -> {
				source.displayDealTracker(dealTracker);
			});
	}


	@Override
	public void beginSavingDealTracker(TravelHunterView source, DealTracker dealTracker) {
		
		if(!securityController.isUserAuthenticated()) {
			throw new SecurityException("This operation (beginSavingDealTracker) is restricted to authenticated users");
		}
		
		dealTracker.setUserId(securityController.getUserName());
		dealTracker.setUserRealm(securityController.getAuthenticationProvider());
		
		dealTrackingService.saveDealTracker(dealTracker)
			.subscribe(savedDealTracker -> {
				source.displayDealTracker(dealTracker);
			});
	}


	@Override
	public void beginSearchingForDeals(TravelHunterView source, TravelDealFilter filter) {
		
		//TODO Maybe we need a single flux and cancel all items should a new request arrive?
		Flux<TravelDeal> search = hotels.searchDeals(filter);
		
		search
			.doFirst(() -> {
				log.debug("Clearing all search results");
				source.clearAllSearchResults();
			})

			//Schedulers.enableMetris() -> uses Micrometer. Worth having a look.
			
			.subscribeOn(Schedulers.parallel())
			
			.buffer(20)
			
			.doOnComplete(() -> {
				log.debug("Search completed");
				source.displaySearchCompletedNotification();
			})
			.doOnError((ex) -> {
				source.displaySearchError(ex);
			})
			
			.log("controller", Level.FINEST)
			.subscribe(deals -> {
				log.debug("Received {} deals from subscription", deals.size());
				//source.addSearchResult(deal);
				source.addSearchResults(deals);
			})
			;
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
				//source.deleteDealTracker(dealTracker);
			})

		;
		
	}
	
}
