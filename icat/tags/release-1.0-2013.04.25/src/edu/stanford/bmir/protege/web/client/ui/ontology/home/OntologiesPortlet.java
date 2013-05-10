package edu.stanford.bmir.protege.web.client.ui.ontology.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
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
import edu.stanford.bmir.protege.web.client.rpc.data.ApplicationPropertyNames;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class OntologiesPortlet extends AbstractEntityPortlet {

    private static String LINK_TO_PROTEGE_JNLP = "<a href=\"" + GWT.getModuleBaseURL() + "collabProtege_OWL.jnlp"
            + "\">here</a>";

    private static String DOWNLOAD = "<span style=\"color: #327BAA;\">Download</span>";
    private static String DOWNLOAD_IN_PROGRESS = "<span style=\"color: #AAAAAA;\">Downloading </span><img src=\"images/loading.gif\"/>";

    protected GridPanel ontologiesGrid;
    protected Map<String, ProjectData> ontologies = new HashMap<String, ProjectData>();
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

        recordDef = new RecordDef(new FieldDef[] {
                new StringFieldDef("name"), new StringFieldDef("desc"),
                new StringFieldDef("owner"), new StringFieldDef("download") });

        ArrayReader reader = new ArrayReader(recordDef);
        MemoryProxy dataProxy = new MemoryProxy(new Object[][] {});
        store = new Store(dataProxy, reader);
        store.load();
        ontologiesGrid.setStore(store);

        ontologiesGrid.addGridCellListener(new GridCellListenerAdapter() {
            @Override
            public void onCellClick(GridPanel grid, int rowIndex, int colindex, EventObject e) {
                String col = grid.getColumnModel().getDataIndex(colindex);
                Record record = grid.getStore().getAt(rowIndex);
                String projectName = record.getAsString("name");

                if (col.equals("name")) {
                    currentSelection = new ArrayList<EntityData>();
                    currentSelection.add(new EntityData(projectName));

                    notifySelectionListeners(new SelectionEvent(OntologiesPortlet.this));
                } else if (col.equals("download")) {
                    if (!isDownloading(record)) {
                        onDownload(projectName, record);
                    }
                }
            }
        });
    }

    private boolean isDownloading(Record record) {
        return DOWNLOAD_IN_PROGRESS.equals(record.getAsString("download"));
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
        ownerCol.setWidth(120);
        ownerCol.setResizable(true);
        ownerCol.setSortable(true);

        ColumnConfig downloadCol = new ColumnConfig();
        downloadCol.setHeader("Download");
        downloadCol.setId("download");
        downloadCol.setDataIndex("download");
        downloadCol.setResizable(true);
        downloadCol.setSortable(false);
        downloadCol.setTooltip("Download the latest snapshot of this ontology.");

        ColumnConfig[] columns = new ColumnConfig[] { nameCol, descCol, ownerCol, downloadCol };
        ColumnModel columnModel = new ColumnModel(columns);

        ontologiesGrid.setColumnModel(columnModel);
    }

    public ProjectData getOntologyData(String projectName) {
        return ontologies.get(projectName);
    }

    public List<EntityData> getSelection() {
        return currentSelection;
    }

    protected void onDownload(String projectName, Record record) {
        record.set("download", DOWNLOAD_IN_PROGRESS);
        AdminServiceManager.getInstance().download(projectName, new DownloadProjectHandler(record));
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

                ontologies.put(data.getName(), data);
                Record record = recordDef.createRecord(new Object[] { data.getName(), data.getDescription(), data.getOwner(), DOWNLOAD });
                store.add(record);
            }
        }
    }

    class RefreshMetaProjectHandler extends AbstractAsyncHandler<Void> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("RPC error at refreshing metaproject on the server", caught);
            com.google.gwt.user.client.Window.alert("There were errors at refreshing metaproject information from server.");
        }

        @Override
        public void handleSuccess(Void result) {
            refreshProjectList();
        }
    }

    class DownloadProjectHandler extends AbstractAsyncHandler<String> {

        private Record record;

        public DownloadProjectHandler(Record record) {
            this.record = record;
        }

        @Override
        public void handleFailure(Throwable caught) {
            record.set("download", DOWNLOAD);
            store.commitChanges();
            MessageBox.alert("Error", "The download of the project was not successful. Please try again later.");
        }

        @Override
        public void handleSuccess(String fileName) {
            record.set("download", DOWNLOAD);
            store.commitChanges();

            if (fileName == null) {
                MessageBox.alert("Download", "The project format is currently not supported for download.");
                return;
            }

            String clientShareDir = ClientApplicationPropertiesCache.getStringProperty(ApplicationPropertyNames.DOWNLOAD_CLIENT_REL__PATH_PROP, "") ;
            String downloadLink = GWT.getHostPageBaseURL() +clientShareDir + fileName;
            //FIXME: we should figure out how to make the real HTML enconding for the URL, this is super simplified
            downloadLink = downloadLink.replaceAll(" ", "%20");

            MessageBox.alert("Download successful",
                    "If the download does not start automatically in a few seconds, " +
                            "you may download the file manually, by clicking on this link: <br /><br />" +
                            "<a href=\"" + downloadLink + "\" target=\"_blank\">" + downloadLink + "</a>");

            //FIXME: this should not be hardcoded
            String link = GWT.getModuleBaseURL() + "fileserver?FILE_NAME=" + fileName;
            DOM.setElementAttribute(RootPanel.get("__download").getElement(), "src", link);
        }

    }
}
