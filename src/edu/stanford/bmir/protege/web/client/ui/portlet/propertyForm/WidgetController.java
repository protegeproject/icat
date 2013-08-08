package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Collection;
import java.util.HashMap;
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

	public void setControllingWidget(PropertyWidget widget) {
		this.controllingWidget = widget;
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
	
	private PropertyWidget getWidgetForProperty(String propertyName) {
		//initialize on demand
		if (property2Widget == null) {
			property2Widget = new HashMap<String, PropertyWidget>();
			Collection<PropertyWidget> widgets = formGenerator.getWidgets();
			for (PropertyWidget propertyWidget : widgets) {
				property2Widget.put(propertyWidget.getProperty().getName(), propertyWidget);
			}
		}
		
		return property2Widget.get(propertyName);
	}
}
