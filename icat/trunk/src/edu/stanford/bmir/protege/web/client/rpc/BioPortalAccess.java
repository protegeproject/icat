package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

@RemoteServiceRelativePath("bpaccess")
public interface BioPortalAccess extends RemoteService {

    public String getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData);

    public String getBioPortalSearchContentDetails(String projectName, BioPortalSearchData bpSearchData,
            BioPortalReferenceData bpRefData);

    public EntityData createExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
            String user, String operationDescription);

   public EntityData replaceExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
            EntityData oldValueEntityData, String user, String operationDescription);

}
