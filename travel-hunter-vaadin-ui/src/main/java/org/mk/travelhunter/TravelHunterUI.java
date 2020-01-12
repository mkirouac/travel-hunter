package org.mk.travelhunter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.mk.travelhunter.tracker.DealTracker;
import org.mk.travelhunter.tracker.DealTrackingService;
import org.mk.travelhunter.voyagerabais.VoyagesRabaisHotelProvider;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.data.provider.GridSortOrderBuilder;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import reactor.core.publisher.Flux;

@SpringUI
@Push
public class TravelHunterUI extends UI {

	//Test code..
	private static final String TEST_USER_ID = "TEST-VAADIN-USER";
	
	@Autowired
	private HotelProvider hotels;

	@Autowired
	private DealTrackingService dealTrackingService;
	
	private TextField startDateTextField;
	private TextField endDateTextField;
	private ComboBox<HotelIdentifier> hotelsComboBox;
	private Grid<TravelDeal> travelDealGrid;
	private Set<TravelDeal> travelDeals = new TreeSet<>();// TODO Concurrency, does not belong here, etc..
	private Button testButton;
	private Button saveButton;
	private Label statusLabel = new Label("ready");

	@Override
	protected void init(VaadinRequest request) {
		startDateTextField = new TextField("Start Date");
		startDateTextField.setValue("2020-01-01");
		endDateTextField = new TextField("End Date");
		endDateTextField.setValue("2020-01-31");
		hotelsComboBox = new ComboBox<>("Hotels", hotels.getHotelIdentifiers());
		hotelsComboBox.setWidth("500px");

		travelDealGrid = new Grid<TravelDeal>(TravelDeal.class);
		travelDealGrid.setItems(travelDeals);
		travelDealGrid.setSizeFull();

		testButton = new Button("Test");
		testButton.addClickListener((event) -> {
			// TODO Sync
			travelDeals.clear();
			travelDealGrid.getDataProvider().refreshAll();

			searchButtonClickedReactive(event);

			testButton.setEnabled(false);
		});

		saveButton = new Button("Save");
		saveButton.addClickListener((event) -> {
			UUID dealTrackerId = UUID.randomUUID();
			
			HotelIdentifier hotel = hotelsComboBox.getValue();
			LocalDate startDate = LocalDate.parse(startDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate endDate = LocalDate.parse(endDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);

			TravelDealFilter filter = new TravelDealFilter(hotel, startDate, endDate);
			
			DealTracker dealTracker = new DealTracker(dealTrackerId, "vaadin-deal-tracker-" + dealTrackerId , TEST_USER_ID, filter);
			dealTrackingService.addDealTracker(dealTracker);
		});

		VerticalLayout layout = new VerticalLayout();
		for(DealTracker tracker : dealTrackingService.getDealTrackers(TEST_USER_ID)) {
			Button loadDealTrackerButton = new Button(tracker.getName());
			loadDealTrackerButton.addStyleName(ValoTheme.BUTTON_LINK);
			layout.addComponent(loadDealTrackerButton);
			loadDealTrackerButton.addClickListener((event) -> {
				hotelsComboBox.setValue(tracker.getFilter().getHotel());
				startDateTextField.setValue(tracker.getFilter().getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
				endDateTextField.setValue(tracker.getFilter().getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
			});
		}
		layout.addComponent(hotelsComboBox);
		layout.addComponent(startDateTextField);
		layout.addComponent(endDateTextField);
		layout.addComponent(testButton);
		layout.addComponent(saveButton);
		layout.addComponent(statusLabel);
		layout.addComponent(travelDealGrid);
		layout.setExpandRatio(travelDealGrid, 1);

		setContent(layout);
	}

	private void searchButtonClickedReactive(ClickEvent event) {

		HotelIdentifier hotel = hotelsComboBox.getValue();
		LocalDate startDate = LocalDate.parse(startDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate endDate = LocalDate.parse(endDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);

		search(hotel, startDate, endDate);
	}

	private void search(HotelIdentifier hotel, LocalDate startDate, LocalDate endDate) {
		TravelDealFilter filter = TravelDealFilter.builder().hotel(hotel).startDate(startDate).endDate(endDate).build();

		// TODO Move to controller
		Flux<TravelDeal> dealFlux = hotels.searchDeals(filter);

		dealFlux.doOnComplete(() -> {
			threadSafeUpdateUI((ui) -> {
				testButton.setEnabled(true);
				statusLabel.setValue("Search Completed. Results: " + travelDeals.size());
			});
		}).subscribe(deal -> {
			threadSafeUpdateUI((ui) -> {
				travelDeals.add(deal);
				travelDealGrid.getDataProvider().refreshAll();
				statusLabel.setValue("Searching... Results: " + travelDeals.size());
			});
		});
	}

	private void threadSafeUpdateUI(Consumer<UI> uiUpdater) {
		UI ui = getUI();
		if (ui != null) {
			ui.access(() -> {
				uiUpdater.accept(ui);
			});
		}
	}

}
