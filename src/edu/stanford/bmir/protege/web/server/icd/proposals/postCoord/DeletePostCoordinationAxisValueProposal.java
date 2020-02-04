package edu.stanford.bmir.protege.web.server.icd.proposals.postCoord;

import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.server.icd.proposals.ICDProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportResult;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class DeletePostCoordinationAxisValueProposal extends PostcoordinationProposal {

	public DeletePostCoordinationAxisValueProposal(OWLModel owlModel, String contributionId, String contributableId, String entityId,
			String entityPublicId, String contributorFullName, String entryDateTime, String status, String rationale,
			String proposalType, String proposalGroupId, String url, String propertyId, String oldValue,
			String newValue, String idFromValueSet, String valueSetName) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}


	@Override
	public void importThis(ImportResult importResult) {
		ICDContentModel cm = getICDContentModel();
//		RDFResource value = getOwlModel().getRDFResource(getNewValue());
		OWLProperty property = getOwlModel().getOWLProperty(getPropertyId());

		RDFSNamedClass icdCat = cm.getICDClass(getEntityId());

		RDFResource refTerm = getReferenceTermToBeDeleted();
		if (refTerm != null) {
			icdCat.removePropertyValue(property, refTerm);
		}
		else {
			Log.getLogger().log(Level.WARNING, "Property value " + getContributableId() + " (" + getOldValue() + ") for " + property + " on " + getEntityId() + " could not be found.");;
		}
		
	}

	
	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Removed ");
		buffer.append("property value for ");
		buffer.append(getEntity().getName() + "(" + getEntity().getBrowserText() + "). ");
		buffer.append("<br /><br />");
		buffer.append("property: <i>");
		buffer.append(getPropertyId());
		buffer.append("</i><br /><br />");
		buffer.append("value: <i>");
		buffer.append(this.getOldValue());
		buffer.append("</i><br /><br />");
		buffer.append(getHtmlUrl());
		
		return buffer.toString();
	}


	@Override
	protected boolean checkData(ImportResult importResult) {
		//order of checking is important, don't change
		return checkOldValueExists(importResult) &&	//TODO if Action.DELETE check for oldValue() not empty. Best, split the implementation in two separate classes and get rid of this
				checkEntityExists(importResult) &&
				checkPropertyExists(importResult);
	}
	
	@Override
	public boolean checkOldValueExists(ImportResult importResult) {
		//TODO implement this so as to test for existing values as referenced entities.
		//Maybe even check for contributable Id, and use that for faster find.
		return true;
	}
	
	public RDFResource getReferenceTermToBeDeleted() {
		RDFResource entity = getEntity();
		RDFProperty property = getProperty();
		RDFResource value = getOwlModel().getRDFResource(getOldValue());
		
		
		ICDContentModel cm = getICDContentModel();
		Collection<?> propertyValues = entity.getPropertyValues(property);
		if (propertyValues == null || propertyValues.isEmpty()) {
			return null;
		}
		
		RDFSNamedClass pcValueRefClass = cm.getPostcoordinationValueReferenceClass();
		RDFProperty referencedValueProperty = cm.getReferencedValueProperty();

		for (Object propValue : propertyValues) {
			if (propValue instanceof RDFIndividual && ((RDFIndividual) propValue).hasRDFType(pcValueRefClass, true)) {
				RDFIndividual valueInst = (RDFIndividual)propValue;
				if (valueInst.equals(getContributableEntity())) {
					Object referencedValue = valueInst.getPropertyValue(referencedValueProperty);
					if (!value.equals(referencedValue)) {
						Log.getLogger().log(Level.WARNING, "Old value " + getOldValue() + " is not set as referenced value on " + getContributableId());
					}
					return valueInst;
				}
				else {
					Object referencedValue = valueInst.getPropertyValue(referencedValueProperty);
					if (value.equals(referencedValue)) {
						return valueInst;
					}
				}
			}
			else {
				Log.getLogger().log(Level.WARNING, "Property value for" + property + " on " + entity + " has wrong type.");;
			}
		}
		
		return null;
	}

}
