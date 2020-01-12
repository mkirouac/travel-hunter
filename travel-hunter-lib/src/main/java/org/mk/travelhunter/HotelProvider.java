package org.mk.travelhunter;

import java.util.Collection;

import reactor.core.publisher.Flux;

public interface HotelProvider {
	Collection<HotelIdentifier> getHotelIdentifiers();
	Flux<TravelDeal> searchDeals(TravelDealFilter filter);
}
