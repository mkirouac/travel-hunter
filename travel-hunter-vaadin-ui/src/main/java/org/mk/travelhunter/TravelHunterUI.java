package org.mk.travelhunter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.tracker.DealTracker;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
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
	
	private SearchDealView  searchDealView;
	private Grid<TravelDeal> travelDealGrid;
	private Set<TravelDeal> travelDeals = new TreeSet<>();// TODO Concurrency, does not belong here, etc..
	private Label statusLabel = new Label("ready");
	private VerticalLayout savedDealsLayout;

	@Autowired
	public TravelHunterUI(TravelHunterController controller) {
		this.controller = controller;
	}
	
	@Override
	protected void init(VaadinRequest request) {
		
		log.debug("Initiating TravelHunerUI");

		searchDealView = new SearchDealView(controller, this, TEST_USER_ID);

		travelDealGrid = new Grid<TravelDeal>(TravelDeal.class);
		travelDealGrid.setItems(travelDeals);
		travelDealGrid.setSizeFull();

		savedDealsLayout = new VerticalLayout();

		VerticalLayout layout = new VerticalLayout();
		
		layout.addComponent(searchDealView);
		
		layout.addComponent(savedDealsLayout);
		layout.addComponent(statusLabel);
		layout.addComponent(travelDealGrid);
		layout.setExpandRatio(travelDealGrid, 1);

		setContent(layout);
		
		controller.requestDealTrackers(this, TEST_USER_ID);
		controller.requestHotelIdentifiers(this);
	}
	
	@Override
	public void displayHotels(Collection<HotelIdentifier> hotelIdentifiers) {
		searchDealView.setHotels(hotelIdentifiers);
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
			searchDealView.setSearchEnabled(false);
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
			searchDealView.setSearchEnabled(true);
			statusLabel.setValue("Search Completed. Results: " + travelDeals.size());
			log.debug("dealFlux.doOnComplete::end");
		});
	}


	
	private void loadDealTracker(DealTracker dealTracker) {
		searchDealView.loadDealTracker(dealTracker);
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
