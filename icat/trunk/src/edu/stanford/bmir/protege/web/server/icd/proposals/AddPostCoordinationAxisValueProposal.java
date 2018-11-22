package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.List;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AddPostCoordinationAxisValueProposal extends ICDProposal {

	public AddPostCoordinationAxisValueProposal(OWLModel owlModel, String contributionId, String contributableId, String entityId,
			String entityPublicId, String contributorFullName, String entryDateTime, String status, String rationale,
			String proposalType, String proposalGroupId, String url, String propertyId, String oldValue,
			String newValue, String idFromValueSet, String valueSetName) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
	}


	@Override
	public void importThis(ImportResult importResult) {
		ICDContentModel cm = getICDContentModel();
		RDFResource value = getOwlModel().getRDFResource(getNewValue());
		OWLProperty property = getOwlModel().getOWLProperty(getPropertyId());

		RDFSNamedClass icdCat = cm.getICDClass(getEntityId());
		
		List<RDFResource> ranges = (List<RDFResource>) property.getUnionRangeClasses();
		OWLClass range = null;
		if (ranges == null || ranges.isEmpty()) {
			//TODO warning , give up
		}
		else {
			range = (OWLClass) ranges.iterator().next();
			//TODO check for correct type: cm.getPostcoordinationValueReferenceClass()
		}
//		RDFResource valueRefInst = ((RDFSNamedClass) range).createInstance(null);
//		if ( valueRefInst.hasRDFType(cm.getPostcoordinationValueReferenceClass(), true) ) {
//			//fill this or delete test
//		}
//		
//		RDFSNamedClass pcValueRefClass = cm.getPostcoordinationValueReferenceClass();
//		RDFProperty referencedValueProperty = cm.getReferencedValueProperty();
//		
       	

		String refTermClsName = range.getName();
		OWLNamedClass refTermCls = getOwlModel().getOWLNamedClass(refTermClsName); //TODO: make sure range is not null
		if (refTermCls == null) {
			Log.getLogger().warning("Could not find term reference class: " + refTermClsName);
			return;
		}

		RDFResource refTerm = cm.createTerm(refTermCls);
		refTerm.addPropertyValue(cm.getReferencedValueProperty(), value);

		//TODO create new pc reference value
       	icdCat.addPropertyValue(property, refTerm);
	}

	
	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Added ");
		buffer.append("property value for ");
		buffer.append(getEntity().getName() + "(" + getEntity().getBrowserText() + "). ");
		buffer.append("<br /><br />");
		buffer.append("property: <i>");
		buffer.append(getPropertyId());
		buffer.append("</i><br /><br />");
		buffer.append("value: <i>");
		buffer.append(this.getNewValue());
		buffer.append("</i><br /><br />");
		buffer.append(getHtmlUrl());
		
		return buffer.toString();
	}


	@Override
	protected boolean checkData(ImportResult importResult) {
		//order of checking is important, don't change
		return checkNewValueNotEmpty(importResult) &&	//TODO if Action.DELETE check for oldValue() not empty. Best, split the implementation in two separate classes and get rid of this
				checkEntityExists(importResult) &&
				checkPropertyExists(importResult);
	}

}
