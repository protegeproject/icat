package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.stanford.bmir.protege.web.client.rpc.data.ApplicationPropertyDefaults;
import edu.stanford.bmir.protege.web.client.rpc.data.ApplicationPropertyNames;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;

import java.util.Map;

/**
 * Retrieves the properties from the server.
 *
 * Note that any property included in the blacklist.properties will not be sent over to the client, and thus won't be available here.
 *
 *@author Jack Elliott <jacke@stanford.edu>
 */
public class ApplicationPropertiesServiceManager {
    private static ApplicationPropertiesServiceAsync proxy;
    static ApplicationPropertiesServiceManager instance;

    private ApplicationPropertiesServiceManager() {
        proxy = (ApplicationPropertiesServiceAsync) GWT.create(ApplicationPropertiesService.class);
    }

    public static ApplicationPropertiesServiceManager getInstance() {
        if (instance == null) {
            instance = new ApplicationPropertiesServiceManager();
        }
        return instance;
    }

    public void initialize(final AsyncCallback<Map<String, String>> callback) {
        proxy.initialize(callback);
    }

}