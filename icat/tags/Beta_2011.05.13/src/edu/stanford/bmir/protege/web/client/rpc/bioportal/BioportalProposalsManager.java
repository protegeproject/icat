package edu.stanford.bmir.protege.web.client.rpc.bioportal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BioportalProposalsManager {

    private static BioportalProposalsAsync proxy;
    private static BioportalProposalsManager instance;

    private BioportalProposalsManager() { 
        proxy = (BioportalProposalsAsync) GWT.create(BioportalProposals.class);
    }
    
    public static BioportalProposalsManager getBioportalProposalsManager() {
        if (instance == null) {
            instance = new BioportalProposalsManager();
        }
        return instance;
    }
    
    public  void getBioportalProposals(String projectName, String entityURI, AsyncCallback<String> callback) {
        proxy.getBioportalProposals(projectName, entityURI, callback);
    }
    
}
