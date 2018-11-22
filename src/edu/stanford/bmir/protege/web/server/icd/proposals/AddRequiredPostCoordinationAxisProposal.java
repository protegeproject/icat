package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.bmir.protege.web.server.icd.proposals.ModifyPostCoordinationAxisProposal.Action;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * A proposal class to add a required postcoordination axis to an existing class.
 * The axis is specified by the name of the specific postcoordinationAxis property 
 * (e.g. "http://who.int/icd#hasSeverity") in the "new value" column. 
 * 
 * @author csnyulas
 *
 */
public class AddRequiredPostCoordinationAxisProposal extends ModifyPostCoordinationAxisProposal {

	public AddRequiredPostCoordinationAxisProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime, String status,
			String rationale, String proposalType, String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet, String valueSetName) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}

	@Override
	protected Action getAction() {
		return Action.ADD;
	}

	@Override
	protected boolean isRequiredFlag() {
		return true;
	}

}
