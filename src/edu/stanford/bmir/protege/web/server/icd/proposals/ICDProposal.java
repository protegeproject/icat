package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.logging.Level;

import edu.stanford.bmir.protege.web.server.KBUtil;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

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
	
	public static final String TRANSACTION_TEXT_PREFIX = "Proposal import: ";
	
	private transient OWLModel owlModel;
	
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
		
		if (checkData(null) == false) {
			return;
		}
		
		synchronized (owlModel) {
			KBUtil.morphUser(owlModel, user);
			try {
				owlModel.beginTransaction(getTransactionDescription());
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
	
	
	public RDFResource getEntity(){
		return getOwlModel().getRDFResource(this.getEntityId());
	}
	
	public RDFProperty getProperty(){
		return owlModel.getRDFProperty(this.getPropertyId());
	}
	
	public RDFResource getContributableEntity(){
		return getOwlModel().getRDFResource(this.getContributableId());
	}
	
	public boolean checkEntityExists(ImportResult importResult) {
		RDFResource entity = getEntity();
		if (entity == null) {
			importResult.recordResult(getContributionId(), "Entity does not exist: " + getEntityId(), ImportRowStatus.FAIL);
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
	
	public boolean checkContributableIdNotEmpty(ImportResult importResult) {
		String contributableId = getContributableId();
		if (contributableId == null || contributableId.isEmpty()) {
			importResult.recordResult(contributionId, "contributableId is empty.", ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}
	
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
	
	
}
