package org.mk.travelhunter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.tracker.DealTracker;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import lombok.extern.slf4j.Slf4j;

@SpringUI
@Push
@Slf4j
public class TravelHunterUI extends UI implements TravelHunterView{

	//Test code..
	private static final String TEST_USER_ID = "TEST-VAADIN-USER";
	
	private final TravelHunterController controller;
	
	
	private TextField startDateTextField;
	private TextField endDateTextField;
	private ComboBox<HotelIdentifier> hotelsComboBox;
	private Grid<TravelDeal> travelDealGrid;
	private Set<TravelDeal> travelDeals = new TreeSet<>();// TODO Concurrency, does not belong here, etc..
	private Button testButton;
	private Button saveButton;
	private Label statusLabel = new Label("ready");
	private VerticalLayout savedDealsLayout;

	@Autowired
	public TravelHunterUI(TravelHunterController controller) {
		this.controller = controller;
	}
	
	@Override
	protected void init(VaadinRequest request) {
		
		log.debug("Initiating TravelHunerUI");
		
		
		startDateTextField = new TextField("Start Date");
		startDateTextField.setValue("2020-01-01");
		endDateTextField = new TextField("End Date");
		endDateTextField.setValue("2020-01-31");
		hotelsComboBox = new ComboBox<>("Hotels");
		hotelsComboBox.setWidth("500px");

		travelDealGrid = new Grid<TravelDeal>(TravelDeal.class);
		travelDealGrid.setItems(travelDeals);
		travelDealGrid.setSizeFull();

		testButton = new Button("Test");
		testButton.setDisableOnClick(true);
		testButton.addClickListener((event) -> {
			searchDeals(event);
		});

		saveButton = new Button("Save");
		saveButton.addClickListener((event) -> {
			promptSaveDealTrackerInput();
		});

		
		savedDealsLayout = new VerticalLayout();

		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(savedDealsLayout);
		layout.addComponent(hotelsComboBox);
		layout.addComponent(startDateTextField);
		layout.addComponent(endDateTextField);
		layout.addComponent(testButton);
		layout.addComponent(saveButton);
		layout.addComponent(statusLabel);
		layout.addComponent(travelDealGrid);
		layout.setExpandRatio(travelDealGrid, 1);

		setContent(layout);
		
		controller.requestDealTrackers(this, TEST_USER_ID);
		controller.requestHotelIdentifiers(this);
	}
	
	@Override
	public void displayHotels(Collection<HotelIdentifier> hotelIdentifiers) {
		hotelsComboBox.setItems(hotelIdentifiers);
	}

	@Override
	public void displayDealTracker(DealTracker dealTracker) {
		
		threadSafeUpdateUI(ui -> {
			Button loadDealTrackerButton = new Button(dealTracker.getName());
			loadDealTrackerButton.addStyleName(ValoTheme.BUTTON_LINK);
			savedDealsLayout.addComponent(loadDealTrackerButton);
			loadDealTrackerButton.addClickListener((event) -> {
				loadDealTracker(dealTracker);
			});
		});
	}
	
	private void searchDeals(ClickEvent event) {

		HotelIdentifier hotel = hotelsComboBox.getValue();
		LocalDate startDate = LocalDate.parse(startDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate endDate = LocalDate.parse(endDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);

		searchDeals(hotel, startDate, endDate);
	}

	private void searchDeals(HotelIdentifier hotel, LocalDate startDate, LocalDate endDate) {
		TravelDealFilter filter = TravelDealFilter.builder().hotel(hotel).startDate(startDate).endDate(endDate).build();

		log.debug("Searching with filter: " + filter);
		
		 controller.beginSearchingForDeals(this, filter);
		
		
	}
	
	@Override
	public void addSearchResult(TravelDeal deal) {
		threadSafeUpdateUI((ui) -> {
			log.debug("dealFlux.subscribe::start");
			log.debug("Received a TravelDeal from Flux: " + deal);
			travelDeals.add(deal);
			travelDealGrid.getDataProvider().refreshAll();
			statusLabel.setValue("Searching... Results: " + travelDeals.size());
			log.debug("dealFlux.subscribe::start");
		});
	}

	@Override
	public void displaySearchError(Throwable ex) {
		threadSafeUpdateUI((ui) -> {
			testButton.setEnabled(true);
			travelDeals.clear();
			travelDealGrid.getDataProvider().refreshAll();
			statusLabel.setValue("An error occured, please try again");
			log.error("Flux failed", ex);
		});
	}
	
	@Override
	public void clearAllSearchResults() {
		threadSafeUpdateUI((ui) -> {
			log.debug("dealFlux.doOnFirst::start");
			travelDeals.clear();
			travelDealGrid.getDataProvider().refreshAll();
			log.debug("dealFlux.doOnFirst::end");
		});
	}

	@Override
	public void displaySearchCompletedNotification() {
		threadSafeUpdateUI((ui) -> {
			log.debug("dealFlux.doOnComplete::start");
			testButton.setEnabled(true);
			statusLabel.setValue("Search Completed. Results: " + travelDeals.size());
			log.debug("dealFlux.doOnComplete::end");
		});
	}

	private void saveDealTracker(String dealName) {
		UUID dealTrackerId = UUID.randomUUID();
		
		HotelIdentifier hotel = hotelsComboBox.getValue();
		LocalDate startDate = LocalDate.parse(startDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate endDate = LocalDate.parse(endDateTextField.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);

		TravelDealFilter filter = new TravelDealFilter(hotel, startDate, endDate);
		
		//Controller
		DealTracker dealTracker = new DealTracker(dealTrackerId, dealName, TEST_USER_ID, filter);
		controller.beginSavingDealTracker(this, dealTracker);
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
	
	private void loadDealTracker(DealTracker dealTracker) {
		hotelsComboBox.setValue(dealTracker.getFilter().getHotel());
		startDateTextField.setValue(dealTracker.getFilter().getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
		endDateTextField.setValue(dealTracker.getFilter().getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
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
