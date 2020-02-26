package org.mk.travelhunter.dealtracker;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

public interface DealTrackerRepository extends ReactiveMongoRepository<DealTracker, UUID> {

	
	public Flux<DealTracker> findAllByUserIdAndUserRealm(String userId, String userRealm);
	
}
