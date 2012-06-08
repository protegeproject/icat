package edu.stanford.bmir.protege.web.client.rpc.bioportal;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("bpProposals")
public interface BioportalProposals extends RemoteService {

    String getBioportalProposals(String projectName, String entityURI);
}
