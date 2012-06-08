package edu.stanford.bmir.protege.web.server;

import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.bmir.protege.web.client.rpc.ICDService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICDServiceImpl extends OntologyServiceImpl implements ICDService {

    private static final long serialVersionUID = -10148579388542388L;

    public EntityData createICDCls(final String projectName, String clsName, Collection<String> superClsNames,
            String title, final String user, final String operationDescription, final String reasonForChange) {
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

                cls = cm.createICDCategory(clsName, superClsNames);

                if (clsName != null) {
                    cls.addPropertyValue(cm.getIcdCodeProperty(), clsName);
                }

                RDFResource titleTerm = cm.createTitleTerm();
                cm.fillTerm(titleTerm, null, title, null);
                cm.addTitleTermToClass(cls, titleTerm);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger()
                        .log(Level.SEVERE, "Error at creating class in " + projectName + " class: " + clsName, e);
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

        return entityData;
    }

}
