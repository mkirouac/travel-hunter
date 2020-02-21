package org.mk.travelhunter.voyagerabais;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class VoyageRabaisDateParserTest {

	private VoyageRabaisDateParser dateParser = new VoyageRabaisDateParser();

	@Test
	public void itShouldParseAllMonths() {

		String[] givenMonths =  { "jan", "fév", "mar", "avr", "mai", "jun", "juil", "aoû", "sep", "oct", "nov", "déc" };

		for(int i = 0; i < givenMonths.length; i++) {
		
			String givenMonth = givenMonths[i];
			LocalDate date = dateParser.parseDate("01 " + givenMonth + " 2020");
			int monthNumber = i + 1;
			if(monthNumber < 10) {
				assertThat(date).isEqualTo("2020-0" + monthNumber + "-01");
			} else {
				assertThat(date).isEqualTo("2020-" + monthNumber + "-01");
			}
			
		}

	}

}
