package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Jack Elliott <jack.elliott@stanford.edu>
 */
public interface ApplicationPropertiesServiceAsync {

    void initialize(AsyncCallback<Map<String, String>> callback);
}