package edu.stanford.bmir.protege.web.server.icd.proposals.logDef;

import edu.stanford.bmir.protege.web.server.icd.proposals.ICDProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportProposalsUtil;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportResult;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportRowStatus;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class DeleteLogicalDefinitionProposal extends LogicalDefinitionProposal {

	public DeleteLogicalDefinitionProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime, String status,
			String rationale, String proposalType, String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet, String valueSetName, boolean isDefining) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, 
				idFromValueSet, valueSetName, isDefining);
	}

	@Override
	protected void importThis(ImportResult importResult) {
		boolean res = getLogicalDefinitionUtil().deleteLogicalDefinition(getEntityCls(), getLogDefParent(),
				getProperty(), getOldFillerResource());
		if (res == false) {
			importResult.recordResult(getContributionId(), "Could not delete logical definition for: " + getEntityId() +
						". Logical definition not found.", ImportRowStatus.FAIL);
		}
	}

	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Deleted ");
		buffer.append(isDefining() == false ? "non-defining " : "");
		buffer.append("logical definition: ");
		buffer.append(ImportProposalsUtil.getEntityTitle(getICDContentModel(), getLogDefParent()));
		buffer.append(" and ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append("=");
		buffer.append(ImportProposalsUtil.getEntityTitle(getICDContentModel(), getOldValueEntity()));
		
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
				checkOldValueIsARDFResource(importResult) && //checks also that it exists 
				checkLogicalDefExists(getOldFillerResource(), importResult);
	}

}
