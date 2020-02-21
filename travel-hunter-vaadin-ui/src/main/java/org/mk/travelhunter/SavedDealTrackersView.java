package org.mk.travelhunter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.event.EventListenerSupport;
import org.mk.travelhunter.controller.TravelHunterController;
import org.mk.travelhunter.controller.TravelHunterView;
import org.mk.travelhunter.dealtracker.DealTracker;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;

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
		
		
		Column<DealTracker, ?> actionsColumn = addColumn(dealTracker -> createActionComponents(dealTracker), new ComponentRenderer()).setWidth(130);
		Column<DealTracker, ?> nameColumn = addColumn("name").setExpandRatio(1);
		
		getHeader().getDefaultRow().join(actionsColumn, nameColumn).setText("Saved Deal Trackers");;

		setHeightByRows(5);
		
		setSelectionMode(SelectionMode.NONE);
		
	}

	private Component createActionComponents(DealTracker dealTracker) {
		Button deleteButton = new Button(VaadinIcons.TRASH.getHtml());
		deleteButton.setCaptionAsHtml(true);
		deleteButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		deleteButton.addClickListener(event -> controller.beginDeleteDealTracker(mainView, dealTracker));
		
		Button loadButton = new Button(VaadinIcons.SEARCH.getHtml());
		loadButton.setCaptionAsHtml(true);
		loadButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		loadButton.addClickListener(event -> dealTrackerSelectedListeners.fire().dealSelected(dealTracker));
		
		
		HorizontalLayout layout = VaadinUtils.wrapInHorizontalLayout(loadButton, deleteButton);
		layout.setSpacing(false);
		layout.setMargin(false);
		layout.setComponentAlignment(deleteButton, Alignment.MIDDLE_LEFT);
		layout.setComponentAlignment(loadButton, Alignment.MIDDLE_LEFT);
		return layout;
	}
	
	public void addDealTrackerSelectedListener(DealTrackerSelectedListener listener) {
		this.dealTrackerSelectedListeners.addListener(listener);
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
