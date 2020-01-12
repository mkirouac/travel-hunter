package org.mk.travelhunter.tracker;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DealTrackerRepository extends MongoRepository<DealTracker, UUID> {

	
	public List<DealTracker> findAllByUserId(String userId);
	
}
