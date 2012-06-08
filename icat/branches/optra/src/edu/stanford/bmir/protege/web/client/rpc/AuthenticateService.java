package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * This service is used when rpc call is to be made through module Authenticate <br>
 * (i.e when the browser popup runs with SSL and Authenticate module is loaded
 * in it. )
 * 
 * @author z.khan
 */
@RemoteServiceRelativePath("../authen")
public interface AuthenticateService extends RemoteService {

    //related to openid
    /**
     * @return the value of property
     *         <code>webprotege.authenticate.with.openid</code> from
     *         protege.properties.
     */
    boolean isAuthenticateWithOpenId();

    //related to webprotege account login
    UserData validateUserAndAddInSession(String name, String password);

    UserData validateUser(String name, String password);

    void changePassword(String userName, String password);

    /**
     * This function creates a new WebProtege account <br>
     * and associates it with the Open Id.
     * <p>
     * The Open Id information is retrieved from the following session
     * attributes
     * <ul>
     * <li>Open Id URL information from
     * <code>OpenIdConstants.HTTPSESSION_OPENID_URL</code></li>
     * <li>Open Id email information from
     * <code>OpenIdConstants.HTTPSESSION_OPENID_ID</code></li>
     * <li>Open Id Provider name information from
     * <code>OpenIdConstants.HTTPSESSION_OPENID_PROVIDER</code></li>
     * </ul>
     * 
     * @param name
     *            the user id to assign to new WebProtege account
     * @param password
     *            the password to assign to new WebProtege account
     * @param emailId
     *            the email id to assign to new WebProtege account
     * @return UserData containing information about WebProtege account which
     *         was created and associated with Open Id
     */
    UserData registerUserToAssociateOpenId(String name, String password, String emailId);

    /**
     * This function authenticates the user<br>
     * and associates it with the Open Id.
     * <p>
     * The Open Id information is retrieved from the following session
     * attributes
     * <ul>
     * <li>Open Id URL information from
     * <code>OpenIdConstants.HTTPSESSION_OPENID_URL</code></li>
     * <li>Open Id email information from
     * <code>OpenIdConstants.HTTPSESSION_OPENID_ID</code></li>
     * <li>Open Id Provider name information from
     * <code>OpenIdConstants.HTTPSESSION_OPENID_PROVIDER</code></li>
     * </ul>
     * 
     * @param name
     *            the user id of WebProtege account to authenticate
     * @param password
     *            the password of WebProtege account to authenticate
     * @return UserData containing information about WebProtege account which
     *         was authenticated and associated with Open Id
     */
    UserData validateUserToAssociateOpenId(String name, String password);

    void sendPasswordReminder(String userName);

    UserData registerUser(String name, String password);
    
    /** This function adds Facebook Users details(after authentication) like profile url,
     *  Provider(Facebook) and user id to session attribute used by open Id method, so that 
     *  the OpenId login mechanism can be used( instead of creating new flow). 
     * @return
     */
    boolean addFacebookUserDetailsToSession(String provider, String profileUrl, String userId,String loginMethod);
    
    /**Retrieves the API key for Webprotege application on Facebook 
     * @return
     */
    String getFacebookAPIKey();
}
