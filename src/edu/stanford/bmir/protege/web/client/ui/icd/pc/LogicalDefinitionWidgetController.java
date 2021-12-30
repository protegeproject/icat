package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.AllowedPostcoordinationValuesData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.PrecoordinationClassExpressionData;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormGenerator;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.WidgetController;

public class LogicalDefinitionWidgetController<ControllingWidget extends LogicalDefinitionWidget<?>>
									extends WidgetController<ControllingWidget> {

	private Project project;
	private Collection<PropertyWidget> widgets = null;
	
	private List<String> treeValueProperties = null;
	
	private LogicalDefinitionWidget<?> logicalDefinitionWidget;
	private NecessaryConditionsWidget necessaryConditionsWidget;
	private EntityData lastSelectedSuperclass;
	
	public LogicalDefinitionWidgetController(Project project, Panel tabPanel, FormGenerator formGenerator) {
		super(tabPanel, formGenerator);
		this.project = project;
		initWidgets();//TODO check if this is necessary, or is there a better place to call this
	}

	//copied from PreCoordinationWidgetController
	public void initWidgets() {
		this.widgets = new ArrayList<PropertyWidget>();
		this.treeValueProperties = new ArrayList<String>();
	}

	//copied from PreCoordinationWidgetController
	public void setWidgets(Collection<PropertyWidget> widgets) {
		if (widgets == null || widgets.isEmpty()) {
			initWidgets();
		}
		else {
			this.widgets = new ArrayList<PropertyWidget>(widgets);
			updatePropertiesLists(widgets);
		}
	}

	//copied from PreCoordinationWidgetController
	public void addWidget(PropertyWidget widget) {
		if (this.widgets == null) {
			initWidgets();
		}
		this.widgets.add(widget);
		updatePropertiesLists(widget);
	}

	//copied from PreCoordinationWidgetController
	private void updatePropertiesLists(Collection<PropertyWidget> widgets) {
		for (PropertyWidget widget : widgets) {
			updatePropertiesLists(widget);
		}
	}

	//copied from PreCoordinationWidgetController
	private void updatePropertiesLists(PropertyWidget widget) {
		if (widget instanceof TreeValueSelector){
			treeValueProperties.add(widget.getProperty().getName());
		} else {
			if (widget == null) {
				GWT.log("Null widget! Can't initialize property list.");
			}
			else {
				GWT.log("Unrecognized value selector type " + widget.getClass() + " for property " + widget.getProperty());
			}
		}
	}


	//copied from PreCoordinationWidgetController
	protected Collection<PropertyWidget> getWidgets() {
		if (widgets == null) {
			return super.getWidgets();
		}
		else {
			return widgets;
		}
	}


	@Override
	public void setControllingWidget(ControllingWidget widget) {
		super.setControllingWidget(widget);
		if (widget instanceof LogicalDefinitionWidget) {
			logicalDefinitionWidget = (LogicalDefinitionWidget<?>) widget;
		}
		else {
			GWT.log("ERROR: The controlling widget of a LogicalDefinitionWidgetController should be of type LogicalDefinitionWidget!");
		}
	}
	
	public LogicalDefinitionWidget<?> getLogicalDefinitionWidget() {
		return logicalDefinitionWidget;
	}

//	public void setLogicalDefinitionWidget(LogicalDefinitionWidget logicalDefinitionWidget) {
//		this.logicalDefinitionWidget = logicalDefinitionWidget;
//	}

	public NecessaryConditionsWidget getNecessaryCondittionsWidget() {
		return necessaryConditionsWidget;
	}

	public void setNecessaryConditionsWidget(NecessaryConditionsWidget necessaryConditionsWidget) {
		this.necessaryConditionsWidget = necessaryConditionsWidget;
	}


	//copied from PreCoordinationWidgetController
	public void onSuperclassChanged(EntityData newSuperclass) {
		lastSelectedSuperclass = newSuperclass;

		//these are from onSubjectChanged (we need to do similar things here):
		EntityData subject = logicalDefinitionWidget.getSubject();
		
		if (newSuperclass != null) {
			getPossiblePostcoordinationAxes(newSuperclass);
			if (subject != null) {
				hideAllWidgets();
				updateLoadingStatus(true);
				//getSuperclassValue();
				getPropertyValues(subject);
				setLogicalDefinitionSuperclassForPropertySelectorWidgets(newSuperclass);
				getPossiblePropertyValues(subject);
				necessaryConditionsWidget.updateNecessaryToLogicalDefinitionButton(true);
			}
			//end
		}
		else {
			hideAllWidgets();
			if (subject != null) {
				getPropertyValues(subject);
				setLogicalDefinitionSuperclassForPropertySelectorWidgets(null);
				necessaryConditionsWidget.updateNecessaryToLogicalDefinitionButton(false);
			}
		}
	}

	public EntityData getLastSelectedSuperclass() {
		return lastSelectedSuperclass;
	}

	//copied from PreCoordinationWidgetController
	private void getPossiblePostcoordinationAxes(EntityData superclass) {
		GWT.log("getPossiblePostcoordinationAxes" + superclass);
		hideAllWidgets();
		ICDServiceManager.getInstance().getListOfSelectedPostCoordinationAxes(
				project.getProjectName(), superclass.getName(), (List<String>) null, 
				new GetPostCoordinationAxesHandler(superclass));
		
	}
	
	//copied from PreCoordinationWidgetController
	private class GetPostCoordinationAxesHandler extends AbstractAsyncHandler<List<String>> {
		private EntityData subjectSuperclass;
		
		public GetPostCoordinationAxesHandler(EntityData superclass) {
			this.subjectSuperclass =superclass;
		}
		
		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Failure on GetPostCoordinationAxesHandler");
			
			updateLoadingStatus(false);
		}

		@Override
		public void handleSuccess(List<String> result) {
			GWT.log("GetPostCoordinationAxesHandler: " + result.size() + " props: " + result);
			
			EntityData lastSelectedSuperclass = getLastSelectedSuperclass();
			String lastSelectedSuperclassName = (lastSelectedSuperclass == null ? null : lastSelectedSuperclass.getName());
			String subjectSuperclassName = (subjectSuperclass == null ? null : subjectSuperclass.getName());
			
			if ( lastSelectedSuperclassName != null && lastSelectedSuperclassName.equals(subjectSuperclassName))  {
				updatePropertyList(result);
				updateLoadingStatus(false);
			}
		}
		
	}

	//copied from PreCoordinationWidgetController
	protected void updateLoadingStatus(boolean isLoading) {
		getControllingWidget().setLoadingStatus(isLoading);
	}

	@Override
	protected List<String> getAllProperties() {
		//TODO: finish this
		//return super.getAllProperties();
		return logicalDefinitionWidget.getValidProperties();
	}


	//@Override
	protected void updatePropertyList(List<String> result) {
		GWT.log("updatePropertyList");
		if (logicalDefinitionWidget != null) {
			logicalDefinitionWidget.setValidProperties(result);
		}
		else {
			GWT.log("logicalDefinitionWidget id not set for " + this);
		}
	}

	//copied from PreCoordinationWidgetController
	public void onSubjectChanged(EntityData subject) {
		GWT.log("getPropertyValues" + subject);
		if (subject != null) {
			//TODO continue here 
				hideAllWidgets();
				updateLoadingStatus(true);
			getSuperclassValue();
			getPropertyValues(subject);
			getPossiblePropertyValues(subject);
		}
		else {
			hideAllWidgets();
		}
	}

	//copied from PreCoordinationWidgetController
	private void getSuperclassValue() {
		// TODO Auto-generated method stub
		GWT.log("getSuperclassValue - do nothing");
	}


	//copied from PreCoordinationWidgetController & modified
	private void getPropertyValues(EntityData subject) {
		GWT.log("getPropertyValues for " + subject + "properties: " + getAllProperties());
		ICDServiceManager.getInstance().getPreCoordinationClassExpressions(
				project.getProjectName(), subject.getName(), logicalDefinitionWidget.getSelectedPrecoordSuperclass(),
				getAllProperties(),
				new AsyncCallback<List<PrecoordinationClassExpressionData>>() {
					
					@Override
					public void onSuccess(List<PrecoordinationClassExpressionData> res) {
						updateWidgetContents(res);
						updateLoadingStatus(false);
					}
					
					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Failed getPreCoordinationClassExpressions");
						updateLoadingStatus(false);
						
					}
				});
	}

	//@Override
	protected PropertyWidget createWidgetForProperty(String propertyName, boolean isDefinitional) {
		if (isDefinitional) {
			logicalDefinitionWidget.addAxisToForm(propertyName);
			return logicalDefinitionWidget.getWidgetForProperty(propertyName);
		}
		else {
			necessaryConditionsWidget.addAxisToForm(propertyName);
			return necessaryConditionsWidget.getWidgetForProperty(propertyName);
		}
	}

	
	//@Override
	protected void removeWidgetForProperty(String propertyName) {
		hideWidgetForProperty(propertyName);
		
	}
	
	//@Override
	public void afterWidgetVisibilityChanged(PropertyWidget widget, boolean isVisible) {
		logicalDefinitionWidget.afterWidgetVisibilityChanged(widget, isVisible);
		//TODO check if this makes sense like this
		necessaryConditionsWidget.afterWidgetVisibilityChanged(widget, true);
	}

	//copied from PreCoordinationWidgetController
	private void updateWidgetContents(
			List<PrecoordinationClassExpressionData> res) {
		GWT.log("updateWidgetContents " + res);
		String selectedPrecoordSuperclass = logicalDefinitionWidget.getSelectedPrecoordSuperclass();
		List<String> allProperties = getAllProperties();
		for (PrecoordinationClassExpressionData classExprData : res) {
			String property = classExprData.getProperty().getName();
			PropertyWidget widget = getWidgetForProperty(property);
			if ( widget == null ) {
				widget = createWidgetForProperty(property, classExprData.isDefinitional());
			}
			else {
				showWidget(widget);
			}
			setWidgetValue(widget, classExprData.getValue(), classExprData.isDefinitional());
			if (widget instanceof AbstractScaleValueSelectorWidget) {
				setLogicalDefinitionSuperclassForWidget( (AbstractScaleValueSelectorWidget)widget, selectedPrecoordSuperclass );
			}
			allProperties.remove(property);
		}
		//allProperties contains now those properties for which we did not have values
		//we need to set their values to null
		String ctrlProperty = getControllingWidget().getProperty().getName();
		for (String property : allProperties) {
			PropertyWidget widget = getWidgetForProperty(property);

			if ( widget != null && !ctrlProperty.equals(property) ) {
				setWidgetValue(widget, null, false);
				removeWidgetForProperty(property);
				afterWidgetVisibilityChanged(widget, false);
			}
		}
	}

	private void setLogicalDefinitionSuperclassForWidget( AbstractScaleValueSelectorWidget scValSelWidget, String selectedPrecoordSuperclass) {
		scValSelWidget.setLogicalDefinitionSuperclass(selectedPrecoordSuperclass);
	}


	//copied from PreCoordinationWidgetController
	protected void setWidgetValue(PropertyWidget widget, EntityData entityData, boolean isDefinitional) {
		GWT.log("setWidgetValue: " + entityData + " isDef: " + isDefinitional);
		if (entityData == null) {
			widget.setValues(null);
		}
		else {
			widget.setValues(Collections.singletonList(entityData));
		}

		if (widget instanceof ValueSelectorComponent) {
			((ValueSelectorComponent)widget).setIsDefinitional(isDefinitional);
		}
	}
	
	//copied from PreCoordinationWidgetController
	private void getPossiblePropertyValues(EntityData subject) {
		GWT.log("getPossiblePropertyValues" + subject);
		ICDServiceManager.getInstance().getAllowedPostCoordinationValues(
				project.getProjectName(), getLogicalDefinitionWidget().getSelectedPrecoordSuperclass(),
						null, treeValueProperties, null,
				new AsyncCallback<List<AllowedPostcoordinationValuesData>>() {
					
					@Override
					public void onSuccess(List<AllowedPostcoordinationValuesData> res) {
						GWT.log("Result of getAllowedPostCoordinationValues: " + res);
						updateWidgetDrop(res);
					}
					
					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Failed getAllowedPostCoordinationValues");
					}
				});
	}

	//copied from PreCoordinationWidgetController
	private void updateWidgetDrop(List<AllowedPostcoordinationValuesData> res) {
		GWT.log("updateWidgetDrop" + res);
		for (AllowedPostcoordinationValuesData allowedPCValueData : res) {
			String propName = allowedPCValueData.getProperty().getName();
			PropertyWidget widget = getWidgetForProperty(propName);
			if (widget instanceof AbstractScaleValueSelectorWidget) {
				AbstractScaleValueSelectorWidget scValSelWidget = (AbstractScaleValueSelectorWidget)widget;
				scValSelWidget.setAllowedValues(allowedPCValueData.getValues());
			}
		}
	}

	//TODO we can probably delete this, if it is only called from onSuperclassChanged, after all widgets are already hidden
	private void setLogicalDefinitionSuperclassForPropertySelectorWidgets(EntityData newSuperclass) {
		GWT.log("setSuperclassForPropertySelectorWidgets " + newSuperclass);
		List<String> allProperties = getAllProperties();
		for (String property : allProperties) {
			PropertyWidget widget = getWidgetForProperty(property);

			if (widget instanceof AbstractScaleValueSelectorWidget) {
				String newSuperclassName = newSuperclass == null ? null : newSuperclass.getName();
				setLogicalDefinitionSuperclassForWidget( (AbstractScaleValueSelectorWidget)widget, newSuperclassName );
			}
		}
	}
	
	@Override
	protected PropertyWidget getWidgetForProperty(String propertyName) {
		PropertyWidget widget = super.getWidgetForProperty(propertyName);
		if ( widget == null ) {
			widget = logicalDefinitionWidget.getWidgetForProperty(propertyName);

			if ( widget == null ) {
				widget = necessaryConditionsWidget.getWidgetForProperty(propertyName);
			}
		}
		return widget;
	}
	
	@Override
	public void hideAllWidgets() {
		// TODO hide widgets
		//super.hideAllWidgets();
		logicalDefinitionWidget.removeAllAxis();
		necessaryConditionsWidget.removeAllAxis();
		
		//update list of properties in properties selector
		logicalDefinitionWidget.clearPropertiesList();
		//TODO see if we need to call this, or something else
		//necessaryConditionsWidget.clearPropertiesList();
	}
	
	public void showHideWidgetsDueToTypeChange(PostCoordinationAxesForm form, List<String> toShow, List<String> toHide) {
		GWT.log("showHideWidgetsDueToTypeChange: " + form + "\ntoShow: " + toShow + "\ntoHide: " + toHide);
		// TODO Check this
    	for (String property : toShow) {
        	showWidgetForProperty(property);
		}
    	for (String property : toHide) {
        	hideWidgetForProperty(property);
		}
	}

	@Override
	public void showWidget(PropertyWidget widget) {
		if (widget != null ) {
			super.showWidget(widget);
			afterWidgetVisibilityChanged(widget, true);
		}
	}

