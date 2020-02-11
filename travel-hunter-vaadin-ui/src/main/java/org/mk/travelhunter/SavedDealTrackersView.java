package org.mk.travelhunter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.event.EventListenerSupport;
import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.tracker.DealTracker;

import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;

public class SavedDealTrackersView extends Grid<DealTracker> {

	private final TravelHunterController controller;
	private final TravelHunterView mainView;
	private final List<DealTracker> dealTrackers = new ArrayList<>();
	private EventListenerSupport<DealTrackerSelectedListener> dealTrackerSelectedListeners = EventListenerSupport.create(DealTrackerSelectedListener.class);
	
	public SavedDealTrackersView(TravelHunterController controller, TravelHunterView mainView) {
		super(DealTracker.class);
		
		this.controller = controller;
		this.mainView = mainView;
		
		setItems(dealTrackers);
		
		removeAllColumns();
		addColumn("name");
		addColumn(n -> "Load", new ButtonRenderer<>(event ->   onLoadDealTrackerClicked(event)));
		addColumn(n -> "Delete", new ButtonRenderer<>(event -> onDeleteDealTrackerClicked(event)));
		setHeightByRows(5);
		
	}

	private void onDeleteDealTrackerClicked(RendererClickEvent<DealTracker> event) {
		controller.beginDeleteDealTracker(mainView, event.getItem());
	}

	public void addDealTrackerSelectedListener(DealTrackerSelectedListener listener) {
		this.dealTrackerSelectedListeners.addListener(listener);
	}
	
	private void onLoadDealTrackerClicked(RendererClickEvent<DealTracker> event) {
		dealTrackerSelectedListeners.fire().dealSelected(event.getItem());
	}
	
	public void displayDealTracker(DealTracker dealTracker) {

		dealTrackers.add(dealTracker);
		getDataProvider().refreshAll();
		
	}
	
	@FunctionalInterface
	public static interface DealTrackerSelectedListener {
		void dealSelected(DealTracker dealTracker);
	}

	public void deleteDealTracker(DealTracker dealTracker) {
		dealTrackers.remove(dealTracker);
		getDataProvider().refreshAll();
	}
}
