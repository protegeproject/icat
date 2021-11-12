package edu.stanford.bmir.protege.web.server;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;

import edu.stanford.bmir.protege.icd.export.ExportICDClassesJob;
import edu.stanford.bmir.protege.web.client.rpc.ICDService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.server.icd.proposals.ImportProposalsUtil;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICDServiceImpl extends WHOFICServiceImpl implements ICDService {

	private static final long serialVersionUID = -1306615171217946081L;
	private static final String EXCEL_FILE_EXTENSION = ".xls";

	// TODO: Event generation is currently disabled for class creation, because
	// it generates too many events and slows down significantly the class creation.
	// The effect is that some other class trees do not get the event, and will not
	// update. We will keep this limitation for now, given the increase in
	// performance.
	
	// createICDSpecificEntities flag is disregarded. Always create them.
	@SuppressWarnings("rawtypes")
	public EntityData createICDCls(final String projectName, String clsName, Collection<String> superClsNames,
			String title, String sortingLabel, boolean createICDSpecificEntities, final String user,
			final String operationDescription, final String reasonForChange) {
		
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
	
		ICDContentModel cm = new ICDContentModel((OWLModel) kb);
	
		RDFSNamedClass cls = null;
	
		if (clsName != null && edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass( ((OWLModel) kb), clsName) != null) {
			throw new RuntimeException("A class with the same name '" + clsName + "' already exists in the model.");
		}
	
		//Log.getLogger().info("Create class: Start create at " + new Date());
		
		boolean eventsEnabled = WebProtegeKBUtil.setEventGenerationForRemotePrj(kb, false);
	
		boolean runsInTransaction = WebProtegeKBUtil.shouldRunInTransaction(operationDescription);
		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {
				if (runsInTransaction) {
					kb.beginTransaction(operationDescription);
				}
	
				// TODO: get index, if valid, pass some arg to the second call
				//Log.getLogger().info("Create class: Start check index at " + new Date());
				boolean isSiblingIndexValid = checkAndRecreateIndex((OWLModel) kb, superClsNames, false);
				//Log.getLogger().info("Create class: End check index. Start create ICD Cat at " + new Date());
	
				// createICDSpecificEntities flag is disregarded. Always create them.
				cls = cm.createICDCategory(clsName, superClsNames);
	
				//Log.getLogger().info("Create class: End create ICD Cat. Start creating terms at " + new Date());
				
				if (clsName != null) {
					cls.addPropertyValue(cm.getIcdCodeProperty(), clsName);
				}
	
				RDFResource titleTerm = cm.createTitleTerm();
				cm.fillTerm(titleTerm, null, title, null);
				cm.addTitleTermToClass(cls, titleTerm);
	
				if (sortingLabel != null) {
					cls.setPropertyValue(cm.getSortingLabelProperty(), sortingLabel);
				}
				
				//Log.getLogger().info("Create class: End create term. Start add child to index at " + new Date());
	
				addChildToIndex(cls, superClsNames, isSiblingIndexValid);
	
				//Log.getLogger().info("Create class: End add child to index. Start proposal util update at " + new Date());
	
				ImportProposalsUtil.getLookupUtil(cm).addCategoryIDTitlePair(cls.getName(), title);
	
				//Log.getLogger().info("Create class: End proposal util update. Start commit transaction at " + new Date());
	
				if (runsInTransaction) {
					kb.commitTransaction();
				}
				
				//Log.getLogger().info("Create class: End commit transaction at " + new Date());
	
			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE, "Error at creating class in " + projectName + " class: " + clsName,
						e);
				if (runsInTransaction) {
					kb.rollbackTransaction();
				}
				throw new RuntimeException("Error at creating class " + clsName + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
				WebProtegeKBUtil.setEventGenerationForRemotePrj(kb, eventsEnabled);
			}
		}
		
		
		// TODO: If the class creation fails for some reason, and the code makes it so
		// far, then we
		// should not return an entity, but rather throw an exception.
	
		// TODO: the note is not created here anymore, but with a different remote call
		// from the client
		// so, theoretically, we do no need the reason for change here, but it doesn't
		// hurt for now
		EntityData entityData = createEntityData(cls, false);
		if (reasonForChange != null && reasonForChange.length() > 0) { // this should be handled already
			entityData.setLocalAnnotationsCount(1);
		}
	
		entityData.setTypes(createEntityList(cls.getDirectTypes()));
		
		//Retrieving the public id from the WHO ID server is done now with a separate remote call,
		//retrievePublicId, to speed up class creation
		
		//entityData.setProperty(ICDClassTreePortlet.PUBLIC_ID_PROP, publicId);
	
		//Log.getLogger().info("Create class: Ended create class at " + new Date());
		return entityData;
	}

	public String retrievePublicId(String projectName, String clsName) {
		//Log.getLogger().info("Retrieve public id: Start add public id at " + new Date());
	
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
	
		RDFSNamedClass cls = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass( ((OWLModel) kb), clsName);
		if (cls == null) {
			Log.getLogger().severe("Could not retrieve public id for class: " + clsName + " because class is null.");
			return null;
		}
		
		ICDContentModel cm = new ICDContentModel((OWLModel) kb);
		String publicId = null;
		
		try {
			publicId = ICDIDUtil.getPublicId(cls.getName());
			if (publicId == null) {
				Log.getLogger().warning("Could not get public ID for class: " + cls.getName());
			} else {
				cls.setPropertyValue(cm.getPublicIdProperty(), publicId);
			}
			// TT - 2016.04.23 - Throwable because of the ClassNotFound error, which we could not diagnose
		} catch (Throwable e) {
			Log.getLogger().log(Level.WARNING, "Could not add public ID in " + projectName + " for class: " + clsName, e);
		}
		
		//Log.getLogger().info("Retrieve public id: End add public at " + new Date());
		
		return publicId;
	}

	private boolean checkAndRecreateIndex(OWLModel owlModel, Collection<String> superClsNames, boolean recreateIndex) {
		boolean success = true;
		for (String superclsname : superClsNames) {
			RDFSNamedClass parent = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, superclsname);
			if (parent != null) {
				success = success && checkAndRecreateIndex(parent, recreateIndex);
			}
		}
		return success;
	}

	private boolean checkAndRecreateIndex(RDFSNamedClass parent, boolean recreateIndex) {
		return getContentModel(parent.getOWLModel()).checkIndexAndRecreate(parent, recreateIndex);
	}

	private boolean addChildToIndex(RDFSNamedClass cls, Collection<String> superClsNames, boolean isSiblingIndexValid) {
		boolean success = true;
		for (String superclsname : superClsNames) {
			RDFSNamedClass parent = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(cls.getOWLModel(), superclsname);
			if (parent != null) {
				success = success && addChildToIndex(parent, cls, isSiblingIndexValid);
			}
		}
		return success;
	}

	private boolean addChildToIndex(RDFSNamedClass parent, RDFSNamedClass cls, boolean isSiblingIndexValid) {
		return getContentModel(parent.getOWLModel()).addChildToIndex(parent, cls, isSiblingIndexValid);
	}

	public String exportICDBranch(String projectName, String parentClass, String userName) {
		Project project = getProject(projectName);
		if (project == null) {
			return null;
		}
		KnowledgeBase kb = project.getKnowledgeBase();
	
		Cls cls = edu.stanford.bmir.whofic.KBUtil.getCls(kb, parentClass);
		if (cls == null) {
			return null;
		}
	
		String fileName = getExportFileName(projectName, cls.getBrowserText().replaceAll("'", ""), userName);
		String exportDirectory = ApplicationProperties.getICDExportDirectory();
		final String exportFilePath = exportDirectory + fileName;
	
		Log.getLogger()
				.info("Started the export of " + cls.getBrowserText() + " for user " + userName + " on " + new Date());
		long t0 = System.currentTimeMillis();
	
		ExportICDClassesJob exportJob = new ExportICDClassesJob(getProject(projectName).getKnowledgeBase(),
				exportFilePath, parentClass);
		try {
			exportJob.execute();
		} catch (ProtegeException e) {
			Log.getLogger().log(Level.SEVERE, "Error at exporting " + cls.getBrowserText() + " to file " + fileName, e);
			return null;
		}
	
		Log.getLogger().info("Export of " + cls.getBrowserText() + " for user " + userName + " took "
				+ (System.currentTimeMillis() - t0) / 1000 + " seconds.");
	
		return fileName;
	}

	private String getExportFileName(String projectName, String entity, String user) {
		StringBuffer fileName = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HHmmss");
		final String formattedDate = sdf.format(new Date());
	
		fileName.append(formattedDate);
		fileName.append("_");
		fileName.append(entity);
		fileName.append("_");
		if (user != null) {
			fileName.append(user);
		}
		fileName.append(EXCEL_FILE_EXTENSION);
	
		return fileName.toString();
	}

	@Override
	public boolean isNonRetireableClass(String projectName, String clsName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		Cls cls = edu.stanford.bmir.whofic.KBUtil.getCls(kb, clsName);
	
		if (cls == null) {
			return false;
		}
	
		if (kb instanceof OWLModel && cls instanceof RDFSNamedClass) {
			return RetirementManager.isNonRetirableId(clsName) == true;
		}
	
		return false;
	}

	@Override
	public boolean isInRetiredTree(String projectName, String clsName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		Cls cls = edu.stanford.bmir.whofic.KBUtil.getCls(kb, clsName);
	
		if (cls == null) {
			return false;
		}
	
		if (kb instanceof OWLModel && cls instanceof RDFSNamedClass) {
			return RetirementManager.isInRetiredTree((OWLModel) kb, (RDFSNamedClass) cls) == true;
		}
	
		return false;
	}

	@Override
	public Collection<EntityData> getClsesInRetiredTree(String projectName, Collection<EntityData> clses) {
		Collection<EntityData> clsesInRetired = new HashSet<EntityData>();
		
		for (EntityData cls : clses) {
			if (isInRetiredTree(projectName, cls.getName())) {
				clsesInRetired.add(cls);
			}
		}
		
		return clsesInRetired;
	}

	protected ICDContentModel getContentModel(OWLModel owlModel) {
		return new ICDContentModel(owlModel);
	}

	@Override
	public String getFrameForPublicId(String projectName, String publicId) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		
		Collection<Frame> frames = kb.getFramesWithValue(kb.getSlot(ICDContentModelConstants.PUBLIC_ID_PROP), null, false, publicId);
		
		return (frames == null || frames.size() == 0) ? 
				null : frames.iterator().next().getName();
	}

}
