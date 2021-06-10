package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.shared.GWT;
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

public class PreCoordinationWidgetController extends WidgetController {

	private Project project;
	private Collection<PropertyWidget> widgets = null;
	
	private List<String> treeValueProperties = null;

	public PreCoordinationWidgetController(Project project, Panel tabPanel,
			FormGenerator formGenerator) {
		super(tabPanel, formGenerator);
		this.project = project;
	}

	
	public void initWidgets() {
		this.widgets = new ArrayList<PropertyWidget>();
		this.treeValueProperties = new ArrayList<String>();
	}

	public void setWidgets(Collection<PropertyWidget> widgets) {
		if (widgets == null || widgets.isEmpty()) {
			initWidgets();
		}
		else {
			this.widgets = new ArrayList<PropertyWidget>(widgets);
			updatePropertiesLists(widgets);
		}
	}

	public void addWidget(PropertyWidget widget) {
		if (this.widgets == null) {
			initWidgets();
		}
		this.widgets.add(widget);
		updatePropertiesLists(widget);
	}

	private void updatePropertiesLists(Collection<PropertyWidget> widgets) {
		for (PropertyWidget widget : widgets) {
			updatePropertiesLists(widget);
		}
	}

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


	protected Collection<PropertyWidget> getWidgets() {
		if (widgets == null) {
			return super.getWidgets();
		}
		else {
			return widgets;
		}
	}

	public void onSuperclassChanged(EntityData newSuperclass) {
		if (newSuperclass != null) {
			getPossiblePostcoordinationAxes(newSuperclass);
		}
		else {
			hideAllWidgets();
		}
	}


	private void getPossiblePostcoordinationAxes(EntityData superclass) {
		GWT.log("getPossiblePostcoordinationAxes" + superclass);
		hideAllWidgets();
		ICDServiceManager.getInstance().getListOfSelectedPostCoordinationAxes(
				project.getProjectName(), superclass.getName(), (List<String>) null, 
				new GetPostCoordinationAxesHandler());
		
	}
	
	private class GetPostCoordinationAxesHandler extends AbstractAsyncHandler<List<String>> {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Failure on GetPostCoordinationAxesHandler");
		}

		@Override
		public void handleSuccess(List<String> result) {
			GWT.log("GetPostCoordinationAxesHandler: " + result.size() + " props: " + result);
			
			updatePropertyList(result);
		}
		
	}

	protected void updatePropertyList(List<String> result) {
		for (String prop : result) {
			for (String relProp : getAllRelatedProperties(prop)) {
				showWidgetForProperty(relProp);
			}
		}
	}

	public void onSubjectChanged(EntityData subject) {
		GWT.log("getPropertyValues" + subject);
		if (subject != null) {
			//TODO continue here hideAllWidgets();
			getSuperclassValue();
			getPropertyValues(subject);
			getPossiblePropertyValues(subject);
		}
		else {
			hideAllWidgets();
		}
	}

	private void getSuperclassValue() {
		// TODO Auto-generated method stub
		GWT.log("getSuperclassValue - do nothing");
	}


	private void getPropertyValues(EntityData subject) {
		GWT.log("getPropertyValues for " + subject + "properties: " + getAllProperties());
		ICDServiceManager.getInstance().getPreCoordinationClassExpressions(
				project.getProjectName(), subject.getName(), getAllProperties(),
				new AsyncCallback<List<PrecoordinationClassExpressionData>>() {
					
					@Override
					public void onSuccess(List<PrecoordinationClassExpressionData> res) {
						updateWidgetContents(res);
					}
					
					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Failed getPreCoordinationClassExpressions");
						
					}
				});
	}

	protected PropertyWidget createWidgetForProperty(String propertyName, boolean isDefinitional) {
		//TODO check this
		//return null for now, as PreCoordinationWidget cannot create widgets on demand (all possible widgets are created at setup)
		//to be overridden in subclasses
		return null;
	}
	
	protected void removeWidgetForProperty(String propertyName) {
		//TODO check this
		//do nothing for now, as PreCoordinationWidget shouldn't remove widgets on demand (all possible widgets are created at setup and should be shown)
		//to be overridden in subclasses
	}

	public void afterWidgetVisibilityChanged(PropertyWidget widget, boolean isVisible) {
		//TODO check this
		//do nothing for now, as PreCoordinationWidget shouldn't add/remove widgets on demand (all possible widgets are created at setup and should be always shown)
		//to be overridden in subclasses
	}
	
	private void updateWidgetContents(
			List<PrecoordinationClassExpressionData> res) {
		GWT.log("updateWidgetContents " + res);
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
	

	private void getPossiblePropertyValues(EntityData subject) {
		GWT.log("getPossiblePropertyValues" + subject);
		ICDServiceManager.getInstance().getAllowedPostCoordinationValues(
				project.getProjectName(), subject.getName(), 
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

}
