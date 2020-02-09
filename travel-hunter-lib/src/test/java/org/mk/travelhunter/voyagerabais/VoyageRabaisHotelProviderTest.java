package org.mk.travelhunter.voyagerabais;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mk.travelhunter.HotelIdentifier;
import org.mk.travelhunter.TravelDealFilter;

public class VoyageRabaisHotelProviderTest {

	@Test
	public void testOneDay() {

		LocalDate startDate = date("2020-01-01");
		LocalDate endDate = date("2020-01-01");

		VoyagesRabaisHotelProvider provider = new VoyagesRabaisHotelProvider(new VoyageRabaisDateParser());

		TravelDealFilter filter = TravelDealFilter.builder()
				.hotel(new HotelIdentifier("test-hotel", "this is a test hotel")).startDate(startDate).endDate(endDate)
				.build();

		List<VoyageRabaisRequest> requests = Lists.newArrayList(provider.createRequests(filter));

		assertThat(requests.size()).isEqualTo(1);
		assertRequest(requests.get(0), startDate, "0", "0");
	}
	
	@Test
	public void testFiveDay() {

		LocalDate startDate = date("2020-01-01");
		LocalDate endDate = date("2020-01-05");

		VoyagesRabaisHotelProvider provider = new VoyagesRabaisHotelProvider(new VoyageRabaisDateParser());

		TravelDealFilter filter = TravelDealFilter.builder()
				.hotel(new HotelIdentifier("test-hotel", "this is a test hotel")).startDate(startDate).endDate(endDate)
				.build();

		List<VoyageRabaisRequest> requests = Lists.newArrayList(provider.createRequests(filter));

		assertThat(requests.size()).isEqualTo(1);
		assertRequest(requests.get(0), endDate.minusDays(1), "3", "1");
	}

	
	@Test
	public void testSevenDays() {

		LocalDate startDate = date("2020-01-01");
		LocalDate endDate = date("2020-01-07");

		VoyagesRabaisHotelProvider provider = new VoyagesRabaisHotelProvider(new VoyageRabaisDateParser());

		TravelDealFilter filter = TravelDealFilter.builder()
				.hotel(new HotelIdentifier("test-hotel", "this is a test hotel")).startDate(startDate).endDate(endDate)
				.build();

		List<VoyageRabaisRequest> requests = Lists.newArrayList(provider.createRequests(filter));

		assertThat(requests.size()).isEqualTo(1);
		assertRequest(requests.get(0), "2020-01-04", "3", "3");
	}

	
	
	@Test
	public void testTenDays() {

		LocalDate startDate = date("2020-01-01");
		LocalDate endDate = date("2020-01-10");

		VoyagesRabaisHotelProvider provider = new VoyagesRabaisHotelProvider(new VoyageRabaisDateParser());

		TravelDealFilter filter = TravelDealFilter.builder()
				.hotel(new HotelIdentifier("test-hotel", "this is a test hotel")).startDate(startDate).endDate(endDate)
				.build();

		List<VoyageRabaisRequest> requests = Lists.newArrayList(provider.createRequests(filter));

		assertThat(requests.size()).isEqualTo(2);
		
		//First request
		assertRequest(requests.get(0), "2020-01-04", "3", "3");
		
		//Second request
		assertRequest(requests.get(1), "2020-01-10", "2", "0");
	}

	@Test
	public void testTwentyDays() {

		LocalDate startDate = date("2020-01-01");
		LocalDate endDate = date("2020-01-20");

		VoyagesRabaisHotelProvider provider = new VoyagesRabaisHotelProvider(new VoyageRabaisDateParser());

		TravelDealFilter filter = TravelDealFilter.builder()
				.hotel(new HotelIdentifier("test-hotel", "this is a test hotel")).startDate(startDate).endDate(endDate)
				.build();

		List<VoyageRabaisRequest> requests = Lists.newArrayList(provider.createRequests(filter));

		assertThat(requests.size()).isEqualTo(3);
		 
		//First request  days 1-7
		assertRequest(requests.get(0), "2020-01-04", "3", "3");
		
		//Second request days 8-14
		assertRequest(requests.get(1), "2020-01-11", "3", "3");
		
		//Third request days 15-20
		assertRequest(requests.get(2), "2020-01-18", "3", "2");
	}
	
	@Test
	public void parseDate() {
		 DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu").toFormatter().withLocale(Locale.FRENCH);
		 LocalDate.parse(replaceMonth("10 jan 2020"), formatter);
		 LocalDate.parse(replaceMonth("1 fév 2020"), formatter);
	}
	
	private String replaceMonth(String dateText) {
		String[] givenMonths = { "jan", "fév", "mars", "avr.", "mai", "juin", "juil", "août", "sept", "oct", "nov", "déc" };
        String[] realMonths = { "janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc." };
        String original = dateText;
        for (int i = 0; i < givenMonths.length; i++) {
            original = original.replaceAll(givenMonths[i], realMonths[i]);
        }
        return original;
	}
	
	private void assertRequest(VoyageRabaisRequest request, LocalDate expectedSearchDate, String flexLow, String flexHigh) {
		assertThat(date(request.getDate())).isEqualTo(expectedSearchDate);
		assertThat(request.getFlexLow()).isEqualTo(flexLow);
		assertThat(request.getFlexHigh()).isEqualTo(flexHigh);
	}
	
	private void assertRequest(VoyageRabaisRequest request, String expectedSearchDate, String flexLow, String flexHigh) {
		assertThat(request.getDate()).isEqualTo(expectedSearchDate);
		assertThat(request.getFlexLow()).isEqualTo(flexLow);
		assertThat(request.getFlexHigh()).isEqualTo(flexHigh);
	}

	
	
	private LocalDate date(String isoDate) {
		return LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
	}

}
