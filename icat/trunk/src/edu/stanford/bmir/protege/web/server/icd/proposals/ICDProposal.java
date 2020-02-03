package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.logging.Level;

import edu.stanford.bmir.protege.web.server.KBUtil;
import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * A proposal structure for updating the iCAT content.
 * Proposals are usually sent via a web service, serialized as a CSV file.
 * There are different types of proposals (add, edit, delete). 
 * Not all the fields will be filled in for all of them.
 * 
 * @author ttania
 *
 */
public abstract class ICDProposal { 
	
	public static final String TRANSACTION_TEXT_PREFIX = 
			"<img src=\"images/import.gif\" style=\"padding-right: 5px; margin-bottom: -5px;\" /><b>Proposal import.</b> ";
	
	private transient OWLModel owlModel;
	private transient ICDContentModel icdContentModel;
	
	private String contributionId;
	private String contributableId;
	private String entityId;
	private String entityPublicId;
	private String contributorFullName;
	private String entryDateTime;
	private String status;
	private String rationale;
	private String proposalType;
	private String proposalGroupId;
	private String url;
	private String propertyId;
	private String oldValue;
	private String newValue;
	private String idFromValueSet;
	private String valueSetName;
	
		
	public ICDProposal(OWLModel owlModel, String contributionId, String contributableId,
			String entityId, String entityPublicId, String contributorFullName, String entryDateTime,
			String status, String rationale, String proposalType,
			String proposalGroupId, String url, String propertyId,
			String oldValue, String newValue, String idFromValueSet,
			String valueSetName) {
		super();
		this.owlModel = owlModel;
		this.icdContentModel = new ICDContentModel(owlModel);
		this.contributionId = contributionId;
		this.contributableId = contributableId;
		this.entityId = entityId;
		this.entityPublicId = entityPublicId;
		this.contributorFullName = contributorFullName;
		this.entryDateTime = entryDateTime;
		this.status = status;
		this.rationale = rationale;
		this.proposalType = proposalType;
		this.proposalGroupId = proposalGroupId;
		this.url = url;
		this.propertyId = propertyId;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.idFromValueSet = idFromValueSet;
		this.valueSetName = valueSetName;
	}

	
	/**
	 * To be implemented in the subclasses
	 * @param importResult 
	 */
	protected abstract void importThis(ImportResult importResult);
	
	protected abstract String getTransactionDescription();
	
