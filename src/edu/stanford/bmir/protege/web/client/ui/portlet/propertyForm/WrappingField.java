package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.gwtext.client.widgets.Panel;


public class WrappingField extends Panel {
	
	public WrappingField() {
		super();
		setHeight(100);
		setTitle("Moto");
		setFrame(true);
	}

	@Override
	public void onBrowserEvent(Event event) {
		GWT.log("Event on wrapping field: " +  DOM.eventGetType(event), null);
	}
	
	

}
