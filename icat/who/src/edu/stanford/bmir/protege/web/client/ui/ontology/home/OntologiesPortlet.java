package edu.stanford.bmir.protege.web.client.ui.ontology.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.data.event.StoreListener;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class OntologiesPortlet extends AbstractEntityPortlet {

    private static String LINK_TO_PROTEGE_JNLP = "<a href=\"" + GWT.getModuleBaseURL() + "collabProtege_OWL.jnlp"
            + "\">here</a>";

    protected GridPanel ontologiesGrid;
    protected HashMap<String, ProjectData> ontologies = new HashMap<String, ProjectData>();
    protected RecordDef recordDef;
    protected Store store;

    protected List<EntityData> currentSelection;

    public OntologiesPortlet() {
        super(null);
    }

    @Override
    public void reload() {
    }

    @Override
    public void initialize() {
        setLayout(new FitLayout());
        setTitle("Ontologies");

        createGrid();
        add(ontologiesGrid);
        //add(createJNLPPanel()) ;
    }

    private HTML createJNLPPanel() {
        // FIXME: the <br> is a hack - fix in css
        return new HTML("<html><br><br><span class=\"jnlpPanel\">To open the same copy of an ontology"
                + " in the Prot&eacute;g&eacute rich client, click " + LINK_TO_PROTEGE_JNLP
                + " .</span><br><br></html>");
    }

    protected void createGrid() {
        ontologiesGrid = new GridPanel();

        ontologiesGrid.setAutoScroll(true);
        ontologiesGrid.setStripeRows(true);
        ontologiesGrid.setAutoExpandColumn("desc");
        ontologiesGrid.setAutoHeight(true);
        // ontologiesGrid.setHeight(500);

        createColumns();

        recordDef = new RecordDef(new FieldDef[] { new StringFieldDef("name"), new StringFieldDef("desc"),
                new StringFieldDef("owner"),
        // new StringFieldDef("action")
                });

        ArrayReader reader = new ArrayReader(recordDef);
        MemoryProxy dataProxy = new MemoryProxy(new Object[][] {});
        store = new Store(dataProxy, reader);
        store.load();
        ontologiesGrid.setStore(store);

        ontologiesGrid.addGridCellListener(new GridCellListenerAdapter() {
            @Override
            public void onCellClick(GridPanel grid, int rowIndex, int colindex, EventObject e) {
                if (grid.getColumnModel().getDataIndex(colindex).equals("name")) {
                    Record record = grid.getStore().getAt(rowIndex);
                    String projectName = record.getAsString("name");

                    currentSelection = new ArrayList<EntityData>();
                    currentSelection.add(new EntityData(projectName));
                    GWT.log(projectName + " selected in OntologiesPortlet", null);
                    notifySelectionListeners(new SelectionEvent(OntologiesPortlet.this));
                }
            }
        });
    }

    public void addStoreListener(StoreListener listener){
        store.addStoreListener(listener);
    }

    protected void createColumns() {
        ColumnConfig nameCol = new ColumnConfig();
        nameCol.setHeader("Name");
        nameCol.setId("name");
        nameCol.setDataIndex("name");
        nameCol.setResizable(true);
        nameCol.setSortable(true);
        nameCol.setWidth(200);

        Renderer nameColRenderer = new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                    Store store) {
                return new String("<a href=\"#" + value.toString() + "\">" + value.toString() + "</a>");
            }
        };
        nameCol.setRenderer(nameColRenderer);

        ColumnConfig descCol = new ColumnConfig();
        descCol.setHeader("Description");
        descCol.setId("desc");
        descCol.setDataIndex("desc");
        descCol.setResizable(true);
        descCol.setSortable(true);

        ColumnConfig ownerCol = new ColumnConfig();
        ownerCol.setHeader("Owner");
        ownerCol.setId("owner");
        ownerCol.setDataIndex("owner");
        ownerCol.setResizable(true);
        ownerCol.setSortable(true);

        /*
         * ColumnConfig actionCol = new ColumnConfig();
         * actionCol.setHeader("Action"); actionCol.setId("action");
         * actionCol.setDataIndex("action"); actionCol.setResizable(true);
         * actionCol.setSortable(false);
         * actionCol.setTooltip("Open the same copy of the ontology in" +
         * "\n Collaborative Protege using Java Web Start");
         * actionCol.setWidth(200);
         */
        /*
         * Renderer actionColRenderer = new Renderer() { public String
         * render(Object value, CellMetadata cellMetadata, Record record, int
         * rowIndex, int colNum, Store store) { return new
         * String("<a href=\"\">view recent changes</a>"); } };
         * actionCol.setRenderer(actionColRenderer);
         */

        ColumnConfig[] columns = new ColumnConfig[] { nameCol, descCol, ownerCol };
        ColumnModel columnModel = new ColumnModel(columns);
        ontologiesGrid.setColumnModel(columnModel);
    }

    public ProjectData getOntologyData(String projectName) {
        return ontologies.get(projectName);
    }

    public List<EntityData> getSelection() {
        return currentSelection;
    }

    @Override
    protected void afterRender() {
        refreshProjectList();
    }

    @Override
    protected void onRefresh() {
        AdminServiceManager.getInstance().refreshMetaproject(new RefreshMetaProjectHandler());
    }

    public void refreshProjectList() {
        AdminServiceManager.getInstance().getProjects(GlobalSettings.getGlobalSettings().getUserName(),
                        new GetProjectsHandler());
    }

    @Override
    public void onLogin(String userName) {
        refreshProjectList();
        doLayout();
    }

    @Override
    public void onLogout(String userName) {
        refreshProjectList();
        doLayout();
    }

    /*
     * Remote calls
     */

    class GetProjectsHandler extends AbstractAsyncHandler<Collection<ProjectData>> {

        @Override
        public void handleFailure(Throwable caught) {
            store.removeAll();
            GWT.log("RPC error getting ontologies from server", caught);
            MessageBox.alert("Error", "There was an error retrieving ontologies from server. Please try again");
        }

        @Override
        public void handleSuccess(Collection<ProjectData> projectsData) {
            store.removeAll();
            Iterator<ProjectData> i = projectsData.iterator();
            while (i.hasNext()) {
                ProjectData data = i.next();
                // Window.alert("Getting project " + data.getName());
                ontologies.put(data.getName(), data);
                Record record = recordDef.createRecord(new Object[] { data.getName(), data.getDescription(),
                        data.getOwner() });
                store.add(record);
            }
        }
    }

    class RefreshMetaProjectHandler extends AbstractAsyncHandler<Void> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("RPC error at refreshing metaproject on the server", caught);
            com.google.gwt.user.client.Window
                    .alert("There were errors at refreshing metaproject information from server.");
        }

        @Override
        public void handleSuccess(Void result) {
            refreshProjectList();
        }

    }
}
