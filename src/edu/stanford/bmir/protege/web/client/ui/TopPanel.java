package edu.stanford.bmir.protege.web.client.ui;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.PermissionConstants;
import edu.stanford.bmir.protege.web.client.model.SystemEventManager;
import edu.stanford.bmir.protege.web.client.model.event.LoginEvent;
import edu.stanford.bmir.protege.web.client.model.listener.SystemListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.ui.login.LoginUtil;

/**
 * The panel shown at the top of the display. It contains the documentation
 * links, the sign in/out links, and may contain other menus, etc.
 *
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class TopPanel extends Panel {

    private HTML signInOutHtml;
    private HTML userNameHtml;

    private Images images = (Images) GWT.create(Images.class);
    private static HorizontalPanel optionsLinks;
    private MenuBar verticalOptionsMenu;
    private MenuItem addUser;

    public interface Images extends ImageBundle {
        public AbstractImagePrototype iCatLogo();
    }

    public TopPanel() {
        setLayout(new FitLayout());
        setAutoWidth(true);
        setCls("top-panel");

        // Outer panel to house logo and inner panel
        HorizontalPanel outer = new HorizontalPanel();
        outer.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        final Image logo = images.iCatLogo().createImage();
        outer.add(logo);
        outer.setCellHorizontalAlignment(logo, HorizontalPanel.ALIGN_LEFT);

        // Inner panel to house links panel
        VerticalPanel inner = new VerticalPanel();
        inner.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        inner.add(getLinksPanel());

        outer.add(inner);
        add(outer);

        SystemEventManager.getSystemEventManager().addLoginListener(new SystemListenerAdapter() {
            @Override
            public void onLogin(LoginEvent loginEvent) {
                adjustUserNameText();
                adjustOptionPanel();
            }

            @Override
            public void onLogout(LoginEvent loginEvent) {
                adjustUserNameText();
                adjustOptionPanel();
            }
        });

        AdminServiceManager.getInstance().getCurrentUserInSession(new AsyncCallback<String>() {
            public void onSuccess(String userData) {
                GlobalSettings.getGlobalSettings().setUserName(userData);
                adjustUserNameText();
                adjustOptionPanel();
            }

            public void onFailure(Throwable caught) {
                GWT.log("Could not get server permission from server", caught);
            }
        });

    }


    /**
     * Method for to displaying the Option link.
     * After SignIn "Options" link should be visible otherwise disable
     */
    public void adjustOptionPanel() {
        String userName = GlobalSettings.getGlobalSettings().getUserName();
        if (userName != null) { //login
            optionsLinks.setVisible(true);
            AdminServiceManager.getInstance().getAllowedServerOperations(userName, new AsyncCallback<Collection<String>>() {
                public void onSuccess(Collection<String> operations) {
                    if (operations.contains(PermissionConstants.CREATE_USERS)) {
                        addUserMenuItem();
                    }
                }

                public void onFailure(Throwable caught) {
                    GWT.log("Could not get server permission from server", caught);
                }
            });
        } else { //logout
            verticalOptionsMenu.removeItem(addUser);
            optionsLinks.setVisible(false);
        }
    }

    protected HorizontalPanel getLinksPanel() {
        HorizontalPanel links = new HorizontalPanel();
        links.setSpacing(2);

        // User name text and/or sign in/out message
        links.add(getUserNameHtml());

        links.add(new HTML("<span style='font-size:75%;'>&nbsp;|&nbsp;</span>"));

        // Sign In and/or Sign Out link
        links.add(getSignInOutHtml());

        links.add(new HTML("<span style='font-size:80%;'>&nbsp;|&nbsp;</span>"));

        // Adding Options menu link
        links.add(getOptionsPanel());

        optionsLinks.add(new HTML("<span style='font-size:80%;'>&nbsp;|&nbsp;</span>"));

        // Feedback link
        links.add(getFeedbackHTML());

        return links;
    }

    protected HorizontalPanel getOptionsPanel() {
        MenuBar horizontalOptionsMenu = new MenuBar();
        verticalOptionsMenu = new MenuBar(true);

        horizontalOptionsMenu.addItem("" + new HTML(
                "<a id='login' href='javascript:;'><span style='font-size:75%; text-decoration:underline;'>Options</span>" +
                        "<span style='font-size:75%; margin-top: 3px;'>&nbsp;&#9660;</span></a>"),
                true, verticalOptionsMenu);
        horizontalOptionsMenu.setStyleName("menuBar");
        verticalOptionsMenu.setStyleName("subMenuBar");

        addChangePasswordMenuItem();

        addEditProfileMenuItem();

        optionsLinks = new HorizontalPanel();
        optionsLinks.add(horizontalOptionsMenu);

        optionsLinks.setVisible(false);

        return optionsLinks;
    }

    protected HTML getUserNameHtml() {
        return userNameHtml = new HTML("<span style='font-size:75%; font-weight:bold;'>" + getUserNameText() + "</span>");
    }

    protected HTML getSignInOutHtml() {
        signInOutHtml = new HTML(
                "<a id='login' href='javascript:;'><span style='font-size:75%; text-decoration:underline;'>"
                        + getSignInOutText() + "</span></a>");
        signInOutHtml.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                onSignInOut();
            }
        });
        return signInOutHtml;
    }

    protected HTML getFeedbackHTML() {
        HTML feedbackHtml = new HTML(
                "<a id='feedback' href='javascript:;'><span style='font-size:75%; text-decoration:underline; padding-right:5px;'>" +
                        "Send&nbsp;feedback!</span></a>");
        feedbackHtml.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final Window window = new Window();
                window.setTitle("Send feedback!");
                window.setClosable(true);
                window.setWidth(400);
                window.setHeight(150);
                window.setHtml(getFeebackText());
                window.setPaddings(7);
                window.setCloseAction(Window.HIDE);
                window.show("feedback");
            }
        });
        return feedbackHtml;
    }

    protected void addChangePasswordMenuItem() {
        MenuItem changePassword = new MenuItem("Change Password", new Command() {
            public void execute() {
                LoginUtil.changePassword();
            }
        });
        verticalOptionsMenu.addItem(changePassword);
    }

    protected void addUserMenuItem() {
        addUser = new MenuItem("Add User", new Command() {
            public void execute() {
                LoginUtil.createNewUser();
            }
        });
        verticalOptionsMenu.addItem(addUser);
    }

    protected void addEditProfileMenuItem() {
        MenuItem editProfile = new MenuItem("Edit Profile", new Command() {
            public void execute() {
                LoginUtil.editProfile();
            }
        });
        verticalOptionsMenu.addItem(editProfile);
    }

    protected String getUserNameText() {
        String name = GlobalSettings.getGlobalSettings().getUserName();
        return name == null ? "You&nbsp;are&nbsp;signed&nbsp;out." : name;
    }

    /*
     * Sign in and Sign out handling
     */

    protected String getSignInOutText() {
        return GlobalSettings.getGlobalSettings().isLoggedIn() ? "Sign&nbsp;Out" : "Sign&nbsp;In";
    }

    protected void onSignInOut() {

        String userName = GlobalSettings.getGlobalSettings().getUserName();
        if (userName == null) {
            LoginUtil.login();
        } else {
            LoginUtil.logout();
        }
    }

    private void adjustUserNameText() {
        signInOutHtml
                .setHTML("<a id='login' href='javascript:;'><span style='font-size:75%; text-decoration:underline;'>"
                        + getSignInOutText() + "</span>");
        userNameHtml.setHTML("<span style='font-size:75%; font-weight:bold;'>" + getUserNameText() + "</span>");
    }

    /*
     * Text for links
     */

    private static String getFeebackText() {
        return "<br /> Thank you for using iCAT! " + "<br /><br /> Your feedback is very important to us. "
                + "Please send your comments, questions, feature requests, bugs, etc. "
                + "on the <a href=\"http://groups.google.com/group/icat-users\">Google icat-users group</a>. <br /><br />";
    }
}
