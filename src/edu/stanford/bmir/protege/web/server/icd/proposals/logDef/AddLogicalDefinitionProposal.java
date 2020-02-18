package edu.stanford.bmir.protege.web.server.icd.proposals.logDef;

import edu.stanford.bmir.protege.web.server.icd.proposals.ICDProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportProposalsUtil;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportResult;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class AddLogicalDefinitionProposal extends LogicalDefinitionProposal {

	public AddLogicalDefinitionProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime, String status,
			String rationale, String proposalType, String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet, String valueSetName, boolean isDefining) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, 
				idFromValueSet, valueSetName, isDefining);
	}

	@Override
	protected void importThis(ImportResult importResult) {
		getLogicalDefinitionUtil().createLogicalDefinition(getEntityCls(), getLogDefParent(),
				getProperty(), getNewFillerResource(), 
				ICDContentModel.isLogicalDefinitionWithHasValueRestriction(getPropertyId()), isDefining());
	}

	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Added ");
		buffer.append(isDefining() == false ? "non-defining " : "");
		buffer.append("logical definition: ");
		buffer.append(ImportProposalsUtil.getEntityTitle(getICDContentModel(), getLogDefParent()));
		buffer.append(" and ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append("=");
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
				checkNewValueIsARDFResource(importResult) &&  //also checks for null
				checkLogicalDefDoesNotExist(getNewFillerResource(), importResult);
	}

}
