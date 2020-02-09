package org.mk.travelhunter.voyagerabais;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class VoyageRabaisDateParser {

	private final DateTimeFormatter dateTimeFormatter;

	public VoyageRabaisDateParser() {
		this(new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu").toFormatter()
				.withLocale(Locale.FRENCH));
	}

	public VoyageRabaisDateParser(DateTimeFormatter formatter) {
		this.dateTimeFormatter = formatter;
	}

	public LocalDate parseDate(String dateText) {
		return LocalDate.parse(replaceMonth(dateText), dateTimeFormatter);
	}
	
	// TODO Document + externalize + junit (this method is duplicated in
	// VoyageRabaisHotelProviderTest
	private String replaceMonth(String dateText) {
		// Thanks to StackOverflow for this workaround. Ugly but works.
		String[] givenMonths = { "jan", "fév", "mar", "avr", "mai", "jun", "juil", "aoû", "sep", "oct", "nov",
				"déc" };
		String[] realMonths = { "janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.",
				"nov.", "déc." };
		String original = dateText;
		for (int i = 0; i < givenMonths.length; i++) {
			original = original.replaceAll(givenMonths[i], realMonths[i]);
		}
		return original;
	}

}
