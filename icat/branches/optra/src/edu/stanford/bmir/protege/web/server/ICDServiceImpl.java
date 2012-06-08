package edu.stanford.bmir.protege.web.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.bmir.protege.icd.export.ExportICDClassesJob;
import edu.stanford.bmir.protege.web.client.rpc.ICDService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICDServiceImpl extends OntologyServiceImpl implements ICDService {

    private static final long serialVersionUID = -10148579388542388L;
    private static final String EXCEL_FILE_EXTENSION = ".xls";

    @SuppressWarnings("rawtypes")
    public EntityData createICDCls(final String projectName, String clsName, Collection<String> superClsNames,
            String title, String sortingLabel, boolean createICDSpecificEntities, final String user, final String operationDescription,
            final String reasonForChange) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        ICDContentModel cm = new ICDContentModel((OWLModel) kb);

        RDFSNamedClass cls = null;

        if (clsName != null && ((OWLModel) kb).getRDFSNamedClass(clsName) != null) {
            throw new RuntimeException("A class with the same name '" + clsName + "' already exists in the model.");
        }

        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

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
            }
        }

        //TODO: the note is not created here anymore, but with a different remote call from the client
        //so, theoretically, we do no need the reason for change here, but it doesn't hurt for now
        EntityData entityData = createEntityData(cls, false);
        if (reasonForChange != null && reasonForChange.length() > 0) { //this should be handled already
            entityData.setLocalAnnotationsCount(1);
        }

        entityData.setTypes(createEntityList((List) cls.getDirectTypes()));

        return entityData;
    }

    public List<EntityPropertyValues> getEntityPropertyValuesForLinearization(String projectName, List<String> entities, List<String> properties,
            List<String> reifiedProps) {
        List<EntityPropertyValues> entityPropertyValues = getEntityPropertyValues(projectName, entities, properties, reifiedProps);
        if (entityPropertyValues == null){
            entityPropertyValues = new ArrayList<EntityPropertyValues>();
        }
        ArrayList<EntityPropertyValues> result = new ArrayList<EntityPropertyValues>(entityPropertyValues);
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
}
