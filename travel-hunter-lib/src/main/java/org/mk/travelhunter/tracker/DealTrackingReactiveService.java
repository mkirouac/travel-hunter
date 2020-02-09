package org.mk.travelhunter.tracker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DealTrackingReactiveService {

	Mono<DealTracker> saveDealTracker(DealTracker dealTracker);
	
	Flux<DealTracker> getDealTrackers(String userId);
	
}
