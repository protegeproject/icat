/**
 *
 */
package edu.stanford.bmir.protege.web.client.ui.editprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;
import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OpenIdServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.OpenIdData;
import edu.stanford.bmir.protege.web.client.ui.login.LoginUtil;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.client.ui.openid.OpenIdUtil;

/**
 * @author z.khan
 */
public class EditProfileUtil {

    /**
     * Creates Edit profile Popup
     */
    public void editProfile() {
        final Window win = new Window();
        FormPanel editProfileFormPanel = new FormPanel();

        Label label = new Label("Welcome! Please edit your profile information.");
        label.setStyleName("login-welcome-msg");

        FlexTable editProfileTable = new FlexTable();
        editProfileTable.setWidget(0, 0, label);
        editProfileTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        editProfileTable.getFlexCellFormatter().setHeight(1, 0, "15px");
        editProfileTable.getFlexCellFormatter().setHeight(2, 0, "25px");
        editProfileTable.getFlexCellFormatter().setHeight(3, 0, "30px");
        editProfileTable.getFlexCellFormatter().setHeight(4, 0, "25px");
        editProfileTable.getFlexCellFormatter().setHeight(5, 0, "50px");
        editProfileFormPanel.add(editProfileTable);

        final String name = GlobalSettings.getGlobalSettings().getUserName();
        final TextBox userNameTextBox = new TextBox();
        userNameTextBox.setWidth("250px");
        userNameTextBox.setEnabled(false);
        Label userNameLabel = new Label("User name:");
        userNameLabel.setStyleName("label");

        editProfileTable.setWidget(2, 0, userNameLabel);
        editProfileTable.setWidget(2, 1, userNameTextBox);

        if (name != null) {
            userNameTextBox.setText(name);
        }

        final HTML changePasswordHTML = new HTML(
                "&nbsp<b><span style='font-size:100%;text-decoration:underline;'>Click here to change your password</span></b>");
        changePasswordHTML.setStyleName("links-blue");
        //if else https
        addChangePasswordHTMLClickHandler(changePasswordHTML);

        editProfileTable.setWidget(3, 1, changePasswordHTML);

        final TextBox userEmailTextBox = new TextBox();
        userEmailTextBox.setWidth("250px");
        Label emailIdLabel = new Label("Email:");
        emailIdLabel.setStyleName("label");
        editProfileTable.setWidget(4, 0, emailIdLabel);
        editProfileTable.setWidget(4, 1, userEmailTextBox);

        Button okButton = new Button("Ok", new OkButtonListenerAdapter(win, userEmailTextBox, userNameTextBox));

        Button cancelButton = new Button("Cancel", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                win.close();
            }
        });

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(20);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        editProfileTable.setWidget(5, 1, buttonPanel);
        editProfileTable.getFlexCellFormatter()
                .setAlignment(5, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);

        FlexTable topEditProfileTable = new FlexTable();
        topEditProfileTable.setWidget(0, 0, editProfileFormPanel);
        topEditProfileTable.getFlexCellFormatter().setAlignment(0, 0, HasAlignment.ALIGN_CENTER,
                HasAlignment.ALIGN_MIDDLE);
        Panel panel = new Panel();
        panel.setBorder(false);
        panel.setPaddings(15);
        panel.setCls("loginpanel");
        panel.setLayout(new FitLayout());
        win.setLayout(new FitLayout());

        panel.add(topEditProfileTable, new AnchorLayoutData("-100 30%"));

        win.setTitle("Edit Profile");
        win.setClosable(true);
        win.setWidth(408);
        win.setHeight(260);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);
        win.add(panel);
        if (name != null) {
            win.show();

            win.getEl().mask("Retrieving user email...");
            AdminServiceManager.getInstance().getUserEmail(name, new RetrieveUserEmailHandler(win, userEmailTextBox));

            final FlexTable editProfTable = editProfileTable;
            OpenIdServiceManager.getInstance().getUsersOpenId(name, new GetUsersOpenIdHandler(win, editProfTable));
        } else {
            MessageBox.alert("Error at Getting User Name, Please try again");
        }
    }

    /**
     * @param changePasswordHTML
     */
    protected void addChangePasswordHTMLClickHandler(final HTML changePasswordHTML) {
        AdminServiceManager.getInstance().isLoginWithHttps(new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean isLoginWithHttps) {
                if (isLoginWithHttps) {
                    changePasswordHTML.addClickHandler(changePasswordWithHTTPSClickHandler);
                } else {
                    addChangePasswordWithEncryptionHandler(changePasswordHTML, isLoginWithHttps);
                }
            }

            public void onFailure(Throwable caught) {
                MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
            }
        });

    }

    class EditProfileHandler extends AbstractAsyncHandler<Void> {
        private Window win;

        public EditProfileHandler(Window win) {
            this.win = win;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at Editing Profile Info", caught);
            win.getEl().unmask();
            MessageBox.alert("Error",
                    "There was an error at changing the user profile infomation.<br />Please try again later.");
        }

        @Override
        public void handleSuccess(Void result) {
            win.getEl().unmask();
            win.close();
            MessageBox.alert("Profile information was updated successfully.");
        }
    }

    protected native boolean isValidEmail(String email) /*-{
        var reg1 = /(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/; // not valid
        var reg2 = /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3})(\]?)$/; // valid
        return !reg1.test(email) && reg2.test(email);
    }-*/;

    class RetrieveUserEmailHandler extends AbstractAsyncHandler<String> {
        private Window win;
        private TextBox userEmailTextBox;

        public RetrieveUserEmailHandler(Window win, TextBox userEmailTextBox) {
            this.win = win;
            this.userEmailTextBox = userEmailTextBox;
        }

        @Override
        public void handleSuccess(String emailId) {
            win.getEl().unmask();
            if (emailId != null) {
                userEmailTextBox.setText(emailId);
            }
        }

        @Override
        public void handleFailure(Throwable caught) {
            win.getEl().unmask();
            GWT.log("Error at getting user email:", caught);
            win.close();
        }
    }

    class GetUsersOpenIdHandler extends AbstractAsyncHandler<OpenIdData> {
        private Window win;
        private FlexTable editProfTable;

        public GetUsersOpenIdHandler(Window win, FlexTable editProfTable) {
            this.win = win;
            this.editProfTable = editProfTable;
        }

        @Override
        public void handleFailure(Throwable caught) {
            MessageBox.alert("Error in retrieving OpenId list");

        }

        @Override
        public void handleSuccess(OpenIdData openIdData) {
            OpenIdUtil opIdUtil = new OpenIdUtil();
            opIdUtil.displayUsersOpenIdList(openIdData, editProfTable, win, false, win.getHeight());

        }

    }

    class OkButtonListenerAdapter extends ButtonListenerAdapter {
        private Window win;
        private TextBox userEmailTextBox;
        private TextBox userNameTextBox;

        public OkButtonListenerAdapter(Window win, TextBox userEmailTextBox, TextBox userNameTextBox) {
            this.win = win;
            this.userEmailTextBox = userEmailTextBox;
            this.userNameTextBox = userNameTextBox;
        }

        @Override
        public void onClick(Button button, EventObject e) {
            boolean isEmailValid = false;
            isEmailValid = isValidEmail(userEmailTextBox.getText().trim());
            if (userEmailTextBox.getText().trim().equals("") || isEmailValid) {
                win.getEl().mask("Saving email ...");
                AdminServiceManager.getInstance().setUserEmail(userNameTextBox.getText().trim(),
                        userEmailTextBox.getText().trim(), new EditProfileHandler(win));
            } else {
                MessageBox.alert("Email is invalid. Please enter correct email");
            }
        }
    }

    ClickHandler changePasswordWithHTTPSClickHandler = new ClickHandler() {

        public void onClick(ClickEvent event) {
            final LoginUtil loginUtil = new LoginUtil();
            AdminServiceManager.getInstance().getApplicationHttpsPort(new AsyncCallback<String>() {

                public void onSuccess(String httpsPort) {
                    Cookies.removeCookie(AuthenticationConstants.CHANGE_PASSWORD_RESULT);
                    notifyIfPasswordChanged();
                    String authUrl = loginUtil.getAuthenticateWindowUrl(
                            AuthenticationConstants.AUTHEN_TYPE_CHANGE_PASSWORD, httpsPort);
                    authUrl = authUrl + "&" + AuthenticationConstants.USERNAME + "="
                            + GlobalSettings.getGlobalSettings().getUserName();
                    loginUtil.openNewWindow(authUrl, "440", "260", "0");
                }

                public void onFailure(Throwable caught) {
                    MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);

                }
            });

        }
    };

    protected void addChangePasswordWithEncryptionHandler(HTML changePasswordHTML, final boolean isLoginWithHttps) {
        changePasswordHTML.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                LoginUtil loginUtil = new LoginUtil();
                loginUtil.changePassword(GlobalSettings.getGlobalSettings().getUserName(), isLoginWithHttps);

            }
        });
    }

    protected void notifyIfPasswordChanged() {
        AdminServiceManager.getInstance().getServerPollingTimeoutMin(new AsyncCallback<Integer>() {

            public void onSuccess(final Integer timeout) {
                final long initTime = System.currentTimeMillis();
                final Timer checkSessionTimer = new Timer() {
                    @Override
                    public void run() {
                        final Timer timer = this;
                        long curTime = System.currentTimeMillis();
                        long maxTime = 1000 * 60 * timeout;
                        if (curTime - initTime > maxTime) {
                            timer.cancel();
                        }
                        String passwordChangedCookie = Cookies
                                .getCookie(AuthenticationConstants.CHANGE_PASSWORD_RESULT);
                        if (passwordChangedCookie != null) {
                            timer.cancel();

                            if (passwordChangedCookie.equalsIgnoreCase(AuthenticationConstants.CHANGE_PASSWORD_SUCCESS)) {
                                MessageBox.alert("Password Changed successfully");
                            }
                            Cookies.removeCookie(AuthenticationConstants.CHANGE_PASSWORD_RESULT);
                        }
                    }
                };
                checkSessionTimer.scheduleRepeating(2000);
            }

            public void onFailure(Throwable caught) {

            }
        });

    }

}
