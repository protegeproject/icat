package edu.stanford.bmir.protege.web.client.ui.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.reveregroup.gwt.facebook4gwt.Facebook;
import com.reveregroup.gwt.facebook4gwt.LoginButton;
import com.reveregroup.gwt.facebook4gwt.events.FacebookLoginEvent;
import com.reveregroup.gwt.facebook4gwt.events.FacebookLoginHandler;
import com.reveregroup.gwt.facebook4gwt.user.FacebookUser;
import com.reveregroup.gwt.facebook4gwt.user.UserField;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.rpc.AuthenticateServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OpenIdServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.OpenIdData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.client.ui.openid.OpenIdUtil;
import edu.stanford.bmir.protege.web.client.ui.openid.listener.AddNewFBProfileListener;
import edu.stanford.bmir.protege.web.client.ui.openid.model.AddNewFBProfileEventManager;

/**
 * This class contains functionality for initializing Facebook connection ,
 * registering Facebook connection change listener(For login and Add New Open Id
 * functionality) and adding login/logout button to login window.
 *
 * @author z.khan
 *
 */
public class FacebookLoginUtil implements AddNewFBProfileListener {

    String API_KEY_WEBPT = "0f02c522de37da78aa7c4548f9a3bf21";
    String API_KEY_WEBPT2 = "d6fc3befaef9e897edf05278c5e73a39";

    public static String loginMethod = AuthenticationConstants.LOGIN_METHOD;

    private static FacebookLoginHandler fbLoginHandlerForAddNewOpenId;
    private static boolean isLoginWithHttps;
    private static Window loginWindow;
    private FlexTable addNewOpenIdListTable;
    private Window addNewOpenIdWin;
    private int addNewOpenIdWindowBaseHt;

    /**
     * @param isLoginWithHttps
     *            the isLoginWithHttps to set
     */
    public static void setLoginWithHttps(boolean isLoginWithHttps) {
        FacebookLoginUtil.isLoginWithHttps = isLoginWithHttps;
    }

    /**
     * @param loginWindow
     *            the loginWindow to set
     */
    public static void setLoginWindow(Window loginWindow) {
        FacebookLoginUtil.loginWindow = loginWindow;
    }

    /**
     * Adds Facebook login button to the FlexTable
     *
     * @param loginTable
     *            The FlexTable which to add the Facebook login button
     * @param row
     *            The starting row of FlexTable for adding Facebook login button
     */
    void addFacebookConnect(FlexTable loginTable, int row) {
        LoginButton loginButton = new LoginButton(true, LoginButton.Size.MEDIUM, LoginButton.Background.LIGHT,
                LoginButton.Length.SHORT);
        FlexTable facebookLoginTable = new FlexTable();

        facebookLoginTable.setWidget(0, 1, loginButton);
        facebookLoginTable.getFlexCellFormatter().setWidth(0, 0, "80px");
        loginTable.setWidget(row, 0, facebookLoginTable);
        loginTable.getFlexCellFormatter().setColSpan(row, 0, 3);

    }

    /**
     * Registers a listeners for Facebook connection status changed.
     *
     * @param isLoginWithHttps
     * @param win
     */
    public void registerFacebookListenerForLogin() {
        Facebook.addLoginHandler(new FacebookLoginHandler() {

            public void loginStatusChanged(FacebookLoginEvent event) {
                if (event.isLoggedIn()) {
                    if (GlobalSettings.getGlobalSettings().getUserName() != null) { // if user is already logged in WebProtege so exit.
                        return;
                    }

                    Facebook.APIClient().users_getLoggedInUser(new AsyncCallback<FacebookUser>() {

                        public void onFailure(Throwable caught) {
                            GWT.log("Error at getting logged in user from Facebook ", caught);

                        }

                        public void onSuccess(FacebookUser user) {
                            AuthenticateServiceManager.getInstance().addFacebookUserDetailsToSession("Facebook",
                                    user.getProfileURL(), user.getName(), loginMethod, new AsyncCallback<Boolean>() {

                                        public void onSuccess(Boolean result) {

                                            LoginUtil loginUtil = new LoginUtil();
                                            if (!isLoginWithHttps) {
                                                if (loginWindow != null) {
                                                    loginWindow.close();
                                                }
                                                loginUtil.checkIfOpenIdInSessionForLogin(AuthenticationConstants.ACCOUNT_TYPE_FACEBOOK);
                                            } else {
                                                loginUtil.closeWindow();
                                            }

                                        }

                                        public void onFailure(Throwable caught) {
                                            MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
                                        }

                                    });
                        }
                    }, UserField.NAME, UserField.STATUS, UserField.PROFILE_URL);
                }
            }
        });
    }

