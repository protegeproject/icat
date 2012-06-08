package edu.stanford.bmir.protege.web.client.rpc.bioportal;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BioportalProposalsAsync {

    void getBioportalProposals(String projectName, String entityURI, AsyncCallback<String> callback);

}
