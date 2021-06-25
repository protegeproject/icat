package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.FormPanel;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.icd.pc.AbstractScaleValueSelectorWidget.SwitchButtonType;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class PostCoordinationAxesForm extends Panel {
	private Project project;
	//private Map<String, String> propertyToLabelMap;
	private Map<String, Map<String, Object>> propertyToConfigMap;
	private Map<String, PropertyWidget> propertyToWidgetMap;
//	private Map<String, Field> propertyToFieldMap;
	private PropertySelectorWidget propertySelector;
	private List<String> fieldNames;
	private int widgetCounter = 0;
	private String formPanelName;
	
    private LogicalDefinitionWidgetController widgetController;
	
	private final String PARENT_REF = "%parent%";

	/**
	 * Make sure to also call the {@link #setPropertyMaps(Map)} and 
	 * {@link #setPropertySelector(PropertySelectorWidget)} methods as well. 
	 * 
	 * @param project
	 * @param name
	 */
	public PostCoordinationAxesForm(Project project, String name, LogicalDefinitionWidgetController widgetController) {
		this.project = project;
		this.formPanelName = name;
		this.widgetController = widgetController;
	}

	public void setPropertyMaps(
		//	Map<String, String> propertyToLabelMap, 
			Map<String, Map<String, Object>> propertyToConfigMap) {
		//TODO init project;
		//this.propertyToLabelMap = propertyToLabelMap;
		this.propertyToConfigMap = propertyToConfigMap;
		this.propertyToWidgetMap = new HashMap<String, PropertyWidget>();
//		this.propertyToFieldMap = new HashMap<String, Field>();
		fieldNames = new ArrayList<String>();
	}

	public void setPropertySelector(PropertySelectorWidget propertySelector) {
		this.propertySelector = propertySelector;
	}

	public LogicalDefinitionWidgetController getWidgetController() {
		return widgetController;
	}
	
	public String getFormPanelName() {
		return formPanelName;
	}
	
	public void addFieldForAxis(String property, String label) {
		addFieldForAxis(property, label, false);
	}
	
	public void addFieldForAxis(String property, String label, boolean openAfterAdd) {
		if ( ! fieldNames.contains(property) ) {
				boolean isLogicalDefinitionForm = FormConstants.LOGICAL_DEFINITIONS_COMP.equals( formPanelName );
				PropertyValueSelectorWidget treeValueSelector = new PropertyValueSelectorWidget(project, isLogicalDefinitionForm);
				treeValueSelector.setContainerFormPanel(this);
				treeValueSelector.setSwitchBetweenLogicalAndNecessaryButtonType(
						( isLogicalDefinitionForm ?
								SwitchButtonType.LOGICAL_TO_NECESSARY : 
								SwitchButtonType.NECESSARY_TO_LOGICAL ) );
				
				Map<String, Object> widgetConfiguration = propertyToConfigMap.get(property);
				updateWidgetName( widgetConfiguration, formPanelName, ++widgetCounter);
				
				treeValueSelector.setup( widgetConfiguration, new PropertyEntityData(property) );
				GWT.log("Set subject: " + propertySelector.getSubject());
				treeValueSelector.setSubject( propertySelector.getSubject() );
				
				super.insert(fieldNames.size(), treeValueSelector.getComponent());
//				//testing
//				treeValueSelector.getComponent().setVisible(true);
				//super.add(treeValueSelector.getComponent());
				propertyToWidgetMap.put(property, treeValueSelector);
				//TODO check if we need this, as this is already done in LogicalDefinitionWidget.addAxisToForm and PropertySelectorWidget.onSelect.
				//			Decide which place is more appropriate
//				if ( propertySelector != null ) {
					propertySelector.setActiveStatusForOption(property, false);
//				}

				fieldNames.add(property);
///*testing*/				GWT.log("doing deep (not shallow) layout");
//				doLayout(false);
				GWT.log("doing (default) layout");
				doLayout();
				
//testing				if (! treeValueSelector.getComponent().isRendered()) {
//					GWT.log("force rendering, because it is not rendered");
//				}
//				else {
//					GWT.log("force rendering, although it is rendered already");
//				}
//				treeValueSelector.getComponent().render(super.getElement());
//				Component comp = treeValueSelector.getComponent();
//				comp.setVisible(true);
//				if (comp instanceof Panel) {
//					GWT.log("do Layout of treeValueSelector.getConmponent():  " + comp);
//					((Panel) comp).doLayout(false);
//					((Panel) comp).doLayout();
//				}
				
				if (openAfterAdd) {
					treeValueSelector.onSelectValue();
				}
		}
		else {
			PropertyWidget widget = getWidgetForProperty(property);
			if (widget.getComponent().isHidden()) {
				widget.getComponent().setVisible(true);
			}
			//GWT.log(String.format("Property %s already present in the form.", property));
//			GWT.log("Property " + property + " already present in the PostCoordinationAxesFormPanel " + this.getId());
			GWT.log("Property " + property + " already present in the PostCoordinationAxesFormPanel " + this);
		}
	}
	
	private void updateWidgetName(Map<String, Object> widgetConfiguration, String parentContainerName, int index) {
		String nameProp = UIUtil.getStringConfigurationProperty(widgetConfiguration, FormConstants.NAME, "");
		GWT.log("Original widget id: " + nameProp);
		nameProp = nameProp.replaceAll(PARENT_REF, parentContainerName);
		//TODO check if we need to use index, or it actually complicates things, because it makes widget name dependent on subject types
		nameProp = nameProp.replaceAll( "_\\d+$", "" )+ "_" + index;
		GWT.log("New widget id: " + nameProp);
		widgetConfiguration.put(FormConstants.NAME, nameProp);
	}

	public void removeFieldForAxis(String property) {
		if (fieldNames.contains(property)) {
			PropertyWidget propertyWidget = propertyToWidgetMap.get(property);
			GWT.log("Removing: " + propertyWidget);
//			super.remove( propertyWidget.getComponent() );
			super.remove( propertyWidget.getComponent() );
			if ( propertySelector != null ) {
				propertySelector.setActiveStatusForOption(property, true);
			}
			propertyToWidgetMap.remove(property);

//			Field propertyField = propertyToFieldMap.get(property);
//			GWT.log("Removing: " + propertyField);
//			super.remove( propertyField);
//			propertyToFieldMap.remove(property);

			fieldNames.remove(property);
			widgetCounter--;
		} else {
			GWT.log("Property " + property + " not in PostCoordinationAxesFormPanel: " + this);
		}
	}

	public void removeAllFields() {
		for ( String fieldName : new ArrayList<>( fieldNames ) ) {
			removeFieldForAxis(fieldName);
		}
		GWT.log("fieldNames count:" + fieldNames.size() + " fields: " + fieldNames);
	}

	public PropertyWidget getWidgetForProperty(String property) {
		return propertyToWidgetMap.get(property);
	}

	/**
	 * TODO Delete this if it turns out to be unnecessary.
	 * 	This method was moved (back) to PropertySelectorWidget, which will call the {@link #showHideWidgetsDueToTypeChange(ArrayList, List)
	 */
	public void updateListOfValidProperties(Collection<EntityData> types, LogicalDefinitionWidgetController widgetController) {
		GWT.log("Warning: this method should not be called, anymore");
//		List<String> oldListOfValidProperties = new ArrayList<String>( propertySelector.getListOfValidProperties() );
//		List<String> newListOfValidProperties = new ArrayList<String>();
//		
//    	List<String> allProperties = propertySelector.getListOfAllProperties();
//    	for (String property : allProperties) {
//    		Map<String, Object> propConfig = propertyToConfigMap.get(property);
//    		
//    		//TODO check whether we need this
//    		boolean isHidden = UIUtil.getBooleanConfigurationProperty(propConfig, FormConstants.HIDDEN, false);
//    		if ( ! isHidden ) {
//	        	List<String> showOnlyForTypesList = UIUtil.getListConfigurationProperty(propConfig, FormConstants.SHOW_ONLY_FOR_TYPES, null);
//	        	List<String> doNotShowForTypesList = UIUtil.getListConfigurationProperty(propConfig, FormConstants.DO_NOT_SHOW_FOR_TYPES, null);
//	
//	        	boolean isWidgetVisibleForThisTypeOfEntity = UIUtil.calculateVisibilityBasedOnSubjectType(types, showOnlyForTypesList, doNotShowForTypesList);
//	        	if (isWidgetVisibleForThisTypeOfEntity) {
//	        		newListOfValidProperties.add(property);
//	        		
//	        	}
//    		}
//		}
//    	propertySelector.setListOfValidProperties( newListOfValidProperties );
//    	
//    	//show/hide widgets
//    	ArrayList<String> toShow = new ArrayList<>(newListOfValidProperties);
//    	toShow.removeAll(oldListOfValidProperties);
//    	for (String property : toShow) {
//        	widgetController.showWidgetForProperty(property);
//		}
//    	oldListOfValidProperties.removeAll(newListOfValidProperties);
//    	for (String property : oldListOfValidProperties) {
//        	widgetController.hideWidgetForProperty(property);
//		}
	}

}
