package org.mk.travelhunter.tracker;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

@Service
public class DealTrackingServiceImpl implements DealTrackingService {

	private final DealTrackerRepository dealTrackerRepository;
	
	public DealTrackingServiceImpl(DealTrackerRepository dealTrackerRepository) {
		this.dealTrackerRepository = dealTrackerRepository;
	}

	@Override
	public void addDealTracker(DealTracker dealTracker) {
		
		Validate.notNull(dealTracker, "addDealTracker(dealTracker) is expecting a non-null dealTracker");
		
		dealTrackerRepository.save(dealTracker);
	}

	@Override
	public List<DealTracker> getDealTrackers(String userId) {
		
		Validate.notNull(userId, "getDealTracker(userId) is expecting a non-null userId");
		
		List<DealTracker> trackers = dealTrackerRepository.findAllByUserId(userId);
		
		return trackers;
				
	}
	
	
	
}
