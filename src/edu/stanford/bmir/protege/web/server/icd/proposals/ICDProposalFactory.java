package edu.stanford.bmir.protege.web.server.icd.proposals;

public class ICDProposalFactory {

	public static ICDProposal createICDProposal(String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new ICDProposal(contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	
	public static AddContentProposal createAddContentProposal(String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new AddContentProposal(contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static EditContentProposal createEditContentProposal(String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new EditContentProposal(contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static DeleteContentProposal createDeleteContentProposal(String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new DeleteContentProposal(contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
}
