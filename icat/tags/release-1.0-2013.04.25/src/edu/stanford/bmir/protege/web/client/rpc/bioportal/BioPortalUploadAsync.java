package edu.stanford.bmir.protege.web.client.rpc.bioportal;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface BioPortalUploadAsync {

    void uploadOntologyFromURL(String bpRestBase, String virtualOntologyId, String downloadLocation, String displayLabel, String userId, String format,
            String dateReleased, String contactName, String contactEmail, String abreviation, String versionNumber,
            String homepage, String documentation, String publication, String viewingRestriction, String useracl,
            String description, String categories, String synonymSlot, String preferredNameSlot,
            String documentationSlot, String authorSlot, AsyncCallback<String> callback);

}
