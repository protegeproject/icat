package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.List;
import java.util.Map;

import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.TextField;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.UIUtil;
import edu.stanford.bmir.protege.web.client.util.Project;

public class ComboBoxWidget extends AbstractFieldWidget {

	private ComboBox comboBox;	
	private Store store;
	private RecordDef recordDef;
	
	public ComboBoxWidget(Project project) {
		super(project);	
	}
	
	
	@Override
	protected TextField createFieldComponent() {	
		comboBox = new ComboBox();
		comboBox.setForceSelection(true);
		comboBox.setMinChars(1);		
		comboBox.setMode(ComboBox.LOCAL);
		comboBox.setTriggerAction(ComboBox.ALL);
		comboBox.setEmptyText("Enter value");
		comboBox.setLoadingText("Searching...");
		comboBox.setTypeAhead(true);
		comboBox.setSelectOnFocus(true);		       	
		comboBox.setHideTrigger(false);
		
		recordDef = new RecordDef(
				new FieldDef[] {
					new StringFieldDef("entityData", "entityData"),
					new StringFieldDef("browserText", "browserText"),				
				}
			);
		
		ArrayReader reader = new ArrayReader(recordDef);		
		MemoryProxy dataProxy = new MemoryProxy(new String[][]{});		
		store = new Store(dataProxy, reader);		
        store.load();		
        
		comboBox.setValueField("entityData");			
		//comboBox.setDisplayField("browserText");
		comboBox.setStore(store);
				
		return comboBox;
	}
	
	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {	
		super.setup(widgetConfiguration, propertyEntityData);
		configureComboBox(propertyEntityData);
	}
	
	
	private void configureComboBox(PropertyEntityData propertyEntityData) {
		List<EntityData> allowedVals = propertyEntityData.getAllowedValues();
		if (allowedVals == null) { return; }
		
		store.removeAll();
		
		//Object[][] data = new Object[allowedVals.size()][2];
		//int i = 0;
		for (EntityData allowedVal : allowedVals) {
			store.add(recordDef.createRecord(new Object[]{allowedVal.getName(), UIUtil.getDisplayText(allowedVal)}));
			//data[i][0] = allowedVal.getName();
			//data[i][1] = UIUtil.getDisplayText(allowedVal);
		}
		//store.setDataProxy(new MemoryProxy(data));
		//store.reload();
	}
	
	@Override
	public void setProperty(PropertyEntityData property) {	
		super.setProperty(property);
		configureComboBox(property);
	}
	
	
}
