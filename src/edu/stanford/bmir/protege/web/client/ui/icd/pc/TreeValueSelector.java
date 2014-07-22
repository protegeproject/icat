package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;

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
		// TODO Auto-generated method stub
		//super.fillValues();
	}

	@Override
	public void setValues(Collection<EntityData> values) {
		beforeSetValues(values);
		// TODO Auto-generated method stub
		System.out.println("Set values for tree: " + getProperty() + " " + values);
		valueSelWidget.setValues(values);
	}

	@Override
	public String getSelectedValue() {
		// TODO Auto-generated method stub
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

}
