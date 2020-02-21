package org.mk.travelhunter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.dealtracker.DealTracker;
import org.mk.travelhunter.security.SecurityController;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
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
	private final SecurityController securityController;
	
	private SearchDealView  searchDealView;
	private Grid<TravelDeal> travelDealGrid;
	private Set<TravelDeal> travelDeals = new TreeSet<>();// TODO Concurrency, does not belong here, etc..
	private SavedDealTrackersView savedDealTrackersView;
	private Component guestDealTrackersView;
	private Label statusLabel = new Label("ready");
	private Button saveCurrentSearchButton = new Button("(save current search)");
	

	@Autowired
	public TravelHunterUI(TravelHunterController controller, SecurityController securityController) {
		this.controller = controller;
		this.securityController = securityController;
	}
	
	@Override
	protected void init(VaadinRequest request) {
		
		
		
		log.debug("Initiating TravelHunerUI");

		searchDealView = new SearchDealView(controller, this);

		travelDealGrid = new Grid<TravelDeal>(TravelDeal.class);
		travelDealGrid.setItems(travelDeals);
		travelDealGrid.setSizeFull();

		savedDealTrackersView = new SavedDealTrackersView(controller, this);
		savedDealTrackersView.setWidth("100%");
		savedDealTrackersView.addDealTrackerSelectedListener(dealTracker -> searchDeals(dealTracker));

		Component guestDealTrackersView = createGuestDealTrackersView();
		
		HorizontalLayout searchLayout = null;
		
		if(securityController.isUserAuthenticated()) {
			searchLayout =  VaadinUtils.wrapInHorizontalLayout(searchDealView, savedDealTrackersView);
			searchLayout.setExpandRatio(savedDealTrackersView, 1);
		} else {
			searchLayout =  VaadinUtils.wrapInHorizontalLayout(searchDealView, guestDealTrackersView);
		}
		
		
		searchLayout.setWidth("100%");
		
		HorizontalLayout statusLayout = VaadinUtils.wrapInHorizontalLayout(statusLabel, saveCurrentSearchButton);
		saveCurrentSearchButton.setVisible(false);
		saveCurrentSearchButton.addStyleName(ValoTheme.BUTTON_LINK);
		saveCurrentSearchButton.addClickListener(event-> {
			promptSaveDealTrackerInput();
		});
		statusLayout.setComponentAlignment(statusLabel, Alignment.MIDDLE_LEFT);
		
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(searchLayout);
		layout.addComponent(statusLayout);
		layout.addComponent(travelDealGrid);
		layout.setExpandRatio(travelDealGrid, 1);

		setContent(layout);
		
		controller.requestDealTrackers(this, TEST_USER_ID);
		controller.requestHotelIdentifiers(this);
	
		Notification.show("Welcome " + getUserName() + "!");
		
	}
	
	
	private Component createGuestDealTrackersView() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(new Label("In order to save deal trackers, please"));
		layout.addComponent(new Link("Login", new ExternalResource("http://localhost:8080/login")));//TODO
		return layout;
	}

	private String getUserName() {
	
		return securityController.getUserName();
		

	}
	
	@Override
	public void displayHotels(Collection<HotelIdentifier> hotelIdentifiers) {
		searchDealView.setHotels(hotelIdentifiers);
	}

	@Override
	public void displayDealTracker(DealTracker dealTracker) {
		
		threadSafeUpdateUI(ui -> {
			savedDealTrackersView.displayDealTracker(dealTracker);
		});
		
	}
	
	private void promptSaveDealTrackerInput() {
		Window window = new Window("Save Deal Tracker");
		VerticalLayout layout = new VerticalLayout();
		window.setContent(layout);
		window.setWidth("70%");
		window.center();
		
		TextField dealTrackerNameTextField = new TextField("Please enter a name for your deal tracker, then click save.");
		dealTrackerNameTextField.setWidth("100%");
		dealTrackerNameTextField.setValue(searchDealView.getHotelIdentifier().getName() + "(" + searchDealView.getStartDate() + " to " + searchDealView.getEndDate() + ")");
		layout.addComponent(dealTrackerNameTextField);
		
		Button confirmButton = new Button("Save");
		confirmButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		layout.addComponent(confirmButton);
		layout.setComponentAlignment(confirmButton, Alignment.MIDDLE_RIGHT);
		
		confirmButton.addClickListener((event) -> {
			saveDealTracker(dealTrackerNameTextField.getValue());
			window.close();
		});
		
		UI.getCurrent().addWindow(window);
		
	}

	private void saveDealTracker(String dealName) {
		UUID dealTrackerId = UUID.randomUUID();
		
		HotelIdentifier hotel = searchDealView.getHotelIdentifier();
		LocalDate startDate = searchDealView.getStartDate();
		LocalDate endDate = searchDealView.getEndDate();

		TravelDealFilter filter = new TravelDealFilter(hotel, startDate, endDate);
		
		//Controller
		DealTracker dealTracker = new DealTracker(dealTrackerId, dealName, TEST_USER_ID, filter);
		controller.beginSavingDealTracker(this, dealTracker);
	}
	
	@Override
	public void addSearchResults(List<TravelDeal> deals) {
		threadSafeUpdateUI((ui) -> {
			
			if (log.isDebugEnabled()) {
				log.debug("Received batch of {} deals: {}", deals.size(), deals.stream().map(deal -> deal.toString()).collect(Collectors.joining(" | ")));
			}
			travelDeals.addAll(deals);
			travelDealGrid.getDataProvider().refreshAll();
			statusLabel.setValue("Searching... Results: " + travelDeals.size());
		});
	}
	
	
	@Override
	public void addSearchResult(TravelDeal deal) {
		threadSafeUpdateUI((ui) -> {
			log.debug("Received a TravelDeal from Flux: " + deal);
			travelDeals.add(deal);
			travelDealGrid.getDataProvider().refreshAll();
			statusLabel.setValue("Searching... Results: " + travelDeals.size());
		});
	}

	@Override
	public void displaySearchError(Throwable ex) {
		threadSafeUpdateUI((ui) -> {
			searchDealView.setSearchEnabled(false);
			travelDeals.clear();
			travelDealGrid.getDataProvider().refreshAll();
			statusLabel.setValue("An error occured, please try again");
			searchDealView.searchCompleted();
			log.error("Flux failed", ex);
		});
	}
	
	@Override
	public void clearAllSearchResults() {
		threadSafeUpdateUI((ui) -> {
			log.debug("clearing search results");
			travelDeals.clear();
			travelDealGrid.getDataProvider().refreshAll();
			saveCurrentSearchButton.setVisible(false);
			statusLabel.setValue("Searching... Results: 0");
		});
	}

	@Override
	public void displaySearchCompletedNotification() {
		threadSafeUpdateUI((ui) -> {
			log.debug("dealFlux.doOnComplete::start");
			searchDealView.setSearchEnabled(true);
			statusLabel.setValue("Search Completed. Results: " + travelDeals.size());
			saveCurrentSearchButton.setVisible(true);
			searchDealView.searchCompleted();
			log.debug("dealFlux.doOnComplete::end");
			
		});
	}


	
	private void searchDeals(DealTracker dealTracker) {
		searchDealView.searchDeals(dealTracker);
	}

	private void threadSafeUpdateUI(Consumer<UI> uiUpdater) {
		UI ui = getUI();
		if (ui != null) {
			ui.access(() -> {
				uiUpdater.accept(ui);
			});
		}
	}

	@Override
	public void deleteDealTracker(DealTracker dealTracker) {
		savedDealTrackersView.deleteDealTracker(dealTracker);
	}


}
