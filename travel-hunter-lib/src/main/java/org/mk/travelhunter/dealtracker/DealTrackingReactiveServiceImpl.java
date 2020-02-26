package org.mk.travelhunter.dealtracker;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DealTrackingReactiveServiceImpl implements DealTrackingReactiveService {

	private final DealTrackerRepository dealTrackerRepository;
	
	public DealTrackingReactiveServiceImpl(DealTrackerRepository dealTrackerRepository) {
		this.dealTrackerRepository = dealTrackerRepository;
	}

	@Override
	public Mono<DealTracker> saveDealTracker(DealTracker dealTracker) {
		
		Validate.notNull(dealTracker, "addDealTracker(dealTracker) is expecting a non-null dealTracker");
		
		return dealTrackerRepository.save(dealTracker);
	}

	@Override
	public Flux<DealTracker> getDealTrackers(String userId, String userRealm) {
		
		Validate.notNull(userId, "getDealTracker(userId) is expecting a non-null userId");
		
		Flux<DealTracker> trackers = dealTrackerRepository.findAllByUserIdAndUserRealm(userId, userRealm);
		
		return trackers;
				
	}

	@Override
	public Mono<Void> deleteDealTracker(DealTracker dealTracker) {
		return dealTrackerRepository.delete(dealTracker);
	}
	
	
	
}
