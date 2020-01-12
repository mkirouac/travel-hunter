package org.mk.travelhunter;

import java.time.LocalDate;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
//TODO don't  implement comparable. let the UI handle how they want to display/sort this.
public class TravelDeal implements Comparable<TravelDeal> {

	private final @Getter String name;
	private final @Getter String price;
	private final @Getter LocalDate date;
	private final @Getter String country;
	private final @Getter String duration;
	private final @Getter String stars;
	private final @Getter String city;
	
	@Override
	public int compareTo(TravelDeal other) {
		return new CompareToBuilder()
			.append(date, other.date)
			.append(price, other.price)
			.toComparison();
	}
	
	
}
