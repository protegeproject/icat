package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.Template;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;
import edu.stanford.bmir.protegex.bp.ref.WidgetConfig;

public class PropertySelectorWidget extends AbstractPropertyWidget {
    private FormPanel wrappingPanel;

	private LinkedHashMap<String, String> labelToPropertyMap;
	private HashMap<String, Map<String, Object>> propertyToConfigMap;
	private HashMap<String, Integer> propertyToOptionOrder;
	private List<String> allProperties;
	private List<String> validProperties;
	private Store cbStore;
	private ComboBox cb;
	private PostCoordinationAxesForm form;

	private static final String LABEL_FIELD = "label";
	private static final String PROPERTY_FIELD = "property";
	private static final String FONT_FIELD = "font";
	private static final String COLOR_FIELD = "textcolor";

	private static final String[] COMBOBOX_RECORD_FIELDS = new String[] { LABEL_FIELD, PROPERTY_FIELD, FONT_FIELD, COLOR_FIELD };

	public PropertySelectorWidget(Project project, PostCoordinationAxesForm form) {
		super(project);
		this.form = form;
	}

	public void setup(Map<String, Object> widgetConfiguration) {
		this.setWidgetConfiguration(widgetConfiguration);
		System.out.println("PropertySelectorWidget constructor");

		int propEntryCnt = widgetConfiguration.size();
		labelToPropertyMap = new LinkedHashMap<>(propEntryCnt);
		propertyToConfigMap = new HashMap<>(propEntryCnt); // String, Map<String, Object>
		propertyToOptionOrder = new HashMap<>(propEntryCnt);
		allProperties = new ArrayList<>(propEntryCnt);
		validProperties = new ArrayList<>(propEntryCnt);

		for (Iterator<String> it = widgetConfiguration.keySet().iterator(); it.hasNext();) {
			String property = it.next();
			Map<String, Object> propConfig = (Map<String, Object>) widgetConfiguration.get(property);
			boolean isHidden = UIUtil.getBooleanConfigurationProperty(propConfig, FormConstants.HIDDEN, false);
			if ( ! isHidden ) {
				propertyToConfigMap.put(property, propConfig);
				String label = (String) propConfig.get(FormConstants.LABEL);
				labelToPropertyMap.put(label, property);
				allProperties.add(property);
				validProperties.add(property);
			}
		}

		createComponent();
	}

//	public List<String> getListOfPropertyLabels() {
//		return new ArrayList<String>(labelToPropertyMap.keySet());
//
//	}

	/**
	 * TODO remove this method if we don't need it. Use {@link PostCoordinationAxesForm.updateListOfValidProperties} instead
	 * @param types
	 */
	public void updateListOfValidPropertiesBasedOnTypes( Collection<EntityData> types, LogicalDefinitionWidgetController widgetController) {
		//TODO delete this method
//		validProperties = new ArrayList<String>();
//		for (String property : allProperties) {
//			Map<String, Object> config = propertyToConfigMap.get(property);
//	    	List<String> showOnlyForTypes = UIUtil.getListConfigurationProperty(config, FormConstants.SHOW_ONLY_FOR_TYPES, null);
//	    	List<String> doNotShowForTypes = UIUtil.getListConfigurationProperty(config, FormConstants.DO_NOT_SHOW_FOR_TYPES, null);
//
//		}
		
		
//		List<String> oldListOfValidProperties = new ArrayList<String>( propertySelector.getListOfValidProperties() );
		List<String> oldListOfValidProperties = validProperties;
		List<String> newListOfValidProperties = new ArrayList<String>();
		
//    	List<String> allProperties = propertySelector.getListOfAllProperties();
    	for (String property : allProperties) {
    		Map<String, Object> propConfig = propertyToConfigMap.get(property);
    		
    		//TODO check whether we need this
    		boolean isHidden = UIUtil.getBooleanConfigurationProperty(propConfig, FormConstants.HIDDEN, false);
    		if ( ! isHidden ) {
	        	List<String> showOnlyForTypesList = UIUtil.getListConfigurationProperty(propConfig, FormConstants.SHOW_ONLY_FOR_TYPES, null);
	        	List<String> doNotShowForTypesList = UIUtil.getListConfigurationProperty(propConfig, FormConstants.DO_NOT_SHOW_FOR_TYPES, null);
	
	        	boolean isWidgetVisibleForThisTypeOfEntity = UIUtil.calculateVisibilityBasedOnSubjectType(types, showOnlyForTypesList, doNotShowForTypesList);
	        	if (isWidgetVisibleForThisTypeOfEntity) {
	        		newListOfValidProperties.add(property);
	        		
	        	}
    		}
		}
//    	propertySelector.setListOfValidProperties( newListOfValidProperties );
    	setListOfValidProperties( newListOfValidProperties );
    	
//    	//show/hide widgets
    	ArrayList<String> toShow = new ArrayList<>(newListOfValidProperties);
    	toShow.removeAll(oldListOfValidProperties);
//    	for (String property : toShow) {
//        	widgetController.showWidgetForProperty(property);
//		}
    	oldListOfValidProperties.removeAll(newListOfValidProperties);
//    	for (String property : oldListOfValidProperties) {
//        	widgetController.hideWidgetForProperty(property);
//		}
    	//TODO update visibility for widgets affected
    	widgetController.showHideWidgetsDueToTypeChange(form, toShow, oldListOfValidProperties);
	}
	
