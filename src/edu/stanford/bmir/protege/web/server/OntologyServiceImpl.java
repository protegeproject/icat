package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.rpc.OntologyService;
import edu.stanford.bmir.protege.web.client.rpc.data.AnnotationData;
import edu.stanford.bmir.protege.web.client.rpc.data.ConditionItem;
import edu.stanford.bmir.protege.web.client.rpc.data.ConditionSuggestion;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.rpc.data.PaginationData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyType;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameCounts;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.query.api.QueryApi;
import edu.stanford.smi.protege.query.api.QueryConfiguration;
import edu.stanford.smi.protege.query.indexer.IndexUtilities;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Tree;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLOntology;
import edu.stanford.smi.protegex.owl.model.ProtegeNames;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.XSPNames;
import edu.stanford.smi.protegex.owl.model.classparser.OWLClassParseException;
import edu.stanford.smi.protegex.owl.model.classparser.OWLClassParser;
import edu.stanford.smi.protegex.owl.model.classparser.ParserUtils;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.model.impl.OWLSystemFrames;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;
import edu.stanford.smi.protegex.owl.model.util.DLExpressivityChecker;
import edu.stanford.smi.protegex.owl.model.util.ModelMetrics;
import edu.stanford.smi.protegex.owl.ui.code.OWLResourceNameMatcher;
import edu.stanford.smi.protegex.owl.ui.conditions.ConditionsTableItem;
import edu.stanford.smi.protegex.owl.ui.conditions.ConditionsTableModel;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {
    private static final long serialVersionUID = -4229789001933130232L;
    private static final int MIN_SEARCH_STRING_LENGTH = 3;

    protected Project getProject(String projectName) {
        return ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
    }

    public Integer loadProject(String projectName) {
        ServerProject<Project> serverProject = Protege3ProjectManager.getProjectManager().getServerProject(projectName);
        if (serverProject == null) {
            return null;
        }
        return Integer.valueOf(serverProject.getServerVersion());

    }

    public String getOntologyURI(String projectName) {
        Project project = getProject(projectName);
        RDFResource owlOntology = getOWLOntologyObject(project);
        return owlOntology.getURI();
    }

    public List<AnnotationData> getAnnotationProperties(String projectName, String entityName) {
        ArrayList<AnnotationData> annotations = new ArrayList<AnnotationData>();
        Project project = getProject(projectName);

        // Frames ontologies don't have annotations.
        if (!isOWLOntology(project)) {
            return annotations;
        }

        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        RDFResource owlOntology = owlModel.getOWLOntologyByURI(URIUtilities.createURI(entityName));

        // Loop through RDF properties for the ontology object.
        for (Object o : owlOntology.getRDFProperties()) {
            RDFProperty property = (RDFProperty) o;

            // Filter on annotation properties.
            if (property.isAnnotationProperty()) {

                // Loop through property values for each property.
                for (Object value : owlOntology.getPropertyValues(property)) {
                    String propName = property.getBrowserText();
                    AnnotationData annotation = new AnnotationData(propName);

                    /*
                     * TODO: This code needs to be fixed later. It makes the
                     * assumption that all values are strings, but this will not
                     * be true going forward.
                     */
                    if (value instanceof RDFSLiteral) {
                        RDFSLiteral literal = (RDFSLiteral) value;
                        annotation.setLang(literal.getLanguage());
                        annotation.setValue(literal.getString());
                    } else {
                        annotation.setValue(value.toString());
                    }

                    annotations.add(annotation);
                }
            }
        }

        Collections.sort(annotations, new AnnotationDataComparator());

        return annotations;
    }

    class AnnotationDataComparator implements Comparator<AnnotationData> {
        public int compare(AnnotationData ad1, AnnotationData ad2) {
            return ad1.getName().compareTo(ad2.getName());
        }
    }

    private RDFResource getOWLOntologyObject(Project project) {
        RDFResource owlOntology = null;

        if (project != null) {
            OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
            owlOntology = owlModel.getDefaultOWLOntology();
        }

        return owlOntology;
    }

    public List<EntityData> getDirectTypes(String projectName, String instanceName) {
        Project project = getProject(projectName);
        if (project == null) {
            return null;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        final Instance instance = kb.getInstance(instanceName);
        if (instance == null) {
            return null;
        }

        return createEntityList(instance.getDirectTypes());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<SubclassEntityData> getSubclasses(String projectName, String className) {
        ArrayList<SubclassEntityData> subclassesData = new ArrayList<SubclassEntityData>();

        Project project = getProject(projectName);
        if (project == null) {
            return subclassesData;
        }
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls superCls = kb.getCls(className);
        if (superCls == null) {
            return subclassesData;
        }

        ArrayList<Cls> subclasses = new ArrayList<Cls>(superCls.getVisibleDirectSubclasses());
        Collections.sort(subclasses, new FrameComparator());

        for (Object element : subclasses) {
            Cls subcls = (Cls) element;

            if (!subcls.isSystem()) {
                SubclassEntityData subclassEntityData = new SubclassEntityData(subcls.getName(),
                        getBrowserText(subcls), createEntityList(subcls.getDirectTypes()), subcls.getVisibleDirectSubclassCount());
                subclassesData.add(subclassEntityData);
                subclassEntityData.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(subcls));
                subclassEntityData.setChildrenAnnotationsCount(HasAnnotationCache.getChildrenAnnotationCount(subcls));

                String user = KBUtil.getUserInSession(getThreadLocalRequest());
                if (user != null) {
                    subclassEntityData.setWatch(WatchedEntitiesCache.getCache(project).getWatchType(user, subcls.getName()));
                }

            }
        }

        return subclassesData;
    }

    //TODO: move to utility
    public static String getBrowserText(Frame frame) {
        String bt = frame.getBrowserText();
        if (bt.contains("'")) {
            //delete any leading and trailing 's if present
            bt = bt.replaceAll("^'|'$", "");
            //delete all 's preceding or following any of these characters: [SPACE].-_
            bt = bt.replaceAll("'([ .-_])|([ .-_])'", "$1$2");
        }
        return bt;
    }

    public EntityData getRootEntity(String projectName) {
        Project project = getProject(projectName);

        if (project == null) {
            return null;
        }

        KnowledgeBase kb = project.getKnowledgeBase();
        Cls root = kb.getRootCls();

        if (kb instanceof OWLModel) {
            root = ((OWLModel) kb).getOWLThingClass();
        }

        EntityData rootEd =  createEntityData(root);

        String user = KBUtil.getUserInSession(getThreadLocalRequest());
        rootEd.setWatch(WatchedEntitiesCache.getCache(project).getWatchType(user, root.getName()));

        return rootEd;
    }

    public EntityData getEntity(String projectName, String entityName) {
        Project project = getProject(projectName);

        if (project == null) {
            return null;
        }

        KnowledgeBase kb = project.getKnowledgeBase();
        Frame frame = kb.getFrame(entityName);

        if (frame == null && kb instanceof OWLModel) {
            frame = ((OWLModel) kb).getRDFResource(entityName);
        }

        if (frame == null) {
            return null;
        }

        EntityData ed = createEntityData(frame);
        ed.setTypes(createEntityList(((Instance) frame).getDirectTypes()));
        ed.setWatch(WatchedEntitiesCache.getCache(project).getWatchType(KBUtil.getUserInSession(getThreadLocalRequest()), frame.getName()));

        return ed;
    }

    public ArrayList<EntityData> getSubproperties(String projectName, String propertyName) {
        ArrayList<EntityData> subpropertyData = new ArrayList<EntityData>();

        Project project = getProject(projectName);
        if (project == null) {
            return subpropertyData;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        ArrayList<Slot> subproperties = new ArrayList<Slot>();
        if (propertyName == null) { // property root case
            subproperties.addAll(getRootProperties(kb));
        } else {
            Slot superProperty = kb.getSlot(propertyName);
            if (superProperty == null) {
                return subpropertyData;
            }
            subproperties.addAll(getSubSlots(superProperty));
        }

        Collections.sort(subproperties, new FrameComparator<Slot>());

        //FIXME: Decide which system props to show. For now, select the most commonly used ones.
        if (kb instanceof OWLModel && propertyName == null) {
            OWLModel owlModel = (OWLModel) kb;
            subproperties.add(0, owlModel.getSystemFrames().getRdfsIsDefinedByProperty());
            subproperties.add(0, owlModel.getSystemFrames().getRdfsSeeAlsoProperty());
            subproperties.add(0, owlModel.getRDFSCommentProperty());
            subproperties.add(0, owlModel.getRDFSLabelProperty());
        }

        for (Slot subprop : subproperties) {
            PropertyEntityData entityData = createPropertyEntityData(subprop, null, true);
            entityData.setPropertyType(getPropertyType(subprop));
            subpropertyData.add(entityData);
            entityData.setIsSystem(subprop.isSystem());
        }

        return subpropertyData;
    }

    public List<EntityData> getProperties(String projectName, List<String> props) {
        ArrayList<EntityData> propsData = new ArrayList<EntityData>();

        Project project = getProject(projectName);
        if (project == null) {
            return propsData;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        for (String prop : props) {
			Slot slot = kb.getSlot(prop);
			if (slot != null) {
				PropertyEntityData entityData = createPropertyEntityData(slot, null, true);
	            entityData.setPropertyType(getPropertyType(slot));
	            propsData.add(entityData);
			}
		}

        return propsData;
    }

    private Collection<Slot> getSubSlots(Slot parentSlot) {
        Collection<Slot> slots = new ArrayList<Slot>();
        for (@SuppressWarnings("unchecked")
        Iterator<Slot> iterator = parentSlot.getSubslots().iterator(); iterator.hasNext();) {
            Slot slot = iterator.next();
            if (slot.isSystem() == false) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public static PropertyType getPropertyType(Slot slot) {
        if (slot instanceof OWLObjectProperty) {
            return PropertyType.OBJECT;
        } else if (slot instanceof OWLDatatypeProperty) {
            return PropertyType.DATATYPE;
        } else if (slot instanceof RDFProperty && ((RDFProperty) slot).isAnnotationProperty()) {
            return PropertyType.ANNOTATION;
        }
        return null;
    }

    public void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
            EntityData valueEntityData, boolean copyIfTemplate, String user, String operationDescription) {
        Project project = getProject(projectName);
        if (project == null) {
            throw new IllegalArgumentException("Add operation failed. Unknown project: " + projectName);
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        
        //CacheControlJob.setCacheStatus(kb, true, true);
        
        Instance subject = kb.getInstance(entityName);
        if (subject == null) {
            throw new IllegalArgumentException("Add operation failed. Unknown subject: " + entityName);
        }
        Slot property = kb.getSlot(propertyEntity.getName());
        if (property == null) {
            new IllegalArgumentException("Add operation failed. Unknown property: " + propertyEntity.getName());
        }

        Object value = getProtegeObject(kb, valueEntityData, property);
        if (value != null) {
            synchronized (kb) {
                KBUtil.morphUser(kb, user);
                boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
                try {
                    if (runsInTransaction) {
                        kb.beginTransaction(operationDescription);
                    }

                    if (copyIfTemplate && KBUtil.isTemplateInstance(value)) {
                    	value = KBUtil.getCopyOfTemplateInstance(value);
                    }
                    
                    subject.addOwnSlotValue(property, value);

                    if (runsInTransaction) {
                        kb.commitTransaction();
                    }
                } catch (Exception e) {
                    Log.getLogger().log(
                            Level.SEVERE,
                            "Error at setting value in " + projectName + " subj: " + subject + " pred: " + property
                            + " value: " + value, e);
                    if (runsInTransaction) {
                        kb.rollbackTransaction();
                    }
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    KBUtil.restoreUser(kb);
                }
            }
        } else {
            throw new IllegalArgumentException("Add operation failed. Invalid value: " + valueEntityData.getName());
        }
    }


	public void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
            EntityData valueEntityData, boolean deleteIfFromTemplate, String user, String operationDescription) {
        Project project = getProject(projectName);
        if (project == null) { return;  }
        KnowledgeBase kb = project.getKnowledgeBase();
        
        //CacheControlJob.setCacheStatus(kb, true, true);
        
        Instance subject = kb.getInstance(entityName);
        if (subject == null) { return; }
        Slot property = kb.getSlot(propertyEntity.getName());
        if (property == null) { return; }

        Object value = getProtegeObject(kb, valueEntityData, property);

        if (value == null || value.equals("")) { return; }

        if (value instanceof RDFSLiteral) {
            value = value.toString();
        }

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                //TODO implement this 
//                if (deleteIfFromTemplate && isCopyOfATemplateInstance(value) && isLastStatementWithObject(value)) {
//                	kb.deleteInstance(value);
//                }
//                else
                subject.removeOwnSlotValue(property, value);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Error at removing value in " + projectName + " subj: " + subject + " pred: " + property
                        + " value: " + value, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }


    public void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
            EntityData oldValue, EntityData newValue, boolean copyIfTemplate, String user, String operationDescription) {
        Project project = getProject(projectName);
        if (project == null) {
            return;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        
        //CacheControlJob.setCacheStatus(kb, true, true);
        
        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }
                if (newValue !=null && newValue.getName() != null) {
                    addPropertyValue(projectName, entityName, propertyEntity, newValue, copyIfTemplate, user, null);
                    KBUtil.morphUser(kb, user); //hack
                }
                if (oldValue != null && oldValue.getName() != null) {
                    removePropertyValue(projectName, entityName, propertyEntity, oldValue, copyIfTemplate, user, null);
                    KBUtil.morphUser(kb, user); //hack
                }

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,  "Error at replacing value in " + projectName + " subj: " + entityName +
                        " pred: " + propertyEntity + " old value: " + oldValue + " new value: " + newValue, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }

    public void setPropertyValues(String projectName, String entityName, PropertyEntityData propertyEntity,
            List<EntityData> valueEntityData, String user, String operationDescription) {
        Project project = getProject(projectName);
        if (project == null) {
            throw new IllegalArgumentException("Set operation failed. Unknown project: " + projectName);
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        Instance subject = kb.getInstance(entityName);
        if (subject == null) {
            throw new IllegalArgumentException("Set operation failed. Unknown subject: " + entityName);
        }
        Slot property = kb.getSlot(propertyEntity.getName());
        if (property == null) {
            new IllegalArgumentException("Set operation failed. Unknown property: " + propertyEntity.getName());
        }
        Collection<Object> values = new ArrayList<Object>();
        for (EntityData entityData : valueEntityData) {
            Object value = getProtegeObject(kb, entityData, property);
            if (value != null) {
                values.add(value);
            } else {
                throw new IllegalArgumentException("Set operation failed. Invalid value: " + entityData.getName());
            }
        }
        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                subject.setOwnSlotValues(property, values);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Error at setting value in " + projectName + " subj: " + subject + " pred: " + property
                        + " value: " + values, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }

    public static Object getProtegeObject(KnowledgeBase kb, EntityData entityData, Slot property) {
        if (entityData == null || entityData.getName() == null) {
            return null;
        }
        edu.stanford.bmir.protege.web.client.rpc.data.ValueType valueType = entityData.getValueType();
        if (valueType == null) {
            ValueType propValueType = property.getValueType();  // get Protege value type
            valueType = propValueType == null ?
                    edu.stanford.bmir.protege.web.client.rpc.data.ValueType.String :
                        edu.stanford.bmir.protege.web.client.rpc.data.ValueType.valueOf(propValueType.toString());
        }
        String value = entityData.getName();
        switch (valueType) {
        case String:
            return value;
        case Symbol:
            return value;
        case Instance:
            return kb.getInstance(value);
        case Cls:
            return kb.getCls(value);
        case Class:
            return kb.getCls(value);
        case Integer:
            try {
                return new Integer(value);
            } catch (NumberFormatException e) {
                return null;
            }
        case Float:
            try {
                return new Float(value);
            } catch (NumberFormatException e) {
                return null;
            }
        case Boolean:
            return new Boolean(value);
        case Date:
            return value; // FIXME for OWL
        case Literal: {
            if (!(kb instanceof OWLModel)) {
                return value;
            }
            OWLModel owlModel = (OWLModel) kb;
            // FIXME: should work with all datatypes
            RDFSLiteral lit = DefaultRDFSLiteral.create(owlModel, value, owlModel.getRDFSDatatypeByName("xsd:string"));
            return lit;
        }
        case Any: { //default to String...
            return value;
        }
        default:
            return null;
        }
    }

    protected Collection<Slot> getRootProperties(KnowledgeBase kb) {
        List<Slot> results = new ArrayList<Slot>(kb.getSlots());
        Iterator<Slot> i = results.iterator();
        while (i.hasNext()) {
            Slot slot = i.next();
            if (slot.getDirectSuperslotCount() > 0 || slot.isSystem()) {
                i.remove();
            }
        }
        Collections.sort(results, new FrameComparator<Slot>());
        return results;
    }

    public List<Triple> getEntityTriples(String projectName, String entityName) {
        ArrayList<Triple> triples = new ArrayList<Triple>();

        Project project = getProject(projectName);
        if (project == null) {
            return triples;
        }

        KnowledgeBase kb = project.getKnowledgeBase();
        Instance inst = kb.getInstance(entityName);
        if (inst == null) {
            return triples;
        }

        for (Object element : inst.getOwnSlots()) {
            Slot slot = (Slot) element;
            if (!slot.isSystem()) {
                triples.addAll(getTriples(inst, slot));
            }
        }

        // add rdfs:comment and rdfs:label
        if (kb instanceof OWLModel) {
            OWLModel owlModel = (OWLModel) kb;
            triples.addAll(getTriples(inst, owlModel.getRDFSCommentProperty()));
            triples.addAll(getTriples(inst, owlModel.getRDFSLabelProperty()));
            triples.addAll(getTriples(inst, owlModel.getSystemFrames().getRdfsSeeAlsoProperty()));
            triples.addAll(getTriples(inst, owlModel.getSystemFrames().getRdfsIsDefinedByProperty()));

            triples.addAll(getTriples(inst, owlModel.getSystemFrames().getOwlVersionInfoProperty()));
            triples.addAll(getTriples(inst, owlModel.getSystemFrames().getOwlBackwardCompatibleWithProperty()));
            triples.addAll(getTriples(inst, owlModel.getSystemFrames().getOwlIncompatibleWithProperty()));
            triples.addAll(getTriples(inst, owlModel.getSystemFrames().getOwlPriorVersionProperty()));

            // domain and range should not be necessarily retrieved
            triples.addAll(getTriples(inst, owlModel.getRDFSDomainProperty()));
            triples.addAll(getTriples(inst, owlModel.getRDFSRangeProperty()));
			
			//rdf:type might be useful
			triples.addAll(getTriples(inst, owlModel.getRDFTypeProperty()));
			
        }

        //TODO - should we show this or not?
        /*
        if (inst instanceof Cls) {
            triples.addAll(getPropertiesInDomain((Cls) inst));
        }
         */

        return triples.size() == 0 ? null : triples;
    }

    public List<Triple> getEntityTriples(String projectName, List<String> entities, List<String> properties) {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        Project project = getProject(projectName);
        if (project == null) {
            return triples;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        for (String entityName : entities) {
            Instance inst = kb.getInstance(entityName);
            if (inst != null) {
                for (String propName : properties) {
                    Slot slot = kb.getSlot(propName);
                    if (slot != null) {
                        triples.addAll(getTriples(inst, slot, true));
                    }
                }
            }
        }
        return triples.size() == 0 ? null : triples;
    }

    public List<Triple> getEntityTriples(String projectName, List<String> entities, List<String> properties, List<String> reifiedProperties) {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        Project project = getProject(projectName);
        if (project == null) {
            return triples;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        for (String entityName : entities) {
            Instance inst = kb.getInstance(entityName);
            if (inst != null) {
                for (String propName : properties) {
                    Slot slot = kb.getSlot(propName);
                    if (slot != null) {
                        Collection values = inst.getOwnSlotValues(slot);
                        for (Object value : values) {
                            if (value instanceof Instance) {
                                Instance valueInst = (Instance) value;
                                for (String reifiedPropName : reifiedProperties) {
                                    Slot reifiedSlot = kb.getSlot(reifiedPropName);
                                    if (reifiedPropName != null) {
                                        triples.addAll(getTriples(valueInst, reifiedSlot, false));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return triples.size() == 0 ? null : triples;
    }


    public List<EntityPropertyValues> getEntityPropertyValues(String projectName, List<String> entities, List<String> properties, List<String> reifiedProperties) {
        List<EntityPropertyValues> entityPropValues = new ArrayList<EntityPropertyValues>();
        Project project = getProject(projectName);
        if (project == null) {
            return entityPropValues.size() == 0 ? null : entityPropValues;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        for (String entityName : entities) {
            Instance inst = kb.getInstance(entityName);
            if (inst != null) {
                for (String propName : properties) {
                    Slot slot = kb.getSlot(propName);
                    if (slot != null) {
                        Collection values = inst.getOwnSlotValues(slot);
                        for (Object value : values) {
                            if (value instanceof Instance) {
                                Instance valueInst = (Instance) value;
                                EntityPropertyValues epv = new EntityPropertyValues(createEntityData(valueInst));
                                for (String reifiedPropName : reifiedProperties) {
                                	if (reifiedPropName != null) { //for example in case of clone columns
	                                    Slot reifiedSlot = kb.getSlot(reifiedPropName);
	                                    if (reifiedSlot != null) {
	                                        epv.addPropertyValues(new PropertyEntityData(reifiedSlot.getName()), createEntityList(valueInst.getOwnSlotValues(reifiedSlot)));
	                                    }
                                	}
                                }
                                entityPropValues.add(epv);
                            }
                        }
                    }
                }
            }
        }
        return entityPropValues.size() == 0 ? null : entityPropValues;
    }

    /**
     * This method returns a list of {@link EntityPropertyValuesList}s,
     * each element in the list representing a multi-level expansion
     * along the reified properties in <code>reifiedProperties</code>
     * of all the instances that are <code>property</code> values on
     * each subject entity in <code>entities</code>. <br> <br>
     * The method first retrieves for each subject entity specified in
     * <code>entities</code> the property values for the property
     * specified by the <code>property</code> argument. Then each of those instances
     * will be further expanded along the properties specified in
     * <code>reifiedProperties</code>. Since this is a multi-level
     * expansion, while some reified properties will refer to the "main"
     * property value instance (extracted in the first step),
     * other reified properties may refer to instances that are
     * themselves property values calculated in another "column".
     * The subjects of the reified properties are specified in the
     * <code>subjectEntityIndexes</code> array. The value -1 in that array
     * means that the reified property applies to the "main" property value instance.
     * <br><br>
     * A particularity of this method is that it generates separate entries
     * in the result list not only for every property value instance
     * on every subject, but if the subject of a reified property is
     * in a different column, and that column contains multiple instances,
     * this method will generate separate entries for each of those instances
     * and will apply the reified properties to each of those instances separately.
     * <br><br>
     * For example: If we have the following relations in our ontology
     * <code>
     * <br> s - p - {a,b}
     * <br> a - p0 - a0
     * <br> b - p0 - b0
     * <br> a - p1 - {i1, i2}
     * <br> b - p1 - i3
     * <br> i1 - p2 - v1
     * <br> i2 - p2 - {v21, v22}
     * <br> i3 - p2 - v3
     * <br> i1 - p3 - v4
     * <br> i2 - p3 - v5
     * <br> i3 - p3 - {v61, v62}
     * </code>
     * <br><br> and we call the method with the arguments:
     * <br>
     * <br> <code>entities = ["s"]</code>
     * <br> <code>property = "p"</code>
     * <br> <code>reifiedProperties = ["p0", "p1", "p2", "p3"]</code>
     * <br> <code>subjectEntityIndexes = [-1, -1, 1, 1]</code>
     * <br><br> we will get the following result (presented in a table form for convenience):
     * <code><table border="1"><tbody>
     * <tr>
     * <th>prop: p0; subjInd: -1</th>
     * <th>prop: p1; subjInd: -1</th>
     * <th>prop: p2; subjInd: 1</th>
     * <th>prop: p3; subjInd: 1</th>
     * <th>"main" instance <br>(i.e. value of prop. "p" on subject "s")</th>
     * </tr>
     * <tr>
     * <td>a0</td>
     * <td>i1</td>
     * <td>v1</td>
     * <td>v4</td>
     * <td>a</td>
     * </tr>
     * <tr>
     * <td>a0</td>
     * <td>i2</td>
     * <td>[v21, v22]</td>
     * <td>v5</td>
     * <td>a</td>
     * </tr>
     * <tr>
     * <td>b0</td>
     * <td>i3</td>
     * <td>v3</td>
     * <td>[v61, v62]</td>
     * <td>b</td>
     * </tr>
     * </tbody></table></code>
     *
     * <br><br>The reason for having two rows for the instance "a"
     * (and having the property value "a0" for property "p0" repeated in both rows),
     * is because the subject of property "p2" (as well of "p3")
     * is in column 1, which for the instance "a" contains
     * multiple values ("i1" and "i2"). In order to disambiguate which instance
     * is the subject of the property value in column 2 and 3
     * we needed to ensure that column 1 contains a single instance, therefore
     * we have split the multiple values in two different entries.
     * This splitting was not necessary for the values of properties
     * "p2" and "p3" ({"v21", "v22"} and {"v61", "v62"}) because
     * those values did not have any reified properties referring to them as subjects.
     *
     * @param projectName name of the project
     * @param entities list of entity names
     * @param property name of property
     * @param reifiedProperties list of reified property names
     * @param subjectEntityIndexes list of indexes that identify the subject
     * that a given reified property in reifiedProperties refer to. The value -1
     * means that the subject of the reified property is the "main" instance,
     * which is the property value for "property" on a subject entity in "entities".
     */
	public List<EntityPropertyValuesList> getMultilevelEntityPropertyValues(
			String projectName, List<String> entities, String property,
			List<String> reifiedProperties, int[] subjectEntityIndexes){

        List<EntityPropertyValuesList> entityPropValues = new ArrayList<EntityPropertyValuesList>();
        Project project = getProject(projectName);
        if (project == null) {
            return entityPropValues.size() == 0 ? null : entityPropValues;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        for (String entityName : entities) {
            Instance inst = kb.getInstance(entityName);
            if (inst != null) {
                Slot slot = kb.getSlot(property);
                if (slot != null) {
                    Collection<?> values = inst.getOwnSlotValues(slot);
                    for (Object value : values) {
                        if (value instanceof Instance) {
                            Instance valueInst = (Instance) value;
                            List<EntityPropertyValuesList> epvs = generateEPVsForMultilevelProperties2(
                            		kb, valueInst, reifiedProperties, subjectEntityIndexes);
                            entityPropValues.addAll(epvs);
                        }
                    }
                }
            }
        }
        return entityPropValues.size() == 0 ? null : entityPropValues;
	}

	private List<EntityPropertyValuesList> generateEPVsForMultilevelProperties2(
			KnowledgeBase kb, Instance valueInst,
			List<String> reifiedProperties, int[] subjectEntityIndexes) {

		List<EntityPropertyValuesList> epvs = new ArrayList<EntityPropertyValuesList>();
		boolean thereAreMoreValuesToRead = true;
		while (thereAreMoreValuesToRead) {
			boolean foundNewValues = false;
		    for (int i = 0; i < reifiedProperties.size(); i++) {
		    	String reifiedPropName = reifiedProperties.get(i);
		    	if (reifiedPropName != null) { //for example in case of clone columns
		    		int subjEntityIndex = subjectEntityIndexes[i];
		            Slot reifiedSlot = kb.getSlot(reifiedPropName);
		            if (reifiedSlot != null) {
		            	foundNewValues |= fillInPropertyValues(kb, valueInst,
		            			reifiedSlot, i, subjEntityIndex, epvs);
		            }
		    	}
		    }
		    boolean epvsContainsNullPropValue = false;
		    Iterator<EntityPropertyValuesList> it = epvs.iterator();
		    while (it.hasNext() && !epvsContainsNullPropValue) {
		    	epvsContainsNullPropValue = it.next().getAllPropertyValues().contains(null);
		    }
		    thereAreMoreValuesToRead = foundNewValues && epvsContainsNullPropValue;
		}
		
		//set the properties for all epvs
		List<PropertyEntityData> reifiedPropEDList = new ArrayList<PropertyEntityData>();
		for (String propName : reifiedProperties) {
			reifiedPropEDList.add(new PropertyEntityData(propName));
		}
	    for (EntityPropertyValuesList epv : epvs) {
	    	epv.setProperties(reifiedPropEDList);;
	    }
		
		return epvs;
	}

	private boolean fillInPropertyValues(KnowledgeBase kb, Instance valueInst,
			Slot reifiedSlot, int propIndex, int subjEntityIndex,
			List<EntityPropertyValuesList> epvs) {
		boolean foundNewValues = false;
		//create first epv if necessary
		if (epvs.isEmpty() && subjEntityIndex == -1) {
			EntityPropertyValuesList epv = new EntityPropertyValuesList(createEntityData(valueInst));
			epvs.add(epv);
		}

		List<EntityPropertyValuesList> epvsToBeSplit = new ArrayList<EntityPropertyValuesList>();

		for (EntityPropertyValuesList epv : epvs) {
			//skip the extraction of the property values that have been already extracted
			if (epv.getPropertyValues(propIndex) != null) {
				continue;
			}
			
    		if (subjEntityIndex == -1) {
    			epv.setPropertyValues(propIndex, createEntityList(valueInst.getOwnSlotValues(reifiedSlot)));
    			foundNewValues = true;
    		}
    		else {
    			List<EntityData> subjEntities = epv.getPropertyValues(subjEntityIndex);
    			if (subjEntities != null && !subjEntities.isEmpty()) {
    				if (subjEntities.size() > 1) {
    					epvsToBeSplit.add(epv);
    				}
    				else {
	    				//take the first subject entity
	    				Instance subjEntity = kb.getInstance(subjEntities.get(0).getName());
	    				if (subjEntity != null) {
	            			epv.setPropertyValues(propIndex, createEntityList(subjEntity.getOwnSlotValues(reifiedSlot)));
	            			foundNewValues = true;
	    				}
	                    else {
	                    	epv.setPropertyValues(propIndex, null);
	                    }
    				}
    			}
                else {
                	epv.setPropertyValues(propIndex, subjEntities == null ? null : createEntityList(new ArrayList<Object>()));
                }
    		}
		}

		if (epvsToBeSplit.size() > 0) {
			for (EntityPropertyValuesList epvToBeSplit : epvsToBeSplit) {
				List<EntityPropertyValuesList> newEpvs = splitEpv(epvToBeSplit, subjEntityIndex);
				epvs.remove(epvToBeSplit);
				epvs.addAll(newEpvs);
			}
			//call again this method
			foundNewValues |= fillInPropertyValues(kb, valueInst, reifiedSlot, propIndex, subjEntityIndex, epvs);
		}

		return foundNewValues;
	}

	private List<EntityPropertyValuesList> splitEpv(
			EntityPropertyValuesList epvToBeSplit, int subjEntityIndex) {
		List<EntityPropertyValuesList> res = new ArrayList<EntityPropertyValuesList>();
		List<EntityData> subjEntities = epvToBeSplit.getPropertyValues(subjEntityIndex);
		if (subjEntities != null) {
			try {
				for (EntityData subjEntity : subjEntities) {
					EntityPropertyValuesList cloneEpv = new EntityPropertyValuesList(epvToBeSplit);
					cloneEpv.setPropertyValues(subjEntityIndex, Collections.singletonList(subjEntity));
					res.add(cloneEpv);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}


    private ArrayList<Triple> getPropertiesInDomain(Cls cls) {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        KnowledgeBase kb = cls.getKnowledgeBase();

        for (Object element : cls.getTemplateSlots()) {
            Slot slot = (Slot) element;

            // hack
            if (slot.isSystem() || slot.getName().startsWith(ProtegeNames.PROTEGE_OWL_NAMESPACE)
                    || slot.getName().startsWith(XSPNames.NS)) {
                continue;
            }
            // TODO: refactor this code

            if (slot.getValueType() == ValueType.INSTANCE) {
                Collection directDomain = slot.getDirectDomain();
                Collection allowedClses = slot.getAllowedClses();
                if (kb instanceof OWLModel) {
                    if (directDomain != null && directDomain.size() > 0 && !directDomain.contains(kb.getRootCls())) {
                        if (allowedClses.size() == 0) {
                            allowedClses.add(kb.getRootCls());
                        }
                        triples.addAll(getClsTriples(cls, slot, allowedClses));
                    }
                } else {
                    triples.addAll(getClsTriples(cls, slot, allowedClses));
                }
            } else if (slot.getValueType() == ValueType.CLS) {
                Collection directDomain = slot.getDirectDomain();
                Collection allowedParents = slot.getAllowedParents();
                if (kb instanceof OWLModel) {
                    if (directDomain != null && directDomain.size() > 0 && !directDomain.contains(kb.getRootCls())) {
                        if (allowedParents.size() == 0) {
                            allowedParents.add(kb.getRootCls());
                        }
                        triples.addAll(getClsTriples(cls, slot, allowedParents));
                    }
                } else {
                    triples.addAll(getClsTriples(cls, slot, allowedParents));
                }
            } else {
                // TODO
                Collection directDomain = slot.getDirectDomain();
                if (kb instanceof OWLModel) {
                    if (directDomain != null && directDomain.size() > 0 && !directDomain.contains(kb.getRootCls())) {
                        triples.add(createTriple(cls, slot, slot.getValueType().toString()));
                    }
                } else {
                    triples.add(createTriple(cls, slot, slot.getValueType().toString()));
                }
            }
        }

        return triples;
    }

    private List<Triple> getClsTriples(Cls cls, Slot slot, Collection values) {
        List<Triple> triples = new ArrayList<Triple>();
        for (Iterator iterator = values.iterator(); iterator.hasNext();) {
            Object value = iterator.next();
            Triple triple = new Triple(createEntityData(cls), createPropertyEntityData(slot, cls, false), createEntityData(value));
            triples.add(triple);
        }
        return triples;
    }

    protected Collection<Triple> getTriples(Instance inst, Slot slot) {
        return getTriples(inst, slot, false);
    }

    protected Collection<Triple> getTriples(Instance inst, Slot slot, boolean includeEmptyValues) {
        return getTriples(inst, slot, inst.getOwnSlotValues(slot), includeEmptyValues);
    }

    protected Collection<Triple> getTriples(Instance inst, Slot slot, Collection values) {
        return getTriples(inst, slot, values, false);
    }

    protected Collection<Triple> getTriples(Instance inst, Slot slot, Collection values, boolean includeEmptyValues) {
        ArrayList<Triple> triples = new ArrayList<Triple>();

        if (includeEmptyValues && (values == null || values.size() == 0)) {
            values = new ArrayList();
            values.add(null);
        }

        for (Iterator it = values.iterator(); it.hasNext();) {
            Object object = it.next();
            triples.add(createTriple(inst, slot, object));
        }

        return triples;
    }

    public List<Triple> getRelatedProperties(String projectName, String className) {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        Project project = getProject(projectName);
        if (project == null) {
            return triples;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        Cls cls = kb.getCls(className);
        if (cls == null) {
            return triples;
        }

        //TODO: this should be reimplemented to match the Properties View output from the rich client
        //TODO: the domains are inherited to the subclasses, which is wrong in OWL (but intuitive..)

        return getPropertiesInDomain(cls);
    }

    protected Triple createTriple(Instance instance, Slot slot, Object object) {
        EntityData subj = createEntityData(instance);
        PropertyEntityData pred = createPropertyEntityData(slot, instance.getDirectType(), false);
        EntityData obj = createEntityData(object, false);
        return new Triple(subj, pred, obj);
    }

    public static EntityData createEntityData(Object object) {
        return createEntityData(object, true);
    }

    public static List<EntityData> createEntityList(Collection list) {
        ArrayList<EntityData> edList = new ArrayList<EntityData>();
        for (Object element : list) {
            Object obj = element;
            edList.add(createEntityData(obj));
        }
        return edList;
    }

    public static EntityData createEntityData(Object object, boolean computeAnnotations) {
        if (object == null) {
            return null;
        }

        if (object instanceof Frame) {
            Frame objFrame = (Frame) object;
            EntityData entityData;
            if (objFrame instanceof Slot) {
                entityData = new PropertyEntityData(objFrame.getName(), getBrowserText(objFrame), null);
                if (computeAnnotations) {
                    entityData.setChildrenAnnotationsCount(HasAnnotationCache.getChildrenAnnotationCount(objFrame));
                    entityData.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(objFrame));
                }
                ((PropertyEntityData) entityData).setPropertyType(OntologyServiceImpl.getPropertyType((Slot) objFrame));
                entityData.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Property);
            } else {
                entityData = new EntityData(objFrame.getName(), getBrowserText(objFrame), null);
                if (computeAnnotations) {
                    entityData.setChildrenAnnotationsCount(HasAnnotationCache.getChildrenAnnotationCount(objFrame));
                    entityData.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(objFrame));
                }
                entityData.setValueType(
                		objFrame instanceof Cls ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Cls : 
                			(objFrame instanceof SimpleInstance ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance : //this test may be too weak or too strong, depending how we want to deal with untyped resources and literals 
                				edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Any));
            }
            return entityData;
        } else {
            EntityData entityData = new EntityData(object.toString());
            entityData.setValueType(
            		object instanceof String ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.String :
            			(object instanceof Boolean ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Boolean :
            				(object instanceof Integer ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Integer :
            					(object instanceof Float ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Float :
            						null)))); //TODO deal with Date, Symbol and Literal
            return entityData;
        }
    }

    public static PropertyEntityData createPropertyEntityData(Slot property, Cls cls, boolean computeAnnotations) {
        if (property == null) {
            return null;
        }

        PropertyEntityData ped = new PropertyEntityData(property.getName(), getBrowserText(property), null);

        if (computeAnnotations) {
            ped.setChildrenAnnotationsCount(HasAnnotationCache.getChildrenAnnotationCount(property));
            ped.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(property));
        }

        if (property instanceof RDFProperty) {
            //TODO: for now check only if the property is functional
            if (((RDFProperty) property).isFunctional()) {
                ped.setMaxCardinality(1);
            }
        } else {
            ped.setMinCardinality(cls == null ? property.getMinimumCardinality() : cls
                    .getTemplateSlotMinimumCardinality(property));
            ped.setMaxCardinality(cls == null ? property.getMaximumCardinality() : cls
                    .getTemplateSlotMaximumCardinality(property));
        }

        if (property.getValueType() == ValueType.STRING) {
            ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.String);
        } else if (property.getValueType() == ValueType.INSTANCE || property.getValueType() == ValueType.CLS) {
            ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance);
            ped.setAllowedValues(createEntityList(property.getAllowedClses())); // FIXME
        } else if (property.getValueType() == ValueType.BOOLEAN) {
            ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Boolean);
        } else if (property.getValueType() == ValueType.SYMBOL) {
            ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Symbol);
            ped.setAllowedValues(createEntityList(property.getAllowedValues()));
        } else {
            ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Any);
        }

        if (property.isSystem()) {
            ped.setIsSystem(true);
        }

        return ped;
    }

    public List<EntityData> getIndividuals(String projectName, String className) {
        List<EntityData> instancesData = new ArrayList<EntityData>();

        Project project = getProject(projectName);

        if (project == null || className == null) {
            return instancesData;
        }

        KnowledgeBase kb = project.getKnowledgeBase();
        Cls cls = kb.getCls(className);

        if (cls == null) {
            return instancesData;
        }

        List<Instance> instances = new ArrayList<Instance>(cls.getDirectInstances());
        Collections.sort(instances, new FrameComparator<Instance>());

        for (Object element : instances) {
            Instance inst = (Instance) element;

            if (inst.isVisible()) { //TODO: is this a good check?
                EntityData instEntityData = new EntityData(inst.getName(), getBrowserText(inst), createEntityList(inst.getDirectTypes()));
                instEntityData.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(inst));
                instancesData.add(instEntityData);
            }
        }

        return instancesData;
    }


    public PaginationData<EntityData> getIndividuals(String projectName, String className, int start, int limit, String sort, String dir) {
        List<EntityData> individuals = getIndividuals(projectName, className);
        return PaginationServerUtil.pagedRecords(individuals, start, limit, sort, dir);
    }


    public ImportsData getImportedOntologies(String projectName) {
        Project project = getProject(projectName);
        ImportsData id = new ImportsData();

        if (isOWLOntology(project)) {
            // OWL ontology
            OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
            id = copyOWLTree(id, owlModel.getDefaultOWLOntology());
        } else {
            // Frames ontology
            Tree<URI> tree = project.getProjectTree();
            id = copyTree(id, tree, tree.getRoot());
        }

        return id;
    }

    private ImportsData copyTree(ImportsData data, Tree<URI> tree, URI node) {
        ImportsData id = data;
        id.setName(URIUtilities.getBaseName(node));

        Set<URI> children = tree.getChildren(node);
        for (URI childNode : children) {
            ImportsData childID = new ImportsData();
            childID = copyTree(childID, tree, childNode);
            id.addImport(childID);
        }

        return id;
    }

    private ImportsData copyOWLTree(ImportsData data, OWLOntology ontology) {
        ImportsData id = data;
        id.setName(ontology.getURI());

        Collection<RDFResource> imports = ontology.getImportResources();
        for (Object o : imports) {
            OWLOntology childOnt = (OWLOntology) o;
            ImportsData childID = new ImportsData(childOnt.getURI());
            childID = copyOWLTree(childID, childOnt);
            id.addImport(childID);
        }

        return id;
    }

    public ArrayList<MetricData> getMetrics(String projectName) {
        ArrayList<MetricData> metrics = new ArrayList<MetricData>();
        Project project = getProject(projectName);

        if (isOWLOntology(project)) {
            // OWL ontology
            OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
            ModelMetrics modelMetrics = new ModelMetrics(owlModel);
            modelMetrics.calculateMetrics();

            Integer count = new Integer(modelMetrics.getNamedClassCount());
            metrics.add(new MetricData("Class count", count.toString()));

            count = new Integer(modelMetrics.getDatatypePropertyCount());
            metrics.add(new MetricData("Datatype property count", count.toString()));

            count = new Integer(modelMetrics.getObjectPropertyCount());
            metrics.add(new MetricData("Object property count", count.toString()));

            count = new Integer(modelMetrics.getAnnotationPropertyCount());
            metrics.add(new MetricData("Annotation property count", count.toString()));

            count = new Integer(modelMetrics.getOwlIndividualCount());
            metrics.add(new MetricData("Individual count", count.toString()));

            DLExpressivityChecker checker = new DLExpressivityChecker(owlModel);
            checker.check();
            metrics.add(new MetricData("DL Expressivity", checker.getDLName()));

        } else {
            // Frames ontology
            FrameCounts frameCounts = project.getKnowledgeBase().getFrameCounts();

            Integer count = new Integer(frameCounts.getTotalClsCount());
            metrics.add(new MetricData("Class count", count.toString()));

            count = new Integer(frameCounts.getTotalSlotCount());
            metrics.add(new MetricData("Slot count", count.toString()));

            count = new Integer(frameCounts.getTotalFacetCount());
            metrics.add(new MetricData("Facet count", count.toString()));

            count = new Integer(frameCounts.getTotalSimpleInstanceCount());
            metrics.add(new MetricData("Instance count", count.toString()));

            count = new Integer(frameCounts.getTotalFrameCount());
            metrics.add(new MetricData("Frame count", count.toString()));
        }

        return metrics;
    }

    private boolean isOWLOntology(Project project) {
        KnowledgeBase kb = project.getKnowledgeBase();
        return (kb instanceof OWLModel) ? true : false;
    }

    public List<OntologyEvent> getEvents(String projectName, long fromVersion) {
        ServerProject<Project> serverProject = Protege3ProjectManager.getProjectManager().getServerProject(projectName, false);
        if (serverProject == null) {
            throw new RuntimeException("Could not get ontology: " + projectName + " from server.");
        }
        return serverProject.isLoaded() ? serverProject.getEvents(fromVersion) : null;
    }

    public EntityData createCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription) {
        return createCls(projectName, clsName, superClsName, true, user, operationDescription);
    }

    public EntityData createCls(String projectName, String clsName, String superClsName, boolean createWithMetaClses, String user, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls superCls = superClsName == null ? kb.getRootCls() : kb.getCls(superClsName);
        EntityData clsEntity = null;
        Cls cls = null;

        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                cls = isOWLOntology(project) ?
                        (superCls != null && superCls instanceof OWLNamedClass ?
                                ((OWLModel) kb).createOWLNamedSubclass(clsName, (OWLNamedClass) superCls) :
                                    ((OWLModel) kb).createOWLNamedClass(clsName)) :     //TODO check for RDFSNamedClass as well
                                        kb.createCls(clsName, superCls == null ? kb.getRootClses()
                                                : CollectionUtilities.createCollection(superCls));

                                if(createWithMetaClses && superCls != null){
                                    Collection<Cls> directTypes = superCls.getDirectTypes();
                                    for (Cls type : directTypes) {
                                        if (! cls.hasDirectType(type)) {
                                            cls.addDirectType(type);
                                        }
                                    }
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

        if (cls != null) {
            clsEntity = createEntityData(cls, false);
        }

        return clsEntity;
    }

    public EntityData createClsWithProperty(String projectName, String clsName, String superClsName, String propertyName,
            EntityData propertyValue, String user, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls superCls = superClsName == null ? kb.getRootCls() : kb.getCls(superClsName);
        EntityData clsEntity = null;
        Cls cls = null;

        Slot property = kb.getSlot(propertyName);
        Object value = getProtegeObject(kb, propertyValue, property);
        if (propertyValue != null && value == null) {
            Log.getLogger().log(Level.WARNING, "Could not set property value " + propertyValue +
                    " for property " + property + " in create cls with property method.");
        }

        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                cls = isOWLOntology(project) ?
                        (superCls != null && superCls instanceof OWLNamedClass ?
                                ((OWLModel) kb).createOWLNamedSubclass(clsName, (OWLNamedClass) superCls) :
                                    ((OWLModel) kb).createOWLNamedClass(clsName)) :     //TODO check for RDFSNamedClass as well
                                        kb.createCls(clsName, superCls == null ?
                                                kb.getRootClses() :
                                                    CollectionUtilities.createCollection(superCls));

                                if (property != null) {
                                    if (isOWLOntology(project)) {
                                        ((RDFSClass)cls).setPropertyValue((RDFProperty)property, value);
                                    }
                                    else {
                                        cls.setOwnSlotValue(property, value);
                                    }
                                }

                                if (runsInTransaction) {
                                    kb.commitTransaction();
                                }
            } catch (Exception e) {
                Log.getLogger()
                .log(Level.SEVERE, "Error at creating class with property in " + projectName + " class: " + clsName + "property name: " + projectName + " property value: " + propertyValue, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Error at creating class " + clsName + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        if (cls != null) {
            clsEntity = createEntityData(cls, false);
        }

        return clsEntity;
    }

    public void deleteEntity(String projectName, String entityName, String user, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Frame frame = kb.getFrame(entityName);
        if (frame == null) {
            return;
        }
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                frame.delete();

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Error at deleting in " + projectName + " entity: " + entityName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Error at deleting " + entityName + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }

    public void addSuperCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription) {
    	
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();
        Cls cls = kb.getCls(clsName);
        Cls superCls = kb.getCls(superClsName);
        
        if (cls == null || superCls == null) {
            return;
        }
        
    	if (kb instanceof OWLModel && superCls instanceof RDFSNamedClass) {

			if (RetirementManager.isNonRetirableId(clsName) == true && 
					RetirementManager.isInRetiredTree((OWLModel)kb, (RDFSNamedClass)superCls)) {
				throw new RuntimeException("Cannot add new parent: " + superCls.getBrowserText() + 
                        " to class " + cls.getBrowserText() + 
                        ". The class is non-retirable as it has been already released."); 
			}
			
		}
        

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                cls.addDirectSuperclass(superCls);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Error at adding direct superclass in " + projectName + " class: " + clsName + " supercls: "
                        + superClsName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Error at adding to class " + clsName + " superclass: " + superClsName
                        + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }

    public void removeSuperCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls cls = kb.getCls(clsName);
        Cls superCls = kb.getCls(superClsName);
        if (cls == null || superCls == null) {
            return;
        }
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                cls.removeDirectSuperclass(superCls);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Error at removing superclass in " + projectName + " class: " + clsName + " superclass: " + superClsName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Error at removing from class " + clsName + " superclass: " + superClsName
                        + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
    }

    public List<EntityData> moveCls(String projectName, String clsName, String oldParentName, String newParentName,
    		boolean checkForCycles,
            String user,  String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls cls = kb.getCls(clsName);
        Cls oldParent = kb.getCls(oldParentName);
        Cls newParent = kb.getCls(newParentName);

        if (cls == null || oldParent == null || newParent == null) {
            return null;
        }

		if (kb instanceof OWLModel && newParent instanceof RDFSNamedClass) {

			if (RetirementManager.isNonRetirableId(clsName) == true && 
					RetirementManager.isInRetiredTree((OWLModel)kb, (RDFSNamedClass)newParent)) {
				throw new RuntimeException("Cannot move class: " + cls.getBrowserText() + 
						"from old parent: " + oldParent.getBrowserText()
                        + " to new parent: " + newParent.getBrowserText() + 
                        ". Class " + cls.getBrowserText() + " is non-retirable as it has been already released."); 
			}
			
		}
        
        
        synchronized (kb) {
            KBUtil.morphUser(kb, user);

            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                cls.addDirectSuperclass(newParent);
                cls.removeDirectSuperclass(oldParent);

                //if the operation has created an orphan cycle add root class as a parent
                if (!cls.getSuperclasses().contains(kb.getRootCls())) {
                    cls.addDirectSuperclass(kb.getRootCls());
                }

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Error at moving class in " + projectName + " class: " + clsName + " old parent: "
                        + oldParentName + " new parent: " + newParentName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Error at moving class: " + clsName + " old parent: " + oldParentName
                        + " new parent: " + newParentName + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        List<EntityData> res = null;
        if (checkForCycles) {
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
        }

        return res;
    }

    public List<EntityData> getParents(String projectName, String className, boolean direct) {
        List<EntityData> parents = new ArrayList<EntityData>();
        Project project = getProject(projectName);

        if (project == null || className == null) {
            return parents;
        }

        KnowledgeBase kb = project.getKnowledgeBase();
        Cls cls = kb.getCls(className);
        if (cls == null) {
            return parents;
        }

        Collection<Cls> superClses = direct == true ? cls.getDirectSuperclasses() : cls.getSuperclasses();
        for (Cls parent : superClses) {
            if (!(parent.isSystem() || (parent instanceof RDFResource && ((RDFResource) parent).isAnonymous()))) {
                parents.add(createEntityData(parent));
            }
        }

        return parents;
    }

    //String concatenation on the client is very slow

    public String getParentsHtml(String projectName, String className, boolean direct) {
        StringBuffer buffer = new StringBuffer();
        List<EntityData> parents = getParents(projectName, className, direct);

        buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");
        for (EntityData parent : parents) {
            buffer.append("<tr><td>");
            buffer.append(UIUtil.getDisplayText(parent));
            buffer.append("</td>");
            buffer.append("<td class=\"parent-column-right\"><a href=\"");
            buffer.append(UIUtil.LOCAL);
            buffer.append(UIUtil.REMOVE_PREFIX);
            buffer.append(parent.getName());
            buffer.append("\">remove</a></td></tr>");
        }

        buffer.append("</table>");
        return buffer.toString();
    }


    public EntityData createProperty(String projectName, String propertyName, String superPropName,
            PropertyType propertyType, String user, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (kb.getFrame(propertyName) != null) {
            throw new RuntimeException("An entity with the same name already exists!");
        }

        boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);

        if (isOWLOntology(project)) { // OWL
            OWLModel owlModel = (OWLModel) kb;
            RDFProperty property = null;
            synchronized (kb) {
                KBUtil.morphUser(kb, user);
                try {
                    if (runsInTransaction) {
                        kb.beginTransaction(operationDescription);
                    }

                    if (propertyType == PropertyType.OBJECT) {
                        property = owlModel.createOWLObjectProperty(propertyName);
                    } else if (propertyType == PropertyType.DATATYPE) {
                        property = owlModel.createOWLDatatypeProperty(propertyName);
                    } else if (propertyType == PropertyType.ANNOTATION) {
                        property = owlModel.createAnnotationProperty(propertyName);
                    }
                    if (superPropName != null) {
                        RDFProperty superProp = owlModel.getRDFProperty(superPropName);
                        if (superProp != null) {
                            property.addSuperproperty(superProp);
                        }
                    }

                    if (runsInTransaction) {
                        kb.commitTransaction();
                    }
                } catch (Exception e) {
                    Log.getLogger().log(Level.SEVERE,
                            "Error at creating property in " + projectName + " property: " + propertyName, e);
                    if (runsInTransaction) {
                        kb.rollbackTransaction();
                    }
                    throw new RuntimeException("Error at creating property: " + propertyName + ". Message: "
                            + e.getMessage(), e);
                } finally {
                    KBUtil.restoreUser(kb);
                }
            }
            return createEntityData(property, false);
        } else { // Frames
            Slot slot = null;
            synchronized (kb) {
                KBUtil.morphUser(kb, user);
                try {
                    if (runsInTransaction) {
                        kb.beginTransaction(operationDescription);
                    }

                    slot = kb.createSlot(propertyName);
                    slot.setValueType(propertyType == PropertyType.OBJECT ? ValueType.INSTANCE : ValueType.STRING);
                    if (superPropName != null) {
                        Slot superSlot = kb.getSlot(superPropName);
                        if (superSlot != null) {
                            slot.addDirectSuperslot(superSlot);
                        }
                    }

                    if (runsInTransaction) {
                        kb.commitTransaction();
                    }
                } catch (Exception e) {
                    Log.getLogger().log(Level.SEVERE,
                            "Error at creating property in " + projectName + " property: " + propertyName, e);
                    if (runsInTransaction) {
                        kb.rollbackTransaction();
                    }
                    throw new RuntimeException("Error at creating property: " + propertyName + ". Message: "
                            + e.getMessage(), e);
                } finally {
                    KBUtil.restoreUser(kb);
                }
            }
            return createEntityData(slot, false);
        }
    }

    public EntityData createDatatypeProperty(String projectName, String propertyName, String superPropName,
            String user, String operationDescription) {
        return createProperty(projectName, propertyName, superPropName, PropertyType.DATATYPE, user,
                operationDescription);
    }

    public EntityData createObjectProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription) {
        return createProperty(projectName, propertyName, superPropName, PropertyType.OBJECT, user, operationDescription);
    }

    public EntityData createAnnotationProperty(String projectName, String propertyName, String superPropName,
            String user, String operationDescription) {
        return createProperty(projectName, propertyName, superPropName, PropertyType.ANNOTATION, user,
                operationDescription);
    }

    public EntityData createInstance(String projectName, String instName, String typeName, String user,
            String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Cls type = (typeName == null) ? kb.getRootCls() : kb.getCls(typeName);

        if (type == null) {
            Log.getLogger().warning("Could not create instance " + instName + " of type " + typeName + ". Null type");
            throw new IllegalArgumentException("Could not create instance " + instName + " of type " + typeName
                    + ". Null type");
        }

        instName = (instName == null) ? IDGenerator.getNextUniqueId() : instName;
        
        Instance inst = null;
        synchronized (kb) {
            KBUtil.morphUser(kb, user);

            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                inst = type.createDirectInstance(instName);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Could not create instance " + instName + " of type " + typeName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new IllegalArgumentException("Could not create instance " + instName + " of type " + typeName);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
        EntityData instData = createEntityData(inst);
        if (instData != null) {
            instData.setTypes(CollectionUtilities.createCollection(new EntityData(typeName)));
        }
        return instData;
    }

    public EntityData createInstanceValue(String projectName, String instName, String typeName, String subjectEntity,
            String propertyEntity, String user, String operationDescription) {

        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();
        Slot slot = kb.getSlot(propertyEntity);

        if (slot == null) {
            Log.getLogger().warning("Invalid property name: " + propertyEntity);
            throw new IllegalArgumentException("Operation failed. Possibly invalid configuration for property " + propertyEntity);
        }

        //if type is null - try to get the range of the property
        if (typeName == null) {
            Collection allowedClses = slot.getAllowedClses();
            if (allowedClses != null && allowedClses.size() > 0) {
                typeName = ((Frame) CollectionUtilities.getFirstItem(allowedClses)).getName();
            }
        }

        EntityData valueData = null;

        synchronized (kb) {
            KBUtil.morphUser(kb, user);

            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                valueData = createInstance(projectName, instName, typeName, user, null);
                if (valueData == null) {
                    if (runsInTransaction) {
                        kb.commitTransaction();
                    }
                    return null;
                }
                KBUtil.morphUser(kb, user); //hack
                PropertyEntityData propEntityData = createPropertyEntityData(slot, null, false);
                addPropertyValue(projectName, subjectEntity, propEntityData, valueData, false, user, null);
                KBUtil.morphUser(kb, user); //hack
                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(
                        Level.SEVERE,
                        "Could not create instance  " + instName + " of type " + typeName + " for " + subjectEntity
                        + " " + propertyEntity, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Could not create instance  " + instName + " of type " + typeName
                        + " for " + subjectEntity + " " + propertyEntity + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

        return valueData;
    }


    public EntityData createInstanceValueWithPropertyValue(String projectName, String instName, String typeName, String subjectEntity,
            String propertyEntity, PropertyEntityData instancePropertyEntity, EntityData valueEntityData, String user, String operationDescription) {
        Project project = getProject(projectName);

        if (project == null) {
            return null;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        if (kb == null) {
            return null;
        }

        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            // original operation description goes in our top-level change ....
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }
                // setting the operationDescription to null will ensure that we have no nested transactions (which are unnecessary) or duplicate changes...
                EntityData valueData = createInstanceValue(projectName, instName, typeName,
                        subjectEntity, propertyEntity, user, null);
                addPropertyValue(projectName, valueData.getName(), instancePropertyEntity,
                        valueEntityData, false, user, null);
                KBUtil.morphUser(kb, user);
                if (runsInTransaction){
                    kb.commitTransaction();
                }
                return valueData;
            } catch (RuntimeException e) {
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                // no logging as we are rethrowing an exception already logged by our called methods.
                throw e;
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

    }

    
    public EntityData[] createPropertyValueInstances(String projectName, EntityData rootSubject, String[] properties,
    		String[] types, String user, String operationDescription){
        Project project = getProject(projectName);

        if (project == null) {
            return null;
        }
        if (rootSubject == null) {
            return null;
        }
        KnowledgeBase kb = project.getKnowledgeBase();
        if (kb == null) {
            return null;
        }

        EntityData[] res = new EntityData[properties.length];
        
        synchronized (kb) {
            KBUtil.morphUser(kb, user);
            // original operation description goes in our top-level change ....
            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }
                // setting the operationDescription to null will ensure that we have no nested transactions (which are unnecessary) or duplicate changes...
                
                Instance subject = kb.getInstance(rootSubject.getName());
                if (subject == null) {
	                if (runsInTransaction) {
	                    kb.commitTransaction();
	                }
	                return null;
                }
                
                for (int i = 0; i < properties.length; i++) {
                	String property = properties[i];
                	String typeName = types[i];
                	
                    Cls type = (typeName == null) ? kb.getRootCls() : kb.getCls(typeName);

                    if (type == null) {
                        Log.getLogger().warning("Could not create instance of type " + typeName + ". Null type");
                        throw new IllegalArgumentException("Could not create instance of type " + typeName
                                + ". Null type");
                    }

                    Instance inst = type.createDirectInstance(null);
                    
                    EntityData instData = createEntityData(inst);
                    if (instData != null) {
                        instData.setTypes(CollectionUtilities.createCollection(new EntityData(typeName)));
                    }
                    res[i] = instData;
                    
                    Slot slot = kb.getSlot(property);
                    if (slot == null) {
                        Log.getLogger().warning("Could not add instance " + inst.getName() + " as value of property " + property + ". Property not found.");
                        throw new IllegalArgumentException("Could not add instance " + inst.getName() + " as value of property " + 
                        		property + ". Property not found.");
                    }
                    
                    subject.addOwnSlotValue(slot, inst);
                    subject = inst;
				}
                                
                KBUtil.morphUser(kb, user);
                if (runsInTransaction){
                    kb.commitTransaction();
                }
                return res;
            } catch (RuntimeException e) {
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                // no logging as we are rethrowing an exception already logged by our called methods.
                throw e;
            } finally {
                KBUtil.restoreUser(kb);
            }
        }

	}

    public EntityData renameEntity(String projectName, String oldName, String newName, String user,
            String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (isOWLOntology(project)) {
            OWLModel owlModel = (OWLModel) kb;
            oldName = OWLUtil.getInternalFullName(owlModel, oldName);
            newName = OWLUtil.getInternalFullName(owlModel, newName);
        }

        Frame oldFrame = kb.getFrame(oldName);
        Frame newFrame = kb.getFrame(newName);

        if (oldFrame == null || newFrame != null) {
            throw new IllegalArgumentException("Could not rename " + oldName + " to " + newName + ". Null name.");
        }

        synchronized (kb) {
            KBUtil.morphUser(kb, user);

            boolean runsInTransaction = KBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                newFrame = oldFrame.rename(newName);

                if (runsInTransaction) {
                    kb.commitTransaction();
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE,
                        "Could not rename in " + projectName + " entity old name:" + oldName + " new name: " + newName,
                        e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Could not rename entity, old name: " + oldName + " to new name: " + newName
                        + ". Message: " + e.getMessage(), e);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
        return createEntityData(newFrame);
    }

    public String getRestrictionHtml(String projectName, String className) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (!(kb instanceof OWLModel)) {
            return "";
        }
        OWLModel owlModel = (OWLModel) kb;
        OWLNamedClass cls = owlModel.getOWLNamedClass(className);
        if (cls == null) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(getConditionsHtml(cls));
        return buffer.toString();
    }

    private StringBuffer getConditionsHtml(OWLNamedClass cls) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");

        ConditionsTableModel ctm = new ConditionsTableModel(cls.getOWLModel());
        ctm.setCls(cls);
        ctm.refresh();

        for (int i = 0; i < ctm.getRowCount(); i++) {
            String row = (String) ctm.getValueAt(i, 0);
            RDFSClass restr = ctm.getClass(i);
            buffer.append("<tr><td>");
            if (restr != null) {
                buffer.append(getConditionHtmlString(restr));
            } else {
                if (row.equals(ConditionsTableItem.NECESSARY)) {
                    buffer.append("<hr>");
                    buffer.append("<div class=\"restiction_title\">Superclasses (Necessary conditions)</div>");
                } else if (row.equals(ConditionsTableItem.SUFFICIENT)) {
                    buffer.append("<div class=\"restiction_title\">Equivalent classes (Necessary and Sufficient conditions)</div>");
                } else if (row.equals(ConditionsTableItem.INHERITED)) {
                    buffer.append("<hr>");
                    buffer.append("<div class=\"restiction_title\">Inherited</div>");
                } else {
                    buffer.append(row.toString());
                }
            }

            buffer.append("</td></tr>");
        }
        buffer.append("</table>");
        return buffer;
    }

    private static final String delimsStrs[] = {"and", "or", "not", "some", "only", "has", "min", "exactly", "max"};
    private static List<String> delims = Arrays.asList(delimsStrs);

    private String getConditionHtmlString(RDFSClass cls) {
        //Could also use: string.replaceAll("(^|\w)or(\w)", "$1or$2)
        StringBuffer buffer = new StringBuffer();
        StringTokenizer st = new StringTokenizer(cls.getBrowserText(), " \t\n\r\f", true);
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (delims.contains(token)) {
                buffer.append("<span class=\"restriction_delim\">");
                buffer.append(token);
                buffer.append("</span>");
            } else {
                buffer.append(token);
            }
        }

        return buffer.toString();
    }

    class RestrictionComparator implements Comparator<RDFSClass> {
        public int compare(RDFSClass cls1, RDFSClass cls2) {
            if (cls1 instanceof RDFSNamedClass && cls2 instanceof RDFSNamedClass) {
                return cls1.getBrowserText().compareTo(cls2.getBrowserText());
            }
            if (cls1 instanceof RDFSNamedClass && !(cls2 instanceof RDFSNamedClass)) {
                return -1;
            }
            if (!(cls1 instanceof RDFSNamedClass) && cls2 instanceof RDFSNamedClass) {
                return 1;
            }
            // for all other cases
            return cls1.getBrowserText().compareTo(cls2.getBrowserText());
        }
    }

    public List<ConditionItem> getClassConditions(String projectName, String className) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (!(kb instanceof OWLModel)) {
            return null;
        }
        OWLModel owlModel = (OWLModel) kb;
        OWLNamedClass cls = owlModel.getOWLNamedClass(className);
        if (cls == null) {
            return null;
        }

        List<ConditionItem> conditions = new ArrayList<ConditionItem>();

        ConditionsTableModel ctm = new ConditionsTableModel(cls.getOWLModel());
        ctm.setCls(cls);
        ctm.refresh();

        for (int i = 0; i < ctm.getRowCount(); i++) {
            String row = (String) ctm.getValueAt(i, 0);
            RDFSClass conditionClass = ctm.getClass(i);
            ConditionItem condItem = new ConditionItem();
            condItem.setIndex(i);
            if (conditionClass != null) {
                condItem.setName(conditionClass.getName());
                condItem.setBrowserText(getConditionHtmlString(conditionClass));
                OWLNamedClass originClass = ctm.getOriginClass(i);
                if (originClass != null) {
                    condItem.setInheritedFromName(originClass.getName());
                    condItem.setInheritedFromBrowserText(originClass.getBrowserText());
                    condItem.setBrowserText(condItem.getBrowserText() + " <span class=\"restriction_separator\"> [from " + originClass.getBrowserText() + "] </span>");
                }
            } else {
                condItem.setName(row);
                condItem.setBrowserText(getSeparatorHtml(row));
            }
            conditions.add(condItem);
        }

        return conditions;
    }

    public List<ConditionItem> deleteCondition(String projectName, String className, ConditionItem conditionItem,  int row,
            String operationDescription) { //operation description is ignored right now - the ctm has a good string
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (!(kb instanceof OWLModel)) {
            return null;
        }
        OWLModel owlModel = (OWLModel) kb;
        OWLNamedClass cls = owlModel.getOWLNamedClass(className);
        if (cls == null) {
            return null;
        }

        synchronized (kb) {
            String user = KBUtil.getUserInSession(getThreadLocalRequest());
            KBUtil.morphUser(kb, user);
            try {
                ConditionsTableModel ctm = new ConditionsTableModel(cls.getOWLModel());
                ctm.setCls(cls);
                ctm.refresh();

                //do the transaction and opdescription

                //validity checks
                RDFSClass conditionToDelete = ctm.getClass(row);
                RDFResource conditionFromClient = owlModel.getRDFResource(conditionItem.getName());
                if (conditionToDelete == null || conditionFromClient == null || (!conditionToDelete.equals(conditionFromClient))) {
                    throw new IllegalArgumentException("Cannot delete condition from class " + cls.getBrowserText() +". Condition is not present at class.");
                }

                ctm.deleteRow(row); //treat exceptions, transactions, etc?
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at deleting condition " + conditionItem.getName() + " from class " + className, e);
                throw new RuntimeException("Error at deleting condition " + conditionItem.getName() + " from class " + className);
            } finally {
                KBUtil.restoreUser(kb);
            }
        }
        return getClassConditions(projectName, className);
    }

    public ConditionSuggestion getConditionAutocompleteSuggestions(String projectName, String condition, int cursorPosition) {
        Project project = getProject(projectName);
        if (!(project.getKnowledgeBase() instanceof OWLModel)) {
            return null;
        }
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        ConditionSuggestion conditionSuggestion = new ConditionSuggestion();

        String leftString = condition.substring(0, cursorPosition);
        int i = ParserUtils.findSplittingPoint(leftString);
        String prefix = leftString.substring(i, leftString.length());

        //validation is on full expression, autocomplete is only for last word
        synchronized (this) { //sync on this... stupid statics..
            OWLClassParser parser = owlModel.getOWLClassDisplay().getParser();
            try {
                parser.checkClass(owlModel, condition);
                conditionSuggestion.setValid(true);
            } catch (OWLClassParseException e) {
                conditionSuggestion.setMessage(e.getMessage());
                conditionSuggestion.setValid(false);
            }

            //used for the resource name matcher - stupid statics
            try {
                parser.checkClass(owlModel, leftString);
            } catch (OWLClassParseException e) { // intentionally left blank
            }

            if (prefix == null || prefix.length() == 0) {
                return conditionSuggestion;
            }

            OWLResourceNameMatcher resourceMatcher = new OWLResourceNameMatcher();
            Set<RDFResource> matches = resourceMatcher.getMatchingResources(prefix, null, owlModel);
            ArrayList<RDFResource> resourceList = new ArrayList<RDFResource>(matches);

            //need the browser text with quotes
            ArrayList<EntityData> dataList = new ArrayList<EntityData>();
            for (RDFResource res : resourceList) {
                dataList.add(new EntityData(res.getName(), res.getBrowserText()));
            }

            //conditionSuggestion.setSuggestions(createEntityList(resourceList)); //this method removes the quotes from the browser text
            conditionSuggestion.setSuggestions(createEntityList(dataList));
            return conditionSuggestion;
        }
    }

    public List<ConditionItem> replaceCondition(String projectName, String className, ConditionItem conditionItem,
            int row, String newCondition, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (!(kb instanceof OWLModel)) {
            return null;
        }
        OWLModel owlModel = (OWLModel) kb;
        OWLNamedClass cls = owlModel.getOWLNamedClass(className);
        if (cls == null) {
            return null;
        }

        synchronized (kb) {
            String user = KBUtil.getUserInSession(getThreadLocalRequest());
            KBUtil.morphUser(kb, user);
            try {
                ConditionsTableModel ctm = new ConditionsTableModel(cls.getOWLModel());
                ctm.setCls(cls);
                ctm.refresh();

                //do the transaction and opdescription
                //validity checks
                RDFSClass conditionToDelete = ctm.getClass(row);
                RDFResource conditionFromClient = owlModel.getRDFResource(conditionItem.getName());
                if (conditionToDelete == null || conditionFromClient == null || (!conditionToDelete.equals(conditionFromClient))) {
                    throw new IllegalArgumentException("Cannot replace condition from class " + cls.getBrowserText() +". Condition is not present at class.");
                }
                ctm.setValueAt(row, owlModel, newCondition);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at parsing class expression: " + newCondition + " for class " + className, e);
                throw new RuntimeException("Could not parse expression " + newCondition);
            }  finally { //treat exceptions, transactions, etc?
                KBUtil.restoreUser(kb);
            }
        }

        return getClassConditions(projectName, className);
    }

    public List<ConditionItem> addCondition(String projectName, String className, int row, String newCondition,
            boolean isNS, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if (!(kb instanceof OWLModel)) {
            return null;
        }
        OWLModel owlModel = (OWLModel) kb;
        OWLNamedClass cls = owlModel.getOWLNamedClass(className);
        if (cls == null) {
            return null;
        }

        synchronized (kb) {
            String user = KBUtil.getUserInSession(getThreadLocalRequest());
            KBUtil.morphUser(kb, user);
            try {
                ConditionsTableModel ctm = new ConditionsTableModel(cls.getOWLModel());
                ctm.setCls(cls);
                ctm.refresh();

                //do the transaction and opdescription
                //validity checks
                if (ctm.getType(row) != (isNS ? ConditionsTableItem.TYPE_DEFINITION_BASE : ConditionsTableItem.TYPE_SUPERCLASS)) {
                    throw new IllegalArgumentException("Cannot add new condition to class " + cls.getBrowserText() +". Class might have changed.");
                }

                ctm.addEmptyRow(row);
                ctm.setValueAt(row+1, owlModel, newCondition);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at adding new condition: " + newCondition + " for class " + className, e);
                throw new RuntimeException( "Error at adding new condition: " + newCondition + " for class " + className);
            } finally {  //treat exceptions, transactions, etc?
                KBUtil.restoreUser(kb);
            }
        }

        return getClassConditions(projectName, className);
    }


    private String getSeparatorHtml(String separator) {
        separator = separator.replaceAll(" ", "&nbsp;");
        return "<table width=\"100%\" class=\"restriction_separator\"><tr><td><hr color=\"#E8E8E8\" /></td><td width=\"1px\">" + separator + "</td><td><hr color=\"#E8E8E8\" /></td></tr></table>";
    }

    public Boolean hasWritePermission(String projectName, String userName) {
        Project project = getProject(projectName);
        if (project == null) {
            return Boolean.FALSE;
        }
        if (project instanceof RemoteClientProject) {// is remote project
            RemoteClientProject remoteClientProject = (RemoteClientProject) project;
            RemoteServer server = remoteClientProject.getServer();
            boolean allowed = false;
            try {
                // using a bogus session with the correct user name
                allowed = server.isOperationAllowed(new Session(userName, "(from web protege)", false),
                        MetaProjectConstants.OPERATION_WRITE, projectName);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at remote call: isOperationAllowed for " + projectName, e);
            }
            return allowed;
        }
        // TODO: in standalone it always returns true - make it work with the
        // metaproject
        return true;
    }

    public PaginationData<EntityData> search(String projectName, String searchString, edu.stanford.bmir.protege.web.client.rpc.data.ValueType valueType, int start, int limit, String sort, String dir) {
        List<EntityData> records = search(projectName, searchString, valueType);
        return PaginationServerUtil.pagedRecords(records, start, limit, sort, dir);
    }


    public List<EntityData> search(String projectName, String searchString) {
        return search(projectName, searchString, null);
    }

    public List<EntityData> search(String projectName, String searchString, edu.stanford.bmir.protege.web.client.rpc.data.ValueType valueType) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        if ((!searchString.startsWith("*"))
                && (!searchString.endsWith("*"))
                && searchString.length() >= MIN_SEARCH_STRING_LENGTH) {
            if (!searchString.startsWith("*")) {
                searchString = "*" + searchString;
            }

            if (!searchString.endsWith("*")) {
                searchString = searchString + "*";
            }
        }

        Collection<Frame> matchedFrames = new ArrayList<Frame>();;

        QueryConfiguration qConf = new QueryApi(kb).install();

        if (qConf == null) {
            //Classic Protege search
            matchedFrames = new HashSet<Frame>(kb.getMatchingFrames(kb.getSystemFrames().getNameSlot(), null,
                    false, searchString, -1));
            if (isOWLOntology(project)) {
                matchedFrames.addAll(kb.getMatchingFrames(((OWLSystemFrames) kb.getSystemFrames()).getRdfsLabelProperty(), null,
                        false, searchString, -1));
            }
        }
        else {
            //search only class value type. The abstract indexer knows about this
            Map<String, String> browerTextToFrameNameMap = IndexUtilities.getBrowserTextToFrameNameMap(kb, searchString);
            List<EntityData> searchResults = new ArrayList<EntityData>();
            for (String browserText : browerTextToFrameNameMap.keySet()) {
                searchResults.add(new EntityData(browerTextToFrameNameMap.get(browserText), browserText));
            }

            return searchResults;
        }

        //Log.getLogger().info("Search string: " + searchString + "  Search results count: " + (matchedFrames == null ? "0" : matchedFrames.size()));

        //filter & sort frames
        ArrayList<Frame> sortedFrames = new ArrayList<Frame>();
        switch (valueType) {
        case Cls:
            for (Frame frame : matchedFrames) {
                if (frame instanceof Cls && !frame.isSystem() && !(frame instanceof OWLAnonymousClass)) {
                    sortedFrames.add(frame);
                }
            }
            break;
        case Instance:
            for (Frame frame : matchedFrames) {
                if (frame instanceof Instance && !frame.isSystem() && !(frame instanceof OWLAnonymousClass)) {
                    sortedFrames.add(frame);
                }
            }
            break;
        case Property:
            for (Frame frame : matchedFrames) {
                if (frame instanceof Slot && !frame.isSystem() && !(frame instanceof OWLAnonymousClass)) {
                    sortedFrames.add(frame);
                }
            }
            break;
        default:    //case valueType == null or not one of the above
        for (Frame frame : matchedFrames) {
            if (!frame.isSystem() && !(frame instanceof OWLAnonymousClass)) {
                sortedFrames.add(frame);
            }
        }
        break;
        }

        // Collections.sort(sortedFrames, new FrameComparator());

        ArrayList<EntityData> results = new ArrayList<EntityData>();
        for (Frame frame : sortedFrames) {
            results.add(createEntityData(frame));
        }

        return results;
    }

    public ArrayList<EntityData> getPathToRoot(String projectName, String entityName) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        ArrayList<EntityData> path = new ArrayList<EntityData>();

        Frame frame = kb.getFrame(entityName);
        if (frame == null || !(frame instanceof Cls)) {
            return path;
        }

        // for now it works only with classes
        Cls entity = kb.getCls(entityName);
        List clsPath = ModelUtilities.getPathToRoot(entity);

        for (Iterator iterator = clsPath.iterator(); iterator.hasNext();) {
            Cls cls = (Cls) iterator.next();
            path.add(createEntityData(cls, false));
        }

        return path;
    }


}
