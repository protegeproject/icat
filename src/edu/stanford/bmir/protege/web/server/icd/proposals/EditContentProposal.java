package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * The Edit Proposal is for properties that have single cardinality, 
 * e.g., title, definition, etc. 
 * 
 * @author ttania
 *
 */
public class EditContentProposal extends ICDProposal {

	public EditContentProposal(OWLModel owlModel, String contributionId, String contributableId,
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
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Edit ");
		buffer.append(TextUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append("New value: ");
		buffer.append(this.getNewValue());
		if (this.getIdFromValueSet() != null) {
			buffer.append(" (");
			buffer.append(this.getIdFromValueSet());
			buffer.append(", ");
			buffer.append(this.getValueSetName());
			buffer.append(")");
		}
		buffer.append(", Old value:");
		buffer.append(this.getOldValue());		
		buffer.append(". See the full proposal in the ICD browser: ");
		buffer.append(this.getUrl());
		
		return buffer.toString();
	}

	@Override
	protected boolean checkData(ImportResult importResult) {
		// TODO Auto-generated method stub
		return false;
	}
	
	

}
