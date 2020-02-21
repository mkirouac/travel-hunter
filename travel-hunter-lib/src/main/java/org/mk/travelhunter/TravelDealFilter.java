package org.mk.travelhunter;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TravelDealFilter {

	private final HotelIdentifier hotel;
	private final LocalDate startDate;
	private final LocalDate endDate;
}
