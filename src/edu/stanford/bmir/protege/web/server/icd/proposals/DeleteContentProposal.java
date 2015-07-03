package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

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
		RDFResource entity = getEntity();
		RDFProperty prop = getProperty();
		
		if (prop instanceof OWLObjectProperty) {
			importReifiedValue(entity, prop);
		} else {
			importSimpleValue(entity, prop);
		}
	}
	
	
	private void importSimpleValue(RDFResource entity, RDFProperty prop) {
		entity.removePropertyValue(prop, this.getOldValue());		
	}
	

	private void importReifiedValue(RDFResource entity, RDFProperty prop) {
		RDFResource contributableEntity = getContributableEntity();
		contributableEntity.delete();
	}

	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Delete ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append("<br /><br />");
		buffer.append("Deleted value: <i>");
		buffer.append(this.getOldValue());
		buffer.append("</i><br /><br />");
		buffer.append(getHtmlUrl());

		return buffer.toString();
	}
	

	@Override
	protected boolean checkData(ImportResult importResult) {
		return checkEntityExists(importResult) &&
				checkPropertyExists(importResult) &&
				//contributableId cannot be empty for object properties, but can be for data properties
				(getProperty() instanceof OWLDatatypeProperty || checkContributableIdNotEmpty(importResult) ) &&  
				checkOldValueExists(importResult);
				
	}

	
	


}
