package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.event.CheckboxListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public abstract class AbstractScaleValueSelectorWidget extends AbstractPropertyWidget {

	private Panel wrappingPanel;
	private Checkbox checkboxDefinitional;
	
	private boolean isDefinitional = false;
	
	
	public AbstractScaleValueSelectorWidget(Project project) {
		super(project);
	}

	@Override
	public void setSubject(EntityData subject) {
		super.setSubject(subject);
		setComponentSubject(subject);
	}
	
	@Override
	public Component getComponent() {
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

				boolean showIsDefined = UIUtil.getBooleanConfigurationProperty(config, FormConstants.SHOW_IS_DEFINED, true);
				if (showIsDefined) {
					createIsDefinitionalCheckbox();
					wrappingPanel.add(checkboxDefinitional);
				}
				
			}
		}
		return wrappingPanel;
	}

	public boolean isDefinitional() {
		return isDefinitional;
	}

	public void setIsDefinitional(boolean isDefinitional){
		this.isDefinitional = isDefinitional;
		setIsDefinitionalChecked(isDefinitional);
	}

	public boolean isDefinitionalChecked() {
		return checkboxDefinitional.getValue();
	}

	public void setIsDefinitionalChecked(boolean checked){
		if (checkboxDefinitional.getValue() != checked) {
			checkboxDefinitional.setValue(checked);
		}
	}

	protected abstract void createValueSelector();

	protected abstract Component getValueSelectorComponent();

	public abstract void setComponentSubject(EntityData subject);
	
	protected abstract void setFieldValue(EntityData value);

	protected abstract void setAllowedValues(List<EntityData> allowedValues);
	
	
	public void onSelectionChanged(final EntityData oldValue, final EntityData newValue) {
		GWT.log("abs. sc. val. sel changed");
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
						}
					}

					private void updateFieldValue(final EntityData value) {
						setFieldValue(value);
						if (value != null) { 
							checkboxDefinitional.enable();
						}
						else {
							checkboxDefinitional.disable();
						}
					}
				});
	}
	
	
	private String getEditOperationDescription(EntityData subject, PropertyEntityData prop, 
			EntityData oldValue, EntityData newValue) {
		
		String oldValueText = oldValue == null || oldValue.getName() == null ? 
				"(empty)" : UIUtil.getDisplayText(oldValue);
		String newValueText = newValue == null || newValue.getName() == null ? 
				"(empty)" : UIUtil.getDisplayText(newValue);
		
		String text = "Edited logical definition for class '" + UIUtil.getDisplayText(subject) + "'" +
				". Changed property '" + UIUtil.getDisplayText(prop) + "'" + 
				". Old value: '" +  oldValueText + "'" +
				", new value: '" + newValueText + "'";
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
			ICDServiceManager.getInstance().changeIsDefinitionalFlag(
					getProject().getProjectName(), getSubject().getName(), getProperty().getName(), 
					checked, new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable arg0) {
							GWT.log("failed to change isDefinitional flag");
							
							checkboxDefinitional.setValue(!checked);
						}

						@Override
						public void onSuccess(Boolean success) {
							isDefinitional = checked;
							
							if (success) {
								GWT.log("isDefinitional flag was changed successfully");
							}
							else {
								GWT.log("Could not find property " + getProperty() + " in class expression. " +
										"Definitional flag was not changed succesfully");
							}
						}
					});
		}

	}
	
	protected void beforeSetValues(Collection<EntityData> values) {
		if (values == null || values.isEmpty()) { 
			checkboxDefinitional.disable();
		}
		else {
			checkboxDefinitional.enable();
		}
	}

}
