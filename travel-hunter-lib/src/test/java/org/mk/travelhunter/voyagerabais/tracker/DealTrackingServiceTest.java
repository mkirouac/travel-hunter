package org.mk.travelhunter.voyagerabais.tracker;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mk.travelhunter.HotelIdentifier;
import org.mk.travelhunter.TravelDealFilter;
import org.mk.travelhunter.tracker.DealTracker;
import org.mk.travelhunter.tracker.DealTrackerRepository;
import org.mk.travelhunter.tracker.DealTrackingReactiveService;
import org.mk.travelhunter.tracker.DealTrackingReactiveServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@EnableMongoRepositories("org.mk.travelhunter.tracker")
@EnableReactiveMongoRepositories("org.mk.travelhunter.tracker")
@DataMongoTest
@ContextConfiguration(classes = { DealTrackingServiceTest.class } )
public class DealTrackingServiceTest {

	//TODO These tests were written before the repository was reactive. 
	
	private DealTrackingReactiveService dealTrackingService;
	
	@Autowired
	private DealTrackerRepository dealTrackerRepository;

	@Before
	public void setUp() {
		dealTrackingService = new DealTrackingReactiveServiceImpl(dealTrackerRepository);
		dealTrackerRepository.deleteAll().block();
	}
	
	@Test 
	public void itShouldAddOneDealTracker() {
		
		//prepare
		DealTracker tracker = dealTracker("test-user-id", "my-test-tracker");
		dealTrackingService.saveDealTracker(tracker).block();
			
		//execute
		List<DealTracker> trackers = dealTrackingService.getDealTrackers("test-user-id").collectList().block();
		
		//assert
		assertThat(trackers).isNotNull();
		assertThat(trackers.size()).isEqualTo(1);
		assertThat(trackers.get(0)).isEqualTo(tracker);
		
	}
	
	@Test
	public void itShouldAddMultipleDealTrackersForOneUser() {
		
		//prepare
		DealTracker tracker1 = dealTracker("test-user-id", "my-test-tracker-1");
		DealTracker tracker2 = dealTracker("test-user-id", "my-test-tracker-2");
		dealTrackingService.saveDealTracker(tracker1).block();
		dealTrackingService.saveDealTracker(tracker2).block();
			
		//execute
		List<DealTracker> trackers = dealTrackingService.getDealTrackers("test-user-id").collectList().block();
		
		//assert
		assertThat(trackers).isNotNull();
		assertThat(trackers.size()).isEqualTo(2);
		assertThat(trackers).contains(tracker1, tracker2);
	}
	
	@Test
	public void itShouldAddMultipleDealTrackersForMultipleUsers() {
		
		//prepare
		DealTracker tracker1 = dealTracker("test-user-id-1", "my-test-tracker-1");
		DealTracker tracker2 = dealTracker("test-user-id-1", "my-test-tracker-2");
		DealTracker tracker3 = dealTracker("test-user-id-2", "my-test-tracker-3");
		dealTrackingService.saveDealTracker(tracker1).block();
		dealTrackingService.saveDealTracker(tracker2).block();
		dealTrackingService.saveDealTracker(tracker3).block();
			
		//execute
		List<DealTracker> trackersForUser1 = dealTrackingService.getDealTrackers("test-user-id-1").collectList().block();
		List<DealTracker> trackersForUser2 = dealTrackingService.getDealTrackers("test-user-id-2").collectList().block();
		
		//assert user 1
		assertThat(trackersForUser1).isNotNull();
		assertThat(trackersForUser1.size()).isEqualTo(2);
		assertThat(trackersForUser1).contains(tracker1, tracker2);
		
		//assert user 2
		assertThat(trackersForUser2).isNotNull();
		assertThat(trackersForUser2.size()).isEqualTo(1);
		assertThat(trackersForUser2).contains(tracker3);
	}
	
	@Test
	public void noTrackersShouldReturnEmptyList() {
		
		//execute
		List<DealTracker> trackers = dealTrackingService.getDealTrackers("test-user-id-1").collectList().block();
		
		//assert
		assertThat(trackers).isNotNull();
		assertThat(trackers.size()).isEqualTo(0);
	}
	
	@Test(expected = NullPointerException.class)
	public void addingDealTrackersWithNullUserIdShouldThrowNullPointerException() {
		
		dealTrackingService.saveDealTracker(null);
		
	}
	
	@Test(expected = NullPointerException.class)
	public void gettingDealTrackersWithNullUserIdShouldThrowNullPointerException() {
		
		dealTrackingService.getDealTrackers(null);
		
	}
	
	private DealTracker dealTracker(String userId, String trackerName) {
		TravelDealFilter filter = TravelDealFilter
				.builder()
				.startDate(LocalDate.ofYearDay(2000, 01))
				.endDate(LocalDate.ofYearDay(2000, 05))
				.hotel(new HotelIdentifier("tt", "test-hotel"))
				.build();
		
		DealTracker tracker = new DealTracker(UUID.randomUUID(), trackerName, userId, filter);
		return tracker;
	}
}