	public List<String> getListOfAllProperties() {
		return allProperties;
	}
	
	public void setListOfValidProperties(List<String> validProperties) {
		boolean validPropertiesChanged = UIUtil.differentCollections(this.validProperties, validProperties);
		
		this.validProperties = validProperties;
		
		if ( validPropertiesChanged ) {
			GWT.log("Valid properties changed!");
			resetCbStore();
		}
	}

	public List<String> getListOfValidProperties() {
//		if (propertyToOptionOrder == null ) {
//			return new ArrayList<String>();
//		}
//		else {
//			return new ArrayList<String>(propertyToOptionOrder.keySet());
//		}
		//return a copy of the validProperties list
		return new ArrayList<>( validProperties );
	}

//	commented out because we reversed the order of key-value pairs
//	public LinkedHashMap<String, String> getPropertyToLabelMap() {
//		return labelToPropertyMap;
//	}

	public Map<String, Map<String, Object>> getPropertyToConfigMap() {
		return propertyToConfigMap;
	}

	@Override
	public Component createComponent() {
		wrappingPanel = new FormPanel();
		wrappingPanel.setLabelWidth(150);
		wrappingPanel.setWidth("100%");
		wrappingPanel.setPaddings(5);

		cb = new ComboBox();
		createCbStore();
		cb.setTpl(new Template("<div class=\"x-combo-list-item\" style = \" "
				+ " font-weight: {" + FONT_FIELD + "};"
				+ " color: {" + COLOR_FIELD + "};"
						+ "\">" 
				+ "{" + LABEL_FIELD + "}" + "<div class=\"x-clear\"></div></div>"));
		cb.setStore(cbStore);
		cb.setLabel("Select an additional axis");
		cb.setLabelSeparator(":");
		cb.setEmptyText("Choose a property value");
		cb.setDisplayField(LABEL_FIELD);
		cb.setEditable(false);
		//cb.setWidth("80%");
		cb.setWidth("400px");
		cb.setMaxLength(200);
		cb.addListener(new PropertySelectorComboBoxListener());
		
		wrappingPanel.add(cb);
		return wrappingPanel;
	}

	protected void createCbStore() {
		GWT.log("createCbStore");
		String[][] values = createDefaultCbStoreValues();
		cbStore = new SimpleStore(COMBOBOX_RECORD_FIELDS, values);
	}

	protected void resetCbStore() {
		GWT.log("resetCbStore");
		String[][] values = createDefaultCbStoreValues();
		//cbStore.removeAll();
		cbStore.setDataProxy( new MemoryProxy( values ) );
		cbStore.load();
		cb.setEmptyText("Choose a property value");
		cb.clearValue();
	}

	private String[][] createDefaultCbStoreValues() {
		String[][] values = new String [validProperties.size()] [COMBOBOX_RECORD_FIELDS.length];
		int i = 0;
		for (String key : labelToPropertyMap.keySet()) {
			String property = labelToPropertyMap.get(key);
			if ( validProperties.contains(property) ) { 
				values[i][0] = key;
				values[i][1] = property;
	//			values[i][2] = "true";
				values[i][2] = "normal";
				values[i][3] = "black";
				propertyToOptionOrder.put(property, i);
				i++;
			}
		}
		System.out.println("cb values: " + values);
		return values;
	}

	@Override
	public Component getComponent() {
		return wrappingPanel;
	}

	@Override
	public void setValues(Collection<EntityData> values) {
		// TODO Auto-generated method stub
	}

	private class PropertySelectorComboBoxListener extends ComboBoxListenerAdapter {
		Record lastSelection = null;

//		@Override
//		public void onChange(Field field, Object newVal, Object oldVal) {
//			if (oldVal != null) {
//				form.removeFieldForAxis(labelToPropertyMap.get(oldVal));
//			}
//			if (newVal != null) {
//				form.addFieldForAxis(labelToPropertyMap.get(newVal));
//			}
//		}

