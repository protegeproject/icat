package edu.stanford.bmir.protege.web.client.ui.openid.model;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.protege.web.client.ui.openid.listener.AddNewFBProfileListener;

/**
 * Manages Listener for associating Facebook account to Webprotege account.
 * 
 * @author z.khan
 * 
 */
public class AddNewFBProfileEventManager {

    private static AddNewFBProfileEventManager addNewFBProfileEventManager;

    private Collection<AddNewFBProfileListener> addNewFBProfileListeners = new ArrayList<AddNewFBProfileListener>();

    /**
     * Constructor
     */
    private AddNewFBProfileEventManager() {

    }

    /**
     * Returns Instance of getAddNewFBProfileEventManager
     * 
     * @return
     */
    public static AddNewFBProfileEventManager getAddNewFBProfileEventManager() {
        if (addNewFBProfileEventManager == null) {
            addNewFBProfileEventManager = new AddNewFBProfileEventManager();
        }
        return addNewFBProfileEventManager;
    }

    /**
     * Sets this listener which will receive event when user loggs in with
     * Facebook account
     * 
     * @param listener
     */
    public void setFBProfileListener(AddNewFBProfileListener listener) {
        addNewFBProfileListeners.clear();
        addNewFBProfileListeners.add(listener);
    }

    /**
     * Notifies the listeners that user has logged in with Facebook account and
     * data is saved in session.
     * 
     * @param showShareLink
     * @param currentSelectedProject
     */
    public void notifyAddNewFBProfileListener() {
        for (AddNewFBProfileListener listener : addNewFBProfileListeners) {
            listener.assocFacebookProfile();
        }

    }
}
