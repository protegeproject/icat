package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.List;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protegex.owl.model.OWLClass;
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
		contributableEntity.delete();
		
		@SuppressWarnings("unchecked")
		List<RDFResource> ranges = (List<RDFResource>) prop.getUnionRangeClasses();
			
		OWLClass range = null;
		if (ranges.isEmpty()){
			range = getOwlModel().getOWLThingClass();
		}
		range = (OWLClass) ranges.iterator().next();
				
		if (range == null) {
			range = getOwlModel().getOWLThingClass();
		}
		
		RDFResource reifiedValue = range.createInstance(IDGenerator.getNextUniqueId());
				
		RDFProperty labelProp = getOwlModel().getRDFProperty(WHOFICContentModelConstants.LABEL_PROP);
		reifiedValue.addPropertyValue(labelProp, this.getNewValue());
		
		if (this.getIdFromValueSet() != null) {
			RDFProperty termIdProp = getOwlModel().getRDFProperty(WHOFICContentModelConstants.TERM_ID_PROP);
			reifiedValue.addPropertyValue(termIdProp, this.getIdFromValueSet());
		}
		
		if (this.getValueSetName() != null) {
			RDFProperty valueSetProp = getOwlModel().getRDFProperty(WHOFICContentModelConstants.ONTOLOGYID_PROP);
			reifiedValue.addPropertyValue(valueSetProp, this.getValueSetName());
		}
		
		entity.addPropertyValue(prop, reifiedValue);
		
	}

	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Edit ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
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
		return checkEntityExists(importResult) &&
				checkPropertyExists(importResult) &&				
				//contributableId cannot be empty for object properties, but can be for data properties
				(getProperty() instanceof OWLDatatypeProperty || checkContributableIdNotEmpty(importResult) ) &&  
				checkOldValueExists(importResult);
	}
	
	

}
