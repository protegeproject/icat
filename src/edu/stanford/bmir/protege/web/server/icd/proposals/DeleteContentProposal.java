package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protegex.owl.model.OWLModel;

public class DeleteContentProposal extends ICDProposal {

	public DeleteContentProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName,
				entryDateTime, status, rationale, proposalType,
				proposalGroupId, url, propertyId, oldValue, newValue,
				idFromValueSet, valueSetName);
	}

	@Override
	public void importThis(ImportResult importResult) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getTransactionDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean checkData(ImportResult importResult) {
		// TODO Auto-generated method stub
		return false;
	}
	
	

}
