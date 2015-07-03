package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

/**
 * A proposal class to add values to a multiple cardinality property.
 * The values are usually reified, but this class also treats the case of simple values. 
 * 
 * @author ttania
 *
 */
public class AddContentProposal extends ICDProposal {

	public AddContentProposal(OWLModel owlModel, String contributionId, String contributableId,
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
		entity.addPropertyValue(prop, getNewValue());
	}


	private void importReifiedValue(RDFResource entity, RDFProperty prop) {
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
		buffer.append("Added ");
		buffer.append(ImportProposalsUtil.getPropertyName(getOwlModel(), getPropertyId()));
		buffer.append("New value: ");
		buffer.append(this.getNewValue());
		if (this.getIdFromValueSet() != null) {
			buffer.append(" (");
			buffer.append(this.getIdFromValueSet());
			buffer.append(", ");
			buffer.append(this.getValueSetName());
			buffer.append(") ");
		}
		buffer.append("See the full proposal in the ICD browser: ");
		buffer.append(this.getUrl());
		
		return buffer.toString();
	}


	@Override
	protected boolean checkData(ImportResult importResult) {
		//order of checking is important, don't change
		return checkNewValueNotEmpty(importResult) &&
				checkEntityExists(importResult) &&
				checkPropertyExists(importResult) &&				
				checkValueNotExists(importResult);
	}

	
	@SuppressWarnings("rawtypes")
	private boolean checkValueNotExists(ImportResult importResult) {
		String newValue = this.getNewValue();
		String idfromVS = this.getIdFromValueSet();
				
		RDFResource entity = getOwlModel().getRDFResource(this.getEntityId());		
		RDFProperty prop = getOwlModel().getRDFProperty(getPropertyId());
		boolean isObjectProperty = prop instanceof OWLObjectProperty;
		
		Collection values = entity.getPropertyValues(prop);
		if (values == null) {
			return true;
		}

		boolean notExists = true;
		Iterator i = values.iterator();
		while (i.hasNext() && notExists == true) {
			Object value = i.next();			
			if (isObjectProperty == true && value instanceof RDFResource) { //reified value 
				notExists = checkReifiedValueNotExists(importResult, (RDFResource) value, newValue, idfromVS);
			} else { //simple value
				notExists = checkSimpleValueNotExists(importResult, (RDFResource) value, newValue, idfromVS);
			}
		}
		
		return notExists;
	}
	
	private boolean checkSimpleValueNotExists(ImportResult importResult, Object value, String newValue, String idfromVS) {
		if (value != null && newValue.equals(value.toString())) {
			importResult.recordResult(this.getContributionId(), "Value already exists. Will not import.", ImportRowStatus.IGNORED);
			return false;
		}
		return true;
	}


	private boolean checkReifiedValueNotExists(ImportResult importResult, RDFResource value, String newValue, String idfromVS) {
		String label = (String) value.getPropertyValue(getOwlModel().getRDFProperty(WHOFICContentModelConstants.LABEL_PROP));		
		String termId = (String) value.getPropertyValue(getOwlModel().getRDFProperty(WHOFICContentModelConstants.TERM_ID_PROP));
		
		
		if (newValue.equals(label) || (idfromVS != null && idfromVS.equals(termId)) ) {
			importResult.recordResult(this.getContributionId(), "Value already exists. Will not import.", ImportRowStatus.IGNORED);
			return false;
		}
		return true;
	}
	

}
