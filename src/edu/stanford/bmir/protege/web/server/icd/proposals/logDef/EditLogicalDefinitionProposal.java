package edu.stanford.bmir.protege.web.server.icd.proposals.logDef;

import edu.stanford.bmir.protege.web.server.icd.proposals.ICDProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportProposalsUtil;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportResult;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportRowStatus;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * The editing of a logical definition works only with editing the filler:
 * the old value (i.e., old filler) will be replaced with the new value (i.e., new filler).
 * 
 * The old OWL intersection class containing the old filler will be deleted, and a 
 * new OWL intersection class with the new filler will be created.
 * 
 * @author ttania
 *
 */
public class EditLogicalDefinitionProposal extends LogicalDefinitionProposal {

	public EditLogicalDefinitionProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime, String status,
			String rationale, String proposalType, String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet, String valueSetName, boolean isDefining) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, 
				idFromValueSet, valueSetName, isDefining);
	}

	@Override
	protected void importThis(ImportResult importResult) {
		boolean res = getLogicalDefinitionUtil().editLogicalDefinition(getEntityCls(), getLogDefParent(),
				getProperty(), getOldFillerResource(), getNewFillerResource(), 
				ICDPostCoordinationMaps.isHasValueRestriction(getPropertyId()), isDefining());
		
		if (res == false) {
			importResult.recordResult(getContributionId(), "Could not edit logical definition for: " + getEntityId() +
						". Old logical definition not found.", ImportRowStatus.FAIL);
		}
	}

	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Edited ");
		buffer.append(isDefining() == false ? "non-defining " : "");
		buffer.append("logical definition: ");
		buffer.append(ImportProposalsUtil.getEntityTitle(getICDContentModel(), getLogDefParent()));
		buffer.append(" and ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append(" Old value: ");
		buffer.append(ImportProposalsUtil.getEntityTitle(getICDContentModel(), getOldValueEntity()));
		buffer.append(" New value: ");
		buffer.append(ImportProposalsUtil.getEntityTitle(getICDContentModel(), getNewValueEntity()));
		
		String htmlUrl = getHtmlUrl();
		if (htmlUrl.length() > 0) {
			buffer.append("<br /><br />");
			buffer.append(getHtmlUrl());
		}
		
		return buffer.toString();
	}

	@Override
	protected boolean checkData(ImportResult importResult) {
		return super.checkData(importResult) && 
				checkOldValueIsARDFResource(importResult) && //checks also for existance
				checkNewValueIsARDFResource(importResult) && //checks also for existance
				checkLogicalDefExists(getOldFillerResource(), importResult) && 
				checkLogicalDefDoesNotExist(getNewFillerResource(), importResult);
	}

}
