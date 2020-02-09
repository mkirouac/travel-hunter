package org.mk.travelhunter;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class VaadinUtils {

	
	public static HorizontalLayout wrapInHorizontalLayout(Component...components) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponents(components);
		return layout;
	}
	
}
