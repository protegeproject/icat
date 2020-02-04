package edu.stanford.bmir.protege.web.server.icd.proposals.postCoord;

import edu.stanford.bmir.protege.web.server.icd.proposals.ICDProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportProposalsUtil;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportResult;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportRowStatus;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public abstract class PostcoordinationProposal extends ICDProposal {

	public PostcoordinationProposal(OWLModel owlModel, String contributionId, String contributableId, String entityId,
			String entityPublicId, String contributorFullName, String entryDateTime, String status, String rationale,
			String proposalType, String proposalGroupId, String url, String propertyId, String oldValue,
			String newValue, String idFromValueSet, String valueSetName) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}
	
	
	public boolean checkPropertyIsValidPostCoordinationAxis(ImportResult importResult) {
		boolean isValidPCAxis = ImportProposalsUtil.getLookupUtil(getICDContentModel()).isPostCoordinationAxis(getPropertyId());
		if (isValidPCAxis == false) {
			importResult.recordResult(getContributionId(), "Property " + getPropertyId() +" is not a valid post-coordination axis.", ImportRowStatus.FAIL);
		}
		return isValidPCAxis;
	}
	
	public boolean checkContributableIdIsValidLinearizationViewl(ImportResult importResult) {
		String contributableId = getContributableId();
		if (contributableId == null || contributableId.isEmpty()) {
			importResult.recordResult(getContributionId(), "contributableId is empty.", ImportRowStatus.FAIL);
			return false;
		}
		else {
			OWLIndividual linView = getOwlModel().getOWLIndividual(contributableId);
			if (linView == null || ! linView.hasRDFType( getICDContentModel().getLinearizationViewClass(), true)) {
				importResult.recordResult(getContributionId(), "Contributable id is " + contributableId + ". It is expected to be a valid linearization view instance.", ImportRowStatus.FAIL);
				return false;
			}
		}
		return true;
	}	
	
	public boolean checkNewValueIsValidPostCoordinationAxisProperty(ImportResult importResult) {
		String newValue = this.getNewValue();
		if (newValue == null) {
			importResult.recordResult(getContributionId(), "New value is null. Expected a valid post-coordination axis property name.", ImportRowStatus.FAIL);
			return false;
		}
		else {
			boolean isValidPCAxis = ImportProposalsUtil.getLookupUtil(getICDContentModel()).isPostCoordinationAxis(newValue);
			if (isValidPCAxis == false) {
				importResult.recordResult(getContributionId(), "New Value " + newValue +" is not a valid post-coordination axis.", ImportRowStatus.FAIL);
				return false;
			}
		}
		return true;
	}
	

}
