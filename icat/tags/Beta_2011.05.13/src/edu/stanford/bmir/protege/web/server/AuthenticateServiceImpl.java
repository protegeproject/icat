package edu.stanford.bmir.protege.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.stanford.bmir.protege.web.client.rpc.AuthenticateService;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserRegData;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.client.ui.openid.constants.OpenIdConstants;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.logging.Level;

/**
 * Service for Authenticate module for authenticating user.
 *
 * @author z.khan
 *
 */
public class AuthenticateServiceImpl extends RemoteServiceServlet implements AuthenticateService {

    private static final long serialVersionUID = 5326582825556868383L;

    private boolean isAuthenticateWithOpenId() {
        return ApplicationProperties.getWebProtegeAuthenticateWithOpenId();
    }

    public UserData validateUserAndAddInSession(String name, String password) {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        session.setAttribute(AuthenticationConstants.LOGIN_METHOD,
                AuthenticationConstants.LOGIN_METHOD_WEBPROTEGE_ACCOUNT);
        if (!Protege3ProjectManager.getProjectManager().getMetaProjectManager().hasValidCredentials(name, password)) {
            session.setAttribute(AuthenticationConstants.USERDATA_OBJECT, null);
            return null;
        }
        UserData userData = new UserData(name, password);
        session.setAttribute(AuthenticationConstants.USERDATA_OBJECT, userData);
        return userData;
    }

    public UserData validateUser(String name, String password) {
        if (!Protege3ProjectManager.getProjectManager().getMetaProjectManager().hasValidCredentials(name, password)) {
            return null;
        }
        Log.getLogger().info("User " + name + " logged in at: " + new Date());
        return new UserData(name, password);
    }

    public void changePassword(String userName, String password) {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().changePassword(userName, password);
    }

    public UserRegData registerUserToAssociateOpenId(String userName, String password, String emailId) {
        UserRegData userRegData = new UserRegData();
        if (isAuthenticateWithOpenId()) {
            HttpServletRequest request = this.getThreadLocalRequest();
            HttpSession session = request.getSession();
            String userOpenId = (String) session.getAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL);
            String openIdAccName = (String) session.getAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID);
            String openIdProvider = (String) session.getAttribute(OpenIdConstants.HTTPSESSION_OPENID_PROVIDER);
            if (userOpenId == null) {
                userRegData.setResult(OpenIdConstants.REGISTER_USER_ERROR);
                return userRegData;
            }
            UserData userData = Protege3ProjectManager.getProjectManager().getMetaProjectManager().registerUser(userName, password);
            if (userData == null) {
                userRegData.setResult(OpenIdConstants.USER_ALREADY_EXISTS);
                return userRegData;
            }
            userRegData.setName(userName);
            User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(userName);

            String openIdPropBase = OpenIdConstants.OPENID_PROPERTY_PREFIX;

            for (int index = 1;; index++) {
                String opnId = user.getPropertyValue(openIdPropBase + index
                        + OpenIdConstants.OPENID_PROPERTY_URL_SUFFIX);
                if (opnId == null) {
                    user.addPropertyValue(openIdPropBase + index + OpenIdConstants.OPENID_PROPERTY_URL_SUFFIX,
                            userOpenId);
                    user.addPropertyValue(openIdPropBase + index + OpenIdConstants.OPENID_PROPERTY_ID_SUFFIX,
                            openIdAccName);
                    user.addPropertyValue(openIdPropBase + index + OpenIdConstants.OPENID_PROPERTY_PROVIDER_SUFFIX,
                            openIdProvider);
                    break;
                }
            }

            user.setEmail(emailId);
            Log.getLogger().info("User " + userName + " created at: " + new Date() + " with OpenId: " + userOpenId);
            session.setAttribute(OpenIdConstants.CREATED_USER_TO_ASSOC_OPEN_ID, userName);
            session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL, null);
            session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID, null);
            session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_PROVIDER, null);
            userRegData.setResult(OpenIdConstants.REGISTER_USER_SUCCESS);

            session.setAttribute(AuthenticationConstants.USERDATA_OBJECT, userData);
        }
        return userRegData;
    }

    public UserData validateUserToAssociateOpenId(String userName, String password) {
        if (isAuthenticateWithOpenId()) {
            try {
                if (!Protege3ProjectManager.getProjectManager().getMetaProjectManager().hasValidCredentials(userName, password)) {
                    return null;
                }
                HttpServletRequest request = this.getThreadLocalRequest();
                HttpSession session = request.getSession();
                String userOpenId = (String) session.getAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL);
                String openIdAccName = (String) session.getAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID);
                String openIdProvider = (String) session.getAttribute(OpenIdConstants.HTTPSESSION_OPENID_PROVIDER);

                if (userOpenId == null) {
                    return null;
                }

                User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(
                        userName);

                String openIdPropBase = OpenIdConstants.OPENID_PROPERTY_PREFIX;

                for (int index = 1;; index++) {
                    String opnId = user.getPropertyValue(openIdPropBase + index
                            + OpenIdConstants.OPENID_PROPERTY_URL_SUFFIX);
                    if (opnId == null) {
                        user.addPropertyValue(openIdPropBase + index + OpenIdConstants.OPENID_PROPERTY_URL_SUFFIX,
                                userOpenId);
                        user.addPropertyValue(openIdPropBase + index + OpenIdConstants.OPENID_PROPERTY_ID_SUFFIX,
                                openIdAccName);
                        user.addPropertyValue(openIdPropBase + index + OpenIdConstants.OPENID_PROPERTY_PROVIDER_SUFFIX,
                                openIdProvider);

                        break;
                    }
                }

                Log.getLogger().info(
                        "User " + userName + " logged in at: " + new Date() + " with OpenId: " + userOpenId);
                session.setAttribute(OpenIdConstants.AUTHENTICATED_USER_TO_ASSOC_OPEN_ID, userName);
                session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL, null);
                session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID, null);
                session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_PROVIDER, null);
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Exception in validateUserToAssociateOpenId", e);
            }
        }
        return new UserData(userName, password);

    }

    public void sendPasswordReminder(String userName) {
        String email = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
        if (email == null) {
            throw new IllegalArgumentException("User " + userName + " does not have an email configured.");
        }
        changePassword(userName, EmailConstants.RESET_PASSWORD);
        EmailUtil.sendEmail(email, EmailConstants.FORGOT_PASSWORD_SUBJECT, EmailConstants.FORGOT_PASSWORD_EMAIL_BODY,
                ApplicationProperties.getEmailAccount());
    }

    public UserData registerUser(String userName, String password) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().registerUser(userName, password);
    }
}