package edu.stanford.bmir.protege.web.server.icd.proposals.logDef;

import edu.stanford.bmir.protege.web.server.icd.proposals.ICDProposal;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportResult;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportRowStatus;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Abstract superclass for all logical definition proposal classes.
 * 
 * It has a flag isDefining that controls if this is a defining or non-defining
 * logical definition.
 * 
 * <b>It uses the idFromValueSet as the logical definition superclass.</b>
 * 
 * @author ttania
 *
 */
public abstract class LogicalDefinitionProposal extends ICDProposal {

	private boolean isDefining;
	private LogicalDefinitionUtils logDefUtil;
	
	
	public LogicalDefinitionProposal(OWLModel owlModel, String contributionId, String contributableId, String entityId,
			String entityPublicId, String contributorFullName, String entryDateTime, String status, String rationale,
			String proposalType, String proposalGroupId, String url, String propertyId, String oldValue,
			String newValue, String idFromValueSet, String valueSetName, boolean isDefining) {
		super(owlModel, contributionId, contributableId, entityId, entityPublicId, contributorFullName, entryDateTime, status,
				rationale, proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName);
		this.isDefining = isDefining;
		this.logDefUtil = new LogicalDefinitionUtils(owlModel);
	}


	public boolean isDefining() {
		return isDefining;
	}
	
	protected RDFSNamedClass getLogDefParent() {
		return getOwlModel().getRDFSNamedClass(getIdFromValueSet());
	}
	
	protected RDFResource getNewValueEntity() {
		return getOwlModel().getRDFResource(getNewValue());
	}
	
	protected RDFResource getOldValueEntity() {
		return getOwlModel().getRDFResource(getOldValue());
	}
	
	protected RDFSNamedClass getEntityCls() {
		return (RDFSNamedClass) getEntity();
	}
	

	protected RDFResource getNewFillerResource() {
		return (RDFResource) getNewValueEntity();
	}
	
	protected RDFResource getOldFillerResource() {
		return (RDFResource) getOldValueEntity();
	}
	
	protected LogicalDefinitionUtils getLogicalDefinitionUtil() {
		return logDefUtil;
	}
	
	protected boolean logicalDefExists(RDFResource filler) {
		String res = logDefUtil.checkLogicalDefinition(getEntityCls(), getProperty(), filler, getLogDefParent());
		return isDefining() == true? LogicalDefinitionUtils.LOG_DEF_MATCH_NS.equals(res) : 
									 LogicalDefinitionUtils.LOG_DEF_MATCH_N.equals(res);
	
	}
	
	protected boolean checkLogDefParentIsAClass(ImportResult importResult) {
		if (checkLogDefParentNotEmpty(importResult) == false) {
			return false;
		}
		
		String parentId = getIdFromValueSet();
		RDFResource entity = getOwlModel().getRDFResource(parentId);
		
		if (entity == null) {
			importResult.recordResult(getContributionId(), "The log def parent (idFromValueSet) does not exist for " + getEntityId(), ImportRowStatus.FAIL);
			return false;
		}
		if ( !(entity instanceof RDFSNamedClass)) {
			importResult.recordResult(getContributionId(), "The log def parent (idFromValueSet) is not a class for " + getEntityId(), ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}
	
	protected boolean checkLogDefParentNotEmpty(ImportResult importResult) {
		String parentId = this.getIdFromValueSet();
		if (parentId == null) {
			importResult.recordResult(getContributionId(), "idFromValueSet (i.e., log def parent) value is null. Expected non-null value.", ImportRowStatus.FAIL);
			return false;
		}
		return true;
	}
	
	protected boolean checkLogicalDefExists(RDFResource filler, ImportResult importResult) {
		boolean exists = logicalDefExists(filler);
		
		if (exists == false) {
			importResult.recordResult(getContributionId(), "Logical definition does not exist for " + getEntityId(), ImportRowStatus.IGNORED);
		}
		
		return exists;		
	}
	
	protected boolean checkLogicalDefDoesNotExist(RDFResource filler, ImportResult importResult) {
		boolean exists = logicalDefExists(filler);
		
		if (exists == true) {
			importResult.recordResult(getContributionId(), "Logical definition already exists for " + getEntityId(), ImportRowStatus.IGNORED);
		}
		
		return exists == false;		
	}
	
	
	@Override
	protected boolean checkData(ImportResult importResult) {
		return checkEntityExists(importResult) &&
				checkEntityIsAClass(importResult) &&
				checkPropertyExists(importResult) && 
				checkLogDefParentIsAClass(importResult); //also checks for null
	}


}
