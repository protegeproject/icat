package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protegex.owl.model.OWLModel;

public class ICDProposalFactory {

	
	public static AddContentProposal createAddContentProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new AddContentProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static EditContentProposal createEditContentProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new EditContentProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static DeleteContentProposal createDeleteContentProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new DeleteContentProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
}
