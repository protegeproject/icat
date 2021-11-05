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
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class NecessaryConditionsWidget extends AbstractPropertyWidget {

	private Panel wrappingPanel;
//	private SuperclassSelectorWidget superclassSelector;
//	private List<AbstractScaleValueSelectorWidget> valueSelWidgets;
	private PostCoordinationAxesForm pcAxesForm;
	private PropertySelectorWidget propertySelectorWidget;
	
    private LogicalDefinitionWidgetController widgetController;
    
    private boolean visibilityUpdateNeeded;

	public NecessaryConditionsWidget(Project project, LogicalDefinitionWidgetController widgetController) {
		super(project);
		this.widgetController = widgetController;
	}

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
//        widgetController.initializePropertyMap(widgetConfiguration);
        System.out.println("seting up propertySelectorWidget...");
        GWT.log("seting up propertySelectorWidget...");
    }

    @Override
    public boolean isTypeSensitive() {
    	return true;
    }

    @Override
    public void changeVisibilityBasedOnSubjectType(Collection<EntityData> types) {

    	GWT.log(getClass().getSimpleName() + ".changeVisibilityBasedOnSubjectType: visibilityUpdateNeeded =  " + visibilityUpdateNeeded);
    	
    	if ( visibilityUpdateNeeded ) {
    		propertySelectorWidget.updateListOfValidPropertiesBasedOnTypes(types, widgetController);
    	}
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
//		superclassSelector = createSuperClassSelectorWidget();
//		Panel propertyValuePanel = createScaleValueSelectorWidgets();
		
//		wrappingPanel.add(superclassSelector.getComponent());
//		wrappingPanel.add(propertyValuePanel);
		System.out.println("createComponent: adding PostCoordinationAxesForm");
		GWT.log("createComponent: adding PostCoordinationAxesForm");
		pcAxesForm = createPostCoordinationAxesForm();
		//propertySelectorWidget = new PropertySelectorWidget(getProject());
		//wrappingPanel.add(propertySelectorWidget.getComponent());
		wrappingPanel.add(pcAxesForm);
		return wrappingPanel;
	}
	

	@Override
	public void setSubject(EntityData subject) {
		Collection<EntityData> oldSubjectTypes = (getSubject() == null ? new ArrayList<EntityData>() : getSubject().getTypes());
		if (oldSubjectTypes == null) {
			GWT.log("WARNING: Old subject's type is NULL. Old subject: " + getSubject());
			oldSubjectTypes = new ArrayList<EntityData>();
		}
		
		super.setSubject(subject);

		Collection<EntityData> newSubjectTypes = (subject == null ? new ArrayList<EntityData>() : subject.getTypes());
		
		boolean sameSubjects = oldSubjectTypes.equals(newSubjectTypes);
		visibilityUpdateNeeded = ! sameSubjects;

//		for (AbstractScaleValueSelectorWidget valueSelector : valueSelWidgets) {
//			valueSelector.setSubject(subject); //TODO: check if it makes remote call; shouldn't
//		}
		List<String> allProperties = widgetController.getAllProperties();
		for ( String property : allProperties ) {
			PropertyWidget widget = widgetController.getWidgetForProperty(property);
			if ( widget != null ) {
				widget.setSubject(subject);
			}
		}
//		superclassSelector.setSubject(subject);
		propertySelectorWidget.setSubject(subject);
	}
    
	@Override
	public void fillValues() {
		//This widget has no property associated, therefore there is no values 
		//to be filled in this widget, but we should fill in the value for the 
		//superclass selector (and as a consequence????) also for the other
		//scale value selector widgets.
		//NO NEED FOR THIS: super.fillValues();    (which means that setValues() won't be called, therefore TODO: It is OK to delete it)
//		superclassSelector.fillValues();
	}
	
    @Override
    public void setValues(Collection<EntityData> values) {
    	// TODO Do nothing for now.
    	// This widget does not have values itself, only its component widgets, 
    	// which will have their values set through different mechanisms
    	//System.out.println("Test: DELETE THIS");
    }

    @Override
    public Collection<EntityData> getValues() {
    	// return null, as this widget does not store (property) values by itself, only its sub-widgets
    	return null;
    }

	public void onSuperclassChanged(EntityData newSuperclass) {
//		widgetController.onSuperclassChanged(newSuperclass);
	}
	
	private PostCoordinationAxesForm createPostCoordinationAxesForm()  {
		pcAxesForm = new PostCoordinationAxesForm(
				getProject(), FormConstants.NECESSARY_CONDITIONS_COMP, widgetController);
		System.out.println("form panel:"  + pcAxesForm + "  adding propertySelectorWidget: " +propertySelectorWidget);
		GWT.log("form panel:"  + pcAxesForm + "  adding propertySelectorWidget: " +propertySelectorWidget);
		
		//form.add(propertySelectorWidget);
		
		propertySelectorWidget = new PropertySelectorWidget(getProject(), pcAxesForm);
		Map<String, Object> propertyDefinitionsConfiguration = UIUtil.getMapConfigurationProperty( 
				getWidgetConfiguration(), FormConstants.LOGICAL_DEF_PROPERTIES_CONFIG);
		propertySelectorWidget.setup( propertyDefinitionsConfiguration);

		pcAxesForm.setPropertyMaps(
//				propertySelectorWidget.getPropertyToLabelMap(), 
				propertySelectorWidget.getPropertyToConfigMap());
		pcAxesForm.setPropertySelector(propertySelectorWidget);

		pcAxesForm.add(propertySelectorWidget.getComponent());

		return pcAxesForm;
	}

	public void setValidProperties(List<String> propertyList) {
		GWT.log("setValidProperties" + propertyList);
		propertySelectorWidget.setRecommendedProperties(propertyList);
	}
	
	public List<String> getValidProperties() {
		GWT.log("getValidProperties");
		return propertySelectorWidget.getListOfValidProperties();
	}
	
	public void clearPropertiesList() {
		GWT.log("clearPropertiesList");
		propertySelectorWidget.clearProperties();
	}
	
	private String getLabelForProperty(String property) {
		return propertySelectorWidget.getLabelForProperty(property);
	}
	
	public void addAxisToForm(String property) {
		pcAxesForm.addFieldForAxis(property, getLabelForProperty(property));
//		widgetController.addWidget2PropertyMap???(getWidgetForProperty(property));
		//TODO delete this, if we keep the call in propertiesForm.addFieldForAxis
		//propertySelectorWidget.setActiveStatusForOption(property, false);
	}

	public void removeAxisFromForm(String property) {
		pcAxesForm.removeFieldForAxis(property);
		//TODO check addAxisToForm for other potential steps to be added here
	}

	public PropertyWidget getWidgetForProperty(String property) {
		return pcAxesForm.getWidgetForProperty(property);
	}
	
	public void removeAllAxis() {
		pcAxesForm.removeAllFields();
	}
	
	public void afterWidgetVisibilityChanged(PropertyWidget widget, boolean isVisible) {
		//TODO check and test this. If good, implement it in NecessaryConditionsWidget as well.
		//get property name
		String property = widget.getProperty().getName();
		//check if widget is part of this
		PropertyWidget widgetForProperty = getWidgetForProperty(property);
		if ( widgetForProperty == widget ) {
			//switch activity status
			propertySelectorWidget.setActiveStatusForOption(property, ! isVisible);
		}
	}
}