		@Override
		public void onSelect(ComboBox comboBox, Record record, int index) {
			super.onSelect(comboBox, record, index);
			if (record != lastSelection && record != null) {
//				if (lastSelection != null) {
//					String oldVal = lastSelection.getAsString(LABEL_FIELD);
//					if (oldVal != null) {
//						String property = labelToPropertyMap.get(oldVal);
//						form.removeFieldForAxis(property);
//						setActiveStatusForOption(property, true);
//					}
//				}

				String newVal = record.getAsString(LABEL_FIELD);
				if (newVal != null) {
					String property = labelToPropertyMap.get(newVal);
					form.addFieldForAxis(property, newVal);
					//TODO delete this, if we keep the call in form.addFieldForAxis
					//setActiveStatusForOption(property, false);
					lastSelection = record;
				}

				//this is good but was moved to form.addFieldForAxis...
				//form.doLayout(true);
			}
		}
	}

	public void setActiveStatusForOption( String property, boolean active ) {
		if (cbStore == null) {
			GWT.log("Store for combobox is not initialized yet");
			return;
		}
		
		Integer index = propertyToOptionOrder.get(property);
		if ( index == null ) {
			return;
		}
		Record record = cbStore.getAt(index);
		if (active) {
			record.set(COLOR_FIELD, "black");
		}
		else {
			record.set(COLOR_FIELD, "lightgray");
		}
		record.commit();
	}

	public void setRecommendedProperties(List<String> propertyList) {
		updateCbStore(propertyList);
	}

	private void updateCbStore(List<String> recommendedProperties) {
		//order list of properties by putting recomended properties first
		//Collection<String> allProperties = labelToPropertyMap.values();
		int indexLastRecommended = -1;
		ArrayList<String> orderedPropertyList = new ArrayList<String>( validProperties.size() );
		for (String propName : validProperties) {
			if ( recommendedProperties.contains(propName) ) {
				orderedPropertyList.add( ++indexLastRecommended, propName );
			}
			else {
				orderedPropertyList.add( propName );
			}
		}
		GWT.log("Ordered list of property:" + orderedPropertyList);
		
		String[][] values = new String [validProperties.size()] [COMBOBOX_RECORD_FIELDS.length];
		Record[] currRecords = cbStore.getRecords();
		if ( currRecords == null || currRecords.length == 0 ) {
			resetCbStore();
			currRecords = cbStore.getRecords();
		}
		//GWT.log("Records: " + currRecords);
//		for (int i=0; i<currRecords.length; i++) {
//			Record rec = currRecords[i];
//			String propName = rec.getAsString(PROPERTY_FIELD);
//		}
		
		HashMap<String, Integer> oldPropertyToOptionOrder = propertyToOptionOrder;
		propertyToOptionOrder = new HashMap<>();
		int i = 0;
		for (String property : orderedPropertyList) {
			Integer oldIndex = oldPropertyToOptionOrder.get(property);
			//GWT.log("Old index: " + oldIndex);
			Record record = (oldIndex == null ? null : currRecords[oldIndex]);
			//GWT.log("Property: " + property + " Record: ");
			values[i][0] = ( record == null ? getLabelForProperty(property) : record.getAsString(COMBOBOX_RECORD_FIELDS[0]) );
			values[i][1] = property;
//			values[i][2] = "true";
			values[i][2] = (i <= indexLastRecommended ? "bold" : "normal" );
			values[i][3] = ( record == null ? "black" : record.getAsString(COMBOBOX_RECORD_FIELDS[3]) );
			propertyToOptionOrder.put(property, i);
			i++;
		}
		//GWT.log("cb values: " + values);
		cbStore.removeAll();
		cbStore.setDataProxy(new MemoryProxy(values));
		cbStore.load();

//		wrappingPanel.render(cb.getId());
	}

	public String getLabelForProperty(String property) {
		Map<String, Object> config = propertyToConfigMap.get(property);
		if ( config != null ) {
			return UIUtil.getStringConfigurationProperty(config, FormConstants.LABEL, "");
		}
		else {
			return "";
		}
	}

	public void clearProperties() {
		propertyToOptionOrder = new HashMap<>();
		//cbStore.removeAll();
		cbStore.setDataProxy(new MemoryProxy(new String[][] {}));
		cbStore.load();
		cb.setEmptyText("Select a superclass to be able to create logical definitions");
		cb.clearValue();
	}
}