    /**
     * Registers a listeners for Facebook connection status changed.
     *
     * @param isLoginWithHttps
     * @param win
     */
    public void registerFacebookListenerForLogin(final boolean isLoginWithHttps, final Window win) {
        Facebook.addLoginHandler(new FacebookLoginHandler() {

            public void loginStatusChanged(FacebookLoginEvent event) {
                if (event.isLoggedIn()) {
                    if (GlobalSettings.getGlobalSettings().getUserName() != null) { // if user is already logged in WebProtege so exit.
                        return;
                    }

                    Facebook.APIClient().users_getLoggedInUser(new AsyncCallback<FacebookUser>() {

                        public void onFailure(Throwable caught) {
                            GWT.log("Error at getting logged in user from Facebook ", caught);

                        }

                        public void onSuccess(FacebookUser user) {
                            AuthenticateServiceManager.getInstance().addFacebookUserDetailsToSession("Facebook",
                                    user.getProfileURL(), user.getName(), loginMethod, new AsyncCallback<Boolean>() {

                                        public void onSuccess(Boolean result) {

                                            LoginUtil loginUtil = new LoginUtil();
                                            if (!isLoginWithHttps) {
                                                if (win != null) {
                                                    win.close();
                                                }
                                                loginUtil.checkIfOpenIdInSessionForLogin(AuthenticationConstants.ACCOUNT_TYPE_FACEBOOK);
                                            } else {
                                                loginUtil.closeWindow();
                                            }

                                        }

                                        public void onFailure(Throwable caught) {
                                            MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
                                        }

                                    });
                        }
                    }, UserField.NAME, UserField.STATUS, UserField.PROFILE_URL);
                }
            }
        });
    }

