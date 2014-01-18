package edu.stanford.bmir.protege.web.client.ui.ontology.classes;

import java.util.Collection;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;

/**
 * A portlet that displays the {@link AllPropertiesGrid} (see for more
 * documentation). Shows all properties of an entity and supports basic editing.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class AllPropertiesPortlet extends AbstractEntityPortlet {

    protected AllPropertiesGrid propGrid;

    public AllPropertiesPortlet(Project project) {
        super(project);
    }

    @Override
    public void reload() {
        setTitle(_currentEntity == null ? "Properties: nothing selected" : "Properties for " + _currentEntity.getBrowserText());

        propGrid.setEntity(_currentEntity);
    }

    @Override
    public void initialize() {
        setTitle("Properties");
        this.propGrid = new AllPropertiesGrid(project);
        add(propGrid);
    }

    @Override
    public void setPortletConfiguration(PortletConfiguration portletConfiguration) {
        super.setPortletConfiguration(portletConfiguration);
        //forward configurations to property grid
        this.propGrid.setConfigurationMap(portletConfiguration.getProperties());
    }

    public Collection<EntityData> getSelection() {
        return propGrid.getSelection();
    }

    @Override
    protected void onRefresh() {
        propGrid.refresh();
    }

    @Override
    public void onPermissionsChanged(Collection<String> permissions) {
        propGrid.updateButtonStates();
    }

}
