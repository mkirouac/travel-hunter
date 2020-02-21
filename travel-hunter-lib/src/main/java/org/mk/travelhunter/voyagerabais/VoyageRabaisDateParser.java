package org.mk.travelhunter.voyagerabais;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class VoyageRabaisDateParser {

	private static final String[] REAL_MONTHS = { "janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.",
			"nov.", "déc." };
	private static final String[] GIVEN_MONTHS = { "jan", "fév", "mar", "avr", "mai", "jun", "juil", "aoû", "sep", "oct", "nov", "déc" };
	
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
	
	// TODO Document 
	private String replaceMonth(String dateText) {
		// Thanks to StackOverflow for this workaround. Ugly but works.
		String original = dateText;
		for (int i = 0; i < GIVEN_MONTHS.length; i++) {
			original = original.replaceAll(GIVEN_MONTHS[i], REAL_MONTHS[i]);
		}
		return original;
	}

}