    /**
     * Adds Facebook login/logout icon in 'Add New Open Id' icon panel.
     *
     * @param editProfileTable
     * @param win
     * @param windowBaseHt
     * @param popupFlex
     * @param row
     * @param column
     */
    public void addFacebookIconForAddNewOpenId(final FlexTable editProfileTable, final Window win,
            final int windowBaseHt, final FlexTable popupFlex, final int row, final int column) {
        AddNewFBProfileEventManager.getAddNewFBProfileEventManager().setFBProfileListener(this);
        this.addNewOpenIdListTable = editProfileTable;
        this.addNewOpenIdWin = win;
        this.addNewOpenIdWindowBaseHt = windowBaseHt;
        OpenIdServiceManager.getInstance().getFacebookAPIKey(new AsyncCallback<String>() {

            public void onSuccess(String facebookApi) {

                if (!Facebook.getConnectionStatus().equals(Facebook.ConnectState.CONNECTED)) {
                    Facebook.init(facebookApi);
                }
                final LoginButton loginButton = new LoginButton(true, LoginButton.Size.SMALL,
                        LoginButton.Background.LIGHT, LoginButton.Length.SHORT);

                HTML facebookLabel = new HTML("&nbsp<b><span style='font-size:75%;'>Facebook</span></b>");
                facebookLabel.setTitle("Login with your Facebook account");
                facebookLabel.setStyleName("menuBar");
                facebookLabel.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        NativeEvent evt = Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false);
                        loginButton.getElement().dispatchEvent(evt);

                    }
                });
                FlexTable FBIconLabelTable = new FlexTable();
                FBIconLabelTable.setWidget(0, 0, loginButton);
                FBIconLabelTable.setWidget(0, 1, facebookLabel);
                popupFlex.setWidget(row, column, FBIconLabelTable);
                popupFlex.getFlexCellFormatter().setColSpan(row, column, 2);

            }

            public void onFailure(Throwable caught) {
                GWT.log("Error at getting Facebook API Key from property file ", caught);
                MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);

            }
        });

    }

    public void assocFacebookProfile() {
        OpenIdServiceManager.getInstance().isFacebookProfileInSessForAddNewOpenId(new AsyncCallback<UserData>() {

            public void onSuccess(UserData userData) {
                if (userData != null) {//open id URL attribute is present HttpSession
                    if (userData.getName() != null) { //open id URL is already associated
                        MessageBox.alert("Facebook profile already associated with WebProtege user '"
                                + userData.getName() + "'.");
                    } else { // associate open id to current user
                        String name = GlobalSettings.getGlobalSettings().getUserName();
                        OpenIdServiceManager.getInstance().assocNewOpenIdToUser(name, new AsyncCallback<OpenIdData>() {

                            public void onSuccess(OpenIdData result) {
                                OpenIdUtil opIdUtil = new OpenIdUtil();
                                opIdUtil.displayUsersOpenIdList(result, addNewOpenIdListTable, addNewOpenIdWin, true,
                                        addNewOpenIdWindowBaseHt);
                            }

                            public void onFailure(Throwable caught) {
                                GWT.log("Error in associating Open Id to user ", caught);
                            }
                        });
                    }
                }

            }

            public void onFailure(Throwable caught) {
                GWT.log("Error in function isFacebookProfileInSessForAddNewOpenId ", caught);
            }
        });

    }

    public void registerFacebookListenerForNewOpenId() {
        if (fbLoginHandlerForAddNewOpenId == null) {
            fbLoginHandlerForAddNewOpenId = new FacebookLoginHandler() {

                public void loginStatusChanged(FacebookLoginEvent event) {

                    if (event.isLoggedIn()) {
                        Facebook.APIClient().users_getLoggedInUser(new AsyncCallback<FacebookUser>() {

                            public void onFailure(Throwable caught) {
                                GWT.log("loginStatusChanged fail ", caught);
                            }

                            public void onSuccess(FacebookUser user) {
                                AuthenticateServiceManager.getInstance().addFacebookUserDetailsToSession("Facebook",
                                        user.getProfileURL(), user.getName(), loginMethod,
                                        new AsyncCallback<Boolean>() {

                                            public void onSuccess(Boolean result) {
                                                AddNewFBProfileEventManager.getAddNewFBProfileEventManager()
                                                        .notifyAddNewFBProfileListener();
                                            }

                                            public void onFailure(Throwable caught) {
                                                GWT.log("Error in adding Facebook User Details To Session ", caught);
                                            }

                                        });
                            }
                        }, UserField.NAME, UserField.STATUS, UserField.PROFILE_URL);
                    }
                }
            };
        }
        Facebook.addLoginHandler(fbLoginHandlerForAddNewOpenId);
    }

    /**
     * Retrieves Facebook API keys and adds listeners for adding new Facebook
     * profile to webprotege account or
     */
    public void addFacebookListener() {

        OpenIdServiceManager.getInstance().getFacebookAPIKey(new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                GWT.log("Error at retrieving Facebook application API key ", caught);
            }

            public void onSuccess(String facebookApi) {

                if (!Facebook.getConnectionStatus().equals(Facebook.ConnectState.CONNECTED)) {
                    Facebook.init(facebookApi);
                }
                registerFacebookListenerForNewOpenId();

                registerFacebookListenerForLogin();
            }
        });
    }

}
