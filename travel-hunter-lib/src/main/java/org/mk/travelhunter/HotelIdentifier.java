package org.mk.travelhunter;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class HotelIdentifier {
	
	private final String code;
	private final String name;
	
	@Override
	public String toString() {
		return name;
	}
}