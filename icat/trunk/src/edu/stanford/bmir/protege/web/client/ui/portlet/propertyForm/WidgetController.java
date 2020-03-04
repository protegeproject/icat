package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.rpc.data.layout.WidgetConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

public class WidgetController {

	private Panel tabPanel;
	private FormGenerator formGenerator;
	private PropertyWidget controllingWidget;
	private Map<String, PropertyWidget> property2Widget = null;
	private Map<String, List<String>> property2PropertiesMap = new HashMap<String, List<String>>();

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
				hideWidgetForProperty(propertyName);
			}
		}
	}

	public void hideWidgetForProperty(String propertyName) {
		PropertyWidget widget = getWidgetForProperty(propertyName);
		if (widget != null) {
			boolean toHide = new WidgetConfiguration(widget.getWidgetConfiguration()).getBooleanProperty(FormConstants.HIDDEN, true);
			if ( toHide ) {
				widget.getComponent().hide();
			}
		}
	}

	public void showWidgetForProperty(String propertyName) {
		PropertyWidget widget = getWidgetForProperty(propertyName);
		if (widget != null) {
			boolean isHidden = new WidgetConfiguration(widget.getWidgetConfiguration()).getBooleanProperty(FormConstants.HIDDEN, false);
			if ( ! isHidden ) {
				widget.getComponent().show();
			}
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

	public void initializePropertyMap(Map<String, Object> widgetConfiguration) {
		Map<String, List<String>> propMap = (Map<String, List<String>>) widgetConfiguration.get(FormConstants.PROPERTY_MAP);
		if (propMap != null) {
			for (String prop : propMap.keySet()) {
				List<String> propList = propMap.get(prop);
				property2PropertiesMap.put(prop, propList);
			}
		}
	}

	/**
	 * returns a list containing this property or all of its related properties (i.e. sub-properties), if they are specified
	 * Note: if a property has the list of its related properties specified, only the properties in that
	 * list will be returned, so in cases when it is desired that both this property and its related properties
	 * to be displayed/hidden, we need to explicitly specify this property as being a related property to itself.
	 * @param propertyName the name of a property
	 * @return
	 */
	public List<String> getAllRelatedProperties(String propertyName) {
		List<String> propList = property2PropertiesMap.get(propertyName);
		if (propList == null) {
			propList = Arrays.asList(propertyName);
		}
		
		return propList;
	}

}
