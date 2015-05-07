package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Map;

import com.gwtext.client.widgets.Component;

import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;

public interface ValueSelectorComponent {
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData);
	public Component getComponent();
	public String getSelectedValue();
	public boolean isDefinitionalChecked();
	public void setIsDefinitional(boolean isDefinitional);
}
