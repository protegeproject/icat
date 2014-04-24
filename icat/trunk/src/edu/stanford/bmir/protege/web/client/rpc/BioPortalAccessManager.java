package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;


public class BioPortalAccessManager {

    private static BioPortalAccessAsync proxy;
    static BioPortalAccessManager instance;

    public static BioPortalAccessManager getInstance() {
        if (instance == null) {
            instance = new BioPortalAccessManager();
        }
        return instance;
    }

    private BioPortalAccessManager() {
        proxy = (BioPortalAccessAsync) GWT.create(BioPortalAccess.class);
    }

    public void getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData,
            AsyncCallback<String> cb) {
        proxy.getBioPortalSearchContent(projectName, entityName, bpSearchData, cb);
    }

    public void getBioPortalSearchContentDetails(String projectName, BioPortalSearchData bpSearchData,
            BioPortalReferenceData bpRefData, AsyncCallback<String> cb) {
        proxy.getBioPortalSearchContentDetails(projectName, bpSearchData, bpRefData, cb);
    }

    public void createExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
            String user, String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createExternalReference(projectName, entityName, bpRefData, user, operationDescription, cb);
    }

    public void replaceExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData, EntityData oldValueEntityData,
            String user, String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.replaceExternalReference(projectName, entityName, bpRefData, oldValueEntityData, user, operationDescription, cb);
    }


}
