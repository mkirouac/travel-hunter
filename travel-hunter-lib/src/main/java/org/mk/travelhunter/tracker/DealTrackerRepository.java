package org.mk.travelhunter.tracker;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

public interface DealTrackerRepository extends ReactiveMongoRepository<DealTracker, UUID> {

	
	public Flux<DealTracker> findAllByUserId(String userId);
	
}
