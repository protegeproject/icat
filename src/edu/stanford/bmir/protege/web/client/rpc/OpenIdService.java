package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.OpenIdData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * A service for creating , associating and maintaining Open Id
 * 
 * @author z.khan
 */
@RemoteServiceRelativePath("../openid")
public interface OpenIdService extends RemoteService {

    /**
     * This function associates Open Id URL to a existing WebProtege account Via
     * Encryption.
     * <P>
     * After user signs through Open Id and Open Id URL is not associated with
     * any WebProtege account,<br>
     * then user is enquired about existence of WebProtege account, <br>
     * if account exits then user name and password is enquired <br>
     * and is provided as a parameter to this method, with password encrypted. <br>
     * After validation, the Open Id URL is associated with the validated
     * account.
     * 
     * @param name
     *            the user id of existing WebProtege account
     * @param encryptedPassword
     *            encrypted password
     * @return UserData object with user id of validated WebProtege account
     */
    UserData validateUserToAssocOpenIdWithEncrypt(String name, String encryptedPassword);

    /**
     * This function creates a new WebProtege account and associates Open Id URL
     * to it via Encryption.
     * <P>
     * After user signs through Open Id and Open Id URL is not associated with
     * any WebProtege account,<br>
     * then user is enquired about existence of WebProtege account, <br>
     * if account doesn't exits and users opts to create a new WebProtege
     * account<br>
     * then information for new WebProtege account is enquired from user <br>
     * and the information is provided as a parameter to this method, with
     * password encrypted. <br>
     * After account creation, the Open Id URL is associated with the new
     * created WebProtege account.
     * 
     * @param name
     * @param hashedPassword
     * @param emailId
     * @return
     */
    UserData regUserToAssocOpenIdWithEncrption(String name, String hashedPassword, String emailId);

    /**
     * This function returns Webprotege account's associated Open Id URL's and
     * their respective email id and provider name.
     * 
     * @param name
     *            user id of WebProtege account
     * @return OpenIdData object, containing lists of Open Id URL's and their
     *         respective email id and provider name.
     */
    OpenIdData getUsersOpenId(String name);

    /**
     * This function removes the association between the Webprotege account and
     * the given Open Id URL
     * 
     * @param name
     *            the WebProtege account to remove association from
     * @param openId
     *            the Open Id URL to remove
     * @return
     */
    OpenIdData removeAssocToOpenId(String name, String openId);

    /**
     * This function associates a new OpenId URL to an existing WebProtege
     * account.
     * <P>
     * When Open Id account is authenticated, the Open Id information is stored
     * in the session as
     * <ul>
     * <li>Open Id URL is stored as
     * <code>OpenIdConstants.HTTPSESSION_OPENID_URL</code></li>
     * <li>Open Id email is stored as
     * <code>OpenIdConstants.HTTPSESSION_OPENID_ID</code></li>
     * <li>Open Id Provider name is stored as
     * <code>OpenIdConstants.HTTPSESSION_OPENID_PROVIDER</code></li>
     * </ul>
     * 
     * This function uses the above previously stored values to associate Open
     * Id <br>
     * with the WebProtege account, identified by parameter <code>name</code>
     * 
     * @param name
     * @return OpenIdData object containing updated list of Open Id's associated
     *         with the WebProtege account
     */
    OpenIdData assocNewOpenIdToUser(String name);

    /**
     * This function checks whether User was Validated by Open Id provider <br>
     * to associate Open Id with the user's WebProtege account <br>
     * by verifying the following Open Id information stored in session,
     * <ul>
     * <li>Open Id URL information in
     * <code>OpenIdConstants.HTTPSESSION_OPENID_URL</code></li>
     * <li>Open Id email information in
     * <code>OpenIdConstants.HTTPSESSION_OPENID_ID</code></li>
     * <li>Open Id Provider name information in
     * <code>OpenIdConstants.HTTPSESSION_OPENID_PROVIDER</code></li>
     * </ul>
     * It also checks whether this Open Id is already associated with another
     * WebProtege account.
     * <ul>
     * <li>If its associated with any other WebProtege account then the
     * associated Webprotege account name is returned in UserData object and
     * client side informs the user that the Open Id cannot be associated with
     * user's account, since its already associated.</li>
     * </ul>
     * 
     * @return UserData, contains User ID of WebProtege account already
     *         associated with Open Id else User Id id null
     */
    UserData isOpenIdInSessForAddNewOpenId();
    
    UserData isFacebookProfileInSessForAddNewOpenId();

    /**
     * This function checks whether User was Validated by Open Id provider, <br>
     * for Login, by verifying the following Open Id information stored in
     * session, <br>
     * And if validated checks if there is WebProtege account associate with
     * that Open Id<br>
     * <ul>
     * <li>Open Id URL information in
     * <code>OpenIdConstants.HTTPSESSION_OPENID_URL</code></li>
     * <li>Open Id email information in
     * <code>OpenIdConstants.HTTPSESSION_OPENID_ID</code></li>
     * <li>Open Id Provider name information in
     * <code>OpenIdConstants.HTTPSESSION_OPENID_PROVIDER</code></li>
     * </ul>
     * 
     * If Open Id is associated with a WebProtege account then the associated
     * Webprotege account name is returned in UserData object and client side
     * can login with that associated WebProtege account.
     * 
     * @return
     */
    UserData checkIfOpenIdInSessionForLogin();

    /**
     * This function clears session attribute
     * <code>OpenIdConstants.CREATED_USER_TO_ASSOC_OPEN_ID</code>,<br>
     * that stores the name of new created WebProtege account.
     * <p>
     * This function is used when property <code>login.with.https</code> in
     * protege.properties is true
     */
    void clearCreateUserToAssocOpenIdSessData();

    /**
     * This function checks the session attribute
     * <code>OpenIdConstants.CREATED_USER_TO_ASSOC_OPEN_ID</code> <br>
     * to determine whether the a new WebProtege account was created <br>
     * and returns the user id of the newly created WebProtege account.
     * <p>
     * This function is used when property <code>login.with.https</code> in
     * protege.properties is true
     * 
     * @return user Id of newly created WebProtege account or null if not
     *         created.
     */
    String checkIfUserCreatedToAssocOpenId();

    /**
     * This function clears session attribute
     * <code>OpenIdConstants.AUTHENTICATED_USER_TO_ASSOC_OPEN_ID</code>,<br>
     * that will store the name of authenticated WebProtege account <br>
     * to associate with Open Id.
     * <p>
     * This function is used when property <code>login.with.https</code> in
     * protege.properties is true
     */
    void clearAuthUserToAssocOpenIdSessData();

    /**
     * This function checks the session attribute
     * <code>OpenIdConstants.AUTHENTICATED_USER_TO_ASSOC_OPEN_ID</code> <br>
     * to determine whether the WebProtege account was authenticated <br>
     * and returns the user id of the authenticated WebProtege account.
     * <p>
     * This function is used when property <code>login.with.https</code> in
     * protege.properties is true
     * 
     * @return user Id of newly created WebProtege account or null if not
     *         created.
     */
    String checkIfUserAuthenticatedToAssocOpenId();
    
    /**Retrieves the API key for Webprotege application on Facebook 
     * @return
     */
    String getFacebookAPIKey();
   
}
