package org.mk.travelhunter.tracker;

import java.util.List;

public interface DealTrackingService {

	void addDealTracker(DealTracker dealTracker);
	
	List<DealTracker> getDealTrackers(String userId);
	
}
