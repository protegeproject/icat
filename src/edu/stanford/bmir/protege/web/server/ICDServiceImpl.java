package edu.stanford.bmir.protege.web.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.PrecoordinationDefinitionComponent;
import edu.stanford.bmir.whofic.WHOFICContentModel;
import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.bmir.whofic.ici.ICIContentModel;
import edu.stanford.bmir.protege.icd.export.ExportICDClassesJob;
import edu.stanford.bmir.protege.web.client.rpc.ICDService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.AllowedPostcoordinationValuesData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.PrecoordinationClassExpressionData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;
import edu.stanford.bmir.protege.web.client.ui.icd.DisplayStatus;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDClassTreePortlet;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICDServiceImpl extends OntologyServiceImpl implements ICDService {

    private static final long serialVersionUID = -10148579388542388L;
    private static final String EXCEL_FILE_EXTENSION = ".xls";

    private static final String PROPERTY_LINEARIZATION_PARENT = "http://who.int/icd#linearizationParent";

    //TODO: Event generation is currently disabled for class creation, because
    // it generates too many events and slows down significantly the class creation.
    // The effect is that some other class trees do not get the event, and will not
    // update. We will keep this limitation for now, given the increase in performance.
    @SuppressWarnings("rawtypes")
    public EntityData createICDCls(final String projectName, String clsName, Collection<String> superClsNames,
            String title, String sortingLabel, boolean createICDSpecificEntities, final String user, final String operationDescription,
            final String reasonForChange) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        ICDContentModel cm = new ICDContentModel((OWLModel) kb);

        RDFSNamedClass cls = null;
        String publicId = null;

        if (clsName != null && ((OWLModel) kb).getRDFSNamedClass(clsName) != null) {
            throw new RuntimeException("A class with the same name '" + clsName + "' already exists in the model.");
        }

        boolean eventsEnabled = kb.setGenerateEventsEnabled(false);

        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                //TODO: get index, if valid, pass some arg to the second call

                boolean isSiblingIndexValid = checkAndRecreateIndex((OWLModel) kb, superClsNames, false);

                cls = cm.createICDCategory(clsName, superClsNames, true, createICDSpecificEntities);

                if (clsName != null) {
                    cls.addPropertyValue(cm.getIcdCodeProperty(), clsName);
                }

                RDFResource titleTerm = cm.createTitleTerm();
                cm.fillTerm(titleTerm, null, title, null);
                cm.addTitleTermToClass(cls, titleTerm);

                if (sortingLabel != null) {
                    cls.setPropertyValue(cm.getSortingLabelProperty(), sortingLabel);
                }

                //add the public ID
                publicId = ICDIDUtil.getPublicId(cls.getName());
                if (publicId == null) {
                    Log.getLogger().warning("Could not get public ID for newly created class: " + cls.getName());
                } else {
                    cls.setPropertyValue(cm.getPublicIdProperty(), publicId);
                }

                addChildToIndex(cls, superClsNames, isSiblingIndexValid);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Error at creating class in " + projectName + " class: " + clsName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Error at creating class " + clsName + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
                kb.setGenerateDeletingFrameEventsEnabled(eventsEnabled);
            }
        }

        //TODO: the note is not created here anymore, but with a different remote call from the client
        //so, theoretically, we do no need the reason for change here, but it doesn't hurt for now
        EntityData entityData = createEntityData(cls, false);
        if (reasonForChange != null && reasonForChange.length() > 0) { //this should be handled already
            entityData.setLocalAnnotationsCount(1);
        }

        entityData.setTypes(createEntityList(cls.getDirectTypes()));
        entityData.setProperty(ICDClassTreePortlet.PUBLIC_ID_PROP, publicId);

        return entityData;
    }


    private boolean checkAndRecreateIndex(OWLModel owlModel, Collection<String> superClsNames, boolean recreateIndex) {
        boolean success = true;
        for (String superclsname : superClsNames) {
            RDFSNamedClass parent = owlModel.getRDFSNamedClass(superclsname);
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
            RDFSNamedClass parent = cls.getOWLModel().getRDFSNamedClass(superclsname);
            if (parent != null) {
                success = success && addChildToIndex(parent, cls, isSiblingIndexValid);
            }
        }
        return success;
    }


    private boolean addChildToIndex(RDFSNamedClass parent, RDFSNamedClass cls, boolean isSiblingIndexValid) {
        return getContentModel(parent.getOWLModel()).addChildToIndex(parent, cls, isSiblingIndexValid);
    }


    public List<EntityPropertyValues> getEntityPropertyValuesForLinearization(String projectName, List<String> entities, List<String> properties,
            List<String> reifiedProps) {
        List<EntityPropertyValues> entityPropertyValues = getEntityPropertyValues(projectName, entities, properties, reifiedProps);
        return prepareLinearizationEntityPropertyValues(projectName, entities, entityPropertyValues, true);
    }

	private List<EntityPropertyValues> prepareLinearizationEntityPropertyValues(String projectName,
			List<String> entities,
			List<EntityPropertyValues> entityPropertyValues,
			boolean fixLinearizationParent) {
		if (entityPropertyValues == null){
            entityPropertyValues = new ArrayList<EntityPropertyValues>();
        }
        ArrayList<EntityPropertyValues> result = new ArrayList<EntityPropertyValues>(entityPropertyValues);

        if (fixLinearizationParent) {
	        //update display label of linearization parent to default parent, if it makes sense
	        if (entities != null && entities.size() == 1) {
	            List<EntityData> directParents = getParents(projectName, entities.get(0), true);
	            if (directParents.size() == 1) {
	                String parentBrowserText = directParents.get(0).getBrowserText();
	                PropertyEntityData propLinParentED = new PropertyEntityData(PROPERTY_LINEARIZATION_PARENT);
	                for (EntityPropertyValues entityPV : result) {
	                    List<EntityData> values = entityPV.getPropertyValues(propLinParentED);
	                    if (values == null ) {
	                        values = new ArrayList<EntityData>();
	                    }
	                    if ( values.isEmpty() ) {
	                        values.add(new EntityData());
	                    }
	                    String linParentName = values.get(0).getName();
	                    if (linParentName == null || "".equals(linParentName)) {
	                        ArrayList<EntityData> newValues = new ArrayList<EntityData>();
	                        for (EntityData value : values) {
	                            value.setName(null);
	                            value.setBrowserText("[" + parentBrowserText + "]");
	                            newValues.add(value);
	                        }
	                        entityPV.setPropertyValues(propLinParentED, newValues);
	                    }
	                }
	            }
	        }
        }
        Collections.sort(result, new LinearizationEPVComparator());
        return result;
	}

    public String exportICDBranch(String projectName, String parentClass, String userName){
        Project project = getProject(projectName);
        if (project == null) {
            return null;
        }
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls cls = kb.getCls(parentClass);
        if (cls == null) {
            return null;
        }

        String fileName = getExportFileName(projectName, cls.getBrowserText().replaceAll("'",""), userName);
        String exportDirectory = ApplicationProperties.getICDExportDirectory();
        final String exportFilePath = exportDirectory + fileName;

        Log.getLogger().info("Started the export of " + cls.getBrowserText() + " for user " + userName + " on " + new Date());
        long t0 = System.currentTimeMillis();

        ExportICDClassesJob exportJob = new ExportICDClassesJob(getProject(projectName).getKnowledgeBase(), exportFilePath, parentClass);
        try {
            exportJob.execute();
        } catch (ProtegeException e) {
            Log.getLogger().log(Level.SEVERE, "Error at exporting " + cls.getBrowserText() + " to file " + fileName, e);
            return null;
        }

        Log.getLogger().info("Export of " + cls.getBrowserText() + " for user " + userName + " took " + (System.currentTimeMillis() - t0)/1000 + " seconds.");

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

    private class LinearizationEPVComparator implements Comparator<EntityPropertyValues> {

        private final PropertyEntityData linearizationViewPED =
            new PropertyEntityData("http://who.int/icd#linearizationView");

        public int compare(EntityPropertyValues epv1, EntityPropertyValues epv2) {
            //We should not have null values, but if we happen to have we wish to leave the them at the end
            if (epv1 == null) {
                return 1;
            }
            if (epv2 == null) {
                return -1;
            }
            EntityData lin1 = null;
            EntityData lin2 = null;
            List<EntityData> lins;
            lins = epv1.getPropertyValues(linearizationViewPED);
            if (lins != null && lins.size() > 0) {
                lin1 = lins.get(0);
            }
            lins = epv2.getPropertyValues(linearizationViewPED);
            if (lins != null && lins.size() > 0) {
                lin2 = lins.get(0);
            }
            //We should not have null values, but if we happen to have we wish to leave the them at the end
            if (lin1 == null) {
                return 1;
            }
            if (lin2 == null) {
                return -1;
            }

            return lin1.getBrowserText().compareTo(lin2.getBrowserText());
        }
    }

    //have to correspond to the ones in InheritedTagsGrid
    private final static String COL_TAG = "tag";
    private final static String COL_INH_FOM = "inheritedFrom";

    public List<EntityPropertyValues> getSecondaryAndInheritedTags(String projectName, String clsName) {
        Project project = getProject(projectName);
        if (project == null) { return null; }

        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        RDFSNamedClass cls = owlModel.getRDFSNamedClass(clsName);
        if (cls == null) { return null; }

        //set browserText for values that come from primary

        List<EntityPropertyValues> inhTagsList = new ArrayList<EntityPropertyValues>();

        WHOFICContentModel cm = getContentModel(owlModel);
        Map<RDFResource, List<RDFSNamedClass>> tag2inhFrom = cm.getInvolvedTags(cls);

        RDFResource localPrimaryTag = cm.getAssignedPrimaryTag(cls);
        Collection<RDFResource> localSecondaryTags = cm.getAssignedSecondaryTags(cls);

        PropertyEntityData tagEdProp = new PropertyEntityData(COL_TAG);
        PropertyEntityData inhFromEdProp = new PropertyEntityData(COL_INH_FOM);

        for (Iterator<RDFResource> iteratorTags = tag2inhFrom.keySet().iterator(); iteratorTags.hasNext();) {
            RDFResource tagInst = iteratorTags.next();

            EntityData tag = createEntityData(tagInst);
            List<RDFSNamedClass> inhFromClses = tag2inhFrom.get(tagInst);

            EntityPropertyValues epv = new EntityPropertyValues();
            epv.setSubject(tag);

            EntityPropertyValues suplEpv = null;

            epv.addPropertyValue(tagEdProp, tag);
            List<RDFSNamedClass> realInheritedClasses = new ArrayList<RDFSNamedClass>();
            realInheritedClasses.addAll(inhFromClses);
            //remove local primary TAG
            if (tagInst.equals(localPrimaryTag)) {
                realInheritedClasses.remove(cls);
            }

            //split value list, if it contains local secondary TAG
            if (localSecondaryTags !=null && localSecondaryTags.contains(tagInst)) {
                realInheritedClasses.remove(cls);       //TODO see if we need to remove multiple appearances of cls in case a tag is added as secondary tag multiple times

                suplEpv = new EntityPropertyValues();
                suplEpv.setSubject(tag); //not ideal - we need a subject for the values, ideally should be null
                suplEpv.addPropertyValue(tagEdProp, tag);
                suplEpv.addPropertyValues(inhFromEdProp, new ArrayList<EntityData>());
                inhTagsList.add(suplEpv);
            }

            //create value list for inherited classes; add "(P)" if inherited from primary TAG of superclass
            if ( ! realInheritedClasses.isEmpty() ) {
                List<EntityData> inheritedClsesEdList = new ArrayList<EntityData>();
                for (RDFSNamedClass inhCls : realInheritedClasses) {
                    EntityData ed = createEntityData(inhCls);
                    if (tagInst.equals(cm.getAssignedPrimaryTag(inhCls))) {
                        ed.setBrowserText(ed.getBrowserText() + " (P)");
                    }
                    inheritedClsesEdList.add(ed);
                }
                epv.addPropertyValues(inhFromEdProp, inheritedClsesEdList);
                inhTagsList.add(epv);
            }
        }
        Collections.sort(inhTagsList, new InheritedTagEpvComparator());
        return inhTagsList;
    }


    @SuppressWarnings("deprecation")
    @Override
    public List<SubclassEntityData> getSubclasses(String projectName, String className) {
        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();

        ArrayList<SubclassEntityData> subclassesData = new ArrayList<SubclassEntityData>();

        RDFSNamedClass superCls = owlModel.getRDFSNamedClass(className);
        if (superCls == null) {
            return subclassesData;
        }

        WHOFICContentModel cm = getContentModel(owlModel);
        RDFProperty displayStatusProp = cm.getDisplayStatusProperty();
        RDFProperty isObsoleteProp = cm.getIsObsoleteProperty();
        RDFProperty publicIdProp = cm.getPublicIdProperty();

        List<RDFSNamedClass> subclasses = cm.getOrderedChildren(superCls);

        for (Cls subcls : subclasses) {
            if (!subcls.isSystem()) {
                SubclassEntityData subclassEntityData = new SubclassEntityData(subcls.getName(),
                        getBrowserText(subcls), createEntityList(subcls.getDirectTypes()), subcls.getVisibleDirectSubclassCount());
                subclassesData.add(subclassEntityData);
                subclassEntityData.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(subcls));
                subclassEntityData.setChildrenAnnotationsCount(HasAnnotationCache.getChildrenAnnotationCount(subcls));
                setDisplayStatus(subcls, displayStatusProp, subclassEntityData, cm);
                setObsoleteStatus(subcls, isObsoleteProp, subclassEntityData);
                subclassEntityData.setProperty(ICDClassTreePortlet.PUBLIC_ID_PROP, (String) subcls.getOwnSlotValue(publicIdProp));

                String user = KBUtil.getUserInSession(getThreadLocalRequest());
                if (user != null) {
                    subclassEntityData.setWatch(WatchedEntitiesCache.getCache(project).getWatchType(user, subcls.getName()));
                }
            }
        }
        return subclassesData;
    }

    private void setDisplayStatus(Cls cls, Slot displayStatusSlot, EntityData entity, WHOFICContentModel cm) {
        RDFResource status = (RDFResource) cls.getOwnSlotValue(displayStatusSlot);
        if (status == null) {
            return;
        }
        String prop = "status";
        if (status.equals(cm.getDisplayStatusBlueInst())) {
            entity.setProperty(prop, DisplayStatus.B.toString());
        } else if (status.equals(cm.getDisplayStatusYellowInst())) {
            entity.setProperty(prop, DisplayStatus.Y.toString());
        } else if (status.equals(cm.getDisplayStatusRedInst())) {
            entity.setProperty(prop, DisplayStatus.R.toString());
        }
    }


    private void setObsoleteStatus(Cls cls, Slot obsoleteSlot, EntityData entity) {
        Boolean isObsolete = (Boolean) cls.getOwnSlotValue(obsoleteSlot);
        if (isObsolete != null && isObsolete == true) {
            entity.setProperty("obsolete", "true");
        }
    }

    class InheritedTagEpvComparator implements Comparator<EntityPropertyValues> {
        public int compare(EntityPropertyValues epv1, EntityPropertyValues epv2) {
            Collection<EntityData> inhTags1 = epv1.getPropertyValues(new PropertyEntityData(COL_INH_FOM));
            Collection<EntityData> inhTags2 = epv2.getPropertyValues(new PropertyEntityData(COL_INH_FOM));
            if (inhTags1.size() == inhTags2.size()) {
                EntityData tag1 = CollectionUtilities.getFirstItem(epv1.getPropertyValues(new PropertyEntityData(COL_TAG)));
                EntityData tag2 = CollectionUtilities.getFirstItem(epv2.getPropertyValues(new PropertyEntityData(COL_TAG)));
                return tag2.getBrowserText().compareTo(tag1.getBrowserText());
            }
            return inhTags1.size() - inhTags2.size();
        }

    }

    public EntityPropertyValues changeIndexType(String projectName, String subject, String indexEntity,
            List<String> reifiedProps, String indexType) {
        Project project = getProject(projectName);
        if (project == null) {
            return null;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel((OWLModel) kb);

        Cls cls = kb.getCls(subject);
        if (cls == null || (! (cls instanceof RDFResource))) {
            return null;
        }
        RDFResource subjResource = (RDFResource)cls;

        Instance indexInst = kb.getInstance(indexEntity);
        if (indexInst == null) {
            return null;
        }

        boolean convertToSynonym = WHOFICContentModelConstants.TERM_SYNONYM_CLASS.equals(indexType);
        boolean convertToNarrower = WHOFICContentModelConstants.TERM_NARROWER_CLASS.equals(indexType);
        boolean convertToBaseIndex = ! (convertToSynonym || convertToNarrower);

        RDFSNamedClass termSynonymClass = cm.getTermSynonymClass();
        RDFSNamedClass termNarrowerClass = cm.getTermNarrowerClass();
        RDFSNamedClass termBaseIndexClass = cm.getTermBaseIndexClass();

        String user = KBUtil.getUserInSession(getThreadLocalRequest());
        String operationDescription = "Made base index term '" + indexInst.getBrowserText() + "' a " +
                (convertToSynonym ? "synonym" : convertToNarrower ? "narrower term" : "(unspecified) base index");

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {

                kb.beginTransaction(operationDescription, subject);

                if (termSynonymClass != null && termNarrowerClass != null && termBaseIndexClass != null) {
                    if (convertToSynonym && (! indexInst.hasDirectType(termSynonymClass))) {
                        indexInst.addDirectType(termSynonymClass);
                    }
                    else if (convertToNarrower && (! indexInst.hasDirectType(termNarrowerClass))) {
                        indexInst.addDirectType(termNarrowerClass);
                    }
                    else if (convertToBaseIndex && (! indexInst.hasDirectType(termBaseIndexClass))) {
                        indexInst.addDirectType(termBaseIndexClass);
                    }

                    if ((convertToNarrower || convertToBaseIndex) && indexInst.hasDirectType(termSynonymClass)) {
                        indexInst.removeDirectType(termSynonymClass);
                    }
                    if ((convertToSynonym || convertToBaseIndex) && indexInst.hasDirectType(termNarrowerClass)) {
                        indexInst.removeDirectType(termNarrowerClass);
                    }
                    if ((convertToSynonym || convertToNarrower) && indexInst.hasDirectType(termBaseIndexClass)) {
                        indexInst.removeDirectType(termBaseIndexClass);
                    }
                }

                RDFProperty synonymProperty = cm.getSynonymProperty();
                if (synonymProperty != null) {
                    if (convertToSynonym && (! subjResource.hasPropertyValue(synonymProperty, indexInst))) {
                        subjResource.addPropertyValue(synonymProperty, indexInst);
                    }
                    else if ((convertToNarrower || convertToBaseIndex) && subjResource.hasPropertyValue(synonymProperty, indexInst)) {
                        subjResource.removePropertyValue(synonymProperty, indexInst);
                    }
                }
                RDFProperty narrowerProperty = cm.getNarrowerProperty();
                if (narrowerProperty != null) {
                    if (convertToNarrower && (! subjResource.hasPropertyValue(narrowerProperty, indexInst))) {
                        subjResource.addPropertyValue(narrowerProperty, indexInst);
                    }
                    else if ((convertToSynonym || convertToBaseIndex) && subjResource.hasPropertyValue(narrowerProperty, indexInst)) {
                        subjResource.removePropertyValue(narrowerProperty, indexInst);
                    }
                }
                RDFProperty baseIndexProperty = cm.getBaseIndexProperty();
                if (baseIndexProperty != null) {
                    if ((convertToBaseIndex)&& (! subjResource.hasPropertyValue(baseIndexProperty, indexInst))) {
                        subjResource.addPropertyValue(baseIndexProperty, indexInst);
                    }
                    else if ((convertToSynonym || convertToNarrower)&& subjResource.hasPropertyValue(baseIndexProperty, indexInst)) {
                        subjResource.removePropertyValue(baseIndexProperty, indexInst);
                    }
                }

                kb.commitTransaction();

            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Error at changing index type for: " + subject + " and index entity: " + indexEntity, e);
                kb.rollbackTransaction();
                throw new RuntimeException("Error at changing index type for: " + subject + " and index entity: " + indexEntity +
                        ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        EntityPropertyValues res = new EntityPropertyValues(new EntityData(subject));
        for (String reifiedPropName : reifiedProps) {
            Slot reifiedSlot = kb.getSlot(reifiedPropName);
            if (reifiedSlot != null) {
                res.addPropertyValues(new PropertyEntityData(reifiedSlot.getName()), createEntityList(indexInst.getOwnSlotValues(reifiedSlot)));
            }
        }

        return res;
    }

    public EntityPropertyValues changeInclusionFlagForIndex(String projectName, String subject, String indexEntity,
            List<String> reifiedProps, boolean isInclusionFlag) {
        Project project = getProject(projectName);
        if (project == null) {
            return null;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel((OWLModel) kb);

        Cls cls = kb.getCls(subject);
        if (cls == null || (! (cls instanceof RDFResource))) {
            return null;
        }
        RDFResource subjResource = (RDFResource)cls;

        Instance indexInst = kb.getInstance(indexEntity);
        if (indexInst == null) {
            return null;
        }

        String user = KBUtil.getUserInSession(getThreadLocalRequest());
        String operationDescription = (isInclusionFlag ?
                "Made base index term '" + indexInst.getBrowserText() + "' also a base inclusion term." :
                "Removed base index term '" + indexInst.getBrowserText() + "' from the list of base inclusion terms.");

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {

                kb.beginTransaction(operationDescription, subject);

                RDFSNamedClass termBaseIndexClass = cm.getTermBaseInclusionClass();
                if (termBaseIndexClass != null) {
                    if (isInclusionFlag && (! indexInst.hasDirectType(termBaseIndexClass))) {
                        indexInst.addDirectType(termBaseIndexClass);
                    }
                    else {
                        indexInst.removeDirectType(termBaseIndexClass);
                    }
                }

                RDFProperty indexBaseInclusionProperty = cm.getIndexBaseInclusionProperty();
                if (indexBaseInclusionProperty != null) {
                    if (isInclusionFlag && (! subjResource.hasPropertyValue(indexBaseInclusionProperty, indexInst))) {
                        subjResource.addPropertyValue(indexBaseInclusionProperty, indexInst);
                    }
                    else {
                        subjResource.removePropertyValue(indexBaseInclusionProperty, indexInst);
                    }
                }

                kb.commitTransaction();

            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Error at changing inclusion flag for: " + subject + " and index entity: " + indexEntity, e);
                kb.rollbackTransaction();
                throw new RuntimeException("Error at changing inclusion flag for: " + subject + " and index entity: " + indexEntity +
                        ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        EntityPropertyValues res = new EntityPropertyValues(new EntityData(subject));
        for (String reifiedPropName : reifiedProps) {
            Slot reifiedSlot = kb.getSlot(reifiedPropName);
            if (reifiedSlot != null) {
                res.addPropertyValues(new PropertyEntityData(reifiedSlot.getName()), createEntityList(indexInst.getOwnSlotValues(reifiedSlot)));
            }
        }
        return res;
    }


    public void removeBaseIndexTerm(String projectName, String entityName,
            String value, String user, String operationDescription) {
        Project project = getProject(projectName);
        if (project == null) { return;  }
        KnowledgeBase kb = project.getKnowledgeBase();
        Instance subject = kb.getInstance(entityName);
        if (subject == null) { return; }

        Instance valueInst = kb.getInstance(value);
        if (valueInst == null) {
            throw new RuntimeException("Cannot find index term to remove: " +
                             value + " for entity: " + entityName);
        }

        WHOFICContentModel cm = getContentModel((OWLModel) kb);
        RDFProperty synonymProperty = cm.getSynonymProperty();
        RDFProperty narrowerProperty = cm.getNarrowerProperty();
        RDFProperty baseIndexProperty = cm.getBaseIndexProperty();

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                subject.removeOwnSlotValue(synonymProperty, valueInst);
                subject.removeOwnSlotValue(narrowerProperty, valueInst);
                subject.removeOwnSlotValue(baseIndexProperty, valueInst);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Error at removing base index term subj: " + subject + " value: " + value, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }


    public List<String> getListOfSelectedPostCoordinationAxes(String projectName,
    		String entity, List<String> reifiedProps) {
    	List<String> selectedPCAxisProperties = new ArrayList<String>();

    	Project project = getProject(projectName);
    	KnowledgeBase kb = project.getKnowledgeBase();
    	WHOFICContentModel cm = getContentModel((OWLModel)kb);

    	Slot allowedPcAxesProperty = cm.getAllowedPostcoordinationAxesProperty();
		Slot allowedPcAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
		Slot requiredPcAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

		//need to create a copy of the constant array list in order to be able to modify if (e.g. by calling .retainAll())
		List<String> pcAxisProperties = new ArrayList<String>(cm.getPostcoordinationAxesPropertyList());
		if (reifiedProps != null) {
			pcAxisProperties.retainAll(reifiedProps);
		}

    	Instance subjInst = kb.getInstance(entity);
    	Collection<?> pcSpecs = subjInst.getOwnSlotValues(allowedPcAxesProperty);
    	for (Object pcSpec : pcSpecs) {
			Instance pcSpecInst = (Instance)pcSpec;
			if (pcSpecInst != null) {
				Collection<?> allowedPcAxisPropertyValues = pcSpecInst.getOwnSlotValues(allowedPcAxisPropertyProperty);
				Collection<?> requiredPcAxisPropertyValues = pcSpecInst.getOwnSlotValues(requiredPcAxisPropertyProperty);
	            for (String pcAxisPropName : pcAxisProperties) {
	                Slot pcAxisProperty = kb.getSlot(pcAxisPropName);
	                if (allowedPcAxisPropertyValues.contains(pcAxisProperty)||
	                		requiredPcAxisPropertyValues.contains(pcAxisProperty)) {
	                	selectedPCAxisProperties.add(pcAxisPropName);
	                }
	            }
			}
		}

    	return selectedPCAxisProperties;
    }


    public List<EntityPropertyValues> getEntityPropertyValuesForPostCoordinationAxes(String projectName, List<String> entities, List<String> properties,
    		List<String> reifiedProps) {
    	WHOFICContentModel cm = null;
    	if (projectName != null) {
    		Project project = getProject(projectName);
    		if (project != null) {
    			cm = getContentModel((OWLModel)project.getKnowledgeBase());
    		}
    	}

    	List<String> regularReifiedProperties = new ArrayList<String>(reifiedProps);
    	List<String> specialReifiedProperties = new ArrayList<String>();
    	List<String> pcAxesPropertyList = (cm == null ? new ArrayList<String>() : 
    		cm.getPostcoordinationAxesPropertyList());
    	for (String reifiedProp : reifiedProps) {
    		if (pcAxesPropertyList.contains(reifiedProp)) {
    			regularReifiedProperties.remove(reifiedProp);
    			specialReifiedProperties.add(reifiedProp);
    		}
    	}

    	return getEntityPropertyValuesForPostCoordinationAxes(
        		projectName, entities, properties, regularReifiedProperties,
        		(cm == null ? null : cm.getAllowedPostcoordinationAxisPropertyProperty()),
        		(cm == null ? null : cm.getRequiredPostcoordinationAxisPropertyProperty()),
        		specialReifiedProperties);
    }

    public List<EntityPropertyValues> getEntityPropertyValuesForPostCoordinationAxes(String projectName, List<String> entities, List<String> properties,
            List<String> reifiedProps, RDFProperty allowedPcAxisProperty, RDFProperty requiredPcAxisProperty, List<String> pcAxisProperties) {

        List<EntityPropertyValues> entityPropValues = getEntityPropertyValues(projectName, entities, properties, reifiedProps);

        Project project = getProject(projectName);
        if (project == null) {
            return entityPropValues;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        if (entityPropValues == null) {
        	entityPropValues = new ArrayList<EntityPropertyValues>();
        }
        if (allowedPcAxisProperty != null && pcAxisProperties != null && pcAxisProperties.size() > 0) {
        	for (EntityPropertyValues epv : entityPropValues) {
        		String instanceName = epv.getSubject().getName();
				Instance valueInst = kb.getInstance(instanceName);
        		if (valueInst != null) {
	        		Collection<?> allowedPcAxisPropertyValues = valueInst.getOwnSlotValues(allowedPcAxisProperty);
	        		Collection<?> requiredPcAxisPropertyValues = valueInst.getOwnSlotValues(requiredPcAxisProperty);
		            for (String pcAxisPropName : pcAxisProperties) {
		                Slot pcAxisProperty = kb.getSlot(pcAxisPropName);
		                int value = 0;
		                if (allowedPcAxisPropertyValues.contains(pcAxisProperty)) {
		                	value |= 1;
		                }
		                if (requiredPcAxisPropertyValues.contains(pcAxisProperty)) {
		                	value |= 2;
		                }
		                if (pcAxisProperty != null) {
		                    epv.addPropertyValue(new PropertyEntityData(pcAxisProperty.getName()), createEntityData(value));
		                }
		            }
		            //entityPropValues.add(epv);
        		}
        		else {
        			assert false : "Unable to get instance " + instanceName + " which was created in getEntityPropertyValues(String, List, List, List) method";
        		}
        	}
        }


        return prepareLinearizationEntityPropertyValues(projectName, entities, entityPropValues, false);
    }


    /**
     * @return true if this is the first linearization where we have set
     *         this post-coordination axis as allowed. False otherwise.
     */
    public boolean addAllowedPostCoordinationAxis(String projectName, String subject,
    		 String postcoordinationEntity, String postcoordinationProperty, boolean isRequiredFlag) {
        Project project = getProject(projectName);
        if (project == null) {
            return false;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        if (! (kb instanceof OWLModel)) {
        	assert false : "The methods of ICDServiceImpl, including addAllowedPostCoordinationAxis, " +
        			"are suppose to work with OWL ontologies that conform to the ICD content model";
        	return false;
        }
        OWLModel owlModel = (OWLModel) kb;
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFResource subjResource = owlModel.getOWLNamedClass(subject);
        if (subjResource == null) {
            return false;
        }

        RDFResource postCoordInd = owlModel.getOWLIndividual(postcoordinationEntity);
        if (postCoordInd == null) {
            return false;
        }

        RDFProperty postCoordProperty = owlModel.getOWLProperty(postcoordinationProperty);
        if (postCoordProperty == null) {
            return false;
        }

        RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
        RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

        if (allowedPostCoordinationAxisPropertyProperty == null ||
        		requiredPostCoordinationAxisPropertyProperty == null) {
        	throw new RuntimeException("Invalid content model! The following properties could not be retrieved:" +
        		(allowedPostCoordinationAxisPropertyProperty == null ? " " + WHOFICContentModelConstants.ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP : "") +
        		(requiredPostCoordinationAxisPropertyProperty == null ? " " + WHOFICContentModelConstants.REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP : "") );
        }

        RDFProperty linViewProp = cm.getLinearizationViewProperty();
        RDFResource linView = (RDFResource)postCoordInd.getPropertyValue(linViewProp);

        boolean axisAlreadyUsed = isAxisPartOfAnyLinearization(cm, subjResource, postCoordProperty);

        String user = KBUtil.getUserInSession(getThreadLocalRequest());
		String linViewName = (linView == null ? "" : linView.getBrowserText());
        String operationDescription = "Added '" + postCoordProperty.getBrowserText() + "' as " +
        		(isRequiredFlag ? "a required" : "an allowed") + " post-coordination axis " +
        		"in the " + linViewName + " linearization of " + subjResource.getBrowserText() + ".";

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {

                kb.beginTransaction(operationDescription, subject);

                Collection<?> allowedPcAxes = postCoordInd.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
                Collection<?> requiredPcAxes = postCoordInd.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

                if (requiredPcAxes.contains(postCoordProperty)) {
                	postCoordInd.removePropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
                }
                if (allowedPcAxes.contains(postCoordProperty)) {
                	postCoordInd.removePropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
                }

                if (isRequiredFlag) {
                	postCoordInd.addPropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
                }
                else {
                	postCoordInd.addPropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
                }

                kb.commitTransaction();

            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Error at adding postcoordination property " + postcoordinationProperty + " for: " + subject +
                		" and post-coordination specification: " + postcoordinationEntity + " (" + linViewName + ")", e);
                kb.rollbackTransaction();
                throw new RuntimeException("Error at adding postcoordination property " + postcoordinationProperty + " for: " + subject +
                		" and post-coordination specification: " + postcoordinationEntity + " (" + linViewName + ")" +
                        ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        return (! axisAlreadyUsed);

    }


    /**
     * @return true if after we removed this post-coordination axis,
     *         there are no other linearizations where this property is set
     *         as a possible post-coordination axis. False otherwise.
     */
    public boolean removeAllowedPostCoordinationAxis(String projectName, String subject,
   		 String postcoordinationEntity, String postcoordinationProperty) {
        Project project = getProject(projectName);
        if (project == null) {
            return false;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        if (! (kb instanceof OWLModel)) {
        	assert false : "The methods of ICDServiceImpl, including addAllowedPostCoordinationAxis, " +
        			"are suppose to work with OWL ontologies that conform to the ICD content model";
        	return false;
        }
        OWLModel owlModel = (OWLModel) kb;
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFResource subjResource = owlModel.getOWLNamedClass(subject);
        if (subjResource == null) {
            return false;
        }

        RDFResource postCoordInd = owlModel.getOWLIndividual(postcoordinationEntity);
        if (postCoordInd == null) {
            return false;
        }

        RDFProperty postCoordProperty = owlModel.getOWLProperty(postcoordinationProperty);
        if (postCoordProperty == null) {
            return false;
        }

        RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
        RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

        if (allowedPostCoordinationAxisPropertyProperty == null ||
        		requiredPostCoordinationAxisPropertyProperty == null) {
        	throw new RuntimeException("Invalid content model! The following properties could not be retrieved:" +
        		(allowedPostCoordinationAxisPropertyProperty == null ? " " + WHOFICContentModelConstants.ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP : "") +
        		(requiredPostCoordinationAxisPropertyProperty == null ? " " + WHOFICContentModelConstants.REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP : "") );
        }

        RDFProperty linViewProp = cm.getLinearizationViewProperty();
        RDFResource linView = (RDFResource)postCoordInd.getPropertyValue(linViewProp);

        String user = KBUtil.getUserInSession(getThreadLocalRequest());
		String linViewName = (linView == null ? "" : linView.getBrowserText());
        String operationDescription = "Removed '" + postCoordProperty.getBrowserText() + "' as " +
        		"a possible post-coordination axis " +
        		"in the " + linViewName + " linearization of " + subjResource.getBrowserText() + ".";

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {

                kb.beginTransaction(operationDescription, subject);

                Collection<?> allowedPcAxes = postCoordInd.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
                Collection<?> requiredPcAxes = postCoordInd.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

                if (requiredPcAxes.contains(postCoordProperty)) {
                	postCoordInd.removePropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
                }
                if (allowedPcAxes.contains(postCoordProperty)) {
                	postCoordInd.removePropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
                }

                kb.commitTransaction();

            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Error at removing postcoordination property " + postcoordinationProperty + " for: " + subject +
                		" and post-coordination specification: " + postcoordinationEntity + " (" + linViewName + ")", e);
                kb.rollbackTransaction();
                throw new RuntimeException("Error at removing postcoordination property " + postcoordinationProperty + " for: " + subject +
                		" and post-coordination specification: " + postcoordinationEntity + " (" + linViewName + ")" +
                        ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        boolean axisUsed = isAxisPartOfAnyLinearization(cm, subjResource, postCoordProperty);
        return (! axisUsed);

    }


    private boolean isAxisPartOfAnyLinearization(WHOFICContentModel cm, RDFResource subjResource, RDFProperty postCoordProperty) {
    	RDFProperty allowedPostCoordinationAxesProperty = cm.getAllowedPostcoordinationAxesProperty();
        RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
        RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

        Collection<?> postCoordSpecifications = subjResource.getPropertyValues(allowedPostCoordinationAxesProperty);
    	for (Object postCoordSpec : postCoordSpecifications) {
    		RDFIndividual postCoordSpecInd = (RDFIndividual) postCoordSpec;
            Collection<?> allowedPcAxes = postCoordSpecInd.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
            Collection<?> requiredPcAxes = postCoordSpecInd.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

            if (allowedPcAxes.contains(postCoordProperty) || requiredPcAxes.contains(postCoordProperty)) {
            	return true;
            }
		}

		return false;
	}


	@Override
	public List<ScaleInfoData> getPostCoordinationAxesScales(String projectName,
			List<String> properties) {

    	Project project = getProject(projectName);
    	KnowledgeBase kb = project.getKnowledgeBase();
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
    	WHOFICContentModel cm = getContentModel(owlModel);
    	
    	RDFProperty referencedValueProperty = cm.getReferencedValueProperty();

		List<ScaleInfoData> res = new ArrayList<ScaleInfoData>();
		for (String property : properties) {
			RDFProperty prop = owlModel.getRDFProperty(property);
			if (prop == null) {
				Log.getLogger().warning("No property found with name: " + property + " in method ICDSerivceImpl.getPostCoordinationAxesScales. " +
						"Please check your configurations.");
			}
			else {
				//TODO deal with (or at least check for) multiple ranges (compare with rev. 27220)
				RDFResource range = prop.getRange();
				if (range == null) {
					Log.getLogger().warning("It is not possible to retrieve the (fixed) scale values for property " + property + " because it's range ot set");
				}
				else {
					OWLClass rangeCls = (OWLClass) range;
					Collection<?> allValidValues = rangeCls.getInstances(false);
					List<EntityData> propertyValues = new ArrayList<EntityData>();
					List<String> propertyValueDefinitions = new ArrayList<String>();
					for (Object rangeValue : allValidValues) {
						//TODO get the referencedValue and get the title and definition from it - DONE check below
						OWLIndividual ind = (OWLIndividual) rangeValue;
						Instance refInst = (Instance) ind.getPropertyValue(referencedValueProperty);
						propertyValues.add(new EntityData(ind.getName(), ind.getBrowserText()));
						String definition = getDefinition(cm, refInst);
						propertyValueDefinitions.add(definition);
					}
					//build property info:
					ScaleInfoData propertyInfo = new ScaleInfoData(propertyValues);
					propertyInfo.setProperty(new PropertyEntityData(property));
					propertyInfo.setDefinitions(propertyValueDefinitions);
					res.add(propertyInfo);
				}
			}
		}
		return res;
	}

	private String getDefinition(WHOFICContentModel cm, Instance inst) {
		String definition = null;
		if (inst instanceof RDFSNamedClass) {
	        RDFResource defTerm = cm.getTerm(((RDFSNamedClass)inst), cm.getDefinitionProperty());
	        if (defTerm != null) {
	            definition = (String) defTerm.getPropertyValue(cm.getLabelProperty());
	        }
		}
        return definition;
	}

	/**
	 * This method returns all superclasses or superproperties of a given class or property
	 * ordered from most specific to the most general. 
	 */
	public List<EntityData> getAllSuperEntities(String projectName, EntityData entity) {
		List<EntityData> superEntities = new ArrayList<EntityData>();

        if (entity == null || entity.getName() == null) {return superEntities;}

        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();

        Class<? extends RDFResource> type = null;
        RDFResource res = null;
    	RDFResource top = null;
       if (entity instanceof PropertyEntityData) {
        	type = RDFProperty.class;
        	res = owlModel.getRDFProperty(entity.getName());
        }
        else {
        	type = RDFSClass.class;
        	res = owlModel.getRDFSNamedClass(entity.getName());
    		top = getTopCategoryClass(owlModel);
        }

        if (type != null && res != null) {
        	List<RDFResource> superResources = getAllSuperEntities(owlModel, res, type, top);
    		for (RDFResource superRes : superResources) {
    			if (RDFProperty.class.equals(type)) {
    				superEntities.add(new PropertyEntityData(superRes.getName(), superRes.getBrowserText(), null));
    			}
    			else {
    				superEntities.add(new EntityData(superRes.getName(), superRes.getBrowserText()));
    			}
			}
        }
        
		return superEntities;
	}
	
    private List<RDFResource> getAllSuperEntities(OWLModel owlModel,
    		RDFResource res, Class<? extends RDFResource> type, RDFResource top) {
    	List<RDFResource> result = new ArrayList<RDFResource>();

    	collectAllSuperEntities(owlModel, res, type, top, result);
    	
		return result;
	}

	private void collectAllSuperEntities(OWLModel owlModel, RDFResource res,
			Class<? extends RDFResource> type, RDFResource top, List<RDFResource> result) {
		if ( res.equals(top) ) {
			return;
		}
		
		Collection<RDFResource> superEntities = getSuperEntities(res, type);
		List<RDFResource> toBeVisited = new ArrayList<RDFResource>();
		for (RDFResource parent : superEntities) {
			if (parent != null && /*(!parent.equals(top)) &&*/ (!result.contains(parent))) {
				result.add(parent);
				toBeVisited.add(parent);
			}
		}
		
		for (RDFResource parent : toBeVisited) {
			collectAllSuperEntities(owlModel, parent, type, top, result);
		}
	}


	@SuppressWarnings("unchecked")
	private Collection<RDFResource> getSuperEntities(RDFResource res, Class<? extends RDFResource> type) {
		if (RDFSClass.class.equals(type)) {
			return filterOutAnonymousClasses(((RDFSClass)res).getSuperclasses(false));
		}
		else if (RDFProperty.class.equals(type)) {
			return ((RDFProperty)res).getSuperproperties(false);
		}
		else {
			return Collections.emptyList();
		}
	}

	
	private Collection<RDFResource> filterOutAnonymousClasses(
			Collection<RDFResource> superclasses) {
		Collection<RDFResource> res = new ArrayList<RDFResource>();
		for (RDFResource cls : superclasses) {
			if (cls instanceof RDFSNamedClass) {
				res.add((RDFSNamedClass) cls);
			}
		}
		return res;
	}


	@Override
	public List<PrecoordinationClassExpressionData> getPreCoordinationClassExpressions(
			String projectName, String entity, List<String> properties) {
        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFSNamedClass cls = cm.getICDCategory(entity);
        Collection<PrecoordinationDefinitionComponent> propertyValues = cm.getPrecoordinationPropertyValues(cls, properties);
		List<PrecoordinationClassExpressionData> res = new ArrayList<PrecoordinationClassExpressionData>();
		for (Iterator<PrecoordinationDefinitionComponent> it = propertyValues.iterator(); it.hasNext();) {
			PrecoordinationDefinitionComponent defComp = (PrecoordinationDefinitionComponent) it.next();
			
			PrecoordinationClassExpressionData resData = new PrecoordinationClassExpressionData(
					defComp.getProperty(), defComp.isDefinitional());
			
			String value = defComp.getValue();
			ValueType valueType = defComp.getValueType();
			RDFResource valueRes = null;
			if (valueType == ValueType.INSTANCE) {
				valueRes = owlModel.getRDFIndividual(value);
			}
			else if (valueType == ValueType.CLS) {
				valueRes = owlModel.getOWLNamedClass(value);
			}
			
			if (valueRes != null) {
				resData.setValue(valueRes.getName(), valueRes.getBrowserText(), 
						(valueType == ValueType.INSTANCE ? 
								edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance : 
								edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Class));
				res.add(resData);
			}
			else {
				if (value != null) {
					Log.getLogger().warning("Problem at creating PrecoordinationClassExpressionData for" +
						"PrecoordinationDefinitionComponent: " + defComp + " (value could not be converted)");
				}
			}
		}
		return res;
	}


	@Override
	public List<AllowedPostcoordinationValuesData> getAllowedPostCoordinationValues(
			String projectName, String entity, List<String> customScaleProperties, 
			List<String> treeValueProperties, List<String> fixedScaleProperties) {
        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFSNamedClass cls = cm.getICDCategory(entity);
        RDFSNamedClass precoordSuperclass = cm.getPreecoordinationSuperclass(cls);
        
        List<AllowedPostcoordinationValuesData> res = new ArrayList<AllowedPostcoordinationValuesData>();
        if (precoordSuperclass == null) {
        	return res;
        }
        
        List<String> allProperties = new ArrayList<String>(customScaleProperties);
        allProperties.addAll(treeValueProperties);
        allProperties.addAll(fixedScaleProperties);
        
        List<String> relevantProperties = (List<String>) getListOfSelectedPostCoordinationAxes(projectName, precoordSuperclass.getName(), allProperties);
        List<String> relevantFixedScaleProperties = new ArrayList<String>(fixedScaleProperties);
        relevantFixedScaleProperties.retainAll(relevantProperties);
        Map<String,List<EntityData>> allowedFixedScaleValuesMap = 
        		getAllowedFixedScaleValues(projectName, owlModel, precoordSuperclass, relevantFixedScaleProperties);
        for (String propName : relevantProperties) {
        	AllowedPostcoordinationValuesData allowedPostcoordinationValuesData = new AllowedPostcoordinationValuesData(propName);
        	List<EntityData> propertyValues = null;
        	if (customScaleProperties.contains(propName)) {
        		propertyValues = getAllowedCustomScaleValues(cm, owlModel, precoordSuperclass, propName);
        	}
        	else if (treeValueProperties.contains(propName)) {
        		propertyValues = getAllowedTreeNodeValues(cm, owlModel, precoordSuperclass, propName);
        	}
        	else if (fixedScaleProperties.contains(propName)) {
        		propertyValues = allowedFixedScaleValuesMap.get(propName);
        	}
        	
        	if (propertyValues == null) {
        		allowedPostcoordinationValuesData.setValues(null);
        	}
        	else {
	        	for (EntityData propValue : propertyValues) {
	        		allowedPostcoordinationValuesData.addValue(propValue);
	        	}
        	}
    		res.add(allowedPostcoordinationValuesData);
        }

        return res;
	}

	private List<EntityData> getAllowedCustomScaleValues(WHOFICContentModel cm, OWLModel owlModel,
			RDFSNamedClass icdClass, String pcPropName) {
		String scalePropName = ICDContentModelConstants.PC_AXIS_PROP_TO_VALUE_SET_PROP.get(pcPropName);
		Collection<?> propertyValues = icdClass.getPropertyValues(owlModel.getRDFProperty(scalePropName));
		if (propertyValues == null || propertyValues.isEmpty()) {
			return null;
		}
		RDFSNamedClass pcScaleTermClass = cm.getPostcoordinationScaleTermClass();
		RDFSNamedClass pcValueRefClass = cm.getPostcoordinationValueReferenceClass();
		RDFProperty hasScaleValueProperty = cm.getHasScaleValueProperty();
		RDFProperty referencedValueProperty = cm.getReferencedValueProperty();
		
		List<EntityData> res = new ArrayList<EntityData>();
		for (Object propValue : propertyValues) {
			if (propValue instanceof OWLIndividual) {
				//get scale values 
				OWLIndividual scaleValueTerm = (OWLIndividual) propValue;
				if (scaleValueTerm.hasRDFType(pcScaleTermClass, true)) {
					Collection<?> scaleValues = scaleValueTerm.getPropertyValues(hasScaleValueProperty);
					for (Object scValue : scaleValues) {
						if (scValue instanceof OWLIndividual) {
//							//get referenced value
//							Instance refValue = getReferencedValue((OWLIndividual) scValue, 
//									pcValueRefClass, referencedValueProperty);
//							if (refValue != null) {
//								res.add(new EntityData(refValue.getName(), refValue.getBrowserText())); //set types as well, if relevant
//							}
							Instance scValInst = (OWLIndividual) scValue;
							res.add(new EntityData(scValInst.getName(), scValInst.getBrowserText())); //set types as well, if relevant
						}
						else {
							Log.getLogger().warning("Invalid scale value for PostcoordinationScaleTerm: " + scValue);
						}
					}
				}
				else {
					Log.getLogger().warning("Scale value term " + scaleValueTerm + " does not have the right type");
				}
			}
		}
		return res;
	}


	protected Instance getReferencedValue(OWLIndividual scaleValueInd,
			RDFSNamedClass pcValueRefClass, RDFProperty referencedValueProperty) {
		if (scaleValueInd.hasRDFType(pcValueRefClass,true)) {
			Object refValue = scaleValueInd.getPropertyValue(referencedValueProperty);
			if (refValue instanceof Instance) {
				return (Instance)refValue;
			}
			else {
				Log.getLogger().warning("Invalid referenced value for PostcoordinationValueReference: " + scaleValueInd);
			}
		}
		else {
			Log.getLogger().warning("Scale value " + scaleValueInd + " does not have the right type");
		}
		return null;
	}


	private List<EntityData> getAllowedTreeNodeValues(WHOFICContentModel cm, OWLModel owlModel,
			RDFSNamedClass icdClass, String pcPropName) {
		
		RDFSNamedClass pcValueRefClass = cm.getPostcoordinationValueReferenceClass();
		RDFProperty referencedValueProperty = cm.getReferencedValueProperty();

		Collection<?> propertyValues = icdClass.getPropertyValues(owlModel.getRDFProperty(pcPropName));
		if (propertyValues == null || propertyValues.isEmpty()) {
			return null;
		}

		List<EntityData> res = new ArrayList<EntityData>();
		for (Object scValue : propertyValues) {
			if (scValue instanceof OWLIndividual) {
				//get referenced value
				Instance refValue = getReferencedValue((OWLIndividual) scValue, 
						pcValueRefClass, referencedValueProperty);
				if (refValue != null) {
					res.add(new EntityData(refValue.getName(), refValue.getBrowserText())); //set types as well, if relevant
				}
			}
			else {
				Log.getLogger().warning("Invalid scale value for PostcoordinationScaleTerm: " + scValue);
			}
		}
		
		return res;
	}


	private Map<String, List<EntityData>> getAllowedFixedScaleValues(String projectName, OWLModel owlModel,
			RDFSNamedClass icdClass, List<String> relevantFixedScaleProperties) {
        Map<String, List<EntityData>> res = new HashMap<String, List<EntityData>>();
        
        List<ScaleInfoData> postCoordinationAxesScales = getPostCoordinationAxesScales(projectName, relevantFixedScaleProperties);
        for (ScaleInfoData scInfoData : postCoordinationAxesScales) {
        	List<EntityData> values = new ArrayList<EntityData>();
        	for (int i = 0; i < scInfoData.getValueCount(); i++) {
        		values.add(scInfoData.getScaleValue(i));
        	}
    		res.put(scInfoData.getProperty().getName(), values);
        }
        
        return res;
	}


	@Override
	public boolean setPrecoordinationPropertyValue(String projectName, String entity,
			String property, EntityData oldValue, EntityData newValue) {

        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFSNamedClass cls = cm.getICDCategory(entity);
		return cm.setPrecoordinationDefinitionPropertyValue(cls, property, 
				(oldValue == null ? null : oldValue.getName()), (newValue == null ? null : newValue.getName()));
	}
	
	@Override
	public boolean changeIsDefinitionalFlag(String projectName, String entity,
			String property, boolean isDefinitionalFlag) {

        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFSNamedClass cls = cm.getICDCategory(entity);
		return cm.changeIsDefinitionalFlag(cls, property, isDefinitionalFlag);
	}
	
	
	@Override
    public boolean reorderSiblings(String projectName, String movedClass, String targetClass, boolean isBelow, String parent) {

        String user = KBUtil.getUserInSession(getThreadLocalRequest());
        if (user == null) {
            return false;
        }

        Project project = getProject(projectName);
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        WHOFICContentModel cm = getContentModel(owlModel);

        RDFSNamedClass movedCls = owlModel.getRDFSNamedClass(movedClass);
        RDFSNamedClass targetCls = owlModel.getRDFSNamedClass(targetClass);
        RDFSNamedClass parentCls = owlModel.getRDFSNamedClass(parent);

        if (movedCls == null || targetClass == null || parentCls == null) {
            return false;
        }

        String opDescription = "Changed order of  " + movedCls.getBrowserText() + " in parent: " + parentCls.getBrowserText() +
                ". Moved " + (isBelow ? "below " : "above") + " " +
                targetCls.getBrowserText();

        boolean success = false;

        boolean eventsEnabled = owlModel.setGenerateEventsEnabled(false);
        synchronized (owlModel) {
            KBUtil.morphUser(owlModel, user);
            try {
                owlModel.beginTransaction(opDescription, movedClass);

                success = cm.reorderSibling(movedCls, targetCls, isBelow, parentCls, user);

                owlModel.commitTransaction();
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error on operation: " + opDescription, e);
                owlModel.rollbackTransaction();
                throw new RuntimeException("Error on operation: " + opDescription, e);
            } finally {
                KBUtil.restoreUser(owlModel);
                owlModel.setGenerateEventsEnabled(eventsEnabled);
            }

        return success;
        }
    }

	protected WHOFICContentModel getContentModel(OWLModel owlModel) {
		return new ICIContentModel(owlModel);
		//return new ICDContentModel(owlModel);
	}
	
	protected RDFResource getTopCategoryClass(OWLModel owlModel) {
		return new ICIContentModel(owlModel).getICICategoryClass();
		//return new ICDContentModel(owlModel).getICDCategoryClass();
	}
	
	/**** Internal reference ***/
	
	public EntityData createInternalReference(String projectName, 
			EntityData entity, String referenceClassName,
			String referencePropertyName, String referencedValuePropertyName,
			EntityData referencedEntity,
			String user, String operationDescription) {

		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Instance instance = kb instanceof OWLModel ?
				((OWLModel) kb).getRDFResource(entity.getName()) :
					kb.getInstance(entity.getName());

		if (instance == null) {
			throw new RuntimeException("Failed to import reference. Entity does not exist: " + entity.getName());
		}

		Slot referenceSlot = kb instanceof OWLModel ? 
				((OWLModel) kb).getRDFProperty(referencePropertyName) :
					kb.getSlot(referencePropertyName);

		if (referenceSlot == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the reference property is not part of the ontology. Property name: "
					+ referencePropertyName);
		}
		
		Slot referencedValueSlot = kb instanceof OWLModel ? 
				((OWLModel) kb).getRDFProperty(referencedValuePropertyName) :
					kb.getSlot(referencedValuePropertyName);

		if (referencedValueSlot == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the referenced value property is not part of the ontology. Property name: "
					+ referencedValuePropertyName);
		}
		
		Cls refClass = kb.getCls(referenceClassName);
		
		if (refClass == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the reference class is not part of the ontology. Class name: "
					+ referenceClassName);
		}
		
		Instance refEntity = kb.getInstance(referencedEntity.getName());
		
		if (refEntity == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the referenced entity not part of the ontology. Entity name: "
					+ referencedEntity.getName());
		}
		
		Instance refInstance = null;
		synchronized (kb) {
            KBUtil.morphUser(kb, user);

            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }
                
                refInstance = refClass.createDirectInstance(IDGenerator.getNextUniqueId());
                
                refInstance.setOwnSlotValue(referencedValueSlot, refEntity);
                
                instance.addOwnSlotValue(referenceSlot, refInstance);
                
                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE,
                        "Could not create internal reference in " + projectName + " for entity " + entity.getName(), e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Could not create internal reference for entity " + entity.getName() + ". Message: "
                        + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

		return OntologyServiceImpl.createEntityData(refInstance);
	}
}