//	@Override
//	protected void setWidgetValue(PropertyWidget widget,
//			EntityData entityData, boolean isDefinitional) {
//		super.setWidgetValue(widget, entityData, isDefinitional);
//		if ( entityData == null ) {
//			hide
//		}
//	}

	public void updateIsDefinitionalOfPropertyWidget(String property, PropertyValueSelectorWidget widget, boolean isDefinitional) {
		PropertyWidget logDefWidgetForProperty = logicalDefinitionWidget.getWidgetForProperty(property);
		PropertyWidget necCondWidgetForProperty = necessaryConditionsWidget.getWidgetForProperty(property);
		Collection<EntityData> values = widget.getValues();
		EntityData currValue = null;
		if (values != null &&  ! values.isEmpty()) {
			currValue = values.iterator().next();
		}
//		else {
//			//TODO This is a hack. We should not need this if widget.getValues() would work properly. Make sure to fix  getValues, and remove this.
//			currValue = new EntityData(widget.getSelectedValue());
//		}
		
		if ( isDefinitional ) {
			if ( widget == necCondWidgetForProperty ) {
				necessaryConditionsWidget.removeAxisFromForm(property);
				logicalDefinitionWidget.addAxisToForm(property);
				setWidgetValue( logicalDefinitionWidget.getWidgetForProperty(property), currValue, isDefinitional );
			}
			else if ( widget == logDefWidgetForProperty ) {
				setWidgetValue( logicalDefinitionWidget.getWidgetForProperty(property), currValue, isDefinitional );
			}
		}
		else {
			if ( widget == logDefWidgetForProperty ) {
				logicalDefinitionWidget.removeAxisFromForm(property);
				necessaryConditionsWidget.addAxisToForm(property);
				setWidgetValue( necessaryConditionsWidget.getWidgetForProperty(property), currValue, isDefinitional );
			}
		}
	}
	
}
