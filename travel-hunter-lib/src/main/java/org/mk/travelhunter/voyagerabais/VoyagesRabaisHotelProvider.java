package org.mk.travelhunter.voyagerabais;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mk.travelhunter.HotelIdentifier;
import org.mk.travelhunter.HotelProvider;
import org.mk.travelhunter.TravelDeal;
import org.mk.travelhunter.TravelDealFilter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Component
public class VoyagesRabaisHotelProvider implements HotelProvider {

	public VoyagesRabaisHotelProvider() {
		loadHotels();
	}

	@Cacheable("voyagesRabaisHotelIdentifierCache")
	public Collection<HotelIdentifier> getHotelIdentifiers() {
		return loadHotels();
	}

//	@Override
//	public Collection<TravelDeal> searchDeals(TravelDealFilter filter) {
//
//		List<TravelDeal> allTravelDeals = new ArrayList<>();
//
//		Collection<VoyageRabaisRequest> requests = createRequests(filter);
//
//		for (VoyageRabaisRequest request : requests) {
//			allTravelDeals.addAll(request.execute());
//		}
//
//		return allTravelDeals;
//
//	}
	
//	String body = request.createBody();
//
//	Mono<String> employeeMono = client.post()
//			.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//			.bodyValue(body)
//			.retrieve()
//			.bodyToMono(String.class);

	
	@Override
	public Flux<TravelDeal> searchDeals(TravelDealFilter filter) {


		Collection<VoyageRabaisRequest> requests = createRequests(filter);

		WebClient client = WebClient.create(VoyageRabaisRequest.DEFAULT_URL);
		
		Flux<TravelDeal> travelDealFlux = Flux.empty();
		
		for (VoyageRabaisRequest request : requests) {
			
			String requestBody = request.createBody();
			Flux<TravelDeal> responseBody  = client.post()
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
					.bodyValue(requestBody)
					.retrieve()
					.bodyToMono(String.class)
					.flatMapMany(s -> {
						return Flux.fromIterable(parseResponse(s));
						})
					
					//TODO Look into: 
					//.mergeWith(travelDealFlux) //Would this keep updating the travelDealFlux initially created? 
					;
		
			travelDealFlux = Flux.merge(travelDealFlux, responseBody);//TODO What would be the appropriate approach? Merging the flux each  time doesn't seem right. 
		}

		return travelDealFlux;

	}
	

	private List<TravelDeal> parseResponse(String rawResponse) {

		List<TravelDeal> hotels = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root;

		try {
			root = mapper.readTree(rawResponse);

		JsonNode statusNode = root.path("status");
		JsonNode gridNodes = root.path("grid");

		Iterator<Entry<String, JsonNode>> gridIterator = gridNodes.fields();

		while (gridIterator.hasNext()) {
			Map.Entry<String, JsonNode> gridEntry = (Map.Entry<String, JsonNode>) gridIterator.next();
			String date = gridEntry.getKey();
			JsonNode starsNodes = gridEntry.getValue();

			Iterator<Entry<String, JsonNode>> starsIterator = (Iterator<Entry<String, JsonNode>>) starsNodes.fields();
			while (starsIterator.hasNext()) {
				Map.Entry<String, JsonNode> starEntry = (Map.Entry<String, JsonNode>) starsIterator.next();
				JsonNode hotelNode = starEntry.getValue();
				String hotelStars = starEntry.getKey();
				String hotelName = hotelNode.get("hotel_name").asText();
				String hotelCity = hotelNode.get("city").asText();
				String hotelCountry = hotelNode.get("country").asText();
				String hotelDuration = hotelNode.get("duration").asText();
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu").toFormatter().withLocale(Locale.FRENCH);
				LocalDate hotelDepartureDate = LocalDate.parse(replaceMonth(hotelNode.get("departure_date").asText()), formatter);
				String hotelMinPrice = hotelNode.get("minprice").asText();
				hotels.add(new TravelDeal(hotelName, hotelMinPrice, hotelDepartureDate, hotelCountry, hotelDuration,
						hotelStars, hotelCity));
				System.out.println();
			}

		}

		} catch (JsonProcessingException e) {
			// FIXME
			e.printStackTrace();
		}

		return hotels;
	}

	//TODO Document + externalize + junit (this method is duplicated in VoyageRabaisHotelProviderTest
	private String replaceMonth(String dateText) {
		//Thanks to StackOverflow for this workaround. Ugly but works.
		String[] givenMonths = { "jan", "fév", "mar", "avr", "mai", "juin", "juil", "août", "sept", "oct", "nov", "déc" };
        String[] realMonths = { "janv.", "févr.", "mar.", "avr.", "mai.", "juin.", "juil.", "août.", "sept.", "oct.", "nov.", "déc." };
        String original = dateText;
        for (int i = 0; i < givenMonths.length; i++) {
            original = original.replaceAll(givenMonths[i], realMonths[i]);
        }
        return original;
	}
	
	private List<HotelIdentifier> loadHotels() {

		try {
			List<HotelIdentifier> hotelIdentifiers = new ArrayList<>();

			Resource resource = new ClassPathResource("PuntaCanaHotels.xml");//TODO

			Document document = Jsoup.parse(resource.getFile(), "UTF-8");////TODO

			Elements elements = document.select("option");

			for (Element element : elements) {
				String hotelCode = element.attr("value");
				String hotelDescription = element.text();
				hotelIdentifiers.add(new HotelIdentifier(hotelCode, hotelDescription));
			}
			return hotelIdentifiers;

		} catch (Exception e) {
			throw new RuntimeException("Failed to parse hotel list", e);
		}
	}

	Collection<VoyageRabaisRequest> createRequests(TravelDealFilter filter) {

		List<VoyageRabaisRequest> requests = new ArrayList<>();

		for (LocalDate firstRequestDate = filter.getStartDate(); firstRequestDate.minusDays(1)
				.isBefore(filter.getEndDate()); firstRequestDate = firstRequestDate.plusDays(7)) {

			// Each request contains:
			// A single date for the request
			// a "flexMin" corresponding to how many days to include in the results before
			// the date. Max of 3
			// a "flexMax" corresponding to how many days to include in the results after
			// the date.

			long daysCovered = Math.min(7, filter.getEndDate().toEpochDay() - firstRequestDate.toEpochDay() + 1);

			long dateIndex = Math.min(3, daysCovered - 1);
			long flexMin = Math.min(3, dateIndex);
			long flexMax = Math.max(0, daysCovered - 4);
			String requestDate = firstRequestDate.plusDays(dateIndex).format(DateTimeFormatter.ISO_LOCAL_DATE);

			VoyageRabaisRequest request = new VoyageRabaisRequest();
			request.setDate(requestDate);
			request.setFlexLow(new Long(flexMin).toString());
			request.setFlexHigh(new Long(flexMax).toString());
			request.setHotelCode(filter.getHotel().getCode());
			requests.add(request);
		}

		return requests;

	}
}
