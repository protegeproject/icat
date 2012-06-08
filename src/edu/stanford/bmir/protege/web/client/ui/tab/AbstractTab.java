package edu.stanford.bmir.protege.web.client.ui.tab;

import java.util.*;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.event.PanelListener;
import com.gwtext.client.widgets.event.PanelListenerAdapter;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.portal.Portal;
import com.gwtext.client.widgets.portal.PortalColumn;
import com.gwtext.client.widgets.portal.Portlet;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabColumnConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabConfiguration;
import edu.stanford.bmir.protege.web.client.ui.generated.UIFactory;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassesTab;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.portlet.EntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionListener;

/**
 * Abstract class that should be extended by all tabs that can be added to the
 * main UI of WebProtege.
 * <p/>
 * The tab has a reference to all the portlets that are currently part of the
 * tab and a reference to the controlling portlet -> the portlet that provides
 * the selection for the other portlets.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
// TODO: reordering of portlets in a column at drang-n-drop does not work yet
public abstract class AbstractTab extends Portal {

    protected Project project;
    protected Portal portal;
    private TabConfiguration tabConfiguration;
    private LinkedHashMap<PortalColumn, List<EntityPortlet>> columnToPortletsMap;
    private LinkedHashMap<TabColumnConfiguration, PortalColumn> tabColumnConfigToColumn;
    // TODO: cache the portlets for performance

    /**
     * The portlet that provides the selection for other portlets
     */
    private EntityPortlet controllingPortlet;

    private SelectionListener selectionControllingListener;
    protected PanelListener destroyListener;

    public AbstractTab(Project project) {
        super();

        this.project = project;
        this.portal = new Portal();
        this.columnToPortletsMap = new LinkedHashMap<PortalColumn, List<EntityPortlet>>();
        this.tabColumnConfigToColumn = new LinkedHashMap<TabColumnConfiguration, PortalColumn>();

        this.selectionControllingListener = getSelectionControllingListener();
        this.destroyListener = getDestroyListener();

    }

    public AbstractTab(Project project, int columnCount) {
        this(project, getDefaultColumnData(columnCount));
    }

    /**
     * This constructor will build a tab with the column layout provided as an
     * argument.
     * <p/>
     * Instead, please consider calling the {@link #AbstractTab(Project)}
     * constructor and then call {@link #setTabConfiguration(TabConfiguration)}.
     *
     * @param project       - the project displayed by this tab
     * @param colLayoutData - the layout information for the columns
     */
    public AbstractTab(Project project, ColumnLayoutData[] colLayoutData) {
        this(project);
    }

    protected static ColumnLayoutData[] getDefaultColumnData(int columnCount) {
        ColumnLayoutData[] colLayout = new ColumnLayoutData[columnCount];
        for (int i = 0; i < columnCount; i++) {
            colLayout[i] = new ColumnLayoutData(1 / columnCount);
        }
        return colLayout;
    }

    protected PanelListener getDestroyListener() {
        if (destroyListener == null) {
            this.destroyListener = new PanelListenerAdapter() {
                @Override
                public void onDestroy(Component component) {
                    if (component instanceof EntityPortlet) {
                        removePortlet((EntityPortlet) component);
                    }
                    super.onDestroy(component);
                }
            };
        }
        return destroyListener;
    }

    /**
     * Called on logout of a user. The default implementation calls the onLogout
     * method on each portlet. It can be overridden in subclasses.
     *
     * @param userName - name of the user who logged out
     */
    public void onLogout(String userName) {
        for (EntityPortlet portlet : getPortlets()) {
            portlet.onLogout(userName);
        }
    }

    /**
     * Called on log in of a user. The default implementation calls the onLogin
     * method on each portlet. It can be overridden in subclasses.
     *
     * @param userName - named of the user who logged in
     */
    public void onLogin(String userName) {
        for (EntityPortlet portlet : getPortlets()) {
            portlet.onLogin(userName);
        }
    }

    public void onPermissionsChanged(Collection<String> permissions) {
        for (EntityPortlet portlet : getPortlets()) {
            portlet.onPermissionsChanged(permissions);
        }
    }

    protected SelectionListener getSelectionControllingListener() {
        if (selectionControllingListener == null) {
            this.selectionControllingListener = new SelectionListener() {
                public void selectionChanged(SelectionEvent event) {
                    onSelectionChange(event.getSelectable().getSelection());
                }
            };
        }

        return selectionControllingListener;
    }

    /**
     * This method is called when a new selection has been made in the
     * controlling portlet. If you override this method, make sure to call
     * <code>super.onSelectionChange(newSelection)</code>, otherwise the other
     * portlets will not be updated with the new selection.
     *
     * @param newSelection - the new selection
     */
    protected void onSelectionChange(Collection<EntityData> newSelection) {
        if (controllingPortlet == null) {
            return; // nothing to do
        }

        //TODO: implement multiple selection

        EntityData selection = null;
        if (newSelection != null && newSelection.size() > 0) {
            selection = newSelection.iterator().next();
        }

        for (Object element2 : getPortlets()) {
            EntityPortlet portlet = (EntityPortlet) element2;
            if (!portlet.equals(controllingPortlet)) {
                // TODO: make it work with multiple selections
                portlet.setEntity(selection);
            }
        }
    }

    public int getColumnCount() {
        return columnToPortletsMap.size();
    }

    public List<PortalColumn> getColumns() {
        return new ArrayList<PortalColumn>(columnToPortletsMap.keySet());
    }

    public List<EntityPortlet> getPortlets() {
        List<EntityPortlet> portlets = new ArrayList<EntityPortlet>();
        for (List<EntityPortlet> portletList : columnToPortletsMap.values()) {
            portlets.addAll(portletList);
        }
        return portlets;
    }

    public void addPortlet(EntityPortlet portlet, int column) {
        TabColumnConfiguration col = getTabColumnConfigurationAt(column);
        if (col == null) {
            GWT.log("Column does not exist: " + column, null);
            return;
        }
        PortletConfiguration portletConfiguration = ((AbstractEntityPortlet) portlet).getPortletConfiguration();
        addPortletToColumn(portlet, col, portletConfiguration == null ? project.getLayoutManager().createPortletConfiguration(portlet) : portletConfiguration, true);
    }

    public void removePortlet(EntityPortlet portlet) {
        for (PortalColumn col : columnToPortletsMap.keySet()) {
            List<EntityPortlet> portlets = columnToPortletsMap.get(col);
            if (portlets.contains(portlet)) {
                portlets.remove(portlet);
                removePortletFromTabConfig(portlet);
                ((Portlet) portlet).hide();
                ((Portlet) portlet).destroy();
                // how to remove the destroy listener?
            }
        }
    }

    protected void removePortletFromTabConfig(EntityPortlet portlet) {
        for (TabColumnConfiguration column : tabColumnConfigToColumn.keySet()) {
            PortalColumn portalColumn = tabColumnConfigToColumn.get(column);
            List<PortletConfiguration> portlets = column.getPortlets();
            Component[] comps = portalColumn.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i].equals(portlet)) {
                    if (portlets.size() > i) { // TODO: check should not be
                        // needed, but it seems that
                        // portlets are destroyed several
                        // times
                        PortletConfiguration portletConfig = portlets.get(i);
                        if (portletConfig.getName().equals(portlet.getClass().getName())) {
                            portlets.remove(portletConfig);
                        }
                    }
                }
            }
        }
    }

    public PortalColumn getPortalColumnAt(int index) {
        TabColumnConfiguration config = getTabColumnConfigurationAt(index);
        return config == null ? null : tabColumnConfigToColumn.get(config);
    }

    public TabColumnConfiguration getTabColumnConfigurationAt(int index) {
        Collection<TabColumnConfiguration> tabCols = tabColumnConfigToColumn.keySet();
        if (tabCols.size() < index) {
            return null;
        }
        int i = -1;
        for (TabColumnConfiguration tabColumnConfiguration : tabCols) {
            TabColumnConfiguration col = tabColumnConfiguration;
            i++;
            if (i == index) {
                return col;
            }
        }
        return null;
    }

    public EntityPortlet getControllingPortlet() {
        return controllingPortlet;
    }

    public void setControllingPortlet(EntityPortlet newControllingPortlet) {
        if (controllingPortlet != null) {
            if (controllingPortlet.equals(newControllingPortlet)) {
                return;
            }
            controllingPortlet.removeSelectionListener(selectionControllingListener);
        }
        controllingPortlet = newControllingPortlet;
        if (controllingPortlet != null) {
            controllingPortlet.addSelectionListener(selectionControllingListener);
        }
    }

    public TabConfiguration getTabConfiguration() {
        return tabConfiguration;
    }

    public void setTabConfiguration(TabConfiguration tabConfig) {
        this.tabConfiguration = tabConfig;
    }

    public void setup() {
        setLayout(new FitLayout());
        setHideBorders(true);

        // if configuration is null, initialize the default UI
        if (tabConfiguration == null) {
            tabConfiguration = getDefaultTabConfiguration();
        }

        setTitle(getLabel());
        setClosable(tabConfiguration.getClosable());
        addColumns();
        addPorteltsToColumns();
        setupControllingPortlet();

        add(portal);
    }

    protected void addColumns() {
        for (int i = 0; i < tabConfiguration.getColumns().size(); i++) {
            addColumn(tabConfiguration.getColumns().get(i));
        }
    }

    protected void addColumn(TabColumnConfiguration colConfig) {
        float width = colConfig.getWidth();
        PortalColumn portalColumn = new PortalColumn();
        portalColumn.setPaddings(10, 10, 10, 10);
        portal.add(portalColumn, new ColumnLayoutData(width));
        columnToPortletsMap.put(portalColumn, new ArrayList<EntityPortlet>());
        tabColumnConfigToColumn.put(colConfig, portalColumn);
    }

    protected void addPorteltsToColumns() {
        for (TabColumnConfiguration tabColumnConfiguration : tabColumnConfigToColumn.keySet()) {
            addPortletsToColumn(tabColumnConfiguration);
        }
    }

    protected void addPortletsToColumn(TabColumnConfiguration tabColumnConfiguration) {
        List<PortletConfiguration> portlets = tabColumnConfiguration.getPortlets();
        for (PortletConfiguration portletConfiguration : portlets) {
            addPortletToColumn(tabColumnConfiguration, portletConfiguration, false);
        }
    }

    protected void addPortletToColumn(TabColumnConfiguration tabColumnConfiguration,
                                      PortletConfiguration portletConfiguration, boolean updateColConfig) {
        // configuration
        EntityPortlet portlet = createPortlet(portletConfiguration);
        if (portlet == null) {
            return;
        }
        addPortletToColumn(portlet, tabColumnConfiguration, portletConfiguration, updateColConfig);
    }

    protected void addPortletToColumn(EntityPortlet portlet, TabColumnConfiguration tabColumnConfiguration,
                                      PortletConfiguration portletConfiguration, boolean updateColConfig) {
        if (updateColConfig) {
            tabColumnConfiguration.addPortelt(portletConfiguration, 0);
        }
        ((AbstractEntityPortlet) portlet).setPortletConfiguration(portletConfiguration);
        PortalColumn portalColumn = tabColumnConfigToColumn.get(tabColumnConfiguration);
        portalColumn.add((Portlet) portlet);
        columnToPortletsMap.get(portalColumn).add(portlet);

        ((AbstractEntityPortlet) portlet).setTab(this);
    }

    protected EntityPortlet createPortlet(PortletConfiguration portletConfiguration) {
        String portletClassName = portletConfiguration.getName();
        EntityPortlet portlet = UIFactory.createPortlet(project, portletClassName);
        if (portlet == null) {
            return null;
        }
        int height = portletConfiguration.getHeight();
        if (height == 0) {
            ((Portlet) portlet).setAutoHeight(true);
        } else {
            ((Portlet) portlet).setHeight(height);
        }
        int width = portletConfiguration.getWidth();
        if (width == 0) {
            ((Portlet) portlet).setAutoWidth(true);
        } else {
            ((Portlet) portlet).setWidth(width);
        }
        ((Portlet) portlet).addListener(destroyListener);

        return portlet;
    }

    protected void setupControllingPortlet() {
        PortletConfiguration portletConfiguration = tabConfiguration.getControllingPortlet();
        if (portletConfiguration == null) {
            setControllingPortlet(getDefaultControllingPortlet());
        } else {
            setControllingPortlet(getPortletByClassName(portletConfiguration.getName()));
        }
    }

    public EntityPortlet getPortletByClassName(String javaClassName) {
        for (PortalColumn portalColumn2 : columnToPortletsMap.keySet()) {
            PortalColumn portalColumn = portalColumn2;
            List<EntityPortlet> portlets = columnToPortletsMap.get(portalColumn);
            for (EntityPortlet entityPortlet : portlets) {
                if (entityPortlet.getClass().getName().equals(javaClassName)) {
                    return entityPortlet;
                }
            }
        }
        return null;
    }

    protected EntityPortlet getDefaultControllingPortlet() {
        // take the first portlet from the first column
        PortalColumn portalColumn = columnToPortletsMap.keySet().iterator().next();
        if (portalColumn == null) {
            return null;
        }
        List<EntityPortlet> portlets = columnToPortletsMap.get(portalColumn);
        if (portlets.isEmpty()) {
            return null;
        }
        return portlets.iterator().next();
    }

    /**
     * Overwrite this method to provide a default tab configuration for a tab.
     * For example, the {@link ClassesTab} may set up in the default
     * configuration the default portlets to be shown in the UI (e.g., classes
     * tree portlet, properties portlet and restriction portlet). The default
     * tab configuration will be used if the user has not performed any
     * customization to this tab.
     * <p>
     * This method will not se the tab configuration to the return value of this
     * method. It is responsability of the calling code to make so.
     * </p>
     * <p>
     * The default implementation will return an empty tab.
     * </p>
     *
     * @return the default configuration of this tab
     */
    public TabConfiguration getDefaultTabConfiguration() {
        TabConfiguration tabConfiguration = new TabConfiguration();
        List<TabColumnConfiguration> colList = new ArrayList<TabColumnConfiguration>();
        TabColumnConfiguration column = new TabColumnConfiguration();
        column.setWidth((float) 1.0);
        colList.add(column);
        tabConfiguration.setColumns(colList);
        tabConfiguration.setName(this.getClass().getName());
        return tabConfiguration;
    }

    public String getLabel() {
        String tabLabel = tabConfiguration != null ? tabConfiguration.getLabel() : null;
        return tabLabel == null ? "Tab" : tabLabel;
    }

    public void setLabel(String label) {
        setTitle(label);
        if (tabConfiguration != null) {
            tabConfiguration.setLabel(label);
        }
    }

    public void setSelection(String selectionName) {
        EntityData entityData = new EntityData(selectionName);
        getControllingPortlet().setSelection(Arrays.asList(entityData));
    }
}
