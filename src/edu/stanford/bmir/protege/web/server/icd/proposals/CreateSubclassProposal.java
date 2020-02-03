package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.server.ICDIDUtil;
import edu.stanford.bmir.protege.web.server.icd.proposals.util.LookupUtil;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * A proposal class to add a new subclass to an existing class.
 * The title of the new entity is specified in the "new value" column. 
 * 
 * @author csnyulas
 *
 */
public class CreateSubclassProposal extends ICDProposal {

	private RDFSNamedClass newICDClass;
	
	public CreateSubclassProposal(OWLModel owlModel, String contributionId, String contributableId,
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
		
		RDFSNamedClass parent = (RDFSNamedClass)getEntity();
		boolean isSiblingIndexValid = cm.checkIndexAndRecreate(parent, false);
		
		newICDClass = cm.createICDCategory(null, Collections.singleton(parent.getName()));
		String newTitle = getNewValue();
		cm.createTitleTerm();
		
        RDFResource titleTerm = cm.createTitleTerm();
        cm.fillTerm(titleTerm, null, newTitle, null);
        cm.addTitleTermToClass(newICDClass, titleTerm);
        
        cm.addChildToIndex(parent, newICDClass, isSiblingIndexValid);

        ImportProposalsUtil.getLookupUtil(cm).addCategoryIDTitlePair(newICDClass.getName(), newTitle);

        //---- copied and adapted from ICDServiceImpl.createICDClass ---- //
		/* Add the public ID - before used to be in create class transaction, but now it is done 
		   as a separate operation, because it often fails, and because 
		   of an impossible to diagnose ClassNotFound for the org.apache.http.client.ClientProtocolException 
		*/
		try {
			String publicId = ICDIDUtil.getPublicId(newICDClass.getName());
		    if (publicId == null) {
		        Log.getLogger().warning("Could not get public ID for newly created class: " + newICDClass.getName());
		    } else {
		        newICDClass.setPropertyValue(cm.getPublicIdProperty(), publicId);
		    }
		  //TT - 2016.04.23 - Throwable because of the ClassNotFound error, which we could not diagnose
		} catch (Throwable e) { 
			Log.getLogger().log(Level.WARNING, "Could not add public ID for class: " + newICDClass.getName(), e);
		}
        //---- END copied section ---- //

	}

	
	@Override
	protected String getTransactionDescription() {
		StringBuffer buffer = new StringBuffer(ICDProposal.TRANSACTION_TEXT_PREFIX);
		buffer.append("Created new subclass of ");
		buffer.append(getEntity().getName() + "(" + getEntity().getBrowserText() + "). ");
		buffer.append("<br /><br />");
		buffer.append("New class title is: <i>");
		buffer.append(getNewValue());
		buffer.append("</i><br /><br />");
		buffer.append(getHtmlUrl());
		
		return buffer.toString();
	}


	@Override
	protected boolean checkData(ImportResult importResult) {
		//order of checking is important, don't change
		return checkNewValueNotEmpty(importResult) &&
				checkEntityIsAClass(importResult) &&
				checkClassNotExists(importResult);
	}

	@Override
	public RDFResource getEntity(){
		RDFResource entity = getOwlModel().getRDFResource(this.getEntityId());
		if (entity == null) {
			StringBuffer warningMsg = new StringBuffer("'" + this.getEntityId() + "' is not a valid entity IRI. Trying to match by title...");
			String catId = ImportProposalsUtil.getLookupUtil(getICDContentModel()).getCategoryIDForTitle(this.getEntityId());
			if (catId != null) {
				entity = getOwlModel().getRDFResource(catId);
			}
			if (entity == null) {
				warningMsg.append(" Couldn't find entity with that title.");
			}
			else {
				warningMsg.append(" Found " + entity.getName());
			}
			Log.getLogger().log(Level.WARNING,  warningMsg.toString());
		}
		return entity;
	}

	
	private boolean checkClassNotExists(ImportResult importResult) {
		String newTitle = this.getNewValue();
		ICDContentModel cm = getICDContentModel();
		LookupUtil lookupUtil = ImportProposalsUtil.getLookupUtil(cm);
		
		boolean foundClass = false;
		if (lookupUtil.getCategoryIDForTitle(newTitle) != null) {
			foundClass = true;
		}
		else {
			if (!lookupUtil.hasFullEntityTitleMaps()) {
				RDFResource entity = getEntity();
				lookupUtil.addCategoryIDTitlePair(entity.getName(), ImportProposalsUtil.getEntityTitle(cm, entity));
				
				Collection<RDFSNamedClass> children = cm.getChildren((RDFSNamedClass) entity);
				for (RDFSNamedClass child : children) {
					String childTitle = ImportProposalsUtil.getEntityTitle(cm, child);
					lookupUtil.addCategoryIDTitlePair(child.getName(), childTitle);
					if (newTitle.equals(childTitle)) {
						foundClass = true;
					}
				}
			}
		}
		
		if (foundClass) {
			importResult.recordResult(this.getContributionId(), "Class already exists. Will not import.", ImportRowStatus.IGNORED);
			return false;
		}
		return true;
	}
	

}
