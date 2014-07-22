package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

public class WidgetController {

	private Panel tabPanel;
	private FormGenerator formGenerator;
	private PropertyWidget controllingWidget;
	private Map<String, PropertyWidget> property2Widget = null;

	public WidgetController(Panel tabPanel, FormGenerator formGenerator) {
		this.tabPanel = tabPanel;
		this.formGenerator = formGenerator;
	}

	public FormGenerator getFormGenerator() {
		return formGenerator;
	}
	
	public void setControllingWidget(PropertyWidget widget) {
		this.controllingWidget = widget;
	}

	private void initProperty2WidgetMap() {
		property2Widget = new HashMap<String, PropertyWidget>();
		Collection<PropertyWidget> widgets = getWidgets();
		for (PropertyWidget propertyWidget : widgets) {
			property2Widget.put(propertyWidget.getProperty().getName(), propertyWidget);
		}
	}

	protected Collection<PropertyWidget> getWidgets() {
		return formGenerator.getWidgetsInTab(tabPanel);
	}

	public void hideAllWidgets() {
		//initialize on demand
		if (property2Widget == null) {
			initProperty2WidgetMap();
		}
		String ctrlProperty = controllingWidget.getProperty().getName();

		for (String propertyName : property2Widget.keySet()) {
			if (!propertyName.equals(ctrlProperty)) {
				PropertyWidget widget = getWidgetForProperty(propertyName);
				if (widget != null) {
					widget.getComponent().hide();
				}
			}
		}
	}

	public void hideWidgetForProperty(String propertyName) {
		PropertyWidget widget = getWidgetForProperty(propertyName);
		if (widget != null) {
			widget.getComponent().hide();
		}
	}

	public void showWidgetForProperty(String propertyName) {
		PropertyWidget widget = getWidgetForProperty(propertyName);
		if (widget != null) {
			widget.getComponent().show();
		}
	}
	
	protected PropertyWidget getWidgetForProperty(String propertyName) {
		//initialize on demand
		if (property2Widget == null) {
			initProperty2WidgetMap();
		}
		
		return property2Widget.get(propertyName);
	}
	
	protected List<String> getAllProperties() {
		//initialize on demand
		if (property2Widget == null) {
			initProperty2WidgetMap();
		}
		
		return new ArrayList<String>(property2Widget.keySet());
	}
	
	protected PropertyWidget getControllingWidget() {
		return controllingWidget;
	}
}
