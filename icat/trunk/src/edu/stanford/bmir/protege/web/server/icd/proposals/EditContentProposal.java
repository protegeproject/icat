package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protegex.owl.model.OWLModel;

public class EditContentProposal extends ICDProposal {

	public EditContentProposal(String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		super(contributionId, contributableId, entityId, entityPublicId, contributorFullName,
				entryDateTime, status, rationale, proposalType,
				proposalGroupId, url, propertyId, oldValue, newValue,
				idFromValueSet, valueSetName);
	}

	@Override
	public void importThis(OWLModel owlModel, UploadProposalsResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	

}
