package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.bmir.protege.web.server.WebProtegeKBUtil;
import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

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
		RDFResource entity = getEntity();
		RDFProperty prop = getProperty();
		
		if (prop instanceof OWLObjectProperty) {
			importReifiedValue(entity, prop);
		} else {
			importSimpleValue(entity, prop);
		}
	}
	
	
	private void importSimpleValue(RDFResource entity, RDFProperty prop) {
		entity.setPropertyValue(prop, this.getNewValue());
	}
	

	private void importReifiedValue(RDFResource entity, RDFProperty prop) {
		RDFResource contributableEntity = getContributableEntity();
		RDFProperty labelProp = edu.stanford.bmir.whofic.KBUtil.getRDFProperty(getOwlModel(), WHOFICContentModelConstants.LABEL_PROP);
		
		contributableEntity.setPropertyValue(labelProp, this.getNewValue());
		
		if (this.getIdFromValueSet() != null) {
			RDFProperty termIdProp = edu.stanford.bmir.whofic.KBUtil.getRDFProperty(getOwlModel(), WHOFICContentModelConstants.TERM_ID_PROP);
			contributableEntity.setPropertyValue(termIdProp, this.getIdFromValueSet());
			
			//fill in both shortid and termid, because it is not clear which one is used by different tools.. not ideal.
			RDFProperty shortTermIdProp = edu.stanford.bmir.whofic.KBUtil.getRDFProperty(getOwlModel(), WHOFICContentModelConstants.BP_SHORT_TERM_ID_PROP);
			contributableEntity.setPropertyValue(shortTermIdProp, this.getIdFromValueSet());
		}
		
		if (this.getValueSetName() != null) {
			RDFProperty valueSetProp = edu.stanford.bmir.whofic.KBUtil.getRDFProperty(getOwlModel(), WHOFICContentModelConstants.ONTOLOGYID_PROP);
			contributableEntity.setPropertyValue(valueSetProp, this.getValueSetName());
		}
	}

	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Edit ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append("<br /><br />");
		buffer.append("New value: <i>");
		buffer.append(this.getNewValue());
		String idFromValueSet = this.getIdFromValueSet();
		if (idFromValueSet != null && idFromValueSet.isEmpty() == false) {
			buffer.append(" (");
			buffer.append(this.getIdFromValueSet());
			buffer.append(", ");
			buffer.append(this.getValueSetName());
			buffer.append(")");
		}
		buffer.append("</i><br /><br />");
		buffer.append("Old value: <i>");
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