	protected abstract boolean checkData(ImportResult importResult);
		
	
	public void doImport(String user, ImportResult importResult) {
		
		if (checkData(importResult) == false) {
			return;
		}
		
		synchronized (owlModel) {
			KBUtil.morphUser(owlModel, user);
			try {
				owlModel.beginTransaction(getTransactionDescription(),getEntityId());
				importThis(importResult);
				owlModel.commitTransaction();
				
				importResult.recordResult(this.contributionId, null, ImportRowStatus.SUCCESS);
			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE, "Failed import of proposal id: " + this.contributionId, e);
				owlModel.rollbackTransaction();
				importResult.recordResult(this.contributionId, "Failed: " + e.getMessage(), ImportRowStatus.FAIL);
			} finally {
				KBUtil.restoreUser(owlModel);
			}
		} // end syncronized
	}
	
	public String getHtmlUrl() {
		String url = getUrl();
		if (url == null || url.length() == 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer("<a href=\"");
		buffer.append(url);
		buffer.append("\" target=\"_blank\">See full proposal in the ICD Public Browser</a>");
		return buffer.toString();
	}
	
	public RDFResource getEntity(){
		return getOwlModel().getRDFResource(this.getEntityId());
	}
	
	public RDFProperty getProperty(){
		return owlModel.getRDFProperty(this.getPropertyId());
	}
	
	public RDFResource getContributableEntity(){
		return getOwlModel().getRDFResource(this.getContributableId());
	}
	
	
	// ******************* Checks ***************************/
	
	
	public boolean checkEntityExists(ImportResult importResult) {
		RDFResource entity = getEntity();
		if (entity == null) {
			importResult.recordResult(getContributionId(), "Entity does not exist: " + getEntityId(), ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}
	
	public boolean checkEntityIsAClass(ImportResult importResult) {
		RDFResource entity = getEntity();
		if (entity == null) {
			importResult.recordResult(getContributionId(), "Entity does not exist: " + getEntityId(), ImportRowStatus.FAIL);
			return false;
		}
		if ( !(entity instanceof RDFSNamedClass)) {
			importResult.recordResult(getContributionId(), "Entity is not a class: " + getEntityId(), ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}
	
	public boolean checkPropertyExists(ImportResult importResult) {
		boolean exists = getProperty() != null;
		if (exists == false) {
			importResult.recordResult(contributionId, "Property " + propertyId +" does not exist.", ImportRowStatus.FAIL);
		}
		return exists;
	}
	
	public boolean checkPropertyIsValidPostCoordinationAxis(ImportResult importResult) {
		boolean isValidPCAxis = ImportProposalsUtil.getLookupUtil(getICDContentModel()).isPostCoordinationAxis(getPropertyId());
		if (isValidPCAxis == false) {
			importResult.recordResult(contributionId, "Property " + propertyId +" is not a valid post-coordination axis.", ImportRowStatus.FAIL);
		}
		return isValidPCAxis;
	}
	
	public boolean checkContributableIdNotEmpty(ImportResult importResult) {
		String contributableId = getContributableId();
		if (contributableId == null || contributableId.isEmpty()) {
			importResult.recordResult(contributionId, "contributableId is empty.", ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}	
	
	public boolean checkContributableIdIsValidLinearizationViewl(ImportResult importResult) {
		String contributableId = getContributableId();
		if (contributableId == null || contributableId.isEmpty()) {
			importResult.recordResult(contributionId, "contributableId is empty.", ImportRowStatus.FAIL);
			return false;
		}
		else {
			OWLIndividual linView = owlModel.getOWLIndividual(contributableId);
			if (linView == null || ! linView.hasRDFType( getICDContentModel().getLinearizationViewClass(), true)) {
				importResult.recordResult(contributionId, "Contributable id is " + contributableId + ". It is expected to be a valid linearization view instance.", ImportRowStatus.FAIL);
				return false;
			}
		}
		return true;
	}	
	
	public boolean checkNewValueNotEmpty(ImportResult importResult) {
		String newValue = this.getNewValue();
		if (newValue == null) {
			importResult.recordResult(getContributionId(), "New value is null. Expected non-null value.", ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}
	
	public boolean checkNewValueIsValidPostCoordinationAxisProperty(ImportResult importResult) {
		String newValue = this.getNewValue();
		if (newValue == null) {
			importResult.recordResult(getContributionId(), "New value is null. Expected a valid post-coordination axis property name.", ImportRowStatus.FAIL);
			return false;
		}
		else {
			boolean isValidPCAxis = ImportProposalsUtil.getLookupUtil(getICDContentModel()).isPostCoordinationAxis(newValue);
			if (isValidPCAxis == false) {
				importResult.recordResult(contributionId, "New Value " + newValue +" is not a valid post-coordination axis.", ImportRowStatus.FAIL);
				return false;
			}
		}
		return true;
	}

	public boolean checkOldValueExists(ImportResult importResult) {
		String oldValue = this.getOldValue();
				
		RDFResource entity = getEntity();
		RDFProperty prop = getProperty();
		
		boolean exists = false;
		
		if (prop instanceof OWLObjectProperty) {
			RDFResource contributableEntity = getOwlModel().getRDFResource(this.getContributableId());
			if (entity.hasPropertyValue(prop, contributableEntity) == true) {
				RDFProperty labelProp = getOwlModel().getRDFProperty(WHOFICContentModelConstants.LABEL_PROP);
				String label = (String) contributableEntity.getPropertyValue(labelProp);
				if (label != null) {
					label = label.replaceAll("\\s+", " ").trim();
				}
				if (oldValue != null) {					
					oldValue = oldValue.replaceAll("\\s+", " ").trim();
				}
				if (oldValue != null && oldValue.equals(label)) {
					exists = true;
				} else {
					importResult.recordResult(this.getContributionId(), "The label of the contributable id does not match the oldValue.", ImportRowStatus.FAIL);
				}
			} else {
				importResult.recordResult(this.getContributionId(), "The entity does not have the contributable id as its reified value.", ImportRowStatus.FAIL);
				exists = false;
			}
		} else { //datatype property
			if (entity.hasPropertyValue(prop, oldValue) == false) {
				importResult.recordResult(this.getContributionId(), "The entity does not have oldValue as a value.", ImportRowStatus.FAIL);
				exists = false;
			} else {
				exists = true;
			}
		}
		
		return exists;
	}

	// ******************* Getters and setters ***************************/
	
	
	public String getContributionId() {
		return contributionId;
	}


	public void setContributionId(String contributionId) {
		this.contributionId = contributionId;
	}


	public String getContributableId() {
		return contributableId;
	}


	public void setContributableId(String contributableId) {
		this.contributableId = contributableId;
	}


	public String getEntityId() {
		return entityId;
	}


	public void setEntityPublicId(String entityPublicId) {
		this.entityPublicId = entityPublicId;
	}
	
	public String getEntityPublicId() {
		return entityPublicId;
	}


	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}


	public String getContributorFullName() {
		return contributorFullName;
	}


	public void setContributorFullName(String contributorFullName) {
		this.contributorFullName = contributorFullName;
	}


	public String getEntryDateTime() {
		return entryDateTime;
	}


	public void setEntryDateTime(String entryDateTime) {
		this.entryDateTime = entryDateTime;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getRationale() {
		return rationale;
	}


	public void setRationale(String rationale) {
		this.rationale = rationale;
	}


	public String getProposalType() {
		return proposalType;
	}


	public void setProposalType(String proposalType) {
		this.proposalType = proposalType;
	}


	public String getProposalGroupId() {
		return proposalGroupId;
	}


	public void setProposalGroupId(String proposalGroupId) {
		this.proposalGroupId = proposalGroupId;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getPropertyId() {
		return propertyId;
	}


	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}


	public String getOldValue() {
		return oldValue;
	}


	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}


	public String getNewValue() {
		return newValue;
	}


	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}


	public String getIdFromValueSet() {
		return idFromValueSet;
	}


	public void setIdFromValueSet(String idFromValueSet) {
		this.idFromValueSet = idFromValueSet;
	}


	public String getValueSetName() {
		return valueSetName;
	}

	
	public void setValueSetName(String valueSetName) {
		this.valueSetName = valueSetName;
	}


	public OWLModel getOwlModel() {
		return owlModel;
	}


	public ICDContentModel getICDContentModel() {
		return icdContentModel;
	}
	
	
}
