package org.mk.travelhunter;

import java.time.LocalDate;
import java.util.Collection;

import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.dealtracker.DealTracker;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.themes.ValoTheme;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchDealView extends GridLayout {

	private static final int COLUMNS = 2;
	private static final int ROWS = 5;
	
	
	private final TravelHunterController controller;
	private final TravelHunterView mainView;
	
	private DateField startDateField;
	private DateField endDateField;
	private ComboBox<HotelIdentifier> hotelsComboBox;
	private Button searchButton;
	private ProgressBar progressBar;
	
	public SearchDealView(TravelHunterController controller, TravelHunterView mainView) {
		super(COLUMNS, ROWS);
		this.controller = controller;
		this.mainView = mainView;
	}
	
	@Override
	public void attach() {
		super.attach();
		
		setSpacing(true);
		
		
		int currentRowIndex = 0;
		
		//Search label and load saved search button
		Label searchLabel = new Label("Search for deals");
		searchLabel.addStyleName(ValoTheme.LABEL_LARGE);
		addComponent(searchLabel, 0, currentRowIndex, COLUMNS - 1, currentRowIndex);
		
		currentRowIndex++;
		
		//Dates
		startDateField = new DateField("From");
		startDateField.setValue(LocalDate.now());
		endDateField = new DateField("To");
		endDateField.setValue(LocalDate.now().plusMonths(1L));
		addComponent(startDateField, 0, currentRowIndex);
		addComponent(endDateField, 1, currentRowIndex);
		
		currentRowIndex++;
		
		//hotels
		hotelsComboBox = new ComboBox<>("Hotels");
		hotelsComboBox.setWidth("500px");
		addComponent(hotelsComboBox, 0, currentRowIndex, COLUMNS - 1, currentRowIndex);
		
		currentRowIndex++;
		
		//Search Button
		searchButton = createSearchButton();
		
		progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		
		HorizontalLayout searchButtonLayout = VaadinUtils.wrapInHorizontalLayout(progressBar, searchButton);
		searchButtonLayout.setWidth("100%");
		searchButtonLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
		searchButtonLayout.setComponentAlignment(progressBar, Alignment.MIDDLE_RIGHT);
		
		addComponent(searchButtonLayout, 1, currentRowIndex);
		

		
		currentRowIndex++;
	}
	
	public void setHotels(Collection<HotelIdentifier> hotelIdentifiers) {
		hotelsComboBox.setItems(hotelIdentifiers);
	}
	
	private Button createSearchButton() {
		Button button = new Button("Search");
		button.setDisableOnClick(true);
		
		button.addStyleName(ValoTheme.BUTTON_PRIMARY);
		
		button.addClickListener((event) -> {
			searchDeals(event);
		});
		
		return button;
	}

	public void setSearchEnabled(boolean enabled) {
		searchButton.setEnabled(enabled);
	}
	
	public void searchDeals(DealTracker dealTracker) {
		
		HotelIdentifier hotel = dealTracker.getFilter().getHotel();
		LocalDate startDate = dealTracker.getFilter().getStartDate();
		LocalDate endDate = dealTracker.getFilter().getEndDate();
		hotelsComboBox.setValue(hotel);
		startDateField.setValue(startDate);
		endDateField.setValue(endDate);
		searchDeals(hotel, startDate, endDate);
	}
	
	private void searchDeals(ClickEvent event) {

		HotelIdentifier hotel = hotelsComboBox.getValue();
		LocalDate startDate = startDateField.getValue();
		LocalDate endDate = endDateField.getValue();

		searchDeals(hotel, startDate, endDate);
	}
	
	private void searchDeals(HotelIdentifier hotel, LocalDate startDate, LocalDate endDate) {
		
		TravelDealFilter filter = TravelDealFilter.builder().hotel(hotel).startDate(startDate).endDate(endDate).build();

		log.debug("Searching with filter: " + filter);
		
		controller.beginSearchingForDeals(mainView, filter);
		progressBar.setVisible(true);
		
	}
	
	
	public void searchCompleted() {
		progressBar.setVisible(false);
	}
	
	public HotelIdentifier getHotelIdentifier() {
		return hotelsComboBox.getValue();
	}
	
	public LocalDate getStartDate() {
		return startDateField.getValue();
	}
	
	public LocalDate getEndDate() {
		return endDateField.getValue();
	}


}
