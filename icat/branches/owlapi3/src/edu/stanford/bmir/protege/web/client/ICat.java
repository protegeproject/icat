package edu.stanford.bmir.protege.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Viewport;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.*;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.ui.Ontology;
import edu.stanford.bmir.protege.web.client.ui.TopPanel;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class ICat implements EntryPoint {

    private final static String PROJECT_NAME = "ICD";

    public void onModuleLoad() {
        initServletMagagers();
        Ext.getBody().mask("Initializing <br>iCAT ", "x-mask-loading");

        Ext.getBody().mask("Initializing <br>iCAT ", "x-mask-loading");

        //workaround for event bug
        ProjectData data = new ProjectData(null, null, PROJECT_NAME, null);
        //data.setName(PROJECT_NAME);
        final Project project = new Project(data);
        OntologyServiceManager.getInstance().loadProject(PROJECT_NAME, new AsyncCallback<Integer>() {
            public void onSuccess(Integer revision) {
                int serverVersion = revision.intValue();
                project.setServerVersion(serverVersion);
            }

            public void onFailure(Throwable caught) {
                MessageBox.alert("Error", "There was an error loading project " + PROJECT_NAME +
                        "<br />Please try again later.");
            }
        });

        Timer timer = new Timer() {
            @Override
            public void run() {
                buildUI(project);
                Ext.getBody().unmask();
            }
        };
        timer.schedule(200);
    }

    /**
     * Force Servlet initialization - needed when running in browsers.
     */
    protected void initServletMagagers() {
        AdminServiceManager.getInstance();
        OntologyServiceManager.getInstance();
        ProjectConfigurationServiceManager.getInstance();
        ChAOServiceManager.getInstance();
        ICDServiceManager.getInstance();
        HierarchyServiceManager.getInstance();
        OpenIdServiceManager.getInstance();
    }

    private void buildUI(Project project) {
        // Main panel (contains entire user interface)
        Panel main = new Panel();
        main.setLayout(new FitLayout());
        main.setBorder(false);

        // Wrapper panel (for top and ontology panels)
        final Panel wrapper = new Panel();
        wrapper.setLayout(new AnchorLayout());
        wrapper.setBorder(false);

        // Add top panel to wrapper
        TopPanel top = new TopPanel();
        top.setBodyStyle("background-color:#CDEB8B");
        wrapper.add(new TopPanel());

        // Add ontology panel to wrapper
        Ontology ontology = new Ontology(project);
        ontology.layoutProject();
        wrapper.add(ontology, new AnchorLayoutData("100% 100%"));

        // Add main panel to viewport
        main.add(wrapper);
        new Viewport(main);

        main.doLayout();
    }

}
