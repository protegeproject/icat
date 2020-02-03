package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.bmir.protege.web.server.icd.proposals.postCoord.AddAllowedPostCoordinationAxisProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.postCoord.AddPostCoordinationAxisValueProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.postCoord.AddRequiredPostCoordinationAxisProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.postCoord.DeletePostCoordinationAxisValueProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.postCoord.RemoveAllowedPostCoordinationAxisProposal;
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

	public static CreateSubclassProposal createCreateSubclassProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new CreateSubclassProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static AddAllowedPostCoordinationAxisProposal createAddAllowedPostCoordinationAxisProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new AddAllowedPostCoordinationAxisProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static AddRequiredPostCoordinationAxisProposal createAddRequiredPostCoordinationAxisProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new AddRequiredPostCoordinationAxisProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static RemoveAllowedPostCoordinationAxisProposal createRemovePostCoordinationAxisProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new RemoveAllowedPostCoordinationAxisProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static AddPostCoordinationAxisValueProposal createAddPostCoordinationAxisValueProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new AddPostCoordinationAxisValueProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	public static DeletePostCoordinationAxisValueProposal createDeletePostCoordinationAxisValueProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		return new DeletePostCoordinationAxisValueProposal(owlModel, contributionId, contributableId, entityId, entityPublicId,
				contributorFullName, entryDateTime, status, rationale, proposalType, 
				proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}

	
}
