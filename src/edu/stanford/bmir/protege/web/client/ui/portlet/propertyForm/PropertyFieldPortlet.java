package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.event.PanelListener;
import com.gwtext.client.widgets.event.PanelListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

/**
 * This portlet allows the association of a property with a widget, similar
 * to the forms in the Protege rich client.
 * The portlet has a tabbed form, and the widgets can be organized in tabs.
 * The loading of values is done lazy: we set the subject of all widgets, but only
 * load the values via remote calls for the widgets in the current active tab.
 * There is also some logic that prevents making remote calls if the subject
 * of the widget did not change. To force a refresh of the widget values, call {@link #reload()}.
 * The layout of the tabs is controlled by the {@link FormGenerator}.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class PropertyFieldPortlet extends AbstractEntityPortlet {

    private TabPanel wrappingPanel;
    private FormGenerator formGenerator;

    private PanelListener panelListener;

    /*
     * This portlet should replace the displayed form based on the
     * configuration. However! I had problems with adding and removing
     * dynamically a TabPanel in the portlet and that's why we made a
     * workaround in which the form generator will add the tabs to an existing
     * tab panel (the wrapping panel). In the ideal case, there should be no
     * wrapping panel, and the generator should generate a tab panel (methods
     * are already available), but for some mysterious reason, this does not
     * work.
     */

    public PropertyFieldPortlet(Project project) {
        super(project);
    }

    @Override
    public void initialize() {
        setTitle("Details");
        wrappingPanel = new TabPanel();
        wrappingPanel.setBorder(false);
        wrappingPanel.setAutoScroll(true);
        add(wrappingPanel);
    }
    
    protected PanelListener getPanelListener() {
        if (panelListener == null) {
            panelListener = new PanelListenerAdapter() {
                @Override
                public void onActivate(Panel panel) {
                    PropertyFieldPortlet.this.onActivate(panel);
                }
            };
        }
        return panelListener;
    }

    protected void attachPanelListenerToTabs() {
        Collection<Panel> tabs = formGenerator.getTabs();
        for (Panel tab : tabs) {
            tab.addListener(getPanelListener());
        }
    }

    protected void detachPanelListenerFromTabs() {
        //TODO: we need a remove listener method! None available in gwt-ext
        Collection<Panel> tabs = formGenerator.getTabs();
        for (Panel tab : tabs) {
            //remove listener
        }
    }

    protected void onActivate(Panel panel) {
       fillWidgetValues();
    }

    @Override
    public void setEntity(EntityData newEntity) {
        checkFormGenerator(newEntity);

        setTitle(newEntity == null ? "Details: nothing selected" : "Details for " + newEntity.getBrowserText());

        _currentEntity = newEntity;

        setSubject(_currentEntity);

        fillWidgetValues();
    }


    protected void fillWidgetValues() {
        //set the subject and fill values only for widgets in the active tab
        Panel activeTab = wrappingPanel.getActiveTab();
        if (activeTab != null) {
            Collection<PropertyWidget> widgets = formGenerator.getWidgetsInTab(activeTab);
            if (widgets == null) { return; }
            
            fillWidgetValuesWithBulkCall(activeTab);
            
            for (PropertyWidget widget : widgets) {
            	if (formGenerator.hasEntityTripleHandler(activeTab, widget) == false &&
            			formGenerator.hasEntityPropertyValuesHandler(activeTab, widget) == false) {
            		widget.fillValues();
            	}
            }
        }
    }
    
    protected void fillWidgetValuesWithBulkCall(Panel activeTab) {
    	 GetEntityTripleHandler entityTripleHandler = formGenerator.getEntityTripleHandler(activeTab);
         if (entityTripleHandler != null) {
         	entityTripleHandler.fillValues(_currentEntity); //bulk getEntityTriples call
         }
         
         GetEntityPropertyValuesHandler entityPropertyValuesHandler = formGenerator.getEntityPropertyValuesHandler(activeTab);
         if (entityPropertyValuesHandler != null) {
        	 entityPropertyValuesHandler.fillValues(_currentEntity);
         }
    }

    protected void setSubject(EntityData subject) {
        Collection<PropertyWidget> widgets = formGenerator.getWidgets();
        if (widgets == null) { return; }
        for (PropertyWidget widget : widgets) {
            widget.setSubject(subject);
        }
        
        adjustWidgetsBasedOnType(subject);
    }


    @Override
    public void reload() {
        Panel activeTab = wrappingPanel.getActiveTab();
        if (activeTab != null) {
            Collection<PropertyWidget> widgets = formGenerator.getWidgetsInTab(activeTab);
            if (widgets == null) { return; }
            for (PropertyWidget widget : widgets) {
                widget.refresh();
            }
        }
    }


    /*
     * Returns empty selection for now.
     */
    public List<EntityData> getSelection() {
        return new ArrayList<EntityData>(); //TODO: not clear what it should return
    }

    protected boolean needsNewFormGenerator(EntityData newEntity) {
        try {
            if (_currentEntity != null && newEntity != null) {
                Collection<EntityData> currentTypes = _currentEntity.getTypes();
                Collection<EntityData> newTypes = newEntity.getTypes();

                if (currentTypes == null || newTypes == null) {
                    return true;
                }

                if (currentTypes != null && newTypes != null) {
                    if (currentTypes.size() == newTypes.size()) {
                        return  !currentTypes.equals(newTypes);
                    } else {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return  true;
        }
        return true;
    }

    protected FormGenerator checkFormGenerator(EntityData newEntity) {
        Map<String, Object> properties = getPortletConfiguration().getProperties();
        if (properties == null) { return null; }

        if (formGenerator == null) { // at init
            formGenerator = createFormGenerator();
            adjustTabs(newEntity);
        } else {
            if (needsNewFormGenerator(newEntity)) {
                adjustTabs(newEntity);
            }
        }
        return formGenerator;
    }

    protected void adjustTabs(EntityData newEntity) {
        if (newEntity == null) {
            hideAllTabs();
            return;
        }

        Collection<EntityData> types = newEntity.getTypes();
        if (types == null) {
            hideAllTabs();
            return;
        }

        boolean changed = false;
        Panel activeTab = wrappingPanel.getActiveTab();
        Panel firstVisibleTab = null;

        Collection<Panel> tabs = formGenerator.getTabs();
        for (Panel tab : tabs) {
            if (formGenerator.isSuitableForType(tab, types)) {
                if (!formGenerator.isTabVisible(tab)) {
                    wrappingPanel.unhideTabStripItem(tab);
                    formGenerator.setTabVisible(tab, true);
                    changed = true;
                }
                if (firstVisibleTab == null) {
                    firstVisibleTab = tab;
                }
            } else {
                if (formGenerator.isTabVisible(tab)) {
                    wrappingPanel.hideTabStripItem(tab);
                    formGenerator.setTabVisible(tab, false);
                    changed = true;
                }
            }
        }
        if (changed) {
            if (formGenerator.isTabVisible(activeTab)) {
                wrappingPanel.activate(activeTab.getId());
            }
            else if (firstVisibleTab != null) {
                wrappingPanel.activate(firstVisibleTab.getId());
            }
            else {
                //TODO deactivate tab if possible
            }
        }
    }

    private void hideAllTabs() {
        Collection<Panel> tabs = formGenerator.getTabs();
        for (Panel tab : tabs) {
            wrappingPanel.hideTabStripItem(tab);
            formGenerator.setTabVisible(tab, false);
        }
    }

    protected FormGenerator createFormGenerator() {
        Map<String, Object> properties = getPortletConfiguration().getProperties();
        formGenerator = new FormGenerator(project, properties);
        formGenerator.addFormToTabPanel(wrappingPanel);
        attachPanelListenerToTabs();
        
        //this is a hack to display all the widgets and the scrollbar..
        //the heights in the configuration.xml should be fixed
        wrappingPanel.setHeight(wrappingPanel.getHeight() - 70);
        
        wrappingPanel.activate(0);
        wrappingPanel.doLayout();
        return formGenerator;
    }

    protected void adjustWidgetsBasedOnType(EntityData newEntity) {
    	Collection<PropertyWidget> widgets = formGenerator.getWidgets();
    	Collection<EntityData> types = newEntity.getTypes();
    	for (PropertyWidget widget : widgets) {
    		if (widget.isTypeSensitive()) {
    			widget.changeVisibilityBasedOnSubjectType(types);
    		}
    	}
    }
    
    @Override
    public void setPortletConfiguration(PortletConfiguration portletConfiguration) {
    	super.setPortletConfiguration(portletConfiguration);
    	
    	//this code is here because of initialization problems
    	//this code can be called only after the portletConfiguration has been set
    	if(hasMultiRowTabs() == true) {
        	wrappingPanel.setStyleName("tabpanel-multirow");
        } else {
        	wrappingPanel.setEnableTabScroll(true);
        }
    }

    private boolean hasMultiRowTabs() {
    	return getPortletConfiguration().getBooleanProperty(FormConstants.MULTIROW_TABS, true);
    }

    
}
