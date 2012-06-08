package edu.stanford.bmir.protege.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Viewport;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ChAOServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.HierarchyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationServiceManager;
import edu.stanford.bmir.protege.web.client.ui.OntologyContainer;
import edu.stanford.bmir.protege.web.client.ui.TopPanel;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class WebProtege implements EntryPoint {

    public void onModuleLoad() {
        initServletMagagers();

        Ext.getBody().mask("Initializing <br>WebProt&eacute;g&eacute ", "x-mask-loading");

        Timer timer = new Timer() {
            @Override
            public void run() {
                buildUI();
                Ext.getBody().unmask();
            }
        };
        timer.schedule(10);
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
    }


    private void buildUI() {
        Panel fitPanel = new Panel();
        fitPanel.setLayout(new FitLayout());
        fitPanel.setCls("white-bg");
        fitPanel.setPaddings(2);

        final Panel wrapperPanel = new Panel();
        wrapperPanel.setLayout(new AnchorLayout());
        wrapperPanel.setCls("white-bg");
        wrapperPanel.setBorder(false);
        wrapperPanel.setAutoScroll(false);

        wrapperPanel.add(new TopPanel());
        wrapperPanel.add(new OntologyContainer(), new AnchorLayoutData("100% 100%"));

        fitPanel.add(wrapperPanel);
        Viewport viewport = new Viewport(fitPanel);

        fitPanel.doLayout();
    }

}
