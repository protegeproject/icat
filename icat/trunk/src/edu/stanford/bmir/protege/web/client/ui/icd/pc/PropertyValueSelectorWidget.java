package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Map;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;

public class PropertyValueSelectorWidget extends TreeValueSelector {
	
	private PostCoordinationAxesForm postCoordinationAxesFormPanel;
	private boolean isPartOfLogicalDefinition;

	public PropertyValueSelectorWidget(Project project, boolean isLogicalDefinitionProperty) {
		super(project);
		this.isPartOfLogicalDefinition = isLogicalDefinitionProperty;
	}

	public void setContainerFormPanel(PostCoordinationAxesForm postCoordinationAxesFormPanel) {
		this.postCoordinationAxesFormPanel = postCoordinationAxesFormPanel;
//		this.createValueSelector();
	}
	
	@Override
	protected boolean showIsDefinitionalCheckbox(Map<String, Object> config) {
		return false;
	}
	
	@Override
	protected boolean showSwitchBetweenLogicalAndNecessary(Map<String, Object> config) {
		return true;
	}

//	@Override
//	public Component getComponent() {
//		return super.getValueSelectorComponent();
//	}
	
	@Override
	public void onSelectionChanged(EntityData oldValue, EntityData newValue) {
		if ( newValue == null ) {	//delete value
			super.onSelectionChanged(oldValue, newValue);
			afterDeletePropertyValue(getProperty(), newValue);
		}
		else {
			super.onSelectionChanged(oldValue, newValue);
			if ( isPartOfLogicalDefinition ) {
				//set the isDefinitional flag to true
				//postCoordinationAxesFormPanel.getWidgetController().updateIsDefinitionalOfPropertyWidget(getProperty().getName(), this, true);
				changeDefinitionalStatus(true);
			}
		}
	}
	
	@Override
	protected void afterDeletePropertyValue(PropertyEntityData property, EntityData value) {
		postCoordinationAxesFormPanel.removeFieldForAxis(getProperty().getName());
	}
	
	@Override
	protected void afterDefinitionalStatusChanged( boolean newValue ) {
		LogicalDefinitionWidgetController widgetController = postCoordinationAxesFormPanel.getWidgetController();
		widgetController.updateIsDefinitionalOfPropertyWidget(getProperty().getName(), this, newValue);

	}
	
	public void onSelectValue() {
		valueSelWidget.onSelectEntity();
	}
}
