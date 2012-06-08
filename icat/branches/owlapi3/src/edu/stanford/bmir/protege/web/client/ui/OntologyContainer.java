package edu.stanford.bmir.protege.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.event.StoreListenerAdapter;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.event.PanelListenerAdapter;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.TextFieldListenerAdapter;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.SystemEventManager;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.ui.ontology.home.MyWebProtegeTab;
import edu.stanford.bmir.protege.web.client.ui.portlet.EntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.search.SearchUtil;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionListener;
import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

import java.util.LinkedHashMap;

/**
 * Class that holds all the tabs corresponding to ontologies. It also contains
 * that MyWebProtege Tab. This class manages the loading of projects and their
 * configurations.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class OntologyContainer extends TabPanel {
    private MyWebProtegeTab myWebProTab;
    private LinkedHashMap<String, Ontology> loadedOntologiesMap = new LinkedHashMap<String, Ontology>();
    private TextField searchField;

    public OntologyContainer() {
        super();
        buildUI();
    }

    protected void buildUI() {
        setLayoutOnTabChange(false); // TODO: check if necessary
        setEnableTabScroll(true);
        //  addSearchField(); //FIXME: implement the search in a better way
        createAndAddHomeTab();
    }

    protected void addSearchField() {
        searchField = createSearchField();
        add(searchField);
    }

    private void onSearch() {
        String searchText = searchField.getValueAsString().trim();
        search(searchText);
    }

    private void search(String text) {
        Ontology tab = getActiveOntology();
        if (tab == null) {
            return;
        }
        AbstractTab activeTab = tab.getActiveOntologyTab();
        if (activeTab == null) {
            return;
        }
        EntityPortlet ctrlPortlet = activeTab.getControllingPortlet();
        SearchUtil su = new SearchUtil(tab.getProject(), ctrlPortlet);
        su.search(text);
    }

    public Ontology getActiveOntology() {
        Panel activeTab = getActiveTab();
        return (activeTab instanceof Ontology) ? (Ontology) activeTab : null;
    }

    protected TextField createSearchField() {
        searchField = new TextField("Search: ", "search", 200);
        searchField.addListener(new TextFieldListenerAdapter() {
            @Override
            public void onSpecialKey(Field field, EventObject e) {
                if (e.getKey() == EventObject.ENTER) {
                    onSearch();
                }
            }
        });
        return searchField;
    }

    protected void createAndAddHomeTab() {
        myWebProTab = new MyWebProtegeTab();
        myWebProTab.setTitle(myWebProTab.getLabel());
        add(myWebProTab);

        myWebProTab.getOntologiesPortlet().addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                String projectName = event.getSelectable().getSelection().iterator().next().getName();
                loadProject(projectName);
            }
        });
        myWebProTab.getOntologiesPortlet().addStoreListener(new StoreListenerAdapter(){
            private boolean once = false;

            @Override
            public void onAdd(Store store, Record[] records, int index) {
                final String linkId = Window.Location.getParameter("id");
                final String tabName = Window.Location.getParameter("tab");
                final String ontology = Window.Location.getParameter("ontology");
                Window.Location.getParameter("id");
                if (records.length > 0 && ontology != null && linkId != null && tabName != null && !once) {
                    if (ontology.equals(records[0].getAsString("name"))){
                        once = true;
                        loadProject(ontology, tabName, linkId);
                    }
                }
            }

        });
    }

    public void loadProject(String projectName) {
        loadProject(projectName, null, null);
    }

    public void loadProject(String projectName, final String tabName, final String selectionName) {
        Ontology ontTab = loadedOntologiesMap.get(projectName);
        if (ontTab != null) {
            activate(ontTab.getId());
            return;
        }

        Project project = new Project(myWebProTab.getOntologiesPortlet().getOntologyData(projectName));
        UIUtil.showLoadProgessBar("Loading " + project.getProjectName(), "Loading");
        OntologyServiceManager.getInstance().loadProject(projectName, new LoadProjectHandler(project, tabName, selectionName));
    }

    public void addTab(Project project, final String tabName, final String selection) {
        Ontology ont = new Ontology(project, tabName, selection);
        ont.setClosable(true);
        loadedOntologiesMap.put(project.getProjectName(), ont);

        ont.addListener(new PanelListenerAdapter() {
            @Override
            public boolean doBeforeDestroy(Component component) {
                if (component instanceof Ontology) {
                    Ontology o = (Ontology) component;
                    loadedOntologiesMap.remove(o.getProject().getProjectName());
                    o.getProject().dispose();
                    hideTabStripItem(o);
                    o.hide();
                    activate(0);
                }
                return true;
            }
        });

        add(ont);
        activate(ont.getId());
        setActiveTab(ont.getId());
        ont.layoutProject();
        SystemEventManager.getSystemEventManager().requestPermissions(project);
    }

    protected void layoutProject(Project project) {
        Ontology ontTab = loadedOntologiesMap.get(project.getProjectName());
        if (ontTab == null) {
            GWT.log("Could not find ontology tab for " + project.getProjectName(), null);
            return;
        }
        ontTab.layoutProject();
    }

    /*
     * Remote calls
     */

    class LoadProjectHandler extends AbstractAsyncHandler<Integer> {
        private Project project;
        private String tabName;
        private String selection;

        LoadProjectHandler(Project project, String tabName, String selection) {
            this.project = project;
            this.tabName = tabName;
            this.selection = selection;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("There were errors at loading project " + project.getProjectName(), caught);
            UIUtil.hideLoadProgessBar();
            MessageBox.alert("Load project " + project.getProjectName() + " failed.<br>" + " Message: "
                    + caught.getMessage());
        }

        @Override
        public void handleSuccess(Integer revision) {
            int serverVersion = revision.intValue();
            project.setServerVersion(serverVersion);
            UIUtil.hideLoadProgessBar();
            addTab(project, tabName, selection);
        }
    }
}
