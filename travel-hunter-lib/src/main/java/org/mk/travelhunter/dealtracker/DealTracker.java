package org.mk.travelhunter.dealtracker;

import java.util.UUID;

import org.mk.travelhunter.TravelDealFilter;
import org.springframework.data.annotation.Id;

import com.mongodb.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO Why was this working with final fields before but now MongoDB complains that no ctor is found?
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealTracker {
	
	@Id
	private UUID id;
	
	@NonNull
	private String name;
	
	@NonNull
	private TravelDealFilter filter;
	
	@NonNull
	private String userId;
	
	@NonNull
	private String userRealm;
	
	public DealTracker(String name, TravelDealFilter filter) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.filter = filter;
	}
	
	//Required for SpringData
	public DealTracker(UUID id, String name, TravelDealFilter filter) {
		this.id = id;
		this.name = name;
		this.filter = filter;
	}
	
	
	
	
}
