package org.mk.travelhunter.dealtracker;

import java.util.UUID;

import org.mk.travelhunter.TravelDealFilter;
import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class DealTracker {
	
	@Id
	private final UUID id;
	private final String name;
	private final String userId;
	private final TravelDealFilter filter;
	
	public DealTracker(UUID id, String name, String userId, TravelDealFilter filter) {
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.filter = filter;
	}
	
	
	
}
