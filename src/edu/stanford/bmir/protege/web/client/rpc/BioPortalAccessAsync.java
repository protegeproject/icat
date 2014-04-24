package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;


public interface BioPortalAccessAsync {

    void getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData,
            AsyncCallback<String> cb);

    void getBioPortalSearchContentDetails(String projectName, BioPortalSearchData bpSearchData,
            BioPortalReferenceData bpRefData, AsyncCallback<String> cb);

    void createExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData, String user,
            String operationDescription, AsyncCallback<EntityData> cb);

    void replaceExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
                                  EntityData oldValueEntityData,
                                  String user, String operationDescription, AsyncCallback<EntityData> async);

}
