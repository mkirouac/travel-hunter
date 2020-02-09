package org.mk.travelhunter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.tracker.DealTracker;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchDealView extends GridLayout {

	private static final int COLUMNS = 2;
	private static final int ROWS = 5;
	
	
	private final TravelHunterController controller;
	private final TravelHunterView mainView;
	private final String userId;
	
	private DateField startDateField;
	private DateField endDateField;
	private ComboBox<HotelIdentifier> hotelsComboBox;
	private Button searchButton;
	private Button saveCurrentSearchButton;
	
	public SearchDealView(TravelHunterController controller, TravelHunterView mainView, String userId) {
		super(COLUMNS, ROWS);
		this.controller = controller;
		this.mainView = mainView;
		this.userId = userId;
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
		
		//Load Search Button
		//TODO Find a better way to handle this
		Component loadSavedSearchButton = createLoadSavedSearchButton();
		addComponent(loadSavedSearchButton, 0, currentRowIndex);
		setComponentAlignment(loadSavedSearchButton, Alignment.MIDDLE_LEFT);
		
		//Search Button
		searchButton = createSearchButton();
		saveCurrentSearchButton = new Button("Save Current Search");
		saveCurrentSearchButton.addClickListener((event) -> {
			promptSaveDealTrackerInput();
		});
		
		Component searchButtonsLayout = VaadinUtils.wrapInHorizontalLayout(saveCurrentSearchButton, searchButton);
		
		addComponent(searchButtonsLayout, 1, currentRowIndex);
		setComponentAlignment(searchButtonsLayout, Alignment.MIDDLE_RIGHT);

		
		currentRowIndex++;
	}
	
	public void setHotels(Collection<HotelIdentifier> hotelIdentifiers) {
		hotelsComboBox.setItems(hotelIdentifiers);
	}
	
	private Component createLoadSavedSearchButton() {
		Button button = new Button("load saved search");
		button.addStyleName(ValoTheme.BUTTON_TINY);
		
		button.addClickListener((event) -> {
			Notification.show("Feature not yet implemented");
		});
		return button;
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
		
		
	}

	private void promptSaveDealTrackerInput() {
		Window window = new Window("Save Deal Tracker");
		HorizontalLayout layout = new HorizontalLayout();
		window.setContent(layout);
		
		TextField dealTrackerNameTextField = new TextField("Deal Tracker Name");
		layout.addComponent(dealTrackerNameTextField);
		
		Button confirmButton = new Button("Confirm");
		confirmButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		layout.addComponent(confirmButton);
		
		confirmButton.addClickListener((event) -> {
			saveDealTracker(dealTrackerNameTextField.getValue());
			window.close();
		});
		
		UI.getCurrent().addWindow(window);
		
	}

	private void saveDealTracker(String dealName) {
		UUID dealTrackerId = UUID.randomUUID();
		
		HotelIdentifier hotel = hotelsComboBox.getValue();
		LocalDate startDate = startDateField.getValue();
		LocalDate endDate = endDateField.getValue();

		TravelDealFilter filter = new TravelDealFilter(hotel, startDate, endDate);
		
		//Controller
		DealTracker dealTracker = new DealTracker(dealTrackerId, dealName, userId, filter);
		controller.beginSavingDealTracker(mainView, dealTracker);
	}
	
	public void loadDealTracker(DealTracker dealTracker) {
		hotelsComboBox.setValue(dealTracker.getFilter().getHotel());
		startDateField.setValue(dealTracker.getFilter().getStartDate());
		endDateField.setValue(dealTracker.getFilter().getEndDate());
	}


}
