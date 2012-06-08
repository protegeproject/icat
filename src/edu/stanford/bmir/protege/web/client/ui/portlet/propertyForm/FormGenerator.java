package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FieldSet;
import com.gwtext.client.widgets.layout.FormLayout;
import com.gwtext.client.widgets.portal.Portlet;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * This class generates a form with fields based on the portlet configuration
 * from the server. The Form Generator is able to generate. A field is a 
 * {@link PropertyWidget}.
 * 
 * The Form Generator also has methods for filling in the field values of the generated form.
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class FormGenerator {
	/*
	 * For now, we'll assume that the form configuration map
	 * has a valid configuration stored 
	 */

	//TODO: add a type to the configuration

	private Project project;
	private Map<String, Object> formConf;
	private Map<String, List<PropertyWidget>> prop2widget = new HashMap<String, List<PropertyWidget>>();
	private Portlet portletId;//TODO this should be changed to the String element ID, but we could not retrieve the Portlet based on the ID

	public FormGenerator(Project project, Map<String, Object> formConfiguration) {
		this.project = project;
		this.formConf = formConfiguration;
	}	

	public void setPortletId(Portlet id) {
		portletId = id;
	}
	
	public Panel getFormPanel() {
		Panel panel = null;
		Object tabs = formConf.get(FormConstants.TABS);
		if (tabs != null && (tabs instanceof Collection) && ((Collection)tabs).size() > 0) {	// multiple tabs
			panel = new TabPanel();
			panel.setPaddings(8);	
			panel.setBorder(false);

			for (Iterator iterator = ((Collection)tabs).iterator(); iterator.hasNext();) {
				Object tabObj = (Object) iterator.next();
				if (tabObj instanceof Map) {
					Map tabMap = (Map) tabObj;
					Panel tab = createInnerPanel(tabMap);
					panel.add(tab);
				}
			}
		} else { //no tabs
			panel = createInnerPanel(formConf);
		}
		return panel;
	}

	/**
	 * This is necessary only to circumvent a problem related to adding/removing
	 * components problem that involves a tab panel
	 * @return - the tab panel - same as input
	 */
	public Panel addFormToTabPanel(TabPanel panel) {		
		Object tabs = formConf.get(FormConstants.TABS);
		if (tabs != null && (tabs instanceof Collection) && ((Collection)tabs).size() > 0) {	// multiple tabs			
			for (Iterator iterator = ((Collection)tabs).iterator(); iterator.hasNext();) {
				Object tabObj = (Object) iterator.next();
				if (tabObj instanceof Map) {
					Map tabMap = (Map) tabObj;
					Panel tab = createInnerPanel(tabMap);					
					panel.add(tab);
				}
			}
		} else { //one tab
			Panel createInnerPanel = createInnerPanel(formConf);
			if (createInnerPanel != null) {				
				panel.add(createInnerPanel);
			}
		}
		
		return panel;
	}


	public List<PropertyWidget> getComponents(String propertyName) {
		return prop2widget.get(propertyName);
	}


	protected Panel createInnerPanel(Map panelConf) {
		Panel panel = new Panel();
		panel.setLayout(new FormLayout()); //TODO
		panel.setPaddings(5);	
		panel.setBorder(false);
		panel.setAutoScroll(true);

		String title = (String) panelConf.get(FormConstants.TITLE);
		if (title != null) {
			panel.setTitle(title);
		}

		createInnerPanelComponents(panel, panelConf);
		return panel;
	}

	protected void configurePanel(Panel panel) {

	}

	protected void createInnerPanelComponents(Panel panel, Map panelConf) {		 
		Collection<String> properties = panelConf.keySet(); //TODO: this won't be sorted
		String[] sortedProps = new String[properties.size()];
		
		for (String prop : properties) {
			Object value = panelConf.get(prop);
			if (value instanceof Map) {
				String indexStr = (String) ((Map)value).get(FormConstants.INDEX);
				if (indexStr != null) {
					int index = Integer.parseInt(indexStr);
					sortedProps[index] = prop;
				}
			}
		}
				
		for (int i =0 ; i < sortedProps.length; i ++) {			
			String prop = sortedProps[i];
			Object value = panelConf.get(prop);
			if (value instanceof Map) {
				String component_type = (String) ((Map)value).get(FormConstants.COMPONENT_TYPE);
				if (component_type != null) {
					PropertyWidget widget = null;
					if (component_type.equals(FormConstants.TEXTFIELD)) {				
						widget = createTextField((Map)value, prop);				
					} else if (component_type.equals(FormConstants.TEXTAREA)) {
						widget = createTextArea((Map)value, prop);
					} else if (component_type.equals(FormConstants.COMBOBOX)) {
						widget = createComboBox((Map)value, prop);
					} else if (component_type.equals(FormConstants.HTMLEDITOR)) {
						widget = createHtmlEditor((Map)value, prop);
					} else if (component_type.equals(FormConstants.FIELDSET)) {
						//widget = createFieldSet((Map)value);
					} else if (component_type.equals(FormConstants.MULTITEXTFIELD)) {
						widget = createMultiTextField((Map)value, prop);
					} else if (component_type.equals(FormConstants.GRID)) {
						widget = createGrid((Map)value, prop);
					} else if (component_type.equals(FormConstants.EXTERNALREFERENCE)) {
						widget = createExternalReference((Map)value, prop);
					} else if (component_type.equals(FormConstants.CLASS_SELECTION_FIELD)) {
						widget = createClassSelectionField((Map)value, prop);
					} else if (component_type.equals(FormConstants.PROPERTY_SELECTION_FIELD)) {
						widget = createPropertySelectionField((Map)value, prop);
					}

					if (widget != null && widget.getComponent() != null) {	
						addComponent(prop, widget);
						panel.add(widget.getComponent());						
					}
				}
			}
		}
	}


	protected PropertyWidget createTextField(Map conf, String property) {
		TextFieldWidget textFieldWidget = new TextFieldWidget(project); //TODO: fix me
		textFieldWidget.setup(conf, new PropertyEntityData(property));
		//TODO: remove:		
		return textFieldWidget; 
	}
	
	protected ClassSelectionFieldWidget createClassSelectionField(Map conf, String property) {
		ClassSelectionFieldWidget widget = new ClassSelectionFieldWidget(project); //TODO: fix me
		widget.setup(conf, new PropertyEntityData(property));		
		return widget;
	}
	
	protected PropertySelectionFieldWidget createPropertySelectionField(Map conf, String property) {
		PropertySelectionFieldWidget widget = new PropertySelectionFieldWidget(project); //TODO: fix me
		widget.setup(conf, new PropertyEntityData(property));		
		return widget;
	}

	protected PropertyWidget createTextArea(Map conf, String property) {
		TextAreaWidget textareaWidget = new TextAreaWidget(project); //TODO: fix me
		textareaWidget.setup(conf, new PropertyEntityData(property));
		return textareaWidget; 
	}

	protected PropertyWidget createComboBox(Map conf, String property) {
		ComboBoxWidget comboBoxWidget = new ComboBoxWidget(project); //TODO
		comboBoxWidget.setup(conf, new PropertyEntityData(property));
		return comboBoxWidget;
	}

	protected PropertyWidget createHtmlEditor(Map conf, String property) {
		HTMLEditorWidget htmlEditorWidget = new HTMLEditorWidget(project); //TODO
		htmlEditorWidget.setup(conf, new PropertyEntityData(property));
		return htmlEditorWidget;
	}

	protected Component createFieldSet(Map conf, String property) {
		String label = (String) conf.get(FormConstants.LABEL);		
		FieldSet fieldSet = new FieldSet(label);
		String collapsible = (String) conf.get(FormConstants.FIELDSET_COLLAPISBLE);
		if (collapsible != null && collapsible.equalsIgnoreCase("true")) {
			fieldSet.setCollapsible(true);
			String collapsed = (String) conf.get(FormConstants.FIELDSET_COLLAPISBLE);
			if (collapsed != null && collapsed.equalsIgnoreCase("true")) {
				fieldSet.setCollapsed(true);
			}
		}
		String checkBoxToggle = (String) conf.get(FormConstants.FIELDSET_CHECKBOX_TOGGLE);
		if (checkBoxToggle != null && checkBoxToggle.equalsIgnoreCase("true")) {
			fieldSet.setCheckboxToggle(true);
		}

		createInnerPanelComponents(fieldSet, conf);

		return fieldSet;
	}
	
	private PropertyWidget createMultiTextField(Map conf, String property) {
		TextFieldMultiWidget widget = new TextFieldMultiWidget(project);
		widget.setup(conf, new PropertyEntityData(property));
		return widget;
	}

	protected void addComponent(String property, PropertyWidget widget) {
		List<PropertyWidget> comps = prop2widget.get(property);
		if (comps == null) {
			comps = new ArrayList<PropertyWidget>();
		}
		comps.add(widget);
		prop2widget.put(property, comps);
	}


	protected PropertyWidget createGrid(Map conf, String prop) {
		InstanceGridWidget widget = new InstanceGridWidget(project);
		widget.setPortletId(portletId);
		widget.setup(conf, new PropertyEntityData(prop));
		return widget;
	}
	
	protected PropertyWidget createExternalReference(Map conf, String prop) {
		InstanceGridWidget widget = new ReferenceFieldWidget(project);
		widget.setPortletId(portletId);
		widget.setup(conf, new PropertyEntityData(prop));
		return widget;
	}
	
	/*
	 * Fill values in fields
	 */

	public void fillValues(ArrayList<Triple> triples, EntityData subjectEntityData) {		
		//hack
		EntityData subject = subjectEntityData;
		Map<PropertyEntityData, List<EntityData>> prop2values = new LinkedHashMap<PropertyEntityData, List<EntityData>>();
		for (Triple triple : triples) {
			List<EntityData> values = prop2values.get(triple.getProperty());
			if (values == null) {
				values = new ArrayList<EntityData>();
			}
			values.add(triple.getValue());
			prop2values.put(triple.getProperty(), values);
			subject = triple.getEntity();		
		}				
		
		Set<String> propsWithValue = new HashSet();
		for (PropertyEntityData prop : prop2values.keySet()) {			
			String propName = prop.getName();
			propsWithValue.add(propName);
			List<PropertyWidget> widgets = getComponents(propName);
			if (widgets != null) {
				for (PropertyWidget widget : widgets) {
					widget.setSubject(subject);
					widget.setProperty(prop);
					widget.setValues(prop2values.get(prop)); //TODO			
				}	
			}
		}

		//TODO: optimize
		for (String prop : prop2widget.keySet()) {		
			if (!propsWithValue.contains(prop)) {
				List<PropertyWidget> widgets = getComponents(prop);
				if (widgets != null) {
					for (PropertyWidget widget : widgets) {
						Collection values = new ArrayList();						
						widget.setSubject(subject); //TODO: need to set subject!
						//widget.setProperty(triple.getProperty()); //TODO: need to set pred!!
						widget.setValues(values); //TODO			
					}	
				}
			}
		}
	}


	
	protected void fillComboBox(ComboBox comboBox, Triple triple) {
		List<EntityData> allowedValues = triple.getProperty().getAllowedValues();
		if (allowedValues != null && allowedValues.size() > 0) {
			Object[][] data = new Object[allowedValues.size()][2];
			for (int i = 0; i< allowedValues.size(); i++) {
				data[i][0] = allowedValues.get(i);
				data[i][1] = allowedValues.get(i).getBrowserText();
			}

			SimpleStore store = new SimpleStore(new String[]{"entityData", "browserText"}, data);
			comboBox.setValueField("entityData");			
			comboBox.setDisplayField("browserText");
			comboBox.setStore(store);
		}
		String value = triple.getValue().getBrowserText();
		if (value != null) {
			comboBox.setValue(value);
		}
	}	
}
