package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormGenerator;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class PreCoordinationWidget extends AbstractPropertyWidget implements SuperclassSelectorContainer {

	private Panel wrappingPanel;
	private SuperclassSelectorWidget superclassSelector;
	private List<AbstractScaleValueSelectorWidget> valueSelWidgets;

    private PreCoordinationWidgetController widgetController;
	
	public PreCoordinationWidget(Project project, PreCoordinationWidgetController widgetController) {
		super(project);
		this.widgetController = widgetController;
	}

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        widgetController.initializePropertyMap(widgetConfiguration);
    }

	@Override
	public Component getComponent() {
		if (wrappingPanel == null) {
			createComponent();
		}
		return wrappingPanel;
	}

	@Override
	public Component createComponent() {
		wrappingPanel = new Panel();
		superclassSelector = createSuperClassSelectorWidget();
		Panel propertyValuePanel = createScaleValueSelectorWidgets();
		
		wrappingPanel.add(superclassSelector.getComponent());
		wrappingPanel.add(propertyValuePanel);
		return wrappingPanel;
	}
	
	@Override
	public SuperclassSelectorWidget createSuperClassSelectorWidget() {
		SuperclassSelectorWidget superclassSelector = null;
		Map<String, Object> propConfigMap = (Map<String, Object>) getWidgetConfiguration().get(FormConstants.SUPERCLASS_SELECTOR);
		if ( propConfigMap == null ) {
			GWT.log("Warning: there is no superclass selector widget specified in the configuration of " + this);
			return null;
		}
		for (String prop : propConfigMap.keySet()) {
			Object value = propConfigMap.get(prop);
			if (value instanceof Map) {
				Map<String, Object> config = (Map<String, Object>)value;
				SuperclassSelectorWidget widget = createSuperClassSelectorWidget(config, prop);
				if (superclassSelector == null &&
						widget != null && widget.getComponent() != null && 
						UIUtil.getIntegerConfigurationProperty(config, FormConstants.INDEX, 0) == 0) {
					superclassSelector = widget;
					break;
				}
			}
		}
		return superclassSelector;
	}

	private SuperclassSelectorWidget createSuperClassSelectorWidget(
			Map<String, Object> configMap, String prop) {
//		SuperclassSelectorWidget superclassSelector = new SuperclassSelectorWidget(getProject(), widgetController);
//		//superclassSelector.setup(getWidgetConfiguration(), new PropertyEntityData("http://www.w3.org/2000/01/rdf-schema#subClassOf", "Pre-coordination Parent", null));
//		//superclassSelector.setup(getWidgetConfiguration(), new PropertyEntityData(ICDContentModelConstants.PRECOORDINATION_SUPERCLASS_PROP, "Pre-coordination Parent", null));
//		superclassSelector.setup(configMap, new PropertyEntityData(prop));
//		//TODO check superclassSelector.createComponent();
		
		FormGenerator formGenerator = widgetController.getFormGenerator();
		SuperclassSelectorWidget superclassSelector = formGenerator.createPreCoordinationSuperclassWidget(configMap, prop, widgetController);
		
		superclassSelector.setContainerWidget(this);
		return superclassSelector;
	}

	private Panel createScaleValueSelectorWidgets() {
		widgetController.initWidgets();
		valueSelWidgets = new ArrayList<AbstractScaleValueSelectorWidget>();
		Panel propertyValuePanel = new Panel();
		Map<String, Object> propConfigMap = (Map<String, Object>) getWidgetConfiguration().get(FormConstants.VALUE_SELECTORS);
		for (String prop : propConfigMap.keySet()) {
			Object value = propConfigMap.get(prop);
			if (value instanceof Map) {
				Map<String, Object> config = (Map<String, Object>)value;
				AbstractScaleValueSelectorWidget widget = createScaleValueSelectorWidget(config, prop, propertyValuePanel);
	            if (widget != null && widget.getComponent() != null) {
	            	propertyValuePanel.add(widget.getComponent());
	            	
	            	//TODO see if we need to maintain a widget list and/or a property-widget map
	            	valueSelWidgets.add(widget);

	            	widgetController.addWidget(widget);
	            }
			}
			else {
				GWT.log("Wrong configuration for property " + prop + " (expecting a map)");
			}
		}
		return propertyValuePanel;
	}
	
	private AbstractScaleValueSelectorWidget createScaleValueSelectorWidget(
				Map<String, Object> configMap, String prop, Panel containerPanel) {
		
		FormGenerator formGenerator = widgetController.getFormGenerator();
        String component_type = (String) configMap.get(FormConstants.COMPONENT_TYPE);
        AbstractScaleValueSelectorWidget widget = null;
        
		if (component_type.equals(FormConstants.PRECOORDINATION_CUST_SCALE_VALUE_SELECTOR)) { //ICD specific
        	widget = formGenerator.createPreCoordinationCustomScaleValueSelectorWidget(configMap, prop);
        } else if (component_type.equals(FormConstants.PRECOORDINATION_FIX_SCALE_VALUE_SELECTOR)) { //ICD specific
        	widget = formGenerator.createPreCoordinationFixedScaleValueSelectorWidget(configMap, prop);
        } else if (component_type.equals(FormConstants.PRECOORDINATION_TREE_VALUE_SELECTOR)) { //ICD specific
        	widget = formGenerator.createPreCoordinationTreeValueSelectorWidget(configMap, prop);
        } else if (component_type.equals(FormConstants.HTMLMESSAGE)) {
        	PropertyWidget propWidget = formGenerator.createHtmlMessage(configMap, prop);
        	containerPanel.add(propWidget.getComponent());
        	//we generate the HTML Message widget but we return null, as this is not a scale value selector widget
        }
		//TODO see that this is solved
		//widget.setPreCoordinationWidget(this);
		return widget;
	}

	@Override
	public void setSubject(EntityData subject) {
		super.setSubject(subject);
		for (AbstractScaleValueSelectorWidget valueSelector : valueSelWidgets) {
			valueSelector.setSubject(subject); //TODO: check if it makes remote call; shouldn't
		}
		
		superclassSelector.setSubject(subject);
	}
    
	@Override
	public void fillValues() {
		//This widget has no property associated, therefore there is no values 
		//to be filled in this widget, but we should fill in the value for the 
		//superclass selector (and as a consequence????) also for the other
		//scale value selector widgets.
		superclassSelector.fillValues();
	}
	
    @Override
    public void setValues(Collection<EntityData> values) {
    	// TODO Do nothing for now.
    	// This widget does not have values itself, only its component widgets, 
    	// which will have their values set through different mechanisms
    }

    @Override
    public Collection<EntityData> getValues() {
    	// return null, as this widget does not store (property) values by itself, only its sub-widgets
    	return null;
    }

    @Override
	public void onSuperclassChanged(EntityData newSuperclass) {
		widgetController.onSuperclassChanged(newSuperclass);
	}
}
