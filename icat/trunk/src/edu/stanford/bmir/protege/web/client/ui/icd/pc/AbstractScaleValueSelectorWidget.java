package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.event.CheckboxListenerAdapter;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public abstract class AbstractScaleValueSelectorWidget extends AbstractPropertyWidget {

	private static final String SWITCH_ICON_STYLE_STRING = "style=\"position:relative; top:6px;\"";
	public static enum SwitchButtonType {LOGICAL_TO_NECESSARY, NECESSARY_TO_LOGICAL};
	
	private Panel wrappingPanel;
	private Checkbox checkboxDefinitional;
	
	private boolean isDefinitional = false;
	
	private SwitchButtonType switchButtonType;
	
	public AbstractScaleValueSelectorWidget(Project project) {
		super(project);
	}

	protected void setSwitchBetweenLogicalAndNecessaryButtonType(SwitchButtonType switchButtonType ) {
		this.switchButtonType = switchButtonType;
	}

	@Override
	public void setSubject(EntityData subject) {
		super.setSubject(subject);
		setComponentSubject(subject);
	}
	
	@Override
	public Component getComponent() {
    	GWT.log("called AbstractScaleValueSelectorWidget.getComponent() on: " + this);
		if (wrappingPanel == null) {
			createComponent();
			setComponentSubject(getSubject());
		}
		return wrappingPanel;
	}

	@Override
	public Component createComponent() {
		wrappingPanel = new Panel();
		Map<String, Object> config = getWidgetConfiguration();
		if (config != null) {
			createValueSelector();
			Component valueSelector = getValueSelectorComponent();
			if (valueSelector != null) {
				wrappingPanel.add(valueSelector);

				if ( showSwitchBetweenLogicalAndNecessary(config) ) {
					addLogicalNecessarySwitchButton(valueSelector);
				}
				
				if ( showIsDefinitionalCheckbox(config) ) {
					createIsDefinitionalCheckbox();
					wrappingPanel.add(checkboxDefinitional);
				}
				
			}
		}
		return wrappingPanel;
	}

	protected boolean showIsDefinitionalCheckbox(Map<String, Object> config) {
		return UIUtil.getBooleanConfigurationProperty(config, FormConstants.SHOW_IS_DEFINED, true);
	}
	
	protected boolean showSwitchBetweenLogicalAndNecessary(Map<String, Object> config) {
		return UIUtil.getBooleanConfigurationProperty(config, FormConstants.SHOW_LOGICAL_NECESSARY_SWITCH, false);
	}
	
	public boolean isDefinitional() {
		return isDefinitional;
	}

	public void setIsDefinitional(boolean isDefinitional){
		this.isDefinitional = isDefinitional;
		setIsDefinitionalChecked(isDefinitional);
	}

	public boolean isDefinitionalChecked() {
		return checkboxDefinitional != null && checkboxDefinitional.getValue();
	}

	public void setIsDefinitionalChecked(boolean checked){
		if (checkboxDefinitional != null && checkboxDefinitional.getValue() != checked) {
			checkboxDefinitional.setValue(checked);
		}
	}

	protected abstract void createValueSelector();

	protected abstract Component getValueSelectorComponent();
	
	protected void addLogicalNecessarySwitchButton(Component valueSelectorComponent) {
		Component[] items = wrappingPanel.getItems();
		if ( items != null) {
			for ( int i=0; i < items.length; i++ ) {
				if (items[i].equals(valueSelectorComponent)) {
					Panel valueSelectorWrapper = new Panel();
					valueSelectorWrapper.setLayout(new ColumnLayout());
					valueSelectorWrapper.add(valueSelectorComponent, new ColumnLayoutData(1.0));
					valueSelectorWrapper.add(createLogicalNecessarySwitchButton());
//					wrappingPanel.remove(valueSelectorComponent);
//					wrappingPanel.insert(i, valueSelectorWrapper);
					wrappingPanel = valueSelectorWrapper;
					
					break;
				}
			}
		}
	}

	protected Anchor createLogicalNecessarySwitchButton() {
//        final Map<String, Object> widgetConfiguration = getWidgetConfiguration();
//        final ProjectConfiguration projectConfiguration = getProject().getProjectConfiguration();
//        Anchor addNewLink = new Anchor(
//                InstanceGridWidgetConstants.getIconLink(
//                        "",
//                        InstanceGridWidgetConstants.getAddIcon(widgetConfiguration, projectConfiguration)), true);
////        addNewLink.setText("move to necessary");
//        addNewLink.addClickHandler(new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                if (isWriteOperationAllowed()) {
//                    //TODO add action
//                	//onCreateNewReference();
//                	GWT.log("Pressed on Logical/Necessary switch");
//                }
//            }
//        });
//        
//        return addNewLink;
		
		String iconName = (switchButtonType == SwitchButtonType.LOGICAL_TO_NECESSARY ? "down" : "up");
		String tooltipText = (switchButtonType == SwitchButtonType.LOGICAL_TO_NECESSARY ? 
				"Move to necessary condition" : "Move to logical condition");
    	boolean enabled = ( !isReadOnly() && !isDisabled());
        Anchor addLink = ( enabled ?
        		new Anchor("&nbsp<img src=\"images/" + iconName + ".png\" " + SWITCH_ICON_STYLE_STRING + "></img>", true) :
        		new Anchor("&nbsp<img src=\"images/" + iconName + "_grey.png\" " + SWITCH_ICON_STYLE_STRING + "></img>", true) );
        addLink.setWidth("22px");
        addLink.setHeight("22px");
        addLink.setTitle(enabled ? tooltipText : tooltipText + " is not allowed");
        addLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
            	if (!isReadOnly() && !isDisabled() && isWriteOperationAllowed()) {
            		if ( switchButtonType == SwitchButtonType.LOGICAL_TO_NECESSARY ) {
            			//setIsDefinitional(false);
            			changeDefinitionalStatus(false);
            		}
            		else {
            			setIsDefinitional(true);
            			changeDefinitionalStatus(true);
            		}
            	}
            }
        });
        return addLink;
    }

	public abstract void setComponentSubject(EntityData subject);
	
	protected abstract void setFieldValue(EntityData value);

	protected abstract void setAllowedValues(List<EntityData> allowedValues);
	
	protected SwitchButtonType getLogicalNecessarySwitchButtonType() {
		return SwitchButtonType.LOGICAL_TO_NECESSARY;
	}
	
	public void onSelectionChanged(final EntityData oldValue, final EntityData newValue) {
		GWT.log("abs. sc. val. sel changed: " + getSubject() + "." + getProperty() + "  " + oldValue + " -> " + newValue);
		//TODO
		ICDServiceManager.getInstance().setPrecoordinationPropertyValue(
				getProject().getProjectName(), getSubject().getName(), getProperty().getName(), 
				oldValue, newValue, GlobalSettings.getGlobalSettings().getUserName(), 
				getEditOperationDescription(getSubject(), getProperty(), oldValue, newValue),
				
				new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("failed to change value");
						//TODO reset value to old value
						updateFieldValue(oldValue);
					}

					@Override
					public void onSuccess(Boolean success) {
						updateFieldValue(newValue);

						if (success) {
							GWT.log("value for property " + getProperty() + " was changed successfully");
						}
						else {
							GWT.log("Could not find property " + getProperty() + " in class expression. " +
									"Value was not changed succesfully");
							//TODO reset value to old value
						}
					}

					private void updateFieldValue(final EntityData value) {
						setFieldValue(value);
						if (checkboxDefinitional != null) {
							if (value != null) { 
								checkboxDefinitional.enable();
							}
							else {
								checkboxDefinitional.disable();
							}
						}
					}
				});
	}
	
	
	private String getEditOperationDescription(EntityData subject, PropertyEntityData prop, 
			EntityData oldValue, EntityData newValue) {
		
		String oldValueText = oldValue == null || oldValue.getName() == null ? 
				"(empty)" : "'" + UIUtil.getDisplayText(oldValue) + "'";
		String newValueText = newValue == null || newValue.getName() == null ? 
				"(empty)" : "'" + UIUtil.getDisplayText(newValue) + "'";
		
		String text = "Edited logical definition for class '" + UIUtil.getDisplayText(subject) + "'" +
				". Changed property '" + UIUtil.getDisplayText(prop) + "'" + 
				". Old value: " +  oldValueText +
				". New value: " + newValueText ;
		return UIUtil.getAppliedToTransactionString(text, subject.getName());
	}
	
	
	private void createIsDefinitionalCheckbox() {
		if (checkboxDefinitional == null) {
			checkboxDefinitional = new Checkbox("Is definitional");
			checkboxDefinitional.setStyle("margin: 0px 0px 22px 110px;");
			checkboxDefinitional.addListener(new IsDefinitionalCheckboxListener());
		}
	}
	
	public class IsDefinitionalCheckboxListener extends CheckboxListenerAdapter {

		@Override
		public void onCheck(Checkbox field, final boolean checked) {
			GWT.log("on check: " + checked);
			if (isDefinitional == checked) {
				GWT.log("not a user change. do nothing");
				return;
			}
			if ( ! isWriteOperationAllowed() ) {
				field.setChecked( ! checked );
				return;
			}
			changeDefinitionalStatus(checked);
		}

	}


	protected void changeDefinitionalStatus(final boolean checked) {
		//we update the UI first, assuming that the "change 'is definitional' flag" action will be successful.
		afterDefinitionalStatusChanged(checked);
		
		ICDServiceManager.getInstance().changeIsDefinitionalFlag(
				getProject().getProjectName(), getSubject().getName(), getProperty().getName(), 
				checked, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("failed to change isDefinitional flag");
						
						if ( checkboxDefinitional != null ) {
							checkboxDefinitional.setValue(!checked);
						}
						//TODO: Undo what afterDefinitionalStatusChanged() did
					}

					@Override
					public void onSuccess(Boolean success) {
						isDefinitional = checked;
						
						if (success) {
							GWT.log("isDefinitional flag was changed successfully");
//							afterDefinitionalStatusChanged();
						}
						else {
							GWT.log("Could not find property " + getProperty() + " in class expression. " +
									"Definitional flag was not changed succesfully");
						}
					}
				});
	}

	protected void afterDefinitionalStatusChanged( boolean newValue ) {
		// Do nothing here. Override it in PropertyValueSelectorWidget.
	}

	protected void beforeSetValues(Collection<EntityData> values) {
		if ( checkboxDefinitional != null ) {
			if (values == null || values.isEmpty()) { 
				checkboxDefinitional.disable();
			}
			else {
				checkboxDefinitional.enable();
			}
		}
	}

}
