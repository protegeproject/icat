package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
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
		buffer.append(TextUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append(", Deleted value:");
		buffer.append(this.getOldValue());
		buffer.append(". See the full proposal in the ICD browser: ");
		buffer.append(this.getUrl());

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

	
	private boolean checkOldValueExists(ImportResult importResult) {
		String oldValue = this.getOldValue();
				
		RDFResource entity = getEntity();
		RDFProperty prop = getProperty();
		
		boolean exists = false;
		
		if (prop instanceof OWLObjectProperty) {
			RDFResource contributableEntity = getOwlModel().getRDFResource(this.getContributableId());
			if (entity.hasPropertyValue(prop, contributableEntity) == true) {
				RDFProperty labelProp = getOwlModel().getRDFProperty(WHOFICContentModelConstants.LABEL_PROP);
				String label = (String) contributableEntity.getPropertyValue(labelProp);
				if (oldValue != null && oldValue.equals(label)) {
					exists = true;
				}
				importResult.recordResult(this.getContributionId(), "The label of the contributable id does not match the oldValue.", ImportRowStatus.FAIL);
			}
			importResult.recordResult(this.getContributionId(), "The entity does not have the contributable id as its reified value.", ImportRowStatus.FAIL);
			exists = false;
		} else {
			if (entity.hasPropertyValue(prop, oldValue) == false) {
				importResult.recordResult(this.getContributionId(), "The entity does not have oldValue as a value.", ImportRowStatus.FAIL);
				exists = false;
			}
			exists = true;
		}
		
		return exists;
		
	}


}
