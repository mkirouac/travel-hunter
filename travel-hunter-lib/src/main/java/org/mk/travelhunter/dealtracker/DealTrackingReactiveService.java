package org.mk.travelhunter.dealtracker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DealTrackingReactiveService {

	Mono<DealTracker> saveDealTracker(DealTracker dealTracker);
	
	Flux<DealTracker> getDealTrackers(String userId);

	Mono<Void> deleteDealTracker(DealTracker dealTracker);
	
}
