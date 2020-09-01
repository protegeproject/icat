package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.Component;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.ClassSelectionFieldWidget;

public class TreeValueSelector extends AbstractScaleValueSelectorWidget implements ValueSelectorComponent {

	private ClassSelectionFieldWidget valueSelWidget;

	public TreeValueSelector(Project project) {
		super(project);
	}

	@Override
	public void fillValues() {
		//don't take the values from the server; values will be set by the Precoord..WidgetController with setValues
		//super.fillValues();
	}

	@Override
	public void setValues(Collection<EntityData> values) {
		beforeSetValues(values);
		GWT.log("Set values for tree: " + getProperty() + " " + values);
		valueSelWidget.setValues(values);
	}

	@Override
	public String getSelectedValue() {
		return valueSelWidget.getField().getValueAsString();
	}
	
	@Override
	protected void createValueSelector() {
		if (valueSelWidget == null) {
			valueSelWidget = new ClassSelectionFieldWidget(getProject()) {
				@Override
				protected void onChangeValue(EntityData subj, Object oldVal,
						Object newVal) {
					// TODO check this
					//super.onChangeValue(subj, oldVal, newVal);
					onSelectionChanged((EntityData)oldVal, (EntityData)newVal);
				}
				
				@Override
				protected void deletePropertyValue(EntityData subject,
						String propName, ValueType propValueType,
						EntityData oldEntityData, Object oldDisplayedValue,
						String operationDescription) {
					// TODO check this solution
					//super.deletePropertyValue(subject, propName, propValueType, oldEntityData,
					//		oldDisplayedValue, operationDescription);
					onSelectionChanged(oldEntityData, null);
				}
			};
			valueSelWidget.setup(getWidgetConfiguration(), getProperty());
		}
	}
	
	@Override
	protected Component getValueSelectorComponent() {
		return valueSelWidget.getComponent();
	}

	@Override
	public void setComponentSubject(EntityData subject) {
		valueSelWidget.setSubject(subject);
	}
	
	@Override
	public void onSelectionChanged(EntityData oldValue, EntityData newValue) {
		setFieldValue(newValue);
		super.onSelectionChanged(oldValue, newValue);
	}

	@Override
	protected void setFieldValue(EntityData value) {
		valueSelWidget.getField().setValue(value == null ? "" : value.getBrowserText());
	}

	@Override
	protected void setAllowedValues(List<EntityData> allowedValues) {
		System.out.println("TreeValueSelector.setAllowedValues: " + allowedValues);
		if (allowedValues == null || allowedValues.size() == 0 || allowedValues.size() > 1) { //we can treat last case separately if valueSelWidget will take multiple root classes
			valueSelWidget.resetTopClass();
		}
		else {
			valueSelWidget.setTopClass(allowedValues.get(0).getName());
		}
	}

}
