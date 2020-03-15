package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.HierarchyService;
import edu.stanford.bmir.protege.web.client.rpc.iCATException;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;
import edu.stanford.bmir.whofic.WHOFICContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 *
 * Services to handle changes in the hierarchy: add/remove parents, retire.
 * As of Jan. 15, 2014, this class is ICD specific because it will call the SiblingReordering
 * class from ICD content model.
 *
 * @author ttania
 *
 */
public class HierarchyServiceImpl extends RemoteServiceServlet implements HierarchyService {

    private static final long serialVersionUID = -2929819058734836756L;

    public static final String RETIRED_CLASSES = "Retired";

    protected Project getProject(String projectName) {
        return ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
    }

    public List<EntityData> changeParent(String project, String className, Collection<String> parentsToAdd,
            Collection<String> parentsToRemove, String user, String operationDescription, String reasonForChange)
    		throws iCATException {
        Project prj = getProject(project);
        KnowledgeBase kb = prj.getKnowledgeBase();

        Cls cls = KBUtil.getCls(kb, className);
        if (cls == null) {
            throw new IllegalArgumentException("Class " + className + " does not exist.");
        }

        WHOFICContentModel cm = new ICDContentModel((OWLModel) kb);

        Collection<Cls> parentClsesToAdd = KBUtil.getCollection(kb, parentsToAdd, Cls.class);
        Collection<Cls> parentClsesToRemove = KBUtil.getCollection(kb, parentsToRemove, Cls.class);
        
        if (checkNonRetireableClsHasRetiredNewParent((OWLModel) kb, cls, parentClsesToAdd) == true) {
        	throw new iCATException("Cannot retire class " + cls.getBrowserText() + " because it is non-retirable.");
        }

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                for (Cls parent : parentClsesToAdd) {
                    if (!cls.hasDirectSuperclass(parent)) {
                        boolean isSiblingIndexValid = cm.checkIndexAndRecreate((RDFSNamedClass) parent, false);
                        cls.addDirectSuperclass(parent);
                        cm.addChildToIndex((RDFSNamedClass) parent, (RDFSNamedClass) cls, isSiblingIndexValid);
                    }
                }

                for (Cls parent: parentClsesToRemove) {
                    if (cls.hasDirectSuperclass(parent)) {
                        boolean isSiblingIndexValid = cm.checkIndexAndRecreate((RDFSNamedClass) parent, false);
                        cls.removeDirectSuperclass(parent);
                        cm.removeChildFromIndex((RDFSNamedClass) parent, (RDFSNamedClass) cls, isSiblingIndexValid);
                    }
                }

                //if the operation has created an orphan cycle add root class as a parent
                if (!cls.getSuperclasses().contains(kb.getRootCls())) {
                    cls.addDirectSuperclass(kb.getRootCls());
                }

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {

                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        List<EntityData> res = null;
        if (cls instanceof OWLClass) {
            OWLClass owlcls = (OWLClass)cls;
            //if contains loop
            if (owlcls.getSuperclasses(true).contains(owlcls)) {
                ArrayList<OWLClass> cyclePath = new ArrayList<OWLClass>();
                KBUtil.getPathToSuperClass(owlcls, owlcls, cyclePath);
                //if we really found a cycle (i.e. there was a real cycle that did not involve anonymous classes)
                if (cyclePath.size() > 1) {
                    res = OntologyServiceImpl.createEntityList(cyclePath);
                }
            }
        }
        else {
            //if contains loop
            if (cls.getSuperclasses().contains(cls)) {
                ArrayList<Cls> cyclePath = new ArrayList<Cls>();
                KBUtil.getPathToSuperClass(cls, cls, cyclePath);
                res = OntologyServiceImpl.createEntityList(cyclePath);
            }
        }

        if (reasonForChange != null && reasonForChange.length() > 0) {
            ChAOServiceImpl chaoService = new ChAOServiceImpl();
            NotesData notesData = new NotesData();
            notesData.setAuthor(user);
            notesData.setBody(reasonForChange);
            notesData.setAnnotatedEntity(new EntityData(className));
            notesData.setSubject("[Reason for change]: " + operationDescription);
            chaoService.createNote(project, notesData, false);
        }

        return res;
    }
    
    
    private boolean checkNonRetireableClsHasRetiredNewParent(OWLModel owlModel, Cls cls, Collection<Cls> parents) {
    	
    	if (cls instanceof RDFSNamedClass == false) {
    		return false;
    	}
    	
    	if (RetirementManager.isNonRetirableId(cls.getName()) == false) {
    		return false;
    	}
    	
    	
    	for (Cls parent : parents) {
			if (parent instanceof RDFSNamedClass == true && 
					RetirementManager.isInRetiredTree(owlModel, (RDFSNamedClass)parent)) {
				return true;
			}
		}
    	
    	return false;
    }

    /*
     * This method will not update the sibling index, because it is not used by icd anymore.
     */
    public void retireClasses(String project, Collection<String> classesToRetireNames, boolean retireChildren,
            String newParentName, String reasonForChange, String operationDescription, String user) {
        Project prj = getProject(project);
        KnowledgeBase kb = prj.getKnowledgeBase();

        Collection<Cls> classesToRetire = KBUtil.getCollection(kb, classesToRetireNames, Cls.class);
        Cls retiredSuperCls = getRetiredSuperclass(kb);
        Cls newParent = newParentName == null ? null : kb.getCls(newParentName);

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                for (Cls cls : classesToRetire) {
                    retireCls(kb, cls, retiredSuperCls, newParent, retireChildren);

                    //FIXME: move to an outside transaction
                    //TODO: maybe we should add the cause to all the retired children
                    if (reasonForChange != null && reasonForChange.length() > 0) {
                        ChAOServiceImpl chaoService = new ChAOServiceImpl();
                        NotesData notesData = new NotesData();
                        notesData.setAuthor(user);
                        notesData.setBody(reasonForChange);
                        notesData.setAnnotatedEntity(new EntityData(cls.getName()));
                        notesData.setSubject("[Reason for change]: " + operationDescription);
                        chaoService.createNote(project, notesData, false);
                    }
                }

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }


    private Cls getRetiredSuperclass(KnowledgeBase kb) {
        Cls retiredSuperclass = null;
        if (kb instanceof OWLModel) {
            retiredSuperclass = ((OWLModel)kb).getRDFSNamedClass(RETIRED_CLASSES);
            if (retiredSuperclass == null) {
                retiredSuperclass = ((OWLModel)kb).createOWLNamedClass(RETIRED_CLASSES);
            }
        } else {
            retiredSuperclass = kb.getCls(RETIRED_CLASSES);
            if (retiredSuperclass == null) {
                retiredSuperclass = kb.createCls(RETIRED_CLASSES, kb.getRootClses());
            }
        }
        return retiredSuperclass;
    }

    private void retireCls(KnowledgeBase kb, Cls cls, Cls retiredSuperCls, Cls newParent, boolean retireChildren) {
        retireCls(kb, cls, retiredSuperCls);
        if (retireChildren) {
            for (Iterator iterator = cls.getSubclasses().iterator(); iterator.hasNext();) {
                Cls child = (Cls) iterator.next();
                retireCls(kb, child, retiredSuperCls);
            }
        } else {
            //TODO: what to do if new parent is null?
            if (newParent == null) {
                newParent = kb.getRootCls();
            }
            for (Iterator iterator = cls.getDirectSubclasses().iterator(); iterator.hasNext();) {
                Cls child = (Cls) iterator.next();
                if (!child.hasDirectSuperclass(newParent)) {
                    child.addDirectSuperclass(newParent);
                    child.removeDirectSuperclass(cls);
                }
            }
        }


    }

    private void retireCls(KnowledgeBase kb, Cls cls, Cls retiredSuperCls) {
        if (!(cls instanceof Cls) || !(cls instanceof RDFSNamedClass)) {
            return;
        }
        Collection<Cls> parents = cls instanceof RDFSNamedClass ? ((RDFSNamedClass)cls).getSuperclasses(false) : cls.getDirectSuperclasses();
        for (Cls parent : parents) {
            cls.removeDirectSuperclass(parent);
        }
        cls.addDirectSuperclass(retiredSuperCls);
        if (cls instanceof RDFSNamedClass) {
            ((RDFSNamedClass)cls).setDeprecated(true);
        }
    }

	@Override
	public void bogusCall(iCATException ex) {
		//this call does nothing, it just forces the GWT compiler to compile the iCATException class..
		//weird..
	}


}
