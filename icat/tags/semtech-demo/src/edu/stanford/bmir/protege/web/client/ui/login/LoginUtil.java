package edu.stanford.bmir.protege.web.client.ui.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.rpc.*;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

import java.util.Map;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class LoginUtil {

    public static void login() {
        final Window win = new Window();

        final FormPanel loginFormPanel = new FormPanel();
        loginFormPanel.setWidth("350px");

        Label label = new Label();
        label.setText("Welcome! Please enter your username and password:");
        label.setStyleName("login-welcome-msg");

        FlexTable loginTable = new FlexTable();
        loginTable.setWidget(0, 0, label);
        loginTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        loginTable.getFlexCellFormatter().setHeight(1, 0, "15px");
        loginTable.getFlexCellFormatter().setHeight(2, 0, "25px");
        loginTable.getFlexCellFormatter().setHeight(3, 0, "25px");
        loginTable.getFlexCellFormatter().setHeight(4, 0, "70px");

        loginFormPanel.add(loginTable);

        final TextBox userNameField = new TextBox();
        userNameField.setWidth("250px");
        Label userIdLabel = new Label("User name:");
        userIdLabel.setStyleName("label");
        loginTable.setWidget(2, 0, userIdLabel);
        loginTable.setWidget(2, 1, userNameField);

        final TextBox passwordField = new PasswordTextBox();
        passwordField.setWidth("250px");
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyleName("label");
        loginTable.setWidget(3, 0, passwordLabel);
        loginTable.setWidget(3, 1, passwordField);

        userNameField.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    performSignIn(userNameField.getText(), passwordField, win);
                }
            }
        });

        passwordField.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    performSignIn(userNameField.getText(), passwordField, win);
                }
            }
        });

        Button signInButton = new Button("Sign In", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                performSignIn(userNameField.getText(), passwordField, win);
            }
        });

        ClickHandler hyperlinkClickListener = new ClickHandler() {
            public void onClick(ClickEvent event) {
                String user = userNameField.getText();
                if (user == null || user.length() == 0) {
                    MessageBox
                    .alert(
                            "Warning",
                            "If you forgot your username, please send an email to the administrator.<br /><br />"
                            + "If you forgot your password, please enter a user name in the user name field first and we will reset the password.");
                } else {
                    UIUtil.mask(win.getEl(), "Please wait until we reset your password and send you an email", true, 1);
                    AdminServiceManager.getInstance().sendPasswordReminder(userNameField.getText(),
                            new ForgotPassHandler(win));
                }
            }
        };

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot username or password", "");
        forgotPasswordLink.addClickHandler(hyperlinkClickListener);

        VerticalPanel loginAndForgot = new VerticalPanel();
        loginAndForgot.add(signInButton);
        loginAndForgot.setCellHorizontalAlignment(signInButton, HasAlignment.ALIGN_CENTER);
        loginAndForgot.add(new HTML("<br>"));
        loginAndForgot.add(forgotPasswordLink);

        loginTable.setWidget(4, 1, loginAndForgot);
        loginTable.getFlexCellFormatter().setAlignment(4, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);

        win.setTitle("Sign in");
        win.setClosable(true);
        win.setWidth(360);
        win.setHeight(200);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);
        win.add(loginFormPanel);
        win.show();

        Timer timer = new Timer() {
            @Override
            public void run() {
                userNameField.setFocus(true);
            }
        };
        timer.schedule(100);
    }

    public static void logout() {
        MessageBox.confirm("Log out", "Are you sure you want to log out?", new MessageBox.ConfirmCallback() {
            public void execute(String btnID) {
                if (btnID.equalsIgnoreCase("yes")) {
                    GlobalSettings.getGlobalSettings().setUserName(null);
                    AdminServiceManager.getInstance().logout(new AsyncCallback<Void>(){
                        public void onFailure(Throwable caught) {
                            GWT.log("Error caught on attempting to log out", caught);
                        }

                        public void onSuccess(Void result) {}
                    });
                }
            }
        });
    }

    /**
     * Method to create change password window.
     */
    public static void changePassword() {
        final Window win = new Window();

        FormPanel passwordFormPanel = new FormPanel();

        Label label = new Label("Welcome! Please enter your old password and new password:");
        label.setStyleName("login-welcome-msg");

        FlexTable changePassTable = new FlexTable();
        changePassTable.setWidget(0, 0, label);
        changePassTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        changePassTable.getFlexCellFormatter().setHeight(1, 0, "15px");
        changePassTable.getFlexCellFormatter().setHeight(2, 0, "25px");
        changePassTable.getFlexCellFormatter().setHeight(3, 0, "25px");
        changePassTable.getFlexCellFormatter().setHeight(4, 0, "25px");
        changePassTable.getFlexCellFormatter().setHeight(5, 0, "50px");
        passwordFormPanel.add(changePassTable);

        final PasswordTextBox oldPasswordField = new PasswordTextBox();
        oldPasswordField.setWidth("250px");
        Label oldPasswordLabel = new Label("Old Password:");
        oldPasswordLabel.setStyleName("label");
        changePassTable.setWidget(2, 0, oldPasswordLabel);
        changePassTable.setWidget(2, 1, oldPasswordField);

        final PasswordTextBox newPasswordField = new PasswordTextBox();
        newPasswordField.setWidth("250px");
        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setStyleName("label");
        changePassTable.setWidget(3, 0, newPasswordLabel);
        changePassTable.setWidget(3, 1, newPasswordField);

        final PasswordTextBox newConfirmPassword = new PasswordTextBox();
        newConfirmPassword.setWidth("250px");
        Label newConfPasLabel = new Label("Confirm Password:");
        newConfPasLabel.setStyleName("label");
        changePassTable.setWidget(4, 0, newConfPasLabel);
        changePassTable.setWidget(4, 1, newConfirmPassword);

        Button changePasswordButton = new Button("Change Password", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                if (newPasswordField.getText().equals(newConfirmPassword.getText())) {
                    win.getEl().mask("Changing password...");
                    AdminServiceManager.getInstance().validateUser(GlobalSettings.getGlobalSettings().getUserName(),
                            oldPasswordField.getText(), new AbstractAsyncHandler<UserData>() {
                        @Override
                        public void handleFailure(Throwable caught) {
                            GWT.log("Error at chaging password", caught);
                            win.getEl().unmask();
                            MessageBox.alert("Changing the password failed. Please try again");
                            oldPasswordField.setValue("");
                            newConfirmPassword.setValue("");
                            newPasswordField.setValue("");
                        }

                        @Override
                        public void handleSuccess(UserData userData) {
                            win.getEl().unmask();
                            if (userData != null) {
                                AdminServiceManager.getInstance().changePassword(
                                        GlobalSettings.getGlobalSettings().getUserName(),
                                        newPasswordField.getText(), new ChangePasswordHandler(win));
                            } else {
                                MessageBox.alert("Invalid user name or password. Please try again.");
                                oldPasswordField.setValue("");
                                newConfirmPassword.setValue("");
                                newPasswordField.setValue("");
                            }

                        }

                    });
                } else {
                    MessageBox.alert("Passwords do not match. Please enter them again.");
                }
            }
        });

        changePassTable.setWidget(5, 1, changePasswordButton);
        changePassTable.getFlexCellFormatter().setAlignment(5, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);

        win.setTitle("Change Password");
        win.setClosable(true);
        win.setWidth(428);
        win.setHeight(250);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);
        win.add(passwordFormPanel);
        win.show();
    }

    /**
     * Creates Form for Open Id
     *
     * @param idLabel
     * @param providerId
     */
    public static void authWithOpenId(String idLabel, int providerId) {
        final Window win = new Window();
        final int provId = providerId;

        FormPanel openIdFormPanel = new FormPanel();

        Label label = new Label("Welcome! Please enter your Open Id:");
        label.setStyleName("login-welcome-msg");

        FlexTable openIdTable = new FlexTable();
        openIdTable.setWidget(0, 0, label);
        openIdTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        openIdTable.getFlexCellFormatter().setHeight(1, 0, "15px");
        openIdTable.getFlexCellFormatter().setHeight(2, 0, "25px");
        openIdTable.getFlexCellFormatter().setHeight(3, 0, "25px");
        openIdFormPanel.add(openIdTable);

        final TextBox openIdField = new TextBox();
        openIdField.setWidth("250px");
        Label openIdLabel = new Label(idLabel);
        openIdLabel.setStyleName("label");
        openIdTable.setWidget(2, 0, openIdLabel);
        openIdTable.setWidget(2, 1, openIdField);

        openIdTable.getFlexCellFormatter().setAlignment(3, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);

        win.setTitle("Change Password");
        win.setClosable(true);
        win.setWidth(428);
        win.setHeight(150);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);
        win.add(openIdFormPanel);
        win.show();
    }

    public static void createNewUser() {
        final Window win = new Window();
        FormPanel newUserformPanel = new FormPanel();

        Label label = new Label("Welcome! Please enter your Name, Password and other fields");
        label.setStyleName("login-welcome-msg");

        FlexTable newUserTable = new FlexTable();
        newUserTable.setWidget(0, 0, label);
        newUserTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        newUserTable.getFlexCellFormatter().setHeight(1, 0, "15px");
        newUserTable.getFlexCellFormatter().setHeight(2, 0, "25px");
        newUserTable.getFlexCellFormatter().setHeight(3, 0, "25px");
        newUserTable.getFlexCellFormatter().setHeight(4, 0, "25px");
        newUserTable.getFlexCellFormatter().setHeight(5, 0, "25px");
        newUserTable.getFlexCellFormatter().setHeight(6, 0, "50px");
        newUserformPanel.add(newUserTable);

        final TextBox newUserID = new TextBox();
        newUserID.setWidth("250px");
        Label userIdLabel = new Label("User ID:");
        userIdLabel.setStyleName("label");
        newUserTable.setWidget(2, 0, userIdLabel);
        newUserTable.setWidget(2, 1, newUserID);

        final TextBox newUserEmailID = new TextBox();
        newUserEmailID.setWidth("250px");
        Label emailIDLabel = new Label("Email:");
        emailIDLabel.setStyleName("label");
        newUserTable.setWidget(3, 0, emailIDLabel);
        newUserTable.setWidget(3, 1, newUserEmailID);

        final PasswordTextBox newUserPassword = new PasswordTextBox();
        newUserPassword.setWidth("250px");
        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setStyleName("label");
        newUserTable.setWidget(4, 0, newPasswordLabel);
        newUserTable.setWidget(4, 1, newUserPassword);

        final PasswordTextBox confirmPassword = new PasswordTextBox();
        confirmPassword.setWidth("250px");
        Label newConfirmPassLabel = new Label("Confirm Password:");
        newConfirmPassLabel.setStyleName("label");
        newUserTable.setWidget(5, 0, newConfirmPassLabel);
        newUserTable.setWidget(5, 1, confirmPassword);

        confirmPassword.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    createNewUser(newUserID.getText(), newUserPassword, confirmPassword, win);
                }
            }
        });

        Button register = new Button("Register", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                createNewUser(newUserID.getText(), newUserPassword, confirmPassword, win);
            }
        });

        Button cancel = new Button("Cancel", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                win.close();
            }
        });

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(20);
        buttonPanel.add(register);
        buttonPanel.add(cancel);

        newUserTable.setWidget(6, 1, buttonPanel);
        newUserTable.getFlexCellFormatter().setAlignment(6, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);

        win.setTitle("New User Registration");
        win.setClosable(true);
        win.setWidth(428);
        win.setHeight(240);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);
        win.add(newUserformPanel);
        win.show();
    }

    /**
     * Creates Edit profile Popup
     */
    public static void editProfile() {
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
        editProfileTable.getFlexCellFormatter().setHeight(5, 0, "25px");
        editProfileTable.getFlexCellFormatter().setHeight(6, 0, "25px");
        editProfileTable.getFlexCellFormatter().setHeight(7, 0, "50px");
        editProfileFormPanel.add(editProfileTable);

        String name = GlobalSettings.getGlobalSettings().getUserName();

        final TextBox userNameTextBox = new TextBox();
        userNameTextBox.setWidth("250px");
        userNameTextBox.setEnabled(false);
        Label userNameLabel = new Label("User Name:");
        userNameLabel.setStyleName("label");

        editProfileTable.setWidget(2, 0, userNameLabel);
        editProfileTable.setWidget(2, 1, userNameTextBox);

        if (name != null) {
            userNameTextBox.setText(name);

        }

        final HTML changePasswordHTML = new HTML(
                "<a id='login' href='javascript:;'><span style='font-size:120%; text-decoration:underline;'>"
                + "Change Password" + "</span></a>");
        changePasswordHTML.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                LoginUtil.changePassword();
            }
        });

        editProfileTable.setWidget(3, 1, changePasswordHTML);

        final TextBox userEmailTextBox = new TextBox();
        userEmailTextBox.setWidth("250px");
        Label emailIdLabel = new Label("Email:");
        emailIdLabel.setStyleName("label");
        editProfileTable.setWidget(4, 0, emailIdLabel);
        editProfileTable.setWidget(4, 1, userEmailTextBox);
        Label ontologyNotificationIdLabel = new Label("Ontology Notification Delay:");
        ontologyNotificationIdLabel.setStyleName("label");
        final ListBox ontologyNotificationListBox = new ListBox(false);
        ontologyNotificationListBox.setWidth("250px");
        ontologyNotificationListBox.addItem(NotificationInterval.IMMEDIATELY.getValue());
        ontologyNotificationListBox.addItem(NotificationInterval.HOURLY.getValue());
        ontologyNotificationListBox.addItem(NotificationInterval.DAILY.getValue());
        editProfileTable.setWidget(5, 0, ontologyNotificationIdLabel);
        editProfileTable.setWidget(5, 1, ontologyNotificationListBox);

        Label commentsNotificationIdLabel = new Label("Comments Notification Delay:");
        commentsNotificationIdLabel.setStyleName("label");
        final ListBox commentsNotificationListBox = new ListBox(false);
        commentsNotificationListBox .setWidth("250px");
        commentsNotificationListBox.addItem(NotificationInterval.IMMEDIATELY.getValue());
        commentsNotificationListBox.addItem(NotificationInterval.HOURLY.getValue());
        commentsNotificationListBox.addItem(NotificationInterval.DAILY.getValue());
        editProfileTable.setWidget(6, 0, commentsNotificationIdLabel );
        editProfileTable.setWidget(6, 1, commentsNotificationListBox );

        Button okButton = new Button("Ok", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                boolean isEmailValid = false;
                isEmailValid = isValidEmail(userEmailTextBox.getText().trim());

                if (userEmailTextBox.getText().trim().equals("")) {
                    MessageBox.alert("Please enter your email");
                } else if (isEmailValid) {
                    win.getEl().mask("Saving preferences...");
                    final EditProfileHandler callback = new EditProfileHandler(win);
                    AdminServiceManager.getInstance().setUserEmail(userNameTextBox.getText().trim(),
                            userEmailTextBox.getText().trim(), callback);
                    NotificationServiceManager.getInstance().setNotificationDelay(userNameTextBox.getText().trim(),
                            NotificationType.COMMENT, NotificationInterval.fromString(commentsNotificationListBox.getItemText(commentsNotificationListBox.getSelectedIndex())), callback);
                    NotificationServiceManager.getInstance().setNotificationDelay(userNameTextBox.getText().trim(),
                            NotificationType.ONTOLOGY, NotificationInterval.fromString(ontologyNotificationListBox.getItemText(ontologyNotificationListBox.getSelectedIndex())), callback);
                } else {
                    MessageBox.alert("Email address is invalid. Please enter again a correct email address.");
                }
            }
        });

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

        editProfileTable.setWidget(7, 1, buttonPanel);
        editProfileTable.getFlexCellFormatter()
        .setAlignment(7, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);

        win.setTitle("Edit Profile");
        win.setClosable(true);
        win.setWidth(408);
        win.setHeight(270);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);
        win.add(editProfileFormPanel);
        if (name != null) {
            win.show();

            win.getEl().mask("Retrieving User Email...");
            AdminServiceManager.getInstance().getUserEmail(name, new AsyncCallback<String>() {

                public void onSuccess(String emailId) {
                    win.getEl().unmask();
                    if (emailId != null) {
                        userEmailTextBox.setText(emailId);
                    }
                }

                public void onFailure(Throwable caught) {
                    GWT.log("Error at Getting User Email:", caught);
                    win.getEl().unmask();
                    MessageBox.alert("failed. Please try again. Message: " + caught.getMessage());
                    win.close();
                }
            });
            NotificationServiceManager.getInstance().getNotificationDelay(name, new AsyncCallback<Map<NotificationType, NotificationInterval>>() {

                public void onSuccess(Map<NotificationType, NotificationInterval> notificationPreferences) {
                    win.getEl().unmask();
                    for (NotificationType type : notificationPreferences.keySet()) {
                        if (type.equals(NotificationType.ONTOLOGY)){
                            int i = 0;
                            while (i < ontologyNotificationListBox.getItemCount()){
                                final String s = ontologyNotificationListBox.getItemText(i);
                                if (notificationPreferences.get(type).getValue().equals(s)){
                                    ontologyNotificationListBox.setItemSelected(i, true);
                                }
                                i ++;
                            }
                        }
                        if (type.equals(NotificationType.COMMENT)){
                            int i = 0;
                            while (i < commentsNotificationListBox.getItemCount()){
                                final String s = commentsNotificationListBox.getItemText(i);
                                if (notificationPreferences.get(type).getValue().equals(s)){
                                    commentsNotificationListBox.setItemSelected(i, true);
                                }
                                i ++;
                            }
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    GWT.log("Error at Getting User Notification Preferences:", caught);
                    win.getEl().unmask();
                    MessageBox.alert("failed. Please try again. Message: " + caught.getMessage());
                    win.close();
                }
            });
        } else {
            MessageBox.alert("Error at Getting User Name, Please try again");
        }
    }

    private native static boolean isValidEmail(String email) /*-{
        var reg1 = /(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/; // not valid
        var reg2 = /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3})(\]?)$/; // valid
        return !reg1.test(email) && reg2.test(email);
    }-*/;

    private static void performSignIn(String userName, TextBox passField, Window win) {
        AdminServiceManager.getInstance()
        .validateUser(userName, passField.getText(), new SignInHandler(win, passField));
    }

    private static void createNewUser(String userName, PasswordTextBox newUserPasswordField,
            PasswordTextBox newUserPassword2Field, Window win) {
        String newUserPassword = newUserPasswordField.getText();
        String newUserPassword2 = newUserPassword2Field.getText();

        if (newUserPassword.contentEquals(newUserPassword2)) {
            win.getEl().mask("Creating new user...", true);
            AdminServiceManager.getInstance().registerUser(userName, newUserPassword, new CreateNewUserHandler(win));
        } else {
            MessageBox.alert("Passwords dont match. Please try again.");
            newUserPasswordField.setValue("");
            newUserPassword2Field.setValue("");
        }

    }

    /*
     * Remote calls for Forgot password HyperLink
     */

    static class ForgotPassHandler extends AbstractAsyncHandler<Void> {
        private Window win;

        public ForgotPassHandler(Window win) {
            this.win = win;
        }

        @Override
        public void handleFailure(Throwable caught) {
            win.getEl().unmask();
            GWT.log("Error at forgot password callback : ", caught);
            MessageBox.alert("Error", "There was an error at sending the password reminder.<br />"
                    + "Most likely your user account does not have an email account configured,<br />"
                    + "or the email is invalid.");
        }

        @Override
        public void handleSuccess(Void nothing) {
            win.getEl().unmask();
            MessageBox.alert("Sent password",
                    "Your password has been reset. You should receive an email with the new password.<br /> "
                    + "Please change password the next time you log into the system.");
        }
    }

    /*
     * Remote calls
     */

    static class SignInHandler extends AbstractAsyncHandler<UserData> {
        private Window win;
        private TextBox passField;

        public SignInHandler(Window win, TextBox passField) {
            this.win = win;
            this.passField = passField;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at login", caught);
            win.getEl().unmask();
            MessageBox.alert("Login failed. Please try again.");
            passField.setValue("");
        }

        @Override
        public void handleSuccess(UserData userData) {
            win.getEl().unmask();
            if (userData != null) {
                GlobalSettings.getGlobalSettings().getGlobalSession().setUserName(userData.getName());
                win.close();
            } else {
                MessageBox.alert("Invalid user name or password. Please try again.");
                passField.setValue("");
            }
        }
    }

    /**
     * CallBack for change password process.
     *
     */
    static class ChangePasswordHandler extends AbstractAsyncHandler<Void> {
        private Window win;

        public ChangePasswordHandler(Window win) {
            this.win = win;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at login", caught);
            win.getEl().unmask();
            MessageBox.alert("Error", "There was an error at changing the password.<br />Please try again later.");
        }

        @Override
        public void handleSuccess(Void result) {
            win.getEl().unmask();
            win.close();
            MessageBox.alert("Password changed successfully.");
        }
    }

    static class CreateNewUserHandler extends AbstractAsyncHandler<UserData> {
        private Window win;

        public CreateNewUserHandler(Window win) {
            this.win = win;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at registering new user", caught);
            win.getEl().unmask();
            MessageBox.alert("There was an error at creating the new user. Please try again later.");
        }

        @Override
        public void handleSuccess(UserData userData) {
            win.getEl().unmask();
            if (userData != null) {
                win.close();
                MessageBox.alert("New user created successfully");
            } else {
                MessageBox.alert("New user registration could not be completed. Please try again.");
            }
        }
    }

    /**
     * CallBack for Edit Profile process.
     *
     */
    static class EditProfileHandler extends AbstractAsyncHandler<Void> {
        private Window win;
        private int completions;

        public EditProfileHandler(Window win) {
            this.win = win;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at Editing Profile Info", caught);
            win.getEl().unmask();
            MessageBox.alert("Error",
            "There was an error at changing the User Profile Infomation.<br />Please try again later.");
        }

        @Override
        public void handleSuccess(Void result) {
            synchronized (this){
            completions ++;
            if (completions > 2){
            win.getEl().unmask();
            win.close();
            MessageBox.alert("Profile Information was Updated successfully.");
                completions = 0;
            }
            }
        }
    }

}
