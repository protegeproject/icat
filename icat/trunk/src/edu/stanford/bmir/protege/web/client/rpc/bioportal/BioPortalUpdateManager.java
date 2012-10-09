package edu.stanford.bmir.protege.web.client.rpc.bioportal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class BioPortalUpdateManager {

    private static BioPortalUploadAsync proxy;
    private static BioPortalUpdateManager instance;

    private BioPortalUpdateManager() {
        proxy = (BioPortalUploadAsync) GWT.create(BioPortalUpload.class);
    }

    public static BioPortalUpdateManager getBioportalUploadManager() {
        if (instance == null) {
            instance = new BioPortalUpdateManager();
        }
        return instance;
    }

    void uploadOntologyFromURL(String bpRestBase, String downloadLocation, String displayLabel, String userId, String format,
            String dateReleased, String contactName, String contactEmail, String abreviation, String versionNumber,
            String homepage, String documentation, String publication, String viewingRestriction, String useracl,
            String description, String categories, String synonymSlot, String preferredNameSlot,
            String documentationSlot, String authorSlot, AsyncCallback<String> callback) {
        proxy.uploadOntologyFromURL(bpRestBase, downloadLocation, displayLabel, userId, format, dateReleased,
                contactName, contactEmail, abreviation, versionNumber, homepage, documentation, publication,
                viewingRestriction, useracl, description, categories, synonymSlot, preferredNameSlot, documentationSlot, authorSlot, callback);
    }

}
