package edu.stanford.bmir.protege.web.client;

import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Viewport;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.rpc.AccessPolicyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ApplicationPropertiesServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.AuthenticateServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ChAOServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.HierarchyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.NotificationServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OpenIdServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationServiceManager;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.OntologyContainer;
import edu.stanford.bmir.protege.web.client.ui.TopPanel;
import edu.stanford.bmir.protege.web.client.ui.login.FacebookLoginUtil;
import edu.stanford.bmir.protege.web.client.ui.ontology.accesspolicy.InvitationConstants;
import edu.stanford.bmir.protege.web.client.ui.ontology.accesspolicy.InviteUserUtil;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class WebProtege implements EntryPoint {

    public void onModuleLoad() {

        initServletMagagers();
        Timer timer = new Timer() {
            @Override
            public void run() {
                initializeClientApplicationPropertiesCache();
            }
        };
        timer.schedule(10);
        FacebookLoginUtil fLoginUtil = new FacebookLoginUtil();
        fLoginUtil.addFacebookListener();
        if (isInvitation()) {
            InviteUserUtil inviteUserUtil = new InviteUserUtil();
            inviteUserUtil.ProcessInvitation();

        }
    }

    private void initializeClientApplicationPropertiesCache() {
        ClientApplicationPropertiesCache.initialize(new AsyncCallback<Map<String, String>>() {
            public void onFailure(Throwable caught) {
                init();
                Ext.getBody().unmask();
            }

            public void onSuccess(Map<String, String> result) {
                init();
                Ext.getBody().unmask();
            }
        });
    }

    /**
     * Force Servlet initialization - needed when running in browsers.
     */
    protected void initServletMagagers() {
        AdminServiceManager.getInstance();
        ApplicationPropertiesServiceManager.getInstance();
        AuthenticateServiceManager.getInstance();
        OntologyServiceManager.getInstance();
        ProjectConfigurationServiceManager.getInstance();
        ChAOServiceManager.getInstance();
        ICDServiceManager.getInstance();
        HierarchyServiceManager.getInstance();
        OpenIdServiceManager.getInstance();
        AccessPolicyServiceManager.getInstance();
        NotificationServiceManager.getInstance();
    }

    protected void buildUI() {
        Panel fitPanel = new Panel();
        fitPanel.setLayout(new FitLayout());
        fitPanel.setCls("white-bg");
        fitPanel.setPaddings(2);

        final Panel wrapperPanel = new Panel();
        wrapperPanel.setLayout(new AnchorLayout());
        wrapperPanel.setCls("white-bg");
        wrapperPanel.setBorder(false);
        wrapperPanel.setAutoScroll(false);

        wrapperPanel.add(new TopPanel(), new AnchorLayoutData("100% 100%"));
        wrapperPanel.add(new OntologyContainer(), new AnchorLayoutData("100% 100%"));

        fitPanel.add(wrapperPanel);
        Viewport viewport = new Viewport(fitPanel);

        fitPanel.doLayout();

    }


    /**
     * Checks whether the URL is an invitation URL, by analyzing the parameter
     * <code>InvitationConstants.INVITATION_URL_PARAMETER_IS_INVITATION</code>
     * in URL.
     *
     * @return
     */
    private boolean isInvitation() {
        String isInvitationURL = Window.Location
                .getParameter(InvitationConstants.INVITATION_URL_PARAMETER_IS_INVITATION);
        if (isInvitationURL != null && isInvitationURL.trim().contains("true")) {
            return true;
        }
        return false;
    }


    /*
     * Restore user from session.
     */
    protected void init() {
        AdminServiceManager.getInstance().getCurrentUserInSession(new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                GWT.log("Could not get server permission from server", caught);
                buildUI();
            }

            public void onSuccess(String userData) {
                GlobalSettings.getGlobalSettings().setUserName(userData);
                buildUI();
            }
        });
    }

}
