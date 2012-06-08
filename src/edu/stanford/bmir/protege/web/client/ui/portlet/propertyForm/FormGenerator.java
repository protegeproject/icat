package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.form.FieldSet;
import com.gwtext.client.widgets.layout.FormLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDInclusionWidget;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDIndexWidget;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDLinearizationWidget;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDTitleWidget;
import edu.stanford.bmir.protege.web.client.ui.icd.InheritedTagsGrid;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

/**
 * This class generates a form with fields based on the portlet configuration
 * from the server. The Form Generator is able to generate. A field is a
 * {@link PropertyWidget}.
 *
 * The Form Generator also has methods for filling in the field values of the
 * generated form.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class FormGenerator {

    private Project project;
    private Map<String, Object> formConf;

    private Collection<PropertyWidget> widgets;

    private Map<Panel, Collection<PropertyWidget>> tab2PropWidgets;
    private Map<Panel, Collection<String>> tab2TypesAny;
    //TODO: treat types_all

    public FormGenerator(Project project, Map<String, Object> formConfiguration) {
        this.project = project;
        this.formConf = formConfiguration;
        this.widgets = new ArrayList<PropertyWidget>();
        tab2PropWidgets = new LinkedHashMap<Panel, Collection<PropertyWidget>>();
        tab2TypesAny = new LinkedHashMap<Panel, Collection<String>>();
    }

    /**
     * This is necessary only to circumvent a problem related to adding/removing
     * components problem that involves a tab panel
     *
     * @return - the tab panel - same as input
     */
    public Panel addFormToTabPanel(TabPanel tabPanel) {
        Object tabs = formConf.get(FormConstants.TABS);
        if (tabs != null && (tabs instanceof Collection) && ((Collection) tabs).size() > 0) {
            // multiple tabs
            for (Iterator iterator = ((Collection) tabs).iterator(); iterator.hasNext();) {
                Object tabObj = iterator.next();
                if (tabObj instanceof Map) {
                    Map tabMap = (Map) tabObj;
                    Panel tab = createInnerPanel(tabMap);
                    tab.setVisible(false);
                    setTabVisible(tab, false);
                    tabPanel.add(tab);
                    tabPanel.hideTabStripItem(tab);

                    String title = (String) tabMap.get(FormConstants.TITLE);
                    if (title != null) {
                       tab.setTitle(title);
                    }
                    String headerCssClass = (String) tabMap.get(FormConstants.HEADER_CSS_CLASS);
                    if (headerCssClass != null) {
                        Ext.get(tabPanel.getTabEl(tab)).addClass(headerCssClass);
                    }
                }
            }
        } else { // one tab
            Panel createInnerPanel = createInnerPanel(formConf);
            if (createInnerPanel != null) {
                tabPanel.add(createInnerPanel);
            }
        }
        return tabPanel;
    }

    public boolean isSuitableForType(Panel tab, Collection<EntityData> types) {
        Collection<String> anytypes = tab2TypesAny.get(tab);
        if (anytypes == null) { return true; }
        for (EntityData type : types) {
            if (anytypes.contains(type.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitableForType(TabPanel tab, String type) {
        Collection<String> anytypes = tab2TypesAny.get(tab);
        return anytypes == null ? true : anytypes.contains(type);
    }

    public Collection<PropertyWidget> getWidgetsInTab(Panel tabPanel) {
        return tab2PropWidgets.get(tabPanel);
    }

    public Collection<Panel> getTabs() {
        return tab2PropWidgets.keySet();
    }

    public Collection<PropertyWidget> getWidgets() {
        return widgets;
    }

    protected Panel createInnerPanel(Map panelConf) {
        Panel panel = new Panel();
        panel.setLayout(new FormLayout()); // TODO
        panel.setPaddings(5);
        panel.setBorder(false);
        panel.setAutoScroll(true);

        tab2TypesAny.put(panel, (Collection<String>) panelConf.get(FormConstants.TYPES_ANY));
        setTabVisible(panel, true);

        createInnerPanelComponents(panel, panelConf);

        return panel;
    }


    protected void createInnerPanelComponents(Panel panel, Map panelConf) {
        Collection<String> properties = panelConf.keySet(); // TODO: this won't be sorted
        String[] sortedProps = new String[properties.size()];

        for (String prop : properties) {
            Object value = panelConf.get(prop);
            if (value instanceof Map) {
                String indexStr = (String) ((Map) value).get(FormConstants.INDEX);
                if (indexStr != null) {
                    int index = Integer.parseInt(indexStr);
                    sortedProps[index] = prop;
                }
            }
        }

        for (String prop : sortedProps) {
            Object value = panelConf.get(prop);
            if (value instanceof Map) {
                String component_type = (String) ((Map) value).get(FormConstants.COMPONENT_TYPE);
                if (component_type != null) {
                    PropertyWidget widget = null;
                    if (component_type.equals(FormConstants.TEXTFIELD)) {
                        widget = createTextField((Map<String, Object>) value, prop);
                    } else if (component_type.equals(FormConstants.TEXTAREA)) {
                        widget = createTextArea((Map) value, prop);
                    } else if (component_type.equals(FormConstants.CHECKBOX)) {
                        widget = createCheckBox((Map) value, prop);
                    } else if (component_type.equals(FormConstants.COMBOBOX)) {
                        widget = createComboBox((Map) value, prop);
                    } else if (component_type.equals(FormConstants.HTMLEDITOR)) {
                        widget = createHtmlEditor((Map) value, prop);
                    } else if (component_type.equals(FormConstants.FIELDSET)) {
                        // widget = createFieldSet((Map)value);
                    } else if (component_type.equals(FormConstants.MULTITEXTFIELD)) {
                        widget = createMultiTextField((Map) value, prop);
                    } else if (component_type.equals(FormConstants.INSTANCETEXTFIELD)) {
                    	widget = createInstanceTextField((Map) value, prop);
                    }  else if (component_type.equals(FormConstants.INSTANCEREFERENCE)) {
                    	widget = createInstanceReferenceField((Map) value, prop);
                    } else if (component_type.equals(FormConstants.GRID)) {
                        widget = createGrid((Map) value, prop);
                    } else if (component_type.equals(FormConstants.EXTERNALREFERENCE)) {
                        widget = createExternalReference((Map) value, prop);
                    } else if (component_type.equals(FormConstants.CLASS_SELECTION_FIELD)) {
                        widget = createClassSelectionField((Map) value, prop);
                    } else if (component_type.equals(FormConstants.PROPERTY_SELECTION_FIELD)) {
                        widget = createPropertySelectionField((Map) value, prop);
                    } else if (component_type.equals(FormConstants.HTMLMESSAGE)) {
                        widget = createHtmlMessage((Map<String, Object>) value, prop);
                    } else if (component_type.equals(FormConstants.INSTANCE_CHECKBOX)) {
                        widget = createInstanceCheckBox((Map) value, prop);
                    } else if (component_type.equals(FormConstants.INSTANCE_RADIOBUTTON)) {
                        widget = createInstanceRadioButton((Map) value, prop);
                    } else if (component_type.equals(FormConstants.INSTANCE_COMBOBOX)) {
                        widget = createInstanceComboBox((Map) value, prop);
                    } else if (component_type.equals(FormConstants.ICDTITLE_TEXTFIELD)) { //ICD specific
                        widget = createICDTitleTextField((Map) value, prop);
                    } else if (component_type.equals(FormConstants.ICDLINEARIZATION_GRID)) { //ICD specific
                        widget = createICDLinearizationGrid((Map) value, prop);
                    } else if (component_type.equals(FormConstants.ICDINHERITEDTAG_GRID)) { //ICD specific
                        widget = createICDInheritedTagGrid((Map) value, prop);
                    } else if (component_type.equals(FormConstants.ICDINDEX_GRID)) { //ICD specific
                        widget = createICDIndexGrid((Map) value, prop);
                    } else if (component_type.equals(FormConstants.ICDINCLUSION_GRID)) { //ICD specific
                        widget = createICDInclusionGrid((Map) value, prop);
                    }

                    if (widget != null && widget.getComponent() != null) {
                        widgets.add(widget);
                        panel.add(widget.getComponent());
                        addToMap(panel, widget);
                    }
                }
            }
        }
    }


    protected void addToMap(Panel tabPanel, PropertyWidget propWidget) {
        Collection<PropertyWidget> widgets = tab2PropWidgets.get(tabPanel);
        if (widgets == null) {
            widgets = new ArrayList<PropertyWidget>();
        }
        widgets.add(propWidget);
        tab2PropWidgets.put(tabPanel, widgets);
    }

    public void setTabVisible(Panel tab, boolean isVisible) {
        tab.getElement().setAttribute("vis", isVisible ? "1" : "0");
    }

    public boolean isTabVisible(Panel tab) {
        String val = tab.getElement().getAttribute("vis");
        return val == null ? false : val.equals("1") ? true : false;
    }


    protected PropertyWidget createTextField(Map<String, Object> conf, String property) {
        TextFieldWidget textFieldWidget = new TextFieldWidget(project);
        textFieldWidget.setup(conf, new PropertyEntityData(property));
        return textFieldWidget;
    }

    protected ClassSelectionFieldWidget createClassSelectionField(Map<String, Object> conf, String property) {
        ClassSelectionFieldWidget widget = new ClassSelectionFieldWidget(project);
        widget.setup(conf, new PropertyEntityData(property));
        return widget;
    }

    protected PropertySelectionFieldWidget createPropertySelectionField(Map<String, Object> conf, String property) {
        PropertySelectionFieldWidget widget = new PropertySelectionFieldWidget(project);
        widget.setup(conf, new PropertyEntityData(property));
        return widget;
    }

    protected PropertyWidget createTextArea(Map<String, Object> conf, String property) {
        TextAreaWidget textareaWidget = new TextAreaWidget(project);
        textareaWidget.setup(conf, new PropertyEntityData(property));
        return textareaWidget;
    }

    protected PropertyWidget createCheckBox(Map<String, Object> conf, String property) {
        CheckBoxWidget checkboxWidget = new CheckBoxWidget(project);
        checkboxWidget.setup(conf, new PropertyEntityData(property));
        return checkboxWidget;
    }

    protected PropertyWidget createComboBox(Map<String, Object> conf, String property) {
        //FIXME Get rid of this special treatment for combobox initialization
        ComboBoxWidget comboBoxWidget = new ComboBoxWidget(project);
        final PropertyEntityData propertyEntityData = new PropertyEntityData(property);
        propertyEntityData.setValueType(ValueType.Instance);
        comboBoxWidget.setup(conf, propertyEntityData);
        return comboBoxWidget;
    }

    protected PropertyWidget createHtmlEditor(Map<String, Object> conf, String property) {
        HTMLEditorWidget htmlEditorWidget = new HTMLEditorWidget(project);
        htmlEditorWidget.setup(conf, new PropertyEntityData(property));
        return htmlEditorWidget;
    }

    protected Component createFieldSet(Map<String, Object> conf, String property) {
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

    private PropertyWidget createMultiTextField(Map<String, Object> conf, String property) {
        TextFieldMultiWidget widget = new TextFieldMultiWidget(project);
        widget.setup(conf, new PropertyEntityData(property));
        return widget;
    }

    private PropertyWidget createInstanceTextField(Map<String, Object> conf, String property) {
    	InstanceTextFieldWidget widget = new InstanceTextFieldWidget(project);
        final PropertyEntityData propertyEntityData = new PropertyEntityData(property);
        propertyEntityData.setValueType(ValueType.Instance);
        widget.setup(conf, propertyEntityData);
    	return widget;
    }

    private PropertyWidget createInstanceReferenceField(Map<String, Object> conf, String property) {
    	ClassInstanceReferenceFieldWidget widget = new ClassInstanceReferenceFieldWidget(project);
    	widget.setup(conf, new PropertyEntityData(property));
    	return widget;
    }

    protected PropertyWidget createGrid(Map<String, Object> conf, String prop) {
        InstanceGridWidget widget = new InstanceGridWidget(project);
        widget.setup(conf, new PropertyEntityData(prop));
        return widget;
    }

    protected PropertyWidget createExternalReference(Map<String, Object> conf, String prop) {
        InstanceGridWidget widget = new ReferenceFieldWidget(project);
        widget.setup(conf, new PropertyEntityData(prop));
        return widget;
    }

    protected PropertyWidget createHtmlMessage(Map<String, Object> conf, String prop) {
        HtmlMessageWidget widget = new HtmlMessageWidget(project);
        widget.setup(conf, new PropertyEntityData(prop));
        return widget;
    }

    protected PropertyWidget createInstanceCheckBox(Map<String, Object> conf, String prop) {
        InstanceCheckBoxWidget widget = new InstanceCheckBoxWidget(project);
        widget.setup(conf, new PropertyEntityData(prop));
        return widget;
    }

    protected PropertyWidget createInstanceRadioButton(Map<String, Object> conf, String prop) {
        InstanceRadioButtonWidget widget = new InstanceRadioButtonWidget(project);
        widget.setup(conf, new PropertyEntityData(prop));
        return widget;
    }

    private PropertyWidget createInstanceComboBox(Map<String, Object> conf, String prop) {
        InstanceComboBox widget = new InstanceComboBox(project);
        widget.setup(conf, new PropertyEntityData(prop));
        return widget;
    }

    //ICD specific
    protected PropertyWidget createICDTitleTextField(Map<String, Object> conf, String property) {
        ICDTitleWidget icdTitleWidget = new ICDTitleWidget(project);
        final PropertyEntityData propertyEntityData = new PropertyEntityData(property);
        propertyEntityData.setValueType(ValueType.Instance);
        icdTitleWidget.setup(conf, propertyEntityData);
        return icdTitleWidget;
    }

    //ICD specific
    protected PropertyWidget createICDLinearizationGrid(Map<String, Object> conf, String property) {
        ICDLinearizationWidget icdLinearizationWidget = new ICDLinearizationWidget(project);
        icdLinearizationWidget.setup(conf, new PropertyEntityData(property));
        return icdLinearizationWidget;
    }

    protected PropertyWidget createICDIndexGrid(Map<String, Object> conf, String property) {
        ICDIndexWidget icdIndexWidget = new ICDIndexWidget(project);
        icdIndexWidget.setup(conf, new PropertyEntityData(property));
        return icdIndexWidget;
    }

    protected PropertyWidget createICDInclusionGrid(Map<String, Object> conf, String property) {
        ICDInclusionWidget icdInclusionWidget = new ICDInclusionWidget(project);
        icdInclusionWidget.setup(conf, new PropertyEntityData(property));
        return icdInclusionWidget;
    }


    //ICD specific
    private PropertyWidget createICDInheritedTagGrid(Map<String, Object> conf, String property) {
        InheritedTagsGrid inheritedTagWidget = new InheritedTagsGrid(project);
        inheritedTagWidget.setup(conf, new PropertyEntityData(property));
        return inheritedTagWidget;
    }


    public void dispose() {
        //formConf.clear();
        //TODO: dispose all widgets
        widgets.clear();
        tab2PropWidgets.clear();
    }
}
