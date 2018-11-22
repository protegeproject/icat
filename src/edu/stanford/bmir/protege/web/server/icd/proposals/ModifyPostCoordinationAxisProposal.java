package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * An abstract proposal class to add/remove allowed or required postcoordination axis on an existing class.
 * The axis is specified by the name of the specific postcoordinationAxis property 
 * (e.g. "http://who.int/icd#severity") in the "new value" column. 
 * 
 * @author csnyulas
 *
 */
public abstract class ModifyPostCoordinationAxisProposal extends ICDProposal {

	protected enum Action { ADD, REMOVE };

	
	public ModifyPostCoordinationAxisProposal(OWLModel owlModel, String contributionId, String contributableId,
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
		ICDContentModel cm = getICDContentModel();
		OWLProperty postCoordProperty = getOwlModel().getOWLProperty(getNewValue());

        RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
        RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

        if (allowedPostCoordinationAxisPropertyProperty == null ||
        		requiredPostCoordinationAxisPropertyProperty == null) {
        	throw new RuntimeException("Invalid content model! The following properties could not be retrieved:" +
        		(allowedPostCoordinationAxisPropertyProperty == null ? " " + WHOFICContentModelConstants.ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP : "") +
        		(requiredPostCoordinationAxisPropertyProperty == null ? " " + WHOFICContentModelConstants.REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP : "") );
        }

		RDFResource pcSpec = getLinearizationSpecification(getContributableId());
		
        Collection<?> allowedPcAxes = pcSpec.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
        Collection<?> requiredPcAxes = pcSpec.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

        if (requiredPcAxes.contains(postCoordProperty)) {
        	pcSpec.removePropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
        }
        if (allowedPcAxes.contains(postCoordProperty)) {
        	pcSpec.removePropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
        }

        if (getAction() == Action.ADD) {
	        if (isRequiredFlag()) {
	        	pcSpec.addPropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
	        }
	        else {
	        	pcSpec.addPropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
	        }
        }
		
	}

	
	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		if (getAction() == Action.ADD) {
			buffer.append("Added " + (isRequiredFlag() ? "required " : "allowed " ));
		}
		else {
			buffer.append("Removed ");
		}
		buffer.append("post-coordination axis to");
		buffer.append(getEntity().getName() + "(" + getEntity().getBrowserText() + "). ");
		buffer.append("<br /><br />");
		buffer.append("Post-coordination property is: <i>");
		buffer.append(getNewValue());
		buffer.append("</i><br /><br />");
		buffer.append(getHtmlUrl());
		
		return buffer.toString();
	}


	@Override
	protected boolean checkData(ImportResult importResult) {
		//order of checking is important, don't change
		return checkNewValueIsValidPostCoordinationAxisProperty(importResult) &&
				checkEntityExists(importResult) &&
				checkContributableIdIsValidLinearizationViewl(importResult);
	}

//	@Override
	public RDFResource getLinearizationSpecification(String linearizationViewName) {
		ICDContentModel cm = getICDContentModel();
		OWLIndividual linViewInd = getOwlModel().getOWLIndividual(linearizationViewName);
		RDFSNamedClass icdCat = cm.getICDCategory(getEntityId());
		Collection<RDFResource> pcSpecifications = cm.getAllowedPostcoorcdinationSpecifications(icdCat); //TODO refactor the name of this method to fix typo
		for (RDFResource pcSpecification : pcSpecifications) {
			RDFResource linearization = (RDFResource) pcSpecification.getPropertyValue(cm.getLinearizationViewProperty());
			if (linViewInd.equals(linearization)) {
				return pcSpecification;
			}
		}
		Log.getLogger().log(Level.WARNING,  "Didn't find linearization specification for linearization view: " + linearizationViewName);
		return null;

	}

	protected abstract Action getAction();

	protected abstract boolean isRequiredFlag();

}
