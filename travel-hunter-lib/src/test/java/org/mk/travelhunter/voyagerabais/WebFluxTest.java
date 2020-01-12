package org.mk.travelhunter.voyagerabais;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mk.travelhunter.HotelIdentifier;
import org.mk.travelhunter.TravelDealFilter;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class WebFluxTest {

	
	@Test
	public void testAsyncWebClient() {

		LocalDate startDate = date("2020-01-05");
		LocalDate endDate = date("2020-01-20");

		VoyagesRabaisHotelProvider provider = new VoyagesRabaisHotelProvider();

		// All grand bahia
		// BW_2xQ_c7_fk_2je_l7_53_2xP_bd_rY_Ox_2C-_2IE_2It_2gL_2IB_xG_4y_Lx_4z_Dg

		TravelDealFilter filter = TravelDealFilter.builder()
				.hotel(new HotelIdentifier("BW_2xQ_c7_fk_2je_l7_53_2xP_bd_rY_Ox_2C-_2IE_2It_2gL_2IB_xG_4y_Lx_4z_Dg",
						"this is a test hotel"))
				.startDate(startDate).endDate(endDate).build();

		List<VoyageRabaisRequest> requests = Lists.newArrayList(provider.createRequests(filter));

		assertThat(requests.size()).isEqualTo(3);

		WebClient client = WebClient.create(VoyageRabaisRequest.DEFAULT_URL);

		

		for (VoyageRabaisRequest request : requests) {

			System.out.println("SENDING REQUEST!!");
			String body = request.createBody();

			Mono<String> employeeMono = client.post()
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
					.bodyValue(body)
					.retrieve()
					.bodyToMono(String.class);

			employeeMono.subscribe((s) -> {
				System.out.println("BODY RECEIVED!");
				System.out.println(s);
			});

		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("DONE");

	}

	private LocalDate date(String isoDate) {
		return LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
	}

}
