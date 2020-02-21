package org.mk.travelhunter.voyagerabais;

import java.io.InputStream;
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

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class VoyagesRabaisHotelProvider implements HotelProvider {

	private final VoyageRabaisDateParser dateParser;
	
	public VoyagesRabaisHotelProvider(VoyageRabaisDateParser dateParser) {
		this.dateParser = dateParser;
		loadHotels();
	}

	@Cacheable("voyagesRabaisHotelIdentifierCache")
	public Collection<HotelIdentifier> getHotelIdentifiers() {
		return loadHotels();
	}

	@Override
	public Flux<TravelDeal> searchDeals(TravelDealFilter filter) {

		log.debug("Creating web requests");
		
		Collection<VoyageRabaisRequest> requests = createRequests(filter);
		
		WebClient client = WebClient.create(VoyageRabaisRequest.DEFAULT_URL);
		
		@SuppressWarnings("unchecked")
		Flux<TravelDeal>[] allSearches = new Flux[requests.size()];
		
		int i = 0;
		for(VoyageRabaisRequest request : requests) {
			allSearches[i++] = searchDeals(request, client);
		}
		
		log.debug("Mergin web requests");
		
		return Flux.merge(allSearches);
		

	}
	
	private Flux<TravelDeal> searchDeals(VoyageRabaisRequest request, WebClient client) {
		
		String requestBody = request.createBody();//Can this be done in the Flux?
		
		Flux<TravelDeal> flux = client.post()
			.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
			.bodyValue(requestBody)
			.retrieve()
			.bodyToMono(String.class)
			.flatMapMany(s -> {
				log.debug("Received response from web service");
				return Flux.fromIterable(parseResponse(s));
			});
		
		return flux;
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

				Iterator<Entry<String, JsonNode>> starsIterator = (Iterator<Entry<String, JsonNode>>) starsNodes
						.fields();
				while (starsIterator.hasNext()) {
					Map.Entry<String, JsonNode> starEntry = (Map.Entry<String, JsonNode>) starsIterator.next();
					JsonNode hotelNode = starEntry.getValue();
					String hotelStars = starEntry.getKey();
					String hotelName = hotelNode.get("hotel_name").asText();
					String hotelCity = hotelNode.get("city").asText();
					String hotelCountry = hotelNode.get("country").asText();
					String hotelDuration = hotelNode.get("duration").asText();
					

					LocalDate hotelDepartureDate = dateParser.parseDate(hotelNode.get("departure_date").asText());
					
					String hotelMinPrice = hotelNode.get("minprice").asText();
					
					TravelDeal travelDeal = new TravelDeal(hotelName, hotelMinPrice, hotelDepartureDate, hotelCountry, hotelDuration,
							hotelStars, hotelCity);
					
					hotels.add(travelDeal);
				}

			}

		} catch (JsonProcessingException e) {
			// FIXME
			e.printStackTrace();
		}

		log.debug("Parsed {} travel deals from raw response", hotels.size());
		
		return hotels;
	}
	
	private List<HotelIdentifier> loadHotels() {

		try {
			List<HotelIdentifier> hotelIdentifiers = new ArrayList<>();

			Resource resource = new ClassPathResource("PuntaCanaHotels.xml");

			Document document = null;
			try (InputStream is = resource.getInputStream()) {
				document = Jsoup.parse(is, "UTF-8", "https://mk-travel-hunter.herokuapp.com/");//A URL is needed by JSOUP but not required in this context.
			}
			
			
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
